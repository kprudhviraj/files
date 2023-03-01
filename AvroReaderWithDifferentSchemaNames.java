import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class AvroReaderWithDifferentSchemaNames {
    public static void main(String[] args) throws IOException {
        byte[] avroData = getAvroData(); // assume this returns a byte array of Avro data

        Schema writerSchema = getWriterSchema(); // assume this returns the writer schema
        Schema readerSchema = getReaderSchema(); // assume this returns the reader schema

        DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(writerSchema, readerSchema);
        Decoder decoder = DecoderFactory.get().binaryDecoder(new ByteArrayInputStream(avroData), null);

        GenericRecord record = datumReader.read(null, decoder);

        GenericData genericData = GenericData.get();
        Schema resolvedSchema = genericData.resolveUnion(writerSchema, readerSchema);

        GenericRecord resolvedRecord = new GenericData.Record(resolvedSchema);
        for (Schema.Field field : resolvedSchema.getFields()) {
            String fieldName = field.name();
            Object fieldValue = record.get(fieldName);
            resolvedRecord.put(fieldName, fieldValue);
        }

        System.out.println(resolvedRecord);
    }

    private static byte[] getAvroData() throws IOException {
        Schema schema = getWriterSchema();
        GenericRecord record = new GenericData.Record(schema);
        record.put("name", "Alice");
        record.put("age", 25);

        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);

        datumWriter.write(record, encoder);
        encoder.flush();

        return out.toByteArray();
    }

    private static Schema getWriterSchema() {
        String schemaJson = "{\"type\":\"record\",\"name\":\"Person\",\"namespace\":\"com.example.avro\",\"fields\":[{\"name\":\"full_name\",\"type\":\"string\"},{\"name\":\"age\",\"type\":\"int\"},{\"name\":\"address\",\"type\":{\"type\":\"record\",\"name\":\"Address\",\"fields\":[{\"name\":\"street\",\"type\":\"string\"},{\"name\":\"city\",\"type\":\"string\"},{\"name\":\"zip\",\"type\":\"int\"}]}}]}";
        return new Schema.Parser().parse(schemaJson);
    }

    private static Schema getReaderSchema() {
        String schemaJson = "{\"type\":\"record\",\"name\":\"Person\",\"namespace\":\"com.example.avro\",\"fields\":[{\"name\":\"full_name\",\"type\":\"string\"},{\"name\":\"address\",\"type\":{\"type\":\"record\",\"name\":\"Address\",\"fields\":[{\"name\":\"street\",\"type\":\"string\"},{\"name\":\"city\",\"type\":\"string\"}]}}]}";
        return new Schema.Parser().parse(schemaJson);
    }
}
