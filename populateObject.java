private static JsonObject populateObject(JsonObject object, String outputField, JsonElement inputValue) {
    String[] outputFieldParts = outputField.split("\\.");
    JsonObject currentObject = object;
    for (int i = 0; i < outputFieldParts.length - 1; i++) {
        String outputFieldPart = outputFieldParts[i].replace("[]", "");
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
        if (currentObject.has(outputFieldParts[outputFieldParts.length - 2])) {
            currentObject.getAsJsonArray(outputFieldParts[outputFieldParts.length - 2]).add(inputValue);
        } else {
            if (!inputValue.isJsonArray()) {
                JsonArray newArray = new JsonArray();
                newArray.add(inputValue);
                currentObject.add(outputFieldParts[outputFieldParts.length - 2], newArray);
            } else {
                currentObject.add(outputFieldParts[outputFieldParts.length - 2], inputValue);
            }
        }
    } else {
        if (currentObject.has(outputFieldPart)) {
            JsonElement currentValue = currentObject.get(outputFieldPart);
            if (currentValue.isJsonObject()) {
                populateNestedObject(currentObject, outputFieldParts, inputValue, outputFieldParts.length - 1);
            } else {
                currentObject.add(outputFieldPart, inputValue);
            }
        } else {
            currentObject.add(outputFieldPart, inputValue);
        }
    }
    return object;
}

private static void populateNestedObject(JsonObject object, String[] outputFieldParts, JsonElement inputValue, int index) {
    String outputFieldPart = outputFieldParts[index].replace("[]", "");
    JsonObject currentObject = object.getAsJsonObject(outputFieldPart);
    if (index == outputFieldParts.length - 2) {
        String lastFieldPart = outputFieldParts[outputFieldParts.length - 1];
        if (lastFieldPart.endsWith("[]")) {
            if (currentObject.has(lastFieldPart.substring(0, lastFieldPart.length() - 2))) {
                currentObject.getAsJsonArray(lastFieldPart.substring(0, lastFieldPart.length() - 2)).add(inputValue);
            } else {
                JsonArray newArray = new JsonArray();
                newArray.add(inputValue);
                currentObject.add(lastFieldPart.substring(0, lastFieldPart.length() - 2), newArray);
            }
        } else {
            currentObject.add(lastFieldPart, inputValue);
        }
    } else {
        populateNestedObject(currentObject, outputFieldParts, inputValue, index + 1);
    }
}
