package empfehlungssystem;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.prefs.Preferences;

public class StartseiteController {

    @FXML
    private TextField benutzerTf;

    @FXML
    private Label errorLabel;

    @FXML
    private Button weiterBtn;

    private App mainApp;

    private Stage stage;

    /**
     * Ändere MainApp auf aktuelle Seite
     */
    public void setMainApp(App mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Schließt die aktuelle Seite
     */
    @FXML
    public void handleCloseButtonAction() {

        stage.close();

    }

    /**
     * Weist dem Controller die Stage aus der Mainapp zu
     *
     * @param stage legt die Stage fest
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /*
     * Prüft, ob der eingegebene User in der Datenbank angelegt wurde. Falls
     * erfolgreich, weiterleiten zur Hauptseite
     */
    public void weiterButtonPushed() {
        String lerner = benutzerTf.getText();

        try (RecommenderEngine neuEngine = new RecommenderEngine()) {

            Preferences pref = Preferences.userRoot();
            String userName = neuEngine.pruefeEmail(lerner.toLowerCase());
            pref.put("username", userName);
            mainApp.zeigeHauptseite();

        } catch (Exception e) {
            e.printStackTrace();
            benutzerTf.clear();
            errorLabel.setVisible(true);
        }
    }

    /* Öffnet Seite "Neuer Lerner" */
    public void neuerUserButtonPushed() {
        mainApp.zeigeNeuerUserDialog();
    }
}
