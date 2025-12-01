package org.example.model;

public class Customer {
    private final String cardNumber;
    private String pin;
    private final String name;
    private final Account account;

    public Customer(String cardNumber, String pin, String name, Account account) {
        this.cardNumber = cardNumber;
        this.pin = pin;
        this.name = name;
        this.account = account;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getName() {
        return name;
    }

    public Account getAccount() {
        return account;
    }
}


