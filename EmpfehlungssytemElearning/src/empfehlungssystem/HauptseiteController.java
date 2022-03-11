package empfehlungssystem;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;

/**
 * Zeigt das Hauptfenster mit Auswahlmenü an.
 *
 * 
 */
public class HauptseiteController {

    private Stage stage;

    private App mainApp;

    /* Öffne das Fenster NeuerUser */
    @FXML
    private void neuerUserMenuItemAction(ActionEvent event) {

        mainApp.zeigeNeuerUserDialog();
    }

    /* Öffne das Fenster EmpfehlungUebersicht */
    @FXML
    private void empfehlungUebersichtMenuItemAction(ActionEvent event) {
        mainApp.zeigeEmpfehlungUebersichtDialog();
    }

    /* Öffne das Fenster Profil */
    @FXML
    private void profiltMenuItemAction(ActionEvent event) {
        mainApp.zeigeProfilDialog();
    }

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

}