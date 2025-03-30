package com.example.myapplication.models;

public class Transaction {
    private String id;
    private String category;
    private double amount;
    private String date;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String time; // Add this field
    private String account;
    private String note;
    private String type;

    public Transaction() {
    }

    public Transaction(String id, String category, double amount, String date, String time, String account, String note, String type) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.time = time;
        this.account = account;
        this.note = note;
        this.type = type;
    }

    // Getters and setters for all fields
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}