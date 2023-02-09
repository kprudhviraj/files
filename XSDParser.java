package org.example.datagen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class XSDParser implements DataParser{
    public static void main(String[] args) throws Exception {
        XmlObject xsdSchema = XmlObject.Factory.parse(new File("E:\\HomeLab\\apps\\Microservices\\ScreenCapture\\src\\main\\resources\\schema.xsd"));
        Node schemaNode = xsdSchema.getDomNode();
        NodeList elements = schemaNode.getChildNodes().item(0).getChildNodes();
        List<Field> fields = new ArrayList<>();
        Map<String, String> attributes = new LinkedHashMap<>();
        processElements(elements, fields, "");

        System.out.println(fields);
    }

    private static void processElements(NodeList elements, List<Field> fields, String parent) {
        for (int j = 0; j < elements.getLength(); j++) {
            Node element = elements.item(j);
            if (element.getNodeType() == Node.ELEMENT_NODE) {
                if (element.getNodeName().equals("xs:element")) {
                    Node nameAttr = element.getAttributes().getNamedItem("name");
                    if (nameAttr != null) {
                        String fieldName = nameAttr.getNodeValue();
                        String fullName = (parent.isEmpty() ? "" : parent + ".") + fieldName;
                        Node typeAttr = element.getAttributes().getNamedItem("type");
                        String fieldType = typeAttr != null ? typeAttr.getNodeValue() : "";
                        Field field = new Field(fullName, fieldType);
                        NamedNodeMap attrs = element.getAttributes();
                        for (int k = 0; k < attrs.getLength(); k++) {
                            Node attr = attrs.item(k);
                            if (!attr.getNodeName().equals("name") && !attr.getNodeName().equals("type")) {
                                field.addAttribute(attr.getNodeName(), attr.getNodeValue());
                            }
                        }
                        fields.add(field);
                        NodeList innerElements = element.getChildNodes();
                        processElements(innerElements, fields, fullName);
                    }
                } else if (element.getNodeName().equals("xs:complexType")) {
                    NodeList innerElements = element.getChildNodes().item(1).getChildNodes();
                    NodeList attributeElements = element.getChildNodes();
                    for (int l = 0; l < attributeElements.getLength(); l++) {
                        Node attributeElement = attributeElements.item(l);
                        if (attributeElement.getNodeType() == Node.ELEMENT_NODE && attributeElement.getNodeName().equals("xs:attribute")) {
                            Node nameAttr = attributeElement.getAttributes().getNamedItem("name");
                            if (nameAttr != null) {
                                String fieldName = nameAttr.getNodeValue();
                                Node typeAttr = attributeElement.getAttributes().getNamedItem("type");
                                String fieldValue = typeAttr != null ? typeAttr.getNodeValue() : "";
                                fields.stream().filter(fl -> parent.equals(fl.getName())).forEach(fl -> {
                                    fl.getAttributes().put(fieldName, fieldValue);
                                });
                            }
                        }
                    }
                    processElements(innerElements, fields, parent);
                }
            }
        }
    }

    @Override
    public List<Field> parse(String filepath) throws XmlException, IOException {
        XmlObject xsdSchema = XmlObject.Factory.parse(new File(filepath));
        Node schemaNode = xsdSchema.getDomNode();
        NodeList elements = schemaNode.getChildNodes().item(0).getChildNodes();
        List<Field> fields = new ArrayList<>();
        processElements(elements, fields, "");
        return fields;
    }
}
