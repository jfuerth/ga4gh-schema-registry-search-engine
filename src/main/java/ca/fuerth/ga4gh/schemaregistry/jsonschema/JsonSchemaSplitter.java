package ca.fuerth.ga4gh.schemaregistry.jsonschema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Component
public class JsonSchemaSplitter implements DocumentSplitter {

    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public List<TextSegment> split(Document document) {
        try {
            JsonSchema schema = objectMapper.readValue(document.text(), JsonSchema.class);

            Metadata metadata = document.metadata().copy()
                .put("schema.content.id", defaultIfNull(schema.getId(), "(no $id)"))
                .put("schema.content.description", defaultIfNull(schema.getDescription(), "(no description)"));

            return schema.getProperties().entrySet().stream()
                    .map(entry -> {
                        String propName = entry.getKey();
                        JsonSchema propSchema = entry.getValue();

                        return new TextSegment(
                                toJsonQuietly(propSchema),
                                metadata.copy().put("schema.content.propertyName", propName));
                    })
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String toJsonQuietly(JsonSchema propSchema) {
        try {
            return objectMapper.writeValueAsString(propSchema);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
