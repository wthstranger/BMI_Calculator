package com.wthstranger.bmi_calculator.utils;

public class BmiUtils {

    public static double calculateBmi(double weight, String weightUnit, double height, String heightUnit) {
        double weightKg = weightUnit.equalsIgnoreCase("KG") ? weight : weight * 0.45359237;
        double heightCm = heightUnit.equalsIgnoreCase("CM") ? height : height * 2.54;
        double heightM = heightCm / 100.0;

        if (heightM == 0) return 0;
        return weightKg / (heightM * heightM);
    }

    public static String getBmiCategory(double bmi) {
        if (bmi < 18.5) return "Underweight";
        else if (bmi < 25) return "Normal Weight";
        else if (bmi < 30) return "Overweight";
        else return "Obese";
    }
}
