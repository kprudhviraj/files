package org.example.datagen;

import org.apache.xmlbeans.XmlException;

import java.io.IOException;
import java.util.List;

public class DataGenerator {
    private DataParser parser;
    public DataGenerator(DataParser parser){
        this.parser = parser;
    }
    public void generateFakeData(String filePath) throws Exception {
        List<Field> fields = parser.parse(filePath);

        System.out.println(fields);
        //use Java Faker API to generate fake data
    }
}
