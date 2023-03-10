private static JsonObject populateObject(JsonObject object, String outputField, JsonElement inputValue) {
    String[] outputFieldParts = outputField.split("\\.");
    JsonObject currentObject = object;
    for (int i = 0; i < outputFieldParts.length - 1; i++) {
        String outputFieldPart = outputFieldParts[i].replace("[]","");
        if (outputFieldParts[i + 1].endsWith("[]")) {
            if (!currentObject.has(outputFieldPart)) {
                currentObject.add(outputFieldPart, new JsonArray());
            }
        } else {
            if (!currentObject.has(outputFieldPart)) {
                currentObject.add(outputFieldPart, new JsonObject());
            }
        }            
        currentObject = currentObject.getAsJsonObject(outputFieldPart);
    }
    String outputFieldPart = outputFieldParts[outputFieldParts.length - 1];
    if (outputFieldPart.endsWith("[]")) {
        JsonArray outputArray = currentObject.has(outputFieldPart.substring(0, outputFieldPart.length() - 2)) ?
                currentObject.getAsJsonArray(outputFieldPart.substring(0, outputFieldPart.length() - 2)) : new JsonArray();
        outputArray.add(inputValue);
        currentObject.add(outputFieldPart.substring(0, outputFieldPart.length() - 2), outputArray);
    } else {
        if (currentObject.has(outputFieldPart)) {
            JsonObject outputObject = currentObject.getAsJsonObject(outputFieldPart);
            for (Map.Entry<String, JsonElement> entry : inputValue.getAsJsonObject().entrySet()) {
                outputObject.add(entry.getKey(), entry.getValue());
            }
        } else {
            currentObject.add(outputFieldPart, inputValue);
        }
    }
    return object;
}
