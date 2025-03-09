package ca.fuerth.ga4gh.schemaregistry.client.gscr;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import feign.Feign;
import feign.Target;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                .addInterceptor(
                        new HttpLoggingInterceptor(requestLogger::info)
                                .setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build());

        return Feign.builder()
                .client(okHttpClient)
                .decoder(new JacksonDecoder(jacksonMapper))
                .encoder(new JacksonEncoder(jacksonMapper))
                .target(Target.EmptyTarget.create(GscrClient.class));
    }
}
