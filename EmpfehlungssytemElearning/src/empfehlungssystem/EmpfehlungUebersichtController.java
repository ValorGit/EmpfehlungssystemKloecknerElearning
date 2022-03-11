package empfehlungssystem;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.neo4j.driver.Record;

/** Zeigt die Empfehlungen für den User an */

public class EmpfehlungUebersichtController implements Initializable {

    private Stage stage;
    private App mainApp;

    private String currentuser = "test";

    @FXML
    private Label aktuellePositionLabel;

    @FXML
    private Label lernerLabel;

    @FXML
    private ListView<Kurs> listViewEmpfehlungAktuell;

    @FXML
    private ListView<Kurs> listViewEmpfehlungSkill;

    @FXML
    private AnchorPane topKursPane;

    @FXML
    private Label jobfamilieLabel;

    @FXML
    private Label topKursTitel;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

        currentuser = Preferences.userRoot().get("username", "User");
        lernerLabel.setText(currentuser);
        ObservableList<Kurs> kursPositionObservableList = FXCollections.observableArrayList();
        ObservableList<Kurs> kursSkillObservableList = FXCollections.observableArrayList();

        try (RecommenderEngine re = new RecommenderEngine()) {
            re.findeLerner(currentuser);
            aktuellePositionLabel.setText(re.gibaktuellePosition(currentuser));
            List<Record> kursePosition = re.empfehleKursPosition(currentuser);
            List<Record> kurseSkill = re.empfehleKursSkills(currentuser);
            jobfamilieLabel.setText(re.gibJobfamilie(currentuser));
            Kurs topKurs = re.gibKursDesMonats(currentuser);

            // Node[] nodes = new Node[kurs.size()];
            for (int i = 0; i < kursePosition.size(); i++) {
                Kurs kurs = new Kurs(kursePosition.get(i).values().get(0).get("name").asString(),
                        kursePosition.get(i).values().get(0).get("beschreibung").asString(),
                        kursePosition.get(i).values().get(0).get("bildUrl").asString(), 0,
                        kursePosition.get(i).values().get(0).get("typ").asString());
                kursPositionObservableList.add(kurs);

            }

            for (int i = 0; i < kurseSkill.size(); i++) {
                Kurs kurs = new Kurs(kurseSkill.get(i).values().get(0).get("name").asString(),
                        kurseSkill.get(i).values().get(0).get("beschreibung").asString(),
                        kurseSkill.get(i).values().get(0).get("bildUrl").asString(), 0,
                        kurseSkill.get(i).values().get(0).get("typ").asString());
                kursSkillObservableList.add(kurs);

            }

            listViewEmpfehlungAktuell.setItems(kursPositionObservableList);
            listViewEmpfehlungAktuell.setCellFactory(kursListView -> new KursListViewCell());
            listViewEmpfehlungSkill.setItems(kursSkillObservableList);
            listViewEmpfehlungSkill.setCellFactory(kursListView -> new KursListViewCell());
            topKursTitel.setText(topKurs.getName());

            topKursPane.setOnMouseClicked(new EventHandler<MouseEvent>() {

                /* Öffnet die Kursübersicht des augewählten Kurses */
                @Override
                public void handle(MouseEvent arg0) {
                    try {
                        FXMLLoader loader = new FXMLLoader();
                        loader
                                .setLocation(App.class
                                        .getResource("KursUebersicht.fxml"));
                        Parent kursUebersichtView = (Parent) loader.load();
                        Stage kursUebersichtStage = new Stage();
                        KursUebersichtController controller = loader.getController();
                        controller.setStage(kursUebersichtStage);
                        controller.setKursbeschreibung(topKurs.getBeschreibung());
                        controller.setKurstitel(topKurs.getName());
                        controller.setKursBild(topKurs.getBild());
                        controller.setBtnName(topKurs.getTyp());
                        kursUebersichtStage.setTitle("Kurs");
                        kursUebersichtStage.initModality(Modality.WINDOW_MODAL);

                        Scene scene = new Scene(kursUebersichtView);
                        kursUebersichtStage.setScene(scene);
                        kursUebersichtStage.showAndWait();

                    } catch (IOException ex) {
                        Logger.getLogger(App.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }

                }

            });

        } catch (Exception e) {
            e.printStackTrace();
        }

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
     * Schließt die aktuelle Seite
     */
    @FXML
    public void handleCloseButtonAction() {

        stage.close();

    }

    /**
     * Ändere MainApp auf aktuelle Seite
     */
    public void setMainApp(App mainApp) {
        this.mainApp = mainApp;
    }

}
