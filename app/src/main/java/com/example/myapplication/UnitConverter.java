package com.example.myapplication;

public class UnitConverter {
    public static double convertCelsiusToFahrenheit(double celsius) {
        return celsius * 9 / 5 + 32;
    }

    public static double convertFahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32) * 5 / 9;
    }

    public static double convertKilometersToMiles(double kilometers) {
        return kilometers * 0.621371;
    }

    public static double convertMilesToKilometers(double miles) {
        return miles / 0.621371;
    }

    public static double convertKilogramsToPounds(double kilograms) {
        return kilograms * 2.20462;
    }

    public static double convertPoundsToKilograms(double pounds) {
        return pounds / 2.20462;
    }

    public static double convertMetersToFeet(double meters) {
        return meters * 3.28084;
    }

    public static double convertFeetToMeters(double feet) {
        return feet / 3.28084;
    }

}
