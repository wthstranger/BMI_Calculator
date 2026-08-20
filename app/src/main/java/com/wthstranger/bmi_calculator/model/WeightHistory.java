package com.wthstranger.bmi_calculator.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class WeightHistory {
    private double weight;
    private String unit;
    private double bmi;

    @ServerTimestamp
    private Timestamp date;

    public WeightHistory() {}

    public WeightHistory(double weight, String unit, double bmi) {
        this.weight = weight;
        this.unit = unit;
        this.bmi = bmi;
    }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getBmi() { return bmi; }
    public void setBmi(double bmi) { this.bmi = bmi; }

    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }
}
