// UserTransaction.java
package com.example.myapplication.models;

public class UserTransaction {
    private String id;
    private String sender;
    private String receiver;
    private double amount;
    private String type;
    private String date;
    private String account;

    public double getPayableAmount() {
        return payableAmount;
    }

    public void setPayableAmount(double payableAmount) {
        this.payableAmount = payableAmount;
    }

    public double getReceivableAmount() {
        return receivableAmount;
    }

    public void setReceivableAmount(double receivableAmount) {
        this.receivableAmount = receivableAmount;
    }

    private double settledAmount;
    private double payableAmount;
    private double receivableAmount;

    // Add getter and setter for settledAmount
    public double getSettledAmount() {
        return settledAmount;
    }

    public void setSettledAmount(double settledAmount) {
        this.settledAmount = settledAmount;
    }

    public UserTransaction() {
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    private String status;
    private String note;



    public UserTransaction(String id, String sender, String receiver, double amount, String type, String date, String account, String status, String note) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.account = account;
        this.status = status;
        this.note = note;
    }

    // Getters and setters for all fields
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}