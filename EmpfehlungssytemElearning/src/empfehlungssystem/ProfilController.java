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

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/** Zeigt das Profil des aktuellen Benutzers */
public class ProfilController implements Initializable {

    private Stage stage;
    private App mainApp;

    private String currentuser = "test";

    @FXML
    private Button abbrechenBtn;

    @FXML
    private ListView<String> ausgewaehlteListView;

    @FXML
    private Label lernerLabel;

    @FXML
    private Label positionLabel;

    @FXML
    private ListView<String> skillListView;

    @FXML
    private Label skillsError;

    @FXML
    private Button speichernBtn;

    /**
     * Schließt die aktuelle Seite
     */
    @FXML
    public void handleCloseButtonAction() {

        stage.close();

    }

    /* Falls notwendige Daten eingegeben, werden die Skills aktualisiert */
    @FXML
    void speichern(MouseEvent event) {
        if (this.isValid()) {
            try (RecommenderEngine re = new RecommenderEngine()) {
                re.userSkillZuweisung(currentuser, ausgewaehlteListView.getItems());
                stage.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /* Prüft, ob die notwendigen Daten eingegeben worden sind */
    boolean isValid() {
        boolean valid = true;
        if (ausgewaehlteListView.getItems().size() > 5) {
            skillsError.setVisible(true);
            valid = false;
        } else {
            skillsError.setVisible(false);
        }
        return valid;
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

        currentuser = Preferences.userRoot().get("username", "User");
        lernerLabel.setText(currentuser);
        ObservableList<String> skillObservableList = FXCollections.observableArrayList();
        ObservableList<String> ausgewaehlteObservableList = FXCollections.observableArrayList();

        try (RecommenderEngine re = new RecommenderEngine()) {
            List<String> skills = re.gibAlleSkills();
            List<String> aktuelleSkills = re.gibLernerSkills(currentuser);
            re.findeLerner(currentuser);
            positionLabel.setText(re.gibaktuellePosition(currentuser));

            for (int i = 0; i < skills.size(); i++) {
                skillObservableList.add(skills.get(i));

            }

            for (int i = 0; i < aktuelleSkills.size(); i++) {
                ausgewaehlteObservableList.add(aktuelleSkills.get(i));

            }

            skillListView.setItems(skillObservableList);
            ausgewaehlteListView.setItems(ausgewaehlteObservableList);

        } catch (Exception e) {
            e.printStackTrace();
        }

        skillListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            /* Fügt das angeklickte Item der ausgewaehlteListView hinzu */
            @Override
            public void handle(MouseEvent event) {
                if (!ausgewaehlteObservableList.contains(skillListView.getSelectionModel().getSelectedItem())) {
                    ausgewaehlteObservableList.add(skillListView.getSelectionModel().getSelectedItem());
                    ausgewaehlteListView.setItems(ausgewaehlteObservableList);
                }
            }
        });
        ausgewaehlteListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            /* Löscht das Item aus der ausgewaehltenListView */
            @Override
            public void handle(MouseEvent event) {
                ausgewaehlteObservableList.remove(ausgewaehlteListView.getSelectionModel().getSelectedItem());
            }
        });
    }

    /**
     * Weist dem Controller die Stage aus der Mainapp zu
     *
     * @param stage legt die Stage fest
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Ändere MainApp auf aktuelle Seite
     */
    public void setMainApp(App mainApp) {
        this.mainApp = mainApp;
    }

}