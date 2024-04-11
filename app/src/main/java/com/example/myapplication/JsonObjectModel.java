package com.example.myapplication;

public class JsonObjectModel {
    private String id;
    private String key;
    private String value;
    private String defaultValue;
    private String min;
    private String max;
    private String avg;
    private String unit;
    private String original_unit;

    public JsonObjectModel(String id, String key, String value, String defaultValue, String min, String max, String avg, String unit, String original_unit) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.defaultValue = defaultValue;
        this.min = min;
        this.max = max;
        this.avg = avg;
        this.unit = unit;
        this.original_unit = original_unit;
    }

    public String getId() {
        return id;
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

    public String getMin() {
        return min;
    }

    public String getMax() {
        return max;
    }

    public String getAvg() {
        return avg;
    }

    public String getUnit() {
        return unit;
    }

    public String getOriginalUnit() {
        return original_unit;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public void setAvg(String avg) {
        this.avg = avg;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setOriginalUnit(String original_unit) {
        this.original_unit = original_unit;
    }

}
