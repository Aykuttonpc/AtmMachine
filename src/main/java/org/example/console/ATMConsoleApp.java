package org.example.console;

import org.example.model.ATMState;
import org.example.model.Customer;
import org.example.model.Technician;
import org.example.service.BankCentralSystem;

import java.math.BigDecimal;
import java.util.Scanner;

/**
 * Simple console UI that simulates the ATM front panel and technician keyboard.
 * This class drives the three main use cases described in the homework.
 */
public class ATMConsoleApp {

    private final BankCentralSystem bank = new BankCentralSystem();
    private final Scanner scanner = new Scanner(System.in);

    public void run() {
        while (true) {
            System.out.println("=== ATM Transaction System ===");
            System.out.println("ATM State: " + bank.getAtmState());
            System.out.println("1) Customer login");
            System.out.println("2) Technician login");
            System.out.println("0) Exit");
            System.out.print("Select: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    handleCustomerSession();
                    break;
                case "2":
                    handleTechnicianSession();
                    break;
                case "0":
                    System.out.println("Goodbye.");
                    return;
                default:
                    System.out.println("Invalid selection.\n");
            }
        }
    }

    // --- Customer flow (covers MonetaryTransaction + ManageAccount) ---

    private void handleCustomerSession() {
        if (bank.getAtmState() != ATMState.ACTIVE) {
            System.out.println("ATM is not available for customers right now.\n");
            return;
        }
        System.out.println("\n--- Customer Login ---");
        System.out.print("Enter card number: ");
        String card = scanner.nextLine().trim();
        System.out.print("Enter PIN: ");
        String pin = scanner.nextLine().trim();

        long start = System.currentTimeMillis();
        Customer customer = bank.authenticateCustomer(card, pin);
        long durationMs = System.currentTimeMillis() - start;

        if (customer == null) {
            System.out.println("Authentication failed (" + durationMs + " ms).\n");
            return;
        }
        System.out.println("Welcome, " + customer.getName() + " (" + durationMs + " ms)\n");

        boolean done = false;
        while (!done) {
            System.out.println("--- Customer Menu ---");
            System.out.println("1) Monetary Transactions");
            System.out.println("2) Manage Account");
            System.out.println("0) Logout (card returned)");
            System.out.print("Select: ");
            String sel = scanner.nextLine().trim();
            switch (sel) {
                case "1":
                    handleMonetaryTransactions(customer);
                    done = true; // system auto-logout after a transaction
                    break;
                case "2":
                    handleManageAccount(customer);
                    done = true; // system auto-logout after a transaction
                    break;
                case "0":
                    done = true;
                    break;
                default:
                    System.out.println("Invalid selection.\n");
            }
        }
        System.out.println("Card returned. Session finished.\n");
    }

    private void handleMonetaryTransactions(Customer customer) {
        if (bank.getAtmState() != ATMState.ACTIVE) {
            System.out.println("ATM is not active.\n");
            return;
        }
        System.out.println("\n--- Monetary Transaction ---");
        System.out.println("1) Deposit");
        System.out.println("2) Withdraw");
        System.out.println("3) Check Balance");
        System.out.println("4) Transfer Money");
        System.out.println("0) Cancel");
        System.out.print("Select: ");
        String sel = scanner.nextLine().trim();
        switch (sel) {
            case "1":
                doDeposit(customer);
                break;
            case "2":
                doWithdraw(customer);
                break;
            case "3":
                doCheckBalance(customer);
                break;
            case "4":
                doTransfer(customer);
                break;
            case "0":
                System.out.println("Transaction cancelled.\n");
                break;
            default:
                System.out.println("Invalid selection.\n");
        }
    }

    private void doDeposit(Customer customer) {
        System.out.print("Enter amount to deposit: ");
        BigDecimal amount = readAmount();
        if (amount == null)
            return;
        System.out.println("You are depositing: " + amount + " TL");
        if (!confirm()) {
            System.out.println("Deposit cancelled. Returning money and card.\n");
            return;
        }
        boolean ok = bank.deposit(customer, amount);
        if (ok) {
            System.out.println("Deposit successful. New balance: " + bank.getBalance(customer) + " TL\n");
        } else {
            System.out.println("Error while depositing. Operation cancelled.\n");
        }
    }

    private void doWithdraw(Customer customer) {
        while (true) {
            System.out.print("Enter amount to withdraw: ");
            BigDecimal amount = readAmount();
            if (amount == null)
                return;

            if (bank.getAtmCashStock().compareTo(amount) < 0) {
                System.out.println("ATM does not have enough cash for this withdrawal.");
                System.out.print("Do you want to enter a different amount? (y/n): ");
                String again = scanner.nextLine().trim();
                if (!again.equalsIgnoreCase("y")) {
                    System.out.println("Card returned.\n");
                    return;
                }
                continue;
            }

            System.out.println("You are withdrawing: " + amount + " TL");
            if (!confirm()) {
                System.out.println("Withdrawal cancelled. Card returned.\n");
                return;
            }

            boolean ok = bank.withdraw(customer, amount);
            if (!ok) {
                System.out.println("Insufficient funds. Please enter an amount within your balance.\n");
            } else {
                System.out.println("Please take your cash. New balance: " + bank.getBalance(customer) + " TL\n");
            }
            return;
        }
    }

    private void doCheckBalance(Customer customer) {
        System.out.println("Your current balance: " + bank.getBalance(customer) + " TL");
        System.out.println("Card returned.\n");
    }

