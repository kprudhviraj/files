import org.apache.avro.Schema;
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

        System.out.println(record);
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
        String schemaJson = "{\n" +
                "  \"namespace\": \"com.example.avro\",\n" +
                "  \"name\": \"Person\",\n" +
                "  \"aliases\": [\"com.example.Person\"],\n" +
                "  \"type\": \"record\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"name\": \"full_name\",\n" +
                "      \"type\": \"string\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"age\",\n" +
                "      \"type\": \"int\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"address\",\n" +
                "      \"type\": {\n" +
                "        \"namespace\": \"com.example.avro\",\n" +
                "        \"name\": \"Address\",\n" +
                "        \"aliases\": [\"com.example.Address\"],\n" +
                "        \"type\": \"record\",\n" +
                "        \"fields\": [\n" +
                "          {\n" +
                "            \"name\": \"street\",\n" +
                "            \"type\": \"string\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"name\": \"city\",\n" +
                "            \"type\": \"string\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"name\": \"zip\",\n" +
                "           
            "            \"type\": \"int\"\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    return new Schema.Parser().parse(schemaJson);
}

private static Schema getReaderSchema() {
    String schemaJson = "{\n" +
            "  \"namespace\": \"com.example.avro\",\n" +
            "  \"name\": \"Person\",\n" +
            "  \"aliases\": [\"com.example.Person\"],\n" +
            "  \"type\": \"record\",\n" +
            "  \"fields\": [\n" +
            "    {\n" +
            "      \"name\": \"full_name\",\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"address\",\n" +
            "      \"type\": {\n" +
            "        \"namespace\": \"com.example.avro\",\n" +
            "        \"name\": \"Address\",\n" +
            "        \"aliases\": [\"com.example.Address\"],\n" +
            "        \"type\": \"record\",\n" +
            "        \"fields\": [\n" +
            "          {\n" +
            "            \"name\": \"street\",\n" +
            "            \"type\": \"string\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"name\": \"city\",\n" +
            "            \"type\": \"string\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"name\": \"zip\",\n" +
            "            \"type\": \"int\"\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    return new Schema.Parser().parse(schemaJson);
}

private static Schema addressSchema() {
    String schemaJson = "{\n" +
            "  \"namespace\": \"com.example.avro\",\n" +
            "  \"name\": \"Address\",\n" +
            "  \"aliases\": [\"com.example.Address\"],\n" +
            "  \"type\": \"record\",\n" +
            "  \"fields\": [\n" +
            "    {\n" +
            "      \"name\": \"street\",\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"city\",\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"zip\",\n" +
            "      \"type\": \"int\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    return new Schema.Parser().parse(schemaJson);
}
}
