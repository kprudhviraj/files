import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;

import java.io.IOException;

public class JsonToGenericRecord {

    public static void main(String[] args) throws IOException {
        String schemaJson = "{\n" +
                "  \"type\": \"record\",\n" +
                "  \"name\": \"Person\",\n" +
                "  \"fields\": [\n" +
                "    {\"name\": \"name\", \"type\": \"string\"},\n" +
                "    {\"name\": \"age\", \"type\": \"int\"}\n" +
                "  ]\n" +
                "}";
        Schema schema = new Schema.Parser().parse(schemaJson);

        String jsonData = "{\n" +
                "  \"name\": \"Alice\",\n" +
                "  \"age\": 30\n" +
                "}";

        Decoder decoder = DecoderFactory.get().jsonDecoder(schema, jsonData);
        DatumReader<GenericRecord> reader = new SpecificDatumReader<>(schema);
        GenericRecord record = reader.read(null, decoder);
        System.out.println(record);
    }
}
