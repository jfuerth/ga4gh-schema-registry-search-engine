package ca.fuerth.ga4gh.schemaregistry.jsonschema;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming // turns off snake case / camel case handling
@JsonPropertyOrder({"$id", "$schema", "description", "type"})
public class JsonSchema {

    @JsonProperty("$id")
    private String id;

    @JsonProperty("$schema")
    private String schema;

    private String description;

    @JsonFormat(with = {
            JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
            JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED,
    })
    private List<String> type = new ArrayList<>();

    /**
     * Child properties of this schema. Makes sense if {@code type == "object"}.
     */
    private Map<String, JsonSchema> properties = new LinkedHashMap<>();

    /**
     * Element type of this schema. Makes sense if {@code type == "array"}.
     */
    @JsonFormat(with = {
            JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
            JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED,
    })
    private List<JsonSchema> elements = new ArrayList<>();

    @JsonIgnore
    @JsonAnyGetter
    @JsonAnySetter
    private Map<String, Object> additionalProperties = new LinkedHashMap<>();

}
