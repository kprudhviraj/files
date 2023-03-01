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
        int resolvedSchemaIndex = genericData.resolveUnion(writerSchema, readerSchema);
        Schema resolvedSchema = writerSchema.getTypes().get(resolvedSchemaIndex);

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
        record.put("full_name", "Alice");
        record.put("age", 25);
        GenericRecord address = new GenericData.Record(schema.getField("address").schema());
        address.put("street", "123 Main St.");
        address.put("city", "Anytown");
        address.put("zip", 12345);
        record.put("address", address);

        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);

        datumWriter.write(record, encoder);
        encoder.flush();

        return out.toByteArray();
    }

    private static Schema getWriterSchema() {
        return SchemaBuilder.record("Person")
                .namespace("com.example.avro")
                .fields()
                .name("full_name").type().stringType().noDefault()
                .name("age").type().intType().noDefault()
                .name("address").type(addressSchema()).noDefault()
                .endRecord();
    }

    private static Schema getReaderSchema() {
        return SchemaBuilder.record("Person")
                .namespace("com.example.avro")
                .fields()
                .name("full_name").type().stringType().noDefault()
                .name("address").type(addressSchema()).noDefault()
                .endRecord();
    }

    private static Schema addressSchema() {
        return SchemaBuilder.record("Address")
                .namespace("com.example.avro")
                .fields()
                .name("street").type().stringType().noDefault()
                .name("city").type().stringType().noDefault()
                .name("zip").type().intType().noDefault()
                .endRecord();
    }
}
