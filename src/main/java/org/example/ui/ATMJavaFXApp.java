package org.example.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.ATMState;
import org.example.model.Customer;
import org.example.model.Technician;
import org.example.service.BankCentralSystem;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Simple JavaFX UI for the ATM Transaction System.
 * This reuses the same domain and service classes as the console version.
 */
public class ATMJavaFXApp extends Application {

    private final BankCentralSystem bank = new BankCentralSystem();

    private Stage primaryStage;

    private Customer currentCustomer;
    private Technician currentTechnician;



    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("ATM Transaction System - JavaFX");
        showHomeScene();
        primaryStage.show();
    }

    // --- Scene builders ---

    private void showHomeScene() {
        Label title = new Label("ATM Transaction System");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label stateLabel = new Label("ATM State: " + bank.getAtmState());

        Button customerBtn = new Button("Customer Login");
        Button techBtn = new Button("Technician Login");
        Button exitBtn = new Button("Exit");

        customerBtn.setMaxWidth(Double.MAX_VALUE);
        techBtn.setMaxWidth(Double.MAX_VALUE);
        exitBtn.setMaxWidth(Double.MAX_VALUE);

        customerBtn.setOnAction(e -> showCustomerLoginScene());
        techBtn.setOnAction(e -> showTechnicianLoginScene());
        exitBtn.setOnAction(e -> primaryStage.close());

        VBox center = new VBox(10, title, stateLabel, customerBtn, techBtn, exitBtn);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(20));

        BorderPane root = new BorderPane();
        root.setCenter(center);

        primaryStage.setScene(new Scene(root, 420, 300));
    }

    // --- Customer flow ---

    private void showCustomerLoginScene() {
        if (bank.getAtmState() != ATMState.ACTIVE) {
            showError("ATM is not available for customers right now.");
            showHomeScene();
            return;
        }

        Label title = new Label("Customer Login");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField cardField = new TextField();
        cardField.setPromptText("Card Number");

        PasswordField pinField = new PasswordField();
        pinField.setPromptText("PIN");

        Button loginBtn = new Button("Login");
        Button backBtn = new Button("Back");

        HBox buttons = new HBox(10, backBtn, loginBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox box = new VBox(10, title, cardField, pinField, buttons);
        box.setPadding(new Insets(20));

        backBtn.setOnAction(e -> showHomeScene());
        loginBtn.setOnAction(e -> {
            long start = System.currentTimeMillis();
            Customer c = bank.authenticateCustomer(cardField.getText().trim(), pinField.getText().trim());
            long duration = System.currentTimeMillis() - start;
            if (c == null) {
                showError("Authentication failed (" + duration + " ms).");
            } else {
                this.currentCustomer = c;
                showInfo("Welcome, " + c.getName() + " (" + duration + " ms)");
                showCustomerMenuScene();
            }
        });

        BorderPane root = new BorderPane();
        root.setCenter(box);
        primaryStage.setScene(new Scene(root, 420, 260));
    }

    private void showCustomerMenuScene() {
        if (currentCustomer == null) {
            showHomeScene();
            return;
        }

        Label title = new Label("Customer Menu");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label nameLabel = new Label("Customer: " + currentCustomer.getName());

        Button monetaryBtn = new Button("Monetary Transactions");
        Button manageBtn = new Button("Manage Account");
        Button logoutBtn = new Button("Logout (Card Returned)");

        monetaryBtn.setMaxWidth(Double.MAX_VALUE);
        manageBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setMaxWidth(Double.MAX_VALUE);

        monetaryBtn.setOnAction(e -> {
            showMonetaryScene();
            // Sistem gereksinimine göre: işlemden sonra otomatik logout
        });
        manageBtn.setOnAction(e -> {
            showManageAccountScene();
            // İşlemden sonra otomatik logout
        });
        logoutBtn.setOnAction(e -> {
            this.currentCustomer = null;
            showInfo("Card returned. Session finished.");
            showHomeScene();
        });

        VBox vbox = new VBox(10, title, nameLabel, monetaryBtn, manageBtn, logoutBtn);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setCenter(vbox);

        primaryStage.setScene(new Scene(root, 420, 260));
    }

    private void showMonetaryScene() {
        if (currentCustomer == null) {
            showHomeScene();
            return;
        }

        Label title = new Label("Monetary Transactions");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button depositBtn = new Button("Deposit");
        Button withdrawBtn = new Button("Withdraw");
        Button balanceBtn = new Button("Check Balance");
        Button transferBtn = new Button("Transfer Money");

        depositBtn.setMaxWidth(Double.MAX_VALUE);
        withdrawBtn.setMaxWidth(Double.MAX_VALUE);
        balanceBtn.setMaxWidth(Double.MAX_VALUE);
        transferBtn.setMaxWidth(Double.MAX_VALUE);

        depositBtn.setOnAction(e -> doDepositFx());
        withdrawBtn.setOnAction(e -> doWithdrawFx());
        balanceBtn.setOnAction(e -> {
            showInfo("Your current balance: " + bank.getBalance(currentCustomer) + " TL");
            autoLogoutAfterTransaction();
        });
        transferBtn.setOnAction(e -> doTransferFx());

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> showCustomerMenuScene());

        VBox vbox = new VBox(10, title, depositBtn, withdrawBtn, balanceBtn, transferBtn, backBtn);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        primaryStage.setScene(new Scene(vbox, 420, 280));
    }

    private void showManageAccountScene() {
        if (currentCustomer == null) {
            showHomeScene();
            return;
        }

        Label title = new Label("Manage Account");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button changePinBtn = new Button("Change PIN");
        Button emergencyBtn = new Button("Report Emergency");
        Button backBtn = new Button("Back");

        changePinBtn.setMaxWidth(Double.MAX_VALUE);
        emergencyBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setMaxWidth(Double.MAX_VALUE);

        changePinBtn.setOnAction(e -> doChangePinFx());
        emergencyBtn.setOnAction(e -> doReportEmergencyFx());
        backBtn.setOnAction(e -> showCustomerMenuScene());

        VBox vbox = new VBox(10, title, changePinBtn, emergencyBtn, backBtn);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        primaryStage.setScene(new Scene(vbox, 420, 240));
    }

    // --- Technician flow ---

    private void showTechnicianLoginScene() {
        Label title = new Label("Technician Login");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField userField = new TextField();
        userField.setPromptText("Username");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");

        Button loginBtn = new Button("Login");
        Button backBtn = new Button("Back");

        HBox buttons = new HBox(10, backBtn, loginBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox vbox = new VBox(10, title, userField, passField, buttons);
        vbox.setPadding(new Insets(20));

        backBtn.setOnAction(e -> showHomeScene());
        loginBtn.setOnAction(e -> {
            Technician tech = bank.authenticateTechnician(userField.getText().trim(), passField.getText().trim());
            if (tech == null) {
                showError("Access denied.");
            } else {
                this.currentTechnician = tech;
                showInfo("Welcome, technician " + tech.getUsername());
                showTechnicianMenuScene();
            }
        });

        primaryStage.setScene(new Scene(vbox, 420, 240));
    }

    private void showTechnicianMenuScene() {
        if (currentTechnician == null) {
            showHomeScene();
            return;
        }

        Label title = new Label("Maintenance Menu");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button enableBtn = new Button("Enable Maintenance Mode");
        Button disableBtn = new Button("Disable Maintenance Mode");
        Button logoutBtn = new Button("Log Out");

        enableBtn.setMaxWidth(Double.MAX_VALUE);
        disableBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setMaxWidth(Double.MAX_VALUE);

        enableBtn.setOnAction(e -> enableMaintenanceFx());
        disableBtn.setOnAction(e -> disableMaintenanceFx());
        logoutBtn.setOnAction(e -> {
            this.currentTechnician = null;
            showInfo("Technician logged out.");
            showHomeScene();
        });

        VBox vbox = new VBox(10, title, enableBtn, disableBtn, logoutBtn);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        primaryStage.setScene(new Scene(vbox, 420, 240));
    }

    // --- Monetary actions (FX) ---

    private void doDepositFx() {
        if (currentCustomer == null) return;
        Optional<String> result = showTextInput("Deposit", "Enter amount to deposit:");
        if (result.isEmpty()) return;
        BigDecimal amount = parseAmount(result.get());
        if (amount == null) return;

        if (!showConfirmation("Confirm deposit of " + amount + " TL?")) {
            showInfo("Deposit cancelled. Money and card returned.");
            autoLogoutAfterTransaction();
            return;
        }
        boolean ok = bank.deposit(currentCustomer, amount);
        if (ok) {
            showInfo("Deposit successful. New balance: " + bank.getBalance(currentCustomer) + " TL");
        } else {
            showError("Error while depositing. Operation cancelled.");
        }
        autoLogoutAfterTransaction();
    }

    private void doWithdrawFx() {
        if (currentCustomer == null) return;
        while (true) {
            Optional<String> result = showTextInput("Withdraw", "Enter amount to withdraw:");
            if (result.isEmpty()) return;
            BigDecimal amount = parseAmount(result.get());
            if (amount == null) return;

            if (bank.getAtmCashStock().compareTo(amount) < 0) {
                boolean again = showConfirmation("ATM doesn't have enough cash. Enter different amount?");
                if (!again) {
                    showInfo("Card returned.");
                    autoLogoutAfterTransaction();
                    return;
                }
                continue;
            }

            if (!showConfirmation("Confirm withdrawal of " + amount + " TL?")) {
                showInfo("Withdrawal cancelled. Card returned.");
                autoLogoutAfterTransaction();
                return;
            }

            boolean ok = bank.withdraw(currentCustomer, amount);
            if (!ok) {
                showError("Insufficient funds. Please enter an amount within your balance.");
            } else {
                showInfo("Please take your cash. New balance: " + bank.getBalance(currentCustomer) + " TL");
            }
            autoLogoutAfterTransaction();
            return;
        }
    }

    private void doTransferFx() {
        if (currentCustomer == null) return;
        Optional<String> cardResult = showTextInput("Transfer", "Enter target card number:");
        if (cardResult.isEmpty()) return;
        String targetCard = cardResult.get().trim();
        if (!bank.isValidCard(targetCard)) {
            showError("Invalid card number.");
            return;
        }

        if (!showConfirmation("Is this target card correct?\n" + targetCard)) {
            showInfo("Transfer cancelled. Card returned.");
            autoLogoutAfterTransaction();
            return;
        }

        Optional<String> amountResult = showTextInput("Transfer", "Enter amount to transfer:");
        if (amountResult.isEmpty()) return;
        BigDecimal amount = parseAmount(amountResult.get());
        if (amount == null) return;

        boolean ok = bank.transfer(currentCustomer, targetCard, amount);
        if (ok) {
            showInfo("Transfer completed. New balance: " + bank.getBalance(currentCustomer) + " TL");
        } else {
            showError("Transfer failed (insufficient funds or system error).");
        }
        autoLogoutAfterTransaction();
    }

    // --- Manage account (FX) ---

    private void doChangePinFx() {
        if (currentCustomer == null) return;
        Optional<String> oldPinRes = showTextInput("Change PIN", "Enter your old PIN:");
        if (oldPinRes.isEmpty()) return;
        if (!currentCustomer.getPin().equals(oldPinRes.get().trim())) {
            showError("Incorrect PIN. Card returned.");
            autoLogoutAfterTransaction();
            return;
        }

        Optional<String> newPin1Res = showTextInput("Change PIN", "Enter your new PIN:");
        if (newPin1Res.isEmpty()) return;
        Optional<String> newPin2Res = showTextInput("Change PIN", "Re-enter your new PIN:");
        if (newPin2Res.isEmpty()) return;

        if (!newPin1Res.get().trim().equals(newPin2Res.get().trim())) {
            showError("PINs do not match, try again. Card returned.");
            autoLogoutAfterTransaction();
            return;
        }
        currentCustomer.setPin(newPin1Res.get().trim());
        showInfo("PIN successfully changed.");
        autoLogoutAfterTransaction();
    }

    private void doReportEmergencyFx() {
        if (currentCustomer == null) return;
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Stuck Card",
                "Stuck Card", "Stolen Card", "Cash Jam");
        dialog.setTitle("Report Emergency");
        dialog.setHeaderText("Select emergency type:");
        Optional<String> res = dialog.showAndWait();
        if (res.isEmpty()) return;

        String type = res.get();
        if (!showConfirmation("Do you confirm to report this?\n" + type)) {
            showInfo("Report cancelled.");
            autoLogoutAfterTransaction();
            return;
        }

        long start = System.currentTimeMillis();
        bank.reportEmergency(type, currentCustomer.getCardNumber());
        long duration = System.currentTimeMillis() - start;

        showInfo("Report received (" + duration + " ms). A technician is going to assist you, please be patient.");
        autoLogoutAfterTransaction();
    }

    // --- Technician actions (FX) ---

    private void enableMaintenanceFx() {
        bank.setAtmState(ATMState.ON_MAINTENANCE);
        showInfo("ATM state changed to ON_MAINTENANCE.\nCard reader locked. Screen: 'Temporarily Out of Service'.");
        showTechnicianMenuScene();
    }

    private void disableMaintenanceFx() {
        if (!bank.selfCheckOk()) {
            showError("Error, check the ATM Machine. Staying in ON_MAINTENANCE.");
            bank.setAtmState(ATMState.ON_MAINTENANCE);
        } else {
            bank.setAtmState(ATMState.ACTIVE);
            showInfo("ATM ready. Screen: 'Ready'.");
        }
        showTechnicianMenuScene();
    }

    // --- Helpers ---

    private void autoLogoutAfterTransaction() {
        // Homework gereksinimine göre: işlem tamamlandığında otomatik logout ve kart iadesi
        this.currentCustomer = null;
        showInfo("Task completed. Card returned. You are logged out.");
        showHomeScene();
    }

    private Optional<String> showTextInput(String title, String message) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(message);
        dialog.setContentText(null);
        return dialog.showAndWait();
    }

    private BigDecimal parseAmount(String text) {
        try {
            BigDecimal amount = new BigDecimal(text.trim());
            if (amount.signum() <= 0) {
                showError("Amount must be positive.");
                return null;
            }
            return amount;
        } catch (NumberFormatException e) {
            showError("Invalid number.");
            return null;
        }
    }

    private boolean showConfirmation(String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(msg);
        alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> res = alert.showAndWait();
        return res.isPresent() && res.get() == ButtonType.OK;
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