    private void doTransfer(Customer customer) {
        System.out.print("Enter target card number: ");
        String target = scanner.nextLine().trim();
        if (!bank.isValidCard(target)) {
            System.out.println("Invalid card number.\n");
            return;
        }
        System.out.println("Target card number: " + target);
        if (!confirm()) {
            System.out.println("Transfer cancelled. Card returned.\n");
            return;
        }
        System.out.print("Enter amount to transfer: ");
        BigDecimal amount = readAmount();
        if (amount == null)
            return;
        boolean ok = bank.transfer(customer, target, amount);
        if (ok) {
            System.out.println("Transfer completed. New balance: " + bank.getBalance(customer) + " TL\n");
        } else {
            System.out.println("Transfer failed (insufficient funds or system error).\n");
        }
    }

    // --- ManageAccount use case ---

    private void handleManageAccount(Customer customer) {
        System.out.println("\n--- Manage Account ---");
        System.out.println("1) Change PIN");
        System.out.println("2) Report Emergency");
        System.out.println("0) Cancel");
        System.out.print("Select: ");
        String sel = scanner.nextLine().trim();
        switch (sel) {
            case "1":
                doChangePin(customer);
                break;
            case "2":
                doReportEmergency(customer);
                break;
            case "0":
                System.out.println("Operation cancelled.\n");
                break;
            default:
                System.out.println("Invalid selection.\n");
        }
    }

    private void doChangePin(Customer customer) {
        System.out.print("Enter your old PIN: ");
        String oldPin = scanner.nextLine().trim();
        if (!customer.getPin().equals(oldPin)) {
            System.out.println("Incorrect PIN. Card returned.\n");
            return;
        }
        System.out.print("Enter your new PIN: ");
        String newPin1 = scanner.nextLine().trim();
        System.out.print("Re-enter your new PIN: ");
        String newPin2 = scanner.nextLine().trim();
        if (!newPin1.equals(newPin2)) {
            System.out.println("PINs do not match, try again. Card returned.\n");
            return;
        }
        customer.setPin(newPin1);
        System.out.println("PIN successfully changed.\n");
    }

    private void doReportEmergency(Customer customer) {
        System.out.println("Select emergency type:");
        System.out.println("1) Stuck Card");
        System.out.println("2) Stolen Card");
        System.out.println("3) Cash Jam");
        System.out.print("Select: ");
        String sel = scanner.nextLine().trim();
        String type;
        switch (sel) {
            case "1":
                type = "Stuck Card";
                break;
            case "2":
                type = "Stolen Card";
                break;
            case "3":
                type = "Cash Jam";
                break;
            default:
                System.out.println("Invalid selection.\n");
                return;
        }
        System.out.println("You selected: " + type);
        if (!confirm()) {
            System.out.println("Report cancelled.\n");
            return;
        }
        long start = System.currentTimeMillis();
        bank.reportEmergency(type, customer.getCardNumber());
        long duration = System.currentTimeMillis() - start;
        System.out.println("Report received (" + duration + " ms). A technician will assist you. Please be patient.\n");
    }

    // --- Technician / SystemMaintenance use case ---

    private void handleTechnicianSession() {
        System.out.println("\n--- Technician Login ---");
        System.out.print("Username: ");
        String user = scanner.nextLine().trim();
        System.out.print("Password: ");
        String pass = scanner.nextLine().trim();

        Technician tech = bank.authenticateTechnician(user, pass);
        if (tech == null) {
            System.out.println("Access denied.\n");
            return;
        }
        System.out.println("Welcome, technician " + tech.getUsername() + ".\n");

        boolean done = false;
        while (!done) {
            System.out.println("--- Maintenance Menu ---");
            System.out.println("1) Enable Maintenance Mode");
            System.out.println("2) Disable Maintenance Mode");
            System.out.println("3) Log Out");
            System.out.print("Select: ");
            String sel = scanner.nextLine().trim();
            switch (sel) {
                case "1":
                    enableMaintenance();
                    break;
                case "2":
                    disableMaintenance();
                    break;
                case "3":
                    done = true;
                    break;
                default:
                    System.out.println("Invalid selection.\n");
            }
        }
        System.out.println("Technician logged out.\n");
    }

    private void enableMaintenance() {
        bank.setAtmState(ATMState.ON_MAINTENANCE);
        System.out.println("ATM state changed to ON_MAINTENANCE.");
        System.out.println("Card reader locked. Screen: 'Temporarily Out of Service'.");
        System.out.println("Technician can now physically refill cash / fix errors, then press confirm.\n");
        System.out.print("Press ENTER to confirm finished maintenance...");
        scanner.nextLine();
    }

    private void disableMaintenance() {
        System.out.println("Running self-check...");
        if (!bank.selfCheckOk()) {
            System.out.println("Error, check the ATM Machine. Staying in ON_MAINTENANCE.\n");
            bank.setAtmState(ATMState.ON_MAINTENANCE);
        } else {
            bank.setAtmState(ATMState.ACTIVE);
            System.out.println("ATM ready. Screen: 'Ready'.\n");
        }
    }

    // --- Helpers ---

    private boolean confirm() {
        System.out.print("Confirm? (y/n): ");
        String ans = scanner.nextLine().trim();
        return ans.equalsIgnoreCase("y");
    }

    private BigDecimal readAmount() {
        String text = scanner.nextLine().trim();
        try {
            BigDecimal amount = new BigDecimal(text);
            if (amount.signum() <= 0) {
                System.out.println("Amount must be positive.\n");
                return null;
            }
            return amount;
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.\n");
            return null;
        }
    }
}
