package org.example.newlogic;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GsonExample {
    public static void main(String[] args) throws IOException {
        Gson gson = new Gson();

        // Load the configuration file
        String configJson = new String(Files.readAllBytes(Paths.get("E:\\HomeLab\\apps\\Microservices\\json2avro\\src\\main\\resources\\gson\\config.json")));
        JsonObject config = JsonParser.parseString(configJson).getAsJsonObject();

        // Build the field mappings from the configuration
        Map<String, Map<String, Object>> fieldMappings = buildFieldMappings(config);

        // Parse the input JSON data
        // Read the input JSON data from a file
        String inputJson = new String(Files.readAllBytes(new File("E:\\HomeLab\\apps\\Microservices\\json2avro\\src\\main\\resources\\gson\\complex.json").toPath()), StandardCharsets.UTF_8);

        JsonObject inputJsonObject = gson.fromJson(inputJson, JsonObject.class);

        // Transform the input JSON data using the field mappings
        JsonObject outputJsonObject = transformJson(inputJsonObject, fieldMappings);

        // Convert the output JSON data to a string
        String outputJson = gson.toJson(outputJsonObject);
        System.out.println(outputJson);
    }



    private static Map<String, Map<String, Object>> buildFieldMappings(JsonObject config) {
        Map<String, Map<String, Object>> fieldMappings = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : config.entrySet()) {
            String inputField = entry.getKey();
            JsonObject fieldMappingJson = entry.getValue().getAsJsonObject();
            String targetField = fieldMappingJson.get("target").getAsString();
            Map<String, Object> fieldMapping = new HashMap<>();
            fieldMapping.put("target", targetField);
            if (fieldMappingJson.has("renames")) {
                JsonArray renamesArray = fieldMappingJson.get("renames").getAsJsonArray();
                List<Map<String, String>> renamesList = new ArrayList<>();
                for (JsonElement renameElement : renamesArray) {
                    JsonObject renameObject = renameElement.getAsJsonObject();
                    String renameInputField = renameObject.get("inputField").getAsString();
                    String renameTargetField = renameObject.get("targetField").getAsString();
                    Map<String, String> renameMap = new HashMap<>();
                    renameMap.put("inputField", renameInputField);
                    renameMap.put("targetField", renameTargetField);
                    renamesList.add(renameMap);
                }
                fieldMapping.put("renames", renamesList);
            }
            fieldMappings.put(inputField, fieldMapping);
        }
        return fieldMappings;
    }


    private static JsonObject transformJson(JsonObject inputJsonObject, Map<String, Map<String, Object>> fieldMappings) {
        JsonObject outputJsonObject = new JsonObject();
        for (Map.Entry<String, Map<String, Object>> entry : fieldMappings.entrySet()) {
            String inputFieldExpr = entry.getKey();
            String targetField = (String) entry.getValue().get("target");
            List<Map<String, String>> renamesList = (List<Map<String, String>>) entry.getValue().get("renames");
            JsonElement inputValue = getInputValue(inputJsonObject, inputFieldExpr);
            if (targetField != null && inputValue != null) {
                outputJsonObject = populateObject(outputJsonObject, targetField, inputValue);
                if (renamesList != null) {
                    for (Map<String, String> renameMap : renamesList) {
                        String renameInputField = renameMap.get("inputField");
                        String renameTargetField = renameMap.get("targetField");
                        JsonElement renameInputValue = getInputValue(inputJsonObject, renameInputField);
                        if (renameInputValue != null) {
                            outputJsonObject = populateObject(outputJsonObject, renameTargetField, renameInputValue);
                        }
                    }
                }
            }
        }
        outputJsonObject = convertArrays(outputJsonObject);
        return outputJsonObject;
    }


    private static JsonElement getInputValue(JsonObject inputJsonObject, String inputFieldExpr) {
        String[] inputFieldParts = inputFieldExpr.split("\\.");
        JsonElement inputValue = inputJsonObject;
        for (String inputFieldPart : inputFieldParts) {
            if (inputFieldPart.endsWith("[]")) {
                String inputFieldPartNoArray = inputFieldPart.substring(0, inputFieldPart.length() - 2);

                JsonArray inputArray = inputValue.getAsJsonObject().getAsJsonArray(inputFieldPartNoArray);
                if (inputArray != null && inputArray.size() > 0) {
                    JsonArray newArray = new JsonArray();
                    for (int i = 0; i < inputArray.size(); i++) {
                        JsonElement arrayValue = inputArray.get(i);
                        if (arrayValue.isJsonObject()) {
                            newArray.add(arrayValue.getAsJsonObject());
                        } else {
                            JsonObject objectValue = new JsonObject();
                            objectValue.addProperty(inputFieldPartNoArray, arrayValue.getAsString());
                            newArray.add(objectValue);
                        }
                    }
                    inputValue = newArray;
                } else {
                    return null;
                }
            } else {
                if (inputValue.isJsonArray()) {
                    JsonArray newArray = new JsonArray();
                    for (int i = 0; i < inputValue.getAsJsonArray().size(); i++) {
                        JsonElement arrayValue = inputValue.getAsJsonArray().get(i);
                        if (arrayValue.isJsonObject()) {
                            newArray.add(arrayValue.getAsJsonObject().get(inputFieldPart));
                        } else {
                            JsonObject objectValue = new JsonObject();
                            objectValue.addProperty(inputFieldPart, arrayValue.getAsString());
                            newArray.add(objectValue);
                        }
                    }
                    inputValue = newArray;
                }else
                inputValue = inputValue.getAsJsonObject().get(inputFieldPart);
            }
            if (inputValue == null) {
                return null;
            }
        }
        return inputValue;
    }



    private static JsonObject populateObject(JsonObject object, String outputField, JsonElement inputValue) {
        String[] outputFieldParts = outputField.split("\\.");
        JsonObject currentObject = object;
        for (int i = 0; i < outputFieldParts.length - 1; i++) {
            String outputFieldPart = outputFieldParts[i].replace("[]", "");
            if (currentObject.has(outputFieldPart)) {
                if (currentObject.get(outputFieldPart).isJsonArray()) {
                    JsonArray currentArray = currentObject.getAsJsonArray(outputFieldPart);
                    if (currentArray.size() > 0) {
                        currentObject = currentArray.get(currentArray.size() - 1).getAsJsonObject();
                    } else {
                        JsonObject newObject = new JsonObject();
                        currentArray.add(newObject);
                        currentObject = newObject;
                    }
                } else {
                    currentObject = currentObject.getAsJsonObject(outputFieldPart);
                }
            } else {
                if (outputFieldParts[i + 1].endsWith("[]")) {
                    JsonArray newArray = new JsonArray();
                    currentObject.add(outputFieldPart, newArray);
                    currentObject = newArray.getAsJsonArray().get(0).getAsJsonObject();
                } else {
                    JsonObject newObject = new JsonObject();
                    currentObject.add(outputFieldPart, newObject);
                    currentObject = newObject;
                }
            }
        }
        String outputFieldPart = outputFieldParts[outputFieldParts.length - 1];
        if (outputFieldPart.endsWith("[]")) {
            currentObject.getAsJsonArray(outputFieldPart.substring(0, outputFieldPart.length() - 2)).add(inputValue);
        } else {
            currentObject.add(outputFieldPart, inputValue);
        }
        return object;
    }


    private static JsonObject convertArrays(JsonObject object) {
        JsonObject finalObject = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String outputField = entry.getKey();
            JsonElement outputValue = entry.getValue();
            if (outputField.endsWith("[]") && outputValue.isJsonArray() && outputValue.getAsJsonArray().size() > 1) {
                String[] outputFieldParts = outputField.split("\\.");
                JsonObject currentObject = finalObject;
                for (int i = 0; i < outputFieldParts.length - 1; i++) {
                    String outputFieldPart = outputFieldParts[i];
                    if (!currentObject.has(outputFieldPart)) {
                        currentObject.add(outputFieldPart, new JsonObject());
                    }
                    currentObject = currentObject.getAsJsonObject(outputFieldPart);
                }
                currentObject.add(outputFieldParts[outputFieldParts.length - 1], outputValue);
            } else {
                finalObject.add(outputField, outputValue);
            }
        }
        return finalObject;
    }
}
