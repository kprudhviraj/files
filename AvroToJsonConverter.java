package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;

public class AvroToJsonConverter {
    public static void main(String[] args) throws IOException {


        Schema schema = new Schema.Parser().parse(new File("E:\\HomeLab\\apps\\Microservices\\json2avro\\src\\main\\resources\\data.avsc"));

        GenericRecord record = new GenericData.Record(schema);

        setDefaultValues(schema, record);

        System.out.println(record);
    }

        private static void setDefaultValues(Schema schema, GenericRecord record) {
            if (schema.getType() == Schema.Type.UNION) {
                // Handle union types
                for (Schema unionSchema : schema.getTypes()) {
                    if (unionSchema.getType() != Schema.Type.NULL) {
                        setDefaultValues(unionSchema, record);
                        break;
                    }
                }
            } else {
                for (Schema.Field field : schema.getFields()) {
                    switch (field.schema().getType()) {
                        case INT:
                            record.put(field.name(), 0);
                            break;
                        case LONG:
                            record.put(field.name(), 0L);
                            break;
                        case FLOAT:
                            record.put(field.name(), 0.0f);
                            break;
                        case DOUBLE:
                            record.put(field.name(), 0.0d);
                            break;
                        case BOOLEAN:
                            record.put(field.name(), false);
                            break;
                        case STRING:
                            record.put(field.name(), "");
                            break;
                        case RECORD:
                            GenericRecord childRecord = new GenericData.Record(field.schema());
                            record.put(field.name(), childRecord);
                            setDefaultValues(field.schema(), childRecord);
                            break;
                        case ARRAY:
                            GenericData.Array<Object> array = new GenericData.Array<>(0, field.schema());
                            record.put(field.name(), array);
                            if (field.schema().getElementType().getType() == Schema.Type.RECORD) {
                                GenericRecord childArrayRecord = new GenericData.Record(field.schema().getElementType());
                                array.add(childArrayRecord);
                                setDefaultValues(field.schema().getElementType(), childArrayRecord);
                            }
                            break;
                        case MAP:
                            GenericRecord childMapRecord = new GenericData.Record(field.schema().getValueType());
                            record.put(field.name(), childMapRecord);
                            setDefaultValues(field.schema().getValueType(), childMapRecord);
                            break;
                        default:
                            record.put(field.name(), null);
                    }
                }
            }
        }
    }


