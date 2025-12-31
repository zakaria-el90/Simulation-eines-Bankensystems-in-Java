package UI;

import UI.controller.MainView_Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import bank.PrivateBank;

//FxApplication ist der Einstiegspunkt der JavaFX-Anwendung.
//Sie verbindet GUI (FXML + Controller) mit der Geschäftslogik (PrivateBank) und startet die Anwendung in einem einzigen Fenster.
public class FxApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // FXML + Controller laden
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
        Parent root = loader.load();

        // Controller holen
        MainView_Controller controller = loader.getController();

        // Bank erstellen
        PrivateBank bank = new PrivateBank("MeineBank",0.5,0.1, "bank-data");

        // Controller initialisieren (Bank + Stage übergeben)
        controller.init(bank, stage);

        // Scene setzen (gleiches Fenster)
        stage.setScene(new Scene(root));
        stage.setTitle("Bank");
        stage.show();
    }
    // launch() kümmert sich um Threads & Lifecycle
    public static void main(String[] args) {
        launch(args);
    }

}
