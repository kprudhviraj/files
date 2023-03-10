import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AvroToJsonConverter {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

    public static void main(String[] args) throws IOException {
        // Read the Avro schema from a file
        Schema schema = new Schema.Parser().parse(new File("example.avsc"));

        // Convert the schema to a JSON object
        ObjectNode schemaJson = toJson(schema);

        System.out.println(schemaJson);
    }

    private static ObjectNode toJson(Schema schema) {
        ObjectNode json = nodeFactory.objectNode();

        if (schema.getFields() != null && !schema.getFields().isEmpty()) {
            List<ObjectNode> fieldsJson = new ArrayList<>();

            for (Schema.Field field : schema.getFields()) {
                ObjectNode fieldJson = getFieldJson(field);

                if (fieldJson != null) {
                    fieldsJson.add(fieldJson);
                }
            }

            if (!fieldsJson.isEmpty()) {
                json.put("fields", mapper.valueToTree(fieldsJson));
            }
        }

        return json;
    }

    private static ObjectNode getFieldJson(Schema.Field field) {
        ObjectNode json = nodeFactory.objectNode();

        json.put("name", field.name());

        Schema fieldSchema = field.schema();

        if (fieldSchema.isUnion()) {
            fieldSchema = getDefaultUnionType(fieldSchema);
        }

        Schema.Type fieldType = fieldSchema.getType();

        switch (fieldType) {
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
                json.put("default", 0);
                break;

            case BOOLEAN:
                json.put("default", false);
                break;

            case STRING:
            case BYTES:
                json.put("default", "");
                break;

            case ENUM:
                List<String> symbols = fieldSchema.getEnumSymbols();
                json.put("default", symbols.get(0));
                break;

            case RECORD:
            case ARRAY:
            case MAP:
                json = null;
                break;

            default:
                break;
        }

        return json;
    }

    private static Schema getDefaultUnionType(Schema schema) {
        Schema defaultType = null;

        for (Schema type : schema.getTypes()) {
            if (type.getType() != Schema.Type.NULL) {
                defaultType = type;
                break;
            }
        }

        if (defaultType == null) {
            throw new RuntimeException("Unable to determine default union type for schema: " + schema);
        }

        return defaultType;
    }
}
