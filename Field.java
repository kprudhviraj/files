package org.example.datagen;

import java.util.HashMap;
import java.util.Map;

public class Field {
    private String name;
    private String type;
    private Map<String, String> attributes;

    public Field(String name, String type) {
        this.name = name;
        this.type = type;
        this.attributes = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(String name, String value) {
        this.attributes.put(name, value);
    }

    @Override
    public String toString() {
        return "Field{" +"\n"+
                "name='" + name + '\'' +"\n"+
                ", type='" + type + '\'' +"\n"+
                ", attributes=" + attributes +"\n"+
                "\n"+'}';
    }
}
