package org.example.service;

import org.example.model.ATMState;
import org.example.model.Account;
import org.example.model.Customer;
import org.example.model.Technician;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple in-memory simulation of a Bank Central System.
 * In a real implementation this would be a remote, encrypted service.
 */
public class BankCentralSystem {

    private final Map<String, Customer> customersByCard = new HashMap<>();
    private final Map<String, Technician> techniciansByUser = new HashMap<>();
    private ATMState atmState = ATMState.ACTIVE;
    private BigDecimal atmCashStock = BigDecimal.valueOf(10_000); // cash available in ATM

    public BankCentralSystem() {
        seedDemoData();
    }

    private void seedDemoData() {
        Account a1 = new Account("ACC-1001", BigDecimal.valueOf(2_000));
        Account a2 = new Account("ACC-1002", BigDecimal.valueOf(5_000));
        customersByCard.put("1111222233334444",
                new Customer("1111222233334444", "1234", "Ali Veli", a1));
        customersByCard.put("5555666677778888",
                new Customer("5555666677778888", "4321", "Ayse Fatma", a2));

        techniciansByUser.put("tech1", new Technician("tech1", "password"));
    }

    // --- Authentication ---

    public Customer authenticateCustomer(String cardNumber, String pin) {
        Customer c = customersByCard.get(cardNumber);
        if (c != null && c.getPin().equals(pin)) {
            return c;
        }
        return null;
    }

    public Technician authenticateTechnician(String username, String password) {
        Technician t = techniciansByUser.get(username);
        if (t != null && t.getPassword().equals(password)) {
            return t;
        }
        return null;
    }

    // --- Account / monetary operations ---

    public boolean deposit(Customer customer, BigDecimal amount) {
        if (amount.signum() <= 0) return false;
        customer.getAccount().deposit(amount);
        atmCashStock = atmCashStock.add(amount);
        return true;
    }

    public boolean withdraw(Customer customer, BigDecimal amount) {
        if (amount.signum() <= 0) return false;
        if (atmCashStock.compareTo(amount) < 0) {
            return false; // ATM does not have enough cash
        }
        if (!customer.getAccount().withdraw(amount)) {
            return false; // customer has not enough balance
        }
        atmCashStock = atmCashStock.subtract(amount);
        return true;
    }

    public boolean transfer(Customer from, String targetCardNumber, BigDecimal amount) {
        if (amount.signum() <= 0) return false;
        Customer to = customersByCard.get(targetCardNumber);
        if (to == null) return false;
        Account fromAcc = from.getAccount();
        if (fromAcc.getBalance().compareTo(amount) < 0) {
            return false;
        }
        fromAcc.withdraw(amount);
        to.getAccount().deposit(amount);
        return true;
    }

    public BigDecimal getBalance(Customer customer) {
        return customer.getAccount().getBalance();
    }

    public boolean isValidCard(String cardNumber) {
        return customersByCard.containsKey(cardNumber);
    }

    // --- Emergency / maintenance ---

    public void reportEmergency(String type, String cardNumber) {
        // In real system this would notify the bank; here we just switch state if needed
        if ("Stuck Card".equalsIgnoreCase(type) ||
                "Cash Jam".equalsIgnoreCase(type)) {
            atmState = ATMState.NEED_MAINTENANCE;
        }
        // For demo we don't store logs; assume report processed < 1 minute
    }

    public ATMState getAtmState() {
        return atmState;
    }

    public void setAtmState(ATMState atmState) {
        this.atmState = atmState;
    }

    public boolean selfCheckOk() {
        // simple mock: always OK unless NEED_MAINTENANCE
        return atmState != ATMState.NEED_MAINTENANCE;
    }

    public BigDecimal getAtmCashStock() {
        return atmCashStock;
    }
}


