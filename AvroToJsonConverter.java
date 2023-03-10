import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;

public class AvroGenerator {

    public static void main(String[] args) throws IOException {
        File schemaFile = new File("path/to/schema.avsc");
        Schema schema = new Schema.Parser().parse(schemaFile);

        GenericRecord record = new GenericData.Record(schema);
        for (Schema.Field field : schema.getFields()) {
            Object defaultValue = getDefaultValue(field.schema());
            record.put(field.name(), defaultValue);
        }

        DatumWriter<GenericRecord> datumWriter = new SpecificDatumWriter<>(schema);
        Encoder encoder = EncoderFactory.get().binaryEncoder(System.out, null);
        datumWriter.write(record, encoder);
        encoder.flush();
    }

    private static Object getDefaultValue(Schema schema) {
    Type type = schema.getType();
    switch (type) {
        case ARRAY:
            Schema elementSchema = schema.getElementType();
            if (elementSchema.getType().equals(Type.RECORD)) {
                GenericRecord elementRecord = new GenericData.Record(elementSchema);
                for (Schema.Field field : elementSchema.getFields()) {
                    Object defaultValue = getDefaultValue(field.schema());
                    elementRecord.put(field.name(), defaultValue);
                }
                return new GenericData.Array<>(0, schema);
            } else {
                return new GenericData.Array<>(0, schema);
            }
        case BOOLEAN:
            return false;
        case BYTES:
            return ByteBuffer.wrap(new byte[0]);
        case DOUBLE:
            return 0.0;
        case ENUM:
            return schema.getEnumSymbols().get(0);
        case FIXED:
            return new GenericData.Fixed(schema);
        case FLOAT:
            return 0.0f;
        case INT:
            return 0;
        case LONG:
            return 0L;
        case MAP:
            return new HashMap<>();
        case NULL:
            return null;
        case RECORD:
            GenericRecord record = new GenericData.Record(schema);
            for (Schema.Field field : schema.getFields()) {
                Object defaultValue = getDefaultValue(field.schema());
                record.put(field.name(), defaultValue);
            }
            return record;
        case STRING:
            return "";
        case UNION:
            for (Schema unionSchema : schema.getTypes()) {
                if (!unionSchema.getType().equals(Type.NULL)) {
                    return getDefaultValue(unionSchema);
                }
            }
            return null;
        default:
            throw new IllegalArgumentException("Unknown schema type: " + type);
    }
}

}
