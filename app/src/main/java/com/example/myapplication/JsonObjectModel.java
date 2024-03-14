package com.example.myapplication;

public class JsonObjectModel {
    private String key;
    private String value;
    private String defaultValue;

    public JsonObjectModel(String key, String value, String defaultValue) {
        this.key = key;
        this.value = value;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
