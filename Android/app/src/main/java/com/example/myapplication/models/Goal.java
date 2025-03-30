// Goal.java
package com.example.myapplication.models;

public class Goal {
    private String title;
    private double targetAmount;
    private double currentAmount;
    private String endDate; // Store end date as a string

    public Goal() {
        // Default constructor required for calls to DataSnapshot.getValue(Goal.class)
    }

    public Goal(String title, double targetAmount, double currentAmount, String endDate) {
        this.title = title;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.endDate = endDate;
    }

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }

    public double getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}