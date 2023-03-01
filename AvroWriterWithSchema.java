import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AvroWriterWithSchema {
    public static void main(String[] args) throws IOException {
        Schema writerSchema = createWriterSchema();
        Schema readerSchema = createReaderSchema();

        GenericRecord record = new GenericData.Record(writerSchema);
        record.put("name", "John");
        record.put("age", 30);

        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(writerSchema);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        datumWriter.write(record, encoder);
        encoder.flush();

        byte[] avroData = out.toByteArray();

        DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(writerSchema, readerSchema);
        decoder = DecoderFactory.get().binaryDecoder(new ByteArrayInputStream(avroData), null);
        GenericRecord deserializedRecord = datumReader.read(null, decoder);

        System.out.println("Original record: " + record);
        System.out.println("Deserialized record: " + deserializedRecord);
    }

    private static Schema createWriterSchema() {
        String schemaJson = "{\"type\":\"record\",\"name\":\"Person\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"age\",\"type\":\"int\"}]}";
        return new Schema.Parser().parse(schemaJson);
    }

    private static Schema createReaderSchema() {
        String schemaJson = "{\"type\":\"record\",\"name\":\"Person\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"}]}";
        return new Schema.Parser().parse(schemaJson);
    }
}
