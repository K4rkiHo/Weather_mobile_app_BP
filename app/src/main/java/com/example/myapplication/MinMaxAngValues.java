package com.example.myapplication;

public class MinMaxAngValues {
    private String StringValue;
    private double MinValue;
    private double MaxValue;
    private double AvgValue;

    public MinMaxAngValues(String stringValue, double minValue, double maxValue, double avgValue) {
        StringValue = stringValue;
        MinValue = minValue;
        MaxValue = maxValue;
        AvgValue = avgValue;
    }

    public String getStringValue() {
        return StringValue;
    }

    public double getMinValue() {
        return MinValue;
    }

    public double getMaxValue() {
        return MaxValue;
    }

    public double getAvgValue() {
        return AvgValue;
    }

    public void setStringValue(String stringValue) {
        StringValue = stringValue;
    }

    public void setMinValue(double minValue) {
        MinValue = minValue;
    }

    public void setMaxValue(double maxValue) {
        MaxValue = maxValue;
    }

    public void setAvgValue(double avgValue) {
        AvgValue = avgValue;
    }
}
