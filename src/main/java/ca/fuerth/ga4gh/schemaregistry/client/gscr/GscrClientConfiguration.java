package ca.fuerth.ga4gh.schemaregistry.client.gscr;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import feign.Feign;
import feign.Request;
import feign.Response;
import feign.Target;
import feign.codec.Decoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.executable.ExecutableValidator;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Set;

@Configuration
public class GscrClientConfiguration {

    @Bean
    public GscrClient gscrClient() {

        ObjectMapper jacksonMapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SnakeCaseStrategy.INSTANCE)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .findAndRegisterModules();

        Logger requestLogger = LoggerFactory.getLogger(GscrClient.class);

        OkHttpClient okHttpClient = new OkHttpClient(new okhttp3.OkHttpClient.Builder()
                .followRedirects(true)
                .addInterceptor(
                        new HttpLoggingInterceptor(requestLogger::info)
                                .setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build());

        return Feign.builder()
                .client(okHttpClient)
                .decoder(new ValidatingDecoder(new JacksonDecoder(jacksonMapper)))
                .encoder(new JacksonEncoder(jacksonMapper))
                .target(Target.EmptyTarget.create(GscrClient.class));
    }

    static final class ValidatingDecoder implements Decoder {

        private final Decoder delegate;

        public ValidatingDecoder(Decoder delegate) {
            this.delegate = delegate;
        }

        private static final Validator validator = Validation
                .buildDefaultValidatorFactory()
                .getValidator();

        @Override
        public Object decode(Response response, Type type) throws IOException {
            Object decoded = delegate.decode(response, type);
            Set<ConstraintViolation<Object>> valiationErrors = validator.validate(decoded);
            if (!valiationErrors.isEmpty()) {
                Request request = response.request();
                String message = "Out-of-spec response from %s %s".formatted(request.httpMethod(), request.url());
                throw Ga4ghSchemaRegistryProtocolException.ofValidationErrors(message, valiationErrors);
            }
            return decoded;
        }
    }
}
