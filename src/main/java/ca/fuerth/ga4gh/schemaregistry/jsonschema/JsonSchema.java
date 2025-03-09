package ca.fuerth.ga4gh.schemaregistry.jsonschema;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



// probably don't need this.. just use a string?


@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming // turns off snake case / camel case handling
public class JsonSchema {

    @JsonProperty("$id")
    private String id;

    @JsonProperty("$schema")
    private String schema;

    @JsonAnyGetter
    @JsonAnySetter
    private Map<String, Object> additionalProperties = new LinkedHashMap<>();

    private String type;

    private Map<String, JsonSchema> properties;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<JsonSchema> elements;
}
