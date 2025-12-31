package UI.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.util.Optional;

// import deines Models:
import bank.PrivateBank;

public class MainView_Controller {

    // ---- UI Elemente aus Mainview.fxml ----
    @FXML
    private ListView<String> accountsListView;

    // ---- Daten ----
    private final ObservableList<String> accounts = FXCollections.observableArrayList();

    // ---- Referenzen, die wir übergeben bekommen ----
    private PrivateBank bank;
    private Stage stage;

    // Wird automatisch aufgerufen, sobald FXML geladen ist
    @FXML
    private void initialize() {
        // ListView bekommt eine ObservableList → Aktualisierung wird leicht
        accountsListView.setItems(accounts);
    }

    // Wird von FxApplication nach dem Laden aufgerufen
    public void init(PrivateBank bank, Stage stage) {
        this.bank = bank;
        this.stage = stage;
        refreshAccounts();
    }

    private void refreshAccounts() {
        // Accounts aus der Bank holen und in die ListView schreiben
        accounts.setAll(bank.getAllAccounts());
    }

    // ---------- Event: Button "Neuer Account" ----------
    @FXML
    private void handleCreateAccount() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Neuer Account");
        dialog.setHeaderText("Bitte Accountnamen eingeben:");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return; // User hat abgebrochen
        }

        String name = result.get().trim();
        if (name.isEmpty()) {
            showError("Eingabefehler", "Accountname darf nicht leer sein.");
            return;
        }

        try {
            // diese Methode hast du aus früheren Praktika
            bank.createAccount(name);
            refreshAccounts();
        } catch (Exception ex) {
            // User-Fehler/Probleme als Alert
            showError("Account konnte nicht angelegt werden", ex.getMessage());
        }
    }

    // ---------- Event: Kontextmenü "Löschen" ----------
    @FXML
    private void handleDeleteAccount() {
        String selected = accountsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Kein Account ausgewählt", "Bitte zuerst einen Account anklicken.");
            return;
        }

        boolean ok = askYesNo(
                "Account \"" + selected + "\" wirklich löschen?"
        );

        if (!ok) {
            return;
        }

        try {
            bank.deleteAccount(selected); // soll auch „von Festplatte“ löschen :contentReference[oaicite:9]{index=9}
            refreshAccounts();
        } catch (Exception ex) {
            showError("Löschen fehlgeschlagen", ex.getMessage());
        }
    }

    // ---------- Event: Kontextmenü "Auswählen" ----------
    @FXML
    private void handleSelectAccount() {
        String selected = accountsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Kein Account ausgewählt", "Bitte zuerst einen Account anklicken.");
            return;
        }

        try {
            // Account view laden (gleiches Fenster/Stage!)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AccountView.fxml"));
            Parent root = loader.load();

            AccountView_Controller controller = loader.getController();

            // Account „mitgeben“ (Datenaustausch zwischen Scenes)
            controller.init(bank, stage, selected);

            stage.setScene(new Scene(root));
        } catch (Exception ex) {
            showError("Wechsel zur Account view fehlgeschlagen", ex.getMessage());
        }
    }

    // ---------- Hilfsmethoden: Alerts ----------
    private void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    private boolean askYesNo(String header) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Account löschen");
        a.setHeaderText(header);
        a.setContentText("Diese Aktion löscht den Account auch von der Festplatte.");

        Optional<ButtonType> result = a.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
