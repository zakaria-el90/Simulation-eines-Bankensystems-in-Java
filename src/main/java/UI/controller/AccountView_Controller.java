package UI.controller;

import bank.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import bank.PrivateBank;
import java.util.List;
import bank.Payment;
import bank.IncomingTransfer;
import bank.OutgoingTransfer;



public class AccountView_Controller {

    // ---- UI aus Accountview.fxml ----
    @FXML private Label accountNameLabel;
    @FXML private Label balanceLabel;
    @FXML private ListView<Transaction> transactionsListView;

    // ---- Daten ----
    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();

    private PrivateBank bank;
    private Stage stage;
    private String accountName;

    private enum ViewMode {
        NORMAL,
        SORT_ASC,
        SORT_DESC,
        ONLY_POSITIVE,
        ONLY_NEGATIVE
    }

    private ViewMode currentMode = ViewMode.NORMAL;

    @FXML
    private void handleSortAsc() {
        currentMode = ViewMode.SORT_ASC;
        applyMode();
    }

    @FXML
    private void handleSortDesc() {
        currentMode = ViewMode.SORT_DESC;
        applyMode();
    }

    @FXML
    private void handleShowAll() {
        currentMode = ViewMode.NORMAL;
        applyMode();
    }

    @FXML
    private void handleShowPositive() {
        currentMode = ViewMode.ONLY_POSITIVE;
        applyMode();
    }

    @FXML
    private void handleShowNegative() {
        currentMode = ViewMode.ONLY_NEGATIVE;
        applyMode();
    }


    @FXML
    private void initialize() {
        transactionsListView.setItems(transactions);
    }

    // Wird von MainviewController beim Scene-Wechsel aufgerufen
    public void init(PrivateBank bank, Stage stage, String accountName) {
        this.bank = bank;
        this.stage = stage;
        this.accountName = accountName;

        refreshView();
    }

    private void refreshView() {
        // Accountname anzeigen
        accountNameLabel.setText("Account: " + accountName);

        // Alles Laden + Balance anzeigen macht applyMode()
        applyMode();
    }


    // ---- Back Button: zurück zur Mainview (gleiches Fenster/Stage) ----
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            Parent root = loader.load();

            MainView_Controller controller = loader.getController();
            controller.init(bank, stage); // gleiche Bank weitergeben

