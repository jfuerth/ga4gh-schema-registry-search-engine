package ca.fuerth.ga4gh.schemaregistry.jsonschema;

import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

class JsonSchemaTest {

    static final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    @Test
    public void roundTripJsonSerialization_should_preserveEntireDocument() throws Exception {
        // language=json
        String jsonSchema = """
                {
                  "$id": "sra_flat_PRJEB10573",
                  "description": "SRA custom attributes specific to study PRJEB10573 from SRA Metadata Table (sra.metadata)",
                  "$schema": "http://json-schema.org/draft-07/schema",
                  "properties": {
                    "bases": {
                      "type": "integer",
                      "description": "Count of bases in the run",
                      "$unique_count": 9,
                      "oneOf": [
                        {
                          "title": "4656957490"
                        },
                        {
                          "title": "4535772640"
                        },
                        {
                          "title": "4224395296"
                        },
                        {
                          "title": "4101817050"
                        },
                        {
                          "title": "4246430466"
                        },
                        {
                          "title": "3805756154"
                        },
                        {
                          "title": "3968580072"
                        },
                        {
                          "title": "3351195352"
                        },
                        {
                          "title": "3338878806"
                        }
                      ]
                    },
                    "union_typed_property": {
                      "type": [ "string", "array" ],
                      "description": "Property that could be a string or an array",
                      "elements": {
                        "type": "string"
                      }
                    },
                    "broker_name_sam": {
                      "type": "string",
                      "description": "SRA custom attribute specific to bioproject PRJEB10573",
                      "$unique_count": 1,
                      "oneOf": [
                        {
                          "title": "ArrayExpress"
                        }
                      ]
                    },
                    "bytes": {
                      "type": "string",
                      "description": "SRA custom attribute specific to study scr_icac",
                      "$unique_count": 9,
                      "oneOf": [
                        {
                          "title": "2102993511"
                        },
                        {
                          "title": "2067140717"
                        },
                        {
                          "title": "1916584408"
                        },
                        {
                          "title": "1868357390"
                        },
                        {
                          "title": "1943523295"
                        },
                        {
                          "title": "1721958403"
                        },
                        {
                          "title": "1810681488"
                        },
                        {
                          "title": "1534278711"
                        },
                        {
                          "title": "1531639116"
                        }
                      ]
                    },
                    "cell_line_sam": {
                      "type": "string",
                      "description": "SRA custom attribute specific to bioproject PRJEB10573",
                      "$unique_count": 1,
                      "oneOf": [
                        {
                          "title": "HS401"
                        }
                      ]
                    },
                    "cell_type_sam_ss_dpl37": {
                      "type": "string",
                      "description": "SRA custom attribute specific to bioproject PRJEB10573",
                      "$unique_count": 1,
                      "oneOf": [
                        {
                          "title": "Human embryonic stem cell"
                        }
                      ]
                    },
                    "common_name_sam": {
                      "type": "string",
                      "description": "SRA custom attribute specific to bioproject PRJEB10573",
                      "$unique_count": 1,
                      "oneOf": [
                        {
                          "title": "human"
                        }
                      ]
                    },
                    "ena_first_public_sam": {
                      "type": "string",
                      "description": "SRA custom attribute specific to bioproject PRJEB10573",
                      "$unique_count": 1,
                      "oneOf": [
                        {
                          "title": "2016-10-19T17:01:22Z"
                        }
                      ]
                    }
                  }
                }
                """;

        JsonSchema parsedSchema = objectMapper.readValue(jsonSchema, JsonSchema.class);
        String roundTrippedSchema = objectMapper.writeValueAsString(parsedSchema);

        assertThatJson(roundTrippedSchema).isEqualTo(jsonSchema);
    }
}