import org.apache.avro.Schema;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AvroToJsonConverter {
    public static void main(String[] args) throws IOException {
        // Read the Avro schema from a file
        Schema schema = new Schema.Parser().parse(new File("example.avsc"));

        // Convert the schema to a JSON object
        JSONObject schemaJson = toJson(schema);

        System.out.println(schemaJson.toString(4));
    }

    private static JSONObject toJson(Schema schema) {
        JSONObject json = new JSONObject();

        if (schema.getFields() != null && !schema.getFields().isEmpty()) {
            List<JSONObject> fieldsJson = new ArrayList<>();

            for (Schema.Field field : schema.getFields()) {
                JSONObject fieldJson = getFieldJson(field);

                if (fieldJson != null) {
                    fieldsJson.add(fieldJson);
                }
            }

            if (!fieldsJson.isEmpty()) {
                json.put("fields", new JSONArray(fieldsJson));
            }
        }

        return json;
    }

    private static JSONObject getFieldJson(Schema.Field field) {
        JSONObject json = new JSONObject();

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
