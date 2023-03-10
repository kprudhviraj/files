import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AvroToJsonConverter {

    public static void main(String[] args) throws IOException {
        String schemaFile = "example.avsc";
        Schema schema = new Schema.Parser().parse(new File(schemaFile));
        GenericRecord record = new GenericData.Record(schema);
        JSONObject json = new JSONObject();
        convertToJson(record, json);
        System.out.println(json.toString());
    }

    private static void convertToJson(GenericRecord record, JSONObject json) {
        for (Field field : record.getSchema().getFields()) {
            String fieldName = field.name();
            Object fieldValue = record.get(fieldName);
            Schema fieldSchema = field.schema();
            if (fieldSchema.getType() == Schema.Type.UNION) {
                // Handle union types
                for (Schema unionSchema : fieldSchema.getTypes()) {
                    if (unionSchema.getType() != Schema.Type.NULL) {
                        fieldValue = record.get(fieldName);
                        break;
                    }
                }
            }
            if (fieldValue == null) {
                continue;
            }
            switch (fieldSchema.getType()) {
                case RECORD:
                    JSONObject subJson = new JSONObject();
                    convertToJson((GenericRecord) fieldValue, subJson);
                    json.put(fieldName, subJson);
                    break;
                case ARRAY:
                    List<Object> arrayValues = new ArrayList<>();
                    for (Object arrayValue : (List<?>) fieldValue) {
                        if (arrayValue instanceof GenericRecord) {
                            JSONObject subJsonArrayValue = new JSONObject();
                            convertToJson((GenericRecord) arrayValue, subJsonArrayValue);
                            arrayValues.add(subJsonArrayValue);
                        } else {
                            arrayValues.add(arrayValue);
                        }
                    }
                    json.put(fieldName, arrayValues);
                    break;
                case MAP:
                    Map<Utf8, Object> mapValues = (Map<Utf8, Object>) fieldValue;
                    JSONObject subJsonMap = new JSONObject();
                    for (Map.Entry<Utf8, Object> entry : mapValues.entrySet()) {
                        if (entry.getValue() instanceof GenericRecord) {
                            JSONObject subJsonMapValue = new JSONObject();
                            convertToJson((GenericRecord) entry.getValue(), subJsonMapValue);
                            subJsonMap.put(entry.getKey().toString(), subJsonMapValue);
                        } else {
                            subJsonMap.put(entry.getKey().toString(), entry.getValue());
                        }
                    }
                    json.put(fieldName, subJsonMap);
                    break;
                default:
                    json.put(fieldName, fieldValue);
                    break;
            }
        }
    }
}