            stage.setScene(new Scene(root));
        } catch (Exception ex) {
            showError("Zurück zur Mainview fehlgeschlagen", ex.getMessage());
        }
    }

    @FXML
    private void handleCreateTransaction() {
        Optional<Transaction> created = showCreateTransactionDialog();
        if (created.isEmpty()) {
            return; // Abgebrochen
        }

        try {
            // Methode bei dir evtl. anders benannt (addTransaction / addTransactionToAccount / etc.)
            bank.addTransaction(accountName, created.get());
            applyMode(); // Liste + Kontostand aktualisieren
        } catch (Exception ex) {
            showError("Transaktion konnte nicht hinzugefügt werden", ex.getMessage());
        }
    }

    private Optional<Transaction> showCreateTransactionDialog() {
        Dialog<Transaction> dialog = new Dialog<>();
        dialog.setTitle("Neue Transaktion");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // --- Eingabefelder ---
        ChoiceBox<String> typeChoice = new ChoiceBox<>(
                FXCollections.observableArrayList("Payment", "Transfer")
        );
        typeChoice.setValue("Payment");

        DatePicker datePicker = new DatePicker();
        TextField amountField = new TextField();
        TextField descriptionField = new TextField();

        // Transfer-Felder:
        TextField senderField = new TextField();
        TextField recipientField = new TextField();

        // Layout:
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        int r = 0;
        grid.add(new Label("Typ:"), 0, r);
        grid.add(typeChoice, 1, r++);

        grid.add(new Label("Datum:"), 0, r);
        grid.add(datePicker, 1, r++);

        grid.add(new Label("Betrag:"), 0, r);
        grid.add(amountField, 1, r++);

        grid.add(new Label("Beschreibung:"), 0, r);
        grid.add(descriptionField, 1, r++);

        grid.add(new Label("Sender (nur Transfer):"), 0, r);
        grid.add(senderField, 1, r++);

        grid.add(new Label("Empfänger (nur Transfer):"), 0, r);
        grid.add(recipientField, 1, r++);

        dialog.getDialogPane().setContent(grid);

        // Transfer-Felder am Anfang verstecken/zeigen
        Runnable updateTransferFields = () -> {
            boolean isTransfer = "Transfer".equals(typeChoice.getValue());
            senderField.setDisable(!isTransfer);
            recipientField.setDisable(!isTransfer);
            if (!isTransfer) {
                senderField.clear();
                recipientField.clear();
            }
        };
        updateTransferFields.run();
        typeChoice.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> updateTransferFields.run());

        // --- Validierung: wenn was fehlt → Fehler & Dialog bleibt offen ---
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            String typ = typeChoice.getValue();

            if (datePicker.getValue() == null
                    || amountField.getText().trim().isEmpty()
                    || descriptionField.getText().trim().isEmpty()) {
                showError("Fehler", "Bitte Datum, Betrag und Beschreibung ausfüllen.");
                e.consume();
                return;
            }

            try {
                Double.parseDouble(amountField.getText().trim());
            } catch (NumberFormatException ex) {
                showError("Fehler", "Betrag muss eine Zahl sein (z.B. 12.5).");
                e.consume();
                return;
            }

            if ("Transfer".equals(typ)) {
                if (senderField.getText().trim().isEmpty() || recipientField.getText().trim().isEmpty()) {
                    showError("Fehler", "Bei Transfer bitte Sender und Empfänger ausfüllen.");
                    e.consume();
                    return;
                }

                String sender = senderField.getText().trim();
                String recipient = recipientField.getText().trim();

                // Programmgesteuert Incoming/Outgoing entscheiden :contentReference[oaicite:2]{index=2}
                if (!accountName.equals(sender) && !accountName.equals(recipient)) {
                    showError("Fehler", "Transfer muss diesen Account betreffen (Sender oder Empfänger = aktueller Account).");
                    e.consume();
                }
            }
        });

        // Ergebnis bauen (nur wenn OK gedrückt wurde)
        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;

            LocalDate date = datePicker.getValue();
            double amount = Double.parseDouble(amountField.getText().trim());
            String desc = descriptionField.getText().trim();

            String typ = typeChoice.getValue();

            if ("Payment".equals(typ)) {
                // Passe diesen Konstruktor an DEINE Payment-Klasse an:
                // Beispiel häufig: new Payment(date.toString(), amount, desc, incomingInterest, outgoingInterest)
                return new Payment(date.toString(), amount, desc);
            } else {
                String sender = senderField.getText().trim();
                String recipient = recipientField.getText().trim();

                // Betrag bei Transfer oft als positive Zahl; Richtung steckt in Incoming/Outgoing
                double absAmount = Math.abs(amount);

                if (accountName.equals(sender) && !accountName.equals(recipient)) {
                    return new OutgoingTransfer(date.toString(), absAmount, desc, sender, recipient);
                } else {
                    return new IncomingTransfer(date.toString(), absAmount, desc, sender, recipient);
                }
            }
        });

        return dialog.showAndWait();
    }

    private void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    private void applyMode() {
        try {
            // 1) Liste je nach Modus holen
            List<Transaction> txs = switch (currentMode) {
                case SORT_ASC -> bank.getTransactionsSorted(accountName, true);
                case SORT_DESC -> bank.getTransactionsSorted(accountName, false);
                case ONLY_POSITIVE -> bank.getTransactionsByType(accountName, true);
                case ONLY_NEGATIVE -> bank.getTransactionsByType(accountName, false);
                default -> bank.getTransactions(accountName);
            };

            // WICHTIG: Prüfen ob Liste null ist
            if (txs == null) {
                txs = new ArrayList<>();
            }

            // 2) ListView setzen
            transactions.setAll(txs);

            // 3) Kontostand neu setzen
            double balance = bank.getAccountBalance(accountName);
            balanceLabel.setText(String.format("Kontostand: %.2f €", balance));

        } catch (Exception ex) {
            transactions.clear();
            showError("Fehler beim Anzeigen", ex.getMessage());
        }
    }

    @FXML
    private void handleDeleteTransaction() {
        Transaction selected = transactionsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Keine Transaktion ausgewählt", "Bitte zuerst eine Transaktion anklicken.");
            return;
        }

        boolean ok = askYesNo(
                selected.toString()
        );

        if (!ok) {
            return;
        }

        try {
            // Entfernen aus der Bank (inkl. Speichern/Festplatte laut Aufgabenblatt): contentReference[oaicite:10]{index=10}
            bank.removeTransaction(accountName, selected);
            // (falls deine Methode anders heißt: z.B. deleteTransaction(...), removeTransaction(...), etc.)

            // Danach View neu laden + Kontostand aktualisieren :contentReference[oaicite:11]{index=11}
            applyMode();

        } catch (Exception ex) {
            showError("Löschen fehlgeschlagen", ex.getMessage());
        }
    }

    private boolean askYesNo(String content) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Transaktion löschen");
        a.setHeaderText("Soll diese Transaktion wirklich gelöscht werden?");
        a.setContentText(content);

        Optional<ButtonType> result = a.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
