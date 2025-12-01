package org.example.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
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
        VBox header = createHeader("ATM Transaction System", "Secure Banking Experience");

        Label stateLabel = new Label("ATM State: " + bank.getAtmState());
        stateLabel.getStyleClass().add("atm-state-label");

        Button customerBtn = new Button("Customer Login");
        Button techBtn = new Button("Technician Login");
        Button exitBtn = new Button("Exit");

        customerBtn.getStyleClass().add("atm-primary-button");
        techBtn.getStyleClass().add("atm-secondary-button");
        exitBtn.getStyleClass().add("atm-danger-button");

        customerBtn.setMaxWidth(Double.MAX_VALUE);
        techBtn.setMaxWidth(Double.MAX_VALUE);
        exitBtn.setMaxWidth(Double.MAX_VALUE);

        customerBtn.setOnAction(e -> showCustomerLoginScene());
        techBtn.setOnAction(e -> showTechnicianLoginScene());
        exitBtn.setOnAction(e -> primaryStage.close());

        VBox card = new VBox(12, createCardTitle("Welcome"), stateLabel, customerBtn, techBtn, exitBtn);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("atm-card");

        BorderPane root = createBaseLayout(header, card,
                "Insert your card to begin. For assistance, contact your bank.");
        setSceneWithTheme(root, 460, 320);
    }

    // --- Customer flow ---

    private void showCustomerLoginScene() {
        if (bank.getAtmState() != ATMState.ACTIVE) {
            showError("ATM is not available for customers right now.");
            showHomeScene();
            return;
        }

        VBox header = createHeader("Customer Login", "Please insert your card and enter your PIN.");

        TextField cardField = new TextField();
        cardField.setPromptText("Card Number");
        cardField.getStyleClass().add("atm-input");

        PasswordField pinField = new PasswordField();
        pinField.setPromptText("PIN");
        pinField.getStyleClass().add("atm-input");

        Button loginBtn = new Button("Login");
        Button backBtn = new Button("Back");

        loginBtn.getStyleClass().add("atm-primary-button");
        backBtn.getStyleClass().add("atm-secondary-button");

        HBox buttons = new HBox(10, backBtn, loginBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox card = new VBox(10, createCardTitle("Card & PIN"), cardField, pinField, buttons);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("atm-card");

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

        BorderPane root = createBaseLayout(header, card, "Never share your PIN with anyone, including bank staff.");
        setSceneWithTheme(root, 460, 280);
    }

    private void showCustomerMenuScene() {
        if (currentCustomer == null) {
            showHomeScene();
            return;
        }

        Label title = new Label("Customer Menu");
        title.getStyleClass().add("atm-title");

        Label nameLabel = new Label("Customer: " + currentCustomer.getName());
        nameLabel.getStyleClass().add("atm-label");

        Button monetaryBtn = new Button("Monetary Transactions");
        Button manageBtn = new Button("Manage Account");
        Button logoutBtn = new Button("Logout (Card Returned)");

        monetaryBtn.getStyleClass().add("atm-primary-button");
        manageBtn.getStyleClass().add("atm-secondary-button");
        logoutBtn.getStyleClass().add("atm-danger-button");

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

        VBox card = new VBox(10, title, nameLabel, monetaryBtn, manageBtn, logoutBtn);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("atm-card");

        VBox header = createHeader("Customer Menu", "Select the operation you want to perform.");
        BorderPane root = createBaseLayout(header, card,
                "For your security, you will be logged out after each transaction.");
        setSceneWithTheme(root, 460, 300);
    }

    private void showMonetaryScene() {
        if (currentCustomer == null) {
            showHomeScene();
            return;
        }

        Label title = new Label("Monetary Transactions");
        title.getStyleClass().add("atm-title");

        Button depositBtn = new Button("Deposit");
        Button withdrawBtn = new Button("Withdraw");
        Button balanceBtn = new Button("Check Balance");
        Button transferBtn = new Button("Transfer Money");

        depositBtn.getStyleClass().add("atm-primary-button");
        withdrawBtn.getStyleClass().add("atm-primary-button");
        balanceBtn.getStyleClass().add("atm-secondary-button");
        transferBtn.getStyleClass().add("atm-secondary-button");

        depositBtn.setMaxWidth(Double.MAX_VALUE);
        withdrawBtn.setMaxWidth(Double.MAX_VALUE);
        balanceBtn.setMaxWidth(Double.MAX_VALUE);
        transferBtn.setMaxWidth(Double.MAX_VALUE);

        depositBtn.setOnAction(e -> doDepositFx());
        withdrawBtn.setOnAction(e -> doWithdrawFx());
        balanceBtn.setOnAction(e -> {
            showInfo("Your current balance: " + bank.getBalance(currentCustomer) + " TL");
            showMonetaryScene();
        });
        transferBtn.setOnAction(e -> doTransferFx());

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("atm-secondary-button");
        backBtn.setOnAction(e -> showCustomerMenuScene());

        VBox card = new VBox(10, title, depositBtn, withdrawBtn, balanceBtn, transferBtn, backBtn);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("atm-card");

        VBox header = createHeader("Monetary Transactions", "Choose a transaction type.");
        BorderPane root = createBaseLayout(header, card, "Cash withdrawals are limited to daily transaction limits.");
        setSceneWithTheme(root, 480, 320);
    }

    private void showManageAccountScene() {
        if (currentCustomer == null) {
            showHomeScene();
            return;
        }

        Label title = new Label("Manage Account");
        title.getStyleClass().add("atm-title");

        Button changePinBtn = new Button("Change PIN");
        Button emergencyBtn = new Button("Report Emergency");
        Button backBtn = new Button("Back");

        changePinBtn.getStyleClass().add("atm-primary-button");
        emergencyBtn.getStyleClass().add("atm-secondary-button");
        backBtn.getStyleClass().add("atm-secondary-button");

        changePinBtn.setMaxWidth(Double.MAX_VALUE);
        emergencyBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setMaxWidth(Double.MAX_VALUE);

        changePinBtn.setOnAction(e -> doChangePinFx());
        emergencyBtn.setOnAction(e -> doReportEmergencyFx());
        backBtn.setOnAction(e -> showCustomerMenuScene());

        VBox card = new VBox(10, title, changePinBtn, emergencyBtn, backBtn);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("atm-card");

        VBox header = createHeader("Manage Account", "Update your PIN or report an emergency.");
        BorderPane root = createBaseLayout(header, card,
                "In case of stolen card, immediately contact your bank call center.");
        setSceneWithTheme(root, 460, 280);
    }

    // --- Technician flow ---

    private void showTechnicianLoginScene() {
        VBox header = createHeader("Technician Login", "Authorized personnel only.");

        TextField userField = new TextField();
        userField.setPromptText("Username");
        userField.getStyleClass().add("atm-input");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.getStyleClass().add("atm-input");

        Button loginBtn = new Button("Login");
        Button backBtn = new Button("Back");

        loginBtn.getStyleClass().add("atm-primary-button");
        backBtn.getStyleClass().add("atm-secondary-button");

        HBox buttons = new HBox(10, backBtn, loginBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox card = new VBox(10, createCardTitle("Maintenance Access"), userField, passField, buttons);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("atm-card");

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

        BorderPane root = createBaseLayout(header, card, "All actions are logged for security purposes.");
        setSceneWithTheme(root, 460, 280);
    }

    private void showTechnicianMenuScene() {
        if (currentTechnician == null) {
            showHomeScene();
            return;
        }

        Label title = new Label("Maintenance Menu");
        title.getStyleClass().add("atm-title");

        Button enableBtn = new Button("Enable Maintenance Mode");
        Button disableBtn = new Button("Disable Maintenance Mode");
        Button logoutBtn = new Button("Log Out");

        enableBtn.getStyleClass().add("atm-primary-button");
        disableBtn.getStyleClass().add("atm-secondary-button");
        logoutBtn.getStyleClass().add("atm-danger-button");

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

        VBox card = new VBox(10, title, enableBtn, disableBtn, logoutBtn);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("atm-card");

        VBox header = createHeader("Maintenance Menu", "Change ATM availability state.");
        BorderPane root = createBaseLayout(header, card,
                "Ensure no customer is using the ATM before enabling maintenance mode.");
        setSceneWithTheme(root, 460, 280);
    }

    // --- Monetary actions (FX) ---

    private void doDepositFx() {
        if (currentCustomer == null)
            return;
        Optional<String> result = showTextInput("Deposit", "Enter amount to deposit:");
        if (result.isEmpty())
            return;
        BigDecimal amount = parseAmount(result.get());
        if (amount == null)
            return;

        if (!showConfirmation("Confirm deposit of " + amount + " TL?")) {
            showInfo("Deposit cancelled. Money and card returned.");
            showMonetaryScene();
            return;
        }
        boolean ok = bank.deposit(currentCustomer, amount);
        if (ok) {
            showInfo("Deposit successful. New balance: " + bank.getBalance(currentCustomer) + " TL");
        } else {
            showError("Error while depositing. Operation cancelled.");
        }
        showMonetaryScene();
    }

    private void doWithdrawFx() {
        if (currentCustomer == null)
            return;
        while (true) {
            Optional<String> result = showTextInput("Withdraw", "Enter amount to withdraw:");
            if (result.isEmpty())
                return;
            BigDecimal amount = parseAmount(result.get());
            if (amount == null)
                return;

            if (bank.getAtmCashStock().compareTo(amount) < 0) {
                boolean again = showConfirmation("ATM doesn't have enough cash. Enter different amount?");
                if (!again) {
                    showInfo("Card returned.");
                    showMonetaryScene();
                    return;
                }
                continue;
            }

            if (!showConfirmation("Confirm withdrawal of " + amount + " TL?")) {
                showInfo("Withdrawal cancelled. Card returned.");
                showMonetaryScene();
                return;
            }

            boolean ok = bank.withdraw(currentCustomer, amount);
            if (!ok) {
                showError("Insufficient funds. Please enter an amount within your balance.");
                continue;
            } else {
                showInfo("Please take your cash. New balance: " + bank.getBalance(currentCustomer) + " TL");
                autoLogoutAfterTransaction();
                return;
            }
        }
    }

    private void doTransferFx() {
        if (currentCustomer == null)
            return;
        Optional<String> cardResult = showTextInput("Transfer", "Enter target card number:");
        if (cardResult.isEmpty())
            return;
        String targetCard = cardResult.get().trim();
        if (!bank.isValidCard(targetCard)) {
            showError("Invalid card number.");
            return;
        }

        if (!showConfirmation("Is this target card correct?\n" + targetCard)) {
            showInfo("Transfer cancelled. Card returned.");
            showMonetaryScene();
            return;
        }

        Optional<String> amountResult = showTextInput("Transfer", "Enter amount to transfer:");
        if (amountResult.isEmpty())
            return;
        BigDecimal amount = parseAmount(amountResult.get());
        if (amount == null)
            return;

        boolean ok = bank.transfer(currentCustomer, targetCard, amount);
        if (ok) {
            showInfo("Transfer completed. New balance: " + bank.getBalance(currentCustomer) + " TL");
        } else {
            showError("Transfer failed (insufficient funds or system error).");
        }
        showMonetaryScene();
    }

    // --- Manage account (FX) ---

    private void doChangePinFx() {
        if (currentCustomer == null)
            return;
        Optional<String> oldPinRes = showTextInput("Change PIN", "Enter your old PIN:");
        if (oldPinRes.isEmpty())
            return;
        if (!currentCustomer.getPin().equals(oldPinRes.get().trim())) {
            showError("Incorrect PIN. Card returned.");
            autoLogoutAfterTransaction();
            return;
        }

        Optional<String> newPin1Res = showTextInput("Change PIN", "Enter your new PIN:");
        if (newPin1Res.isEmpty())
            return;
        Optional<String> newPin2Res = showTextInput("Change PIN", "Re-enter your new PIN:");
        if (newPin2Res.isEmpty())
            return;

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
        if (currentCustomer == null)
            return;
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Stuck Card",
                "Stuck Card", "Stolen Card", "Cash Jam");
        dialog.setTitle("Report Emergency");
        dialog.setHeaderText("Select emergency type:");
        Optional<String> res = dialog.showAndWait();
        if (res.isEmpty())
            return;

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
        // Homework gereksinimine göre: işlem tamamlandığında otomatik logout ve kart
        // iadesi
        this.currentCustomer = null;
        showInfo("Task completed. Card returned. You are logged out.");
        showHomeScene();
    }

    private Optional<String> showTextInput(String title, String message) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(title);
        dialog.setContentText(message);
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
        alert.setHeaderText("Confirmation");
        alert.setContentText(msg);
        alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> res = alert.showAndWait();
        return res.isPresent() && res.get() == ButtonType.OK;
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText("Information");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // --- Layout helpers for more realistic UI ---

    private VBox createHeader(String titleText, String subtitleText) {
        Label title = new Label(titleText);
        title.getStyleClass().add("atm-header-title");

        Label subtitle = new Label(subtitleText);
        subtitle.getStyleClass().add("atm-header-subtitle");

        VBox headerText = new VBox(2, title, subtitle);

        HBox header = new HBox(headerText);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("atm-header");

        VBox wrapper = new VBox(header);
        return wrapper;
    }

    private Label createCardTitle(String text) {
        Label title = new Label(text);
        title.getStyleClass().add("atm-title");
        return title;
    }

    private BorderPane createBaseLayout(VBox header, VBox centerCard, String footerText) {
        BorderPane root = new BorderPane();
        root.setTop(header);

        StackPane centerWrapper = new StackPane(centerCard);
        centerWrapper.setPadding(new Insets(24));
        root.setCenter(centerWrapper);

        if (footerText != null && !footerText.isEmpty()) {
            Text footer = new Text(footerText);
            footer.getStyleClass().add("atm-footer-text");
            HBox footerBox = new HBox(footer);
            footerBox.setAlignment(Pos.CENTER);
            footerBox.getStyleClass().add("atm-footer");
            root.setBottom(footerBox);
        }

        return root;
    }

    private void setSceneWithTheme(Parent root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        String css = getClass().getResource("/atm-style.css") != null
                ? getClass().getResource("/atm-style.css").toExternalForm()
                : null;
        if (css != null) {
            scene.getStylesheets().add(css);
        }
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
