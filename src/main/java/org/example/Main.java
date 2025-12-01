package org.example;

/**
 * Entry point for the ATM Transaction System demo.
 *
 * This is a simple console application that simulates the main use cases
 * defined in the homework:
 * - MonetaryTransaction (deposit, withdraw, check balance, transfer)
 * - ManageAccount (change PIN, report emergency)
 * - SystemMaintenance (technician maintenance mode)
 *
 * The focus is on showing the logic and flows, not on real networking or encryption.
 */
public class Main {
    public static void main(String[] args) {
        new org.example.console.ATMConsoleApp().run();
    }
}


