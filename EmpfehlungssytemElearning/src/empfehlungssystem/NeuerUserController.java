package empfehlungssystem;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;

/** Bildschirm zum anlegen eines neuen Benutzers */
public class NeuerUserController implements Initializable {

    private Stage stage;
    private App mainApp;

    @FXML
    private Button abbrechenBtn;

    @FXML
    private ListView<String> ausgewaehlteListView;

    @FXML
    private TextField emailTf;

    @FXML
    private TextField nameTf;

    @FXML
    private ChoiceBox<String> positionCb;

    @FXML
    private ListView<String> skillListView;

    @FXML
    private Button speichernBtn;

    @FXML
    private Label skillsError;

    @FXML
    private Label positionError;

    @FXML
    private Label nameError;

    @FXML
    private Label emailError;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

        ObservableList<String> skillObservableList = FXCollections.observableArrayList();
        ObservableList<String> ausgewaehlteObservableList = FXCollections.observableArrayList();
        ObservableList<String> positionenObservableList = FXCollections.observableArrayList();

        try (RecommenderEngine re = new RecommenderEngine()) {
            List<String> skills = re.gibAlleSkills();
            List<String> positionen = re.gibAllePositionen();

            // Node[] nodes = new Node[kurs.size()];
            for (int i = 0; i < skills.size(); i++) {
                skillObservableList.add(skills.get(i));

            }
            for (int i = 0; i < positionen.size(); i++) {
                positionenObservableList.add(positionen.get(i));

            }

            skillListView.setItems(skillObservableList);
            positionCb.setItems(positionenObservableList);

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

    /* Falls Daten vollständig wird ein neuer Benutzer angelegt */
    public void speichern(MouseEvent event) {
        if (this.isValid()) {
            try (RecommenderEngine re = new RecommenderEngine()) {
                re.neuerUser(nameTf.getText(), emailTf.getText().toLowerCase(), positionCb.getValue(),
                        ausgewaehlteListView.getItems());

            } catch (Exception e) {
                e.printStackTrace();
            }
            stage.close();
        }
    }

    /**
     * Ändere MainApp auf aktuelle Seite
     */
    public void setMainApp(App mainApp) {
        this.mainApp = mainApp;
    }

    /* Prüft, ob die notwendigen Daten eingegeben wurden */
    public boolean isValid() {

        boolean valid = true;

        if (nameTf.getText().isEmpty()) {
            nameError.setVisible(true);
            valid = false;
        } else {
            nameError.setVisible(false);
        }
        if (emailTf.getText().isEmpty()) {
            emailError.setVisible(true);
            valid = false;
        } else {
            emailError.setVisible(false);
        }
        if (positionCb.getValue() == null) {
            positionError.setVisible(true);
            valid = false;
        } else {
            positionError.setVisible(false);
        }
        if (ausgewaehlteListView.getItems().size() > 5) {
            skillsError.setVisible(true);
            valid = false;
        } else {
            skillsError.setVisible(false);
        }
        return valid;
    }

    /**
     * Schließt die aktuelle Seite
     */
    @FXML
    public void handleCloseButtonAction() {

        stage.close();

    }

}