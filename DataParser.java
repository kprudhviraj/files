package org.example.datagen;

import org.apache.xmlbeans.XmlException;

import java.io.IOException;
import java.util.List;

public interface DataParser {
    List<Field> parse(String filepath) throws Exception;
}
