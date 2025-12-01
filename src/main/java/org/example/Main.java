package org.example;

/**
 * Entry point for the ATM Transaction System.
 *
 * Varsayılan olarak JavaFX arayüzünü açar.
 * Konsol versiyonuna ihtiyaç olursa, ATMConsoleApp'i ayrıca çalıştırabilirsin.
 */
public class Main {
    public static void main(String[] args) {
        // JavaFX UI
        org.example.ui.ATMJavaFXApp.main(args);

        // Konsol UI'yi çalıştırmak istersen:
        // new org.example.console.ATMConsoleApp().run();
    }
}


