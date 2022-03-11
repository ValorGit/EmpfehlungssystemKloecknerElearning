package empfehlungssystem;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

/** Eine Kursübersicht mit den relevanten Informationen */
public class KursUebersichtController {

    @FXML
    private Label kursbeschreibung;

    @FXML
    private ImageView kursbild;

    @FXML
    private Label kurstitel;

    @FXML
    private Label kursAnfordernLabel;

    @FXML
    private Button kursButton;

    private Stage stage;

 /**
     * Weist dem Controller die Stage aus der Mainapp zu
     *
     * @param stage legt die Stage fest
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /* Leitet den Lernenden zum Kurs weiter, falls dieser Teil des Angebots in Talentsoft ist */
    public void kursButtonPressed() {
        String btnText = "Kurs starten";

        if(kursButton.getText().equals(btnText)){

        try {
            FXMLLoader loader = new FXMLLoader();
            loader
                    .setLocation(App.class
                            .getResource("Kursseite.fxml"));
            Parent kursSeiteView = (Parent) loader.load();
            Stage kursSeiteStage = new Stage();
            KursseiteController controller = loader.getController();
            controller.setStage(kursSeiteStage);
            controller.setKurstitel(kurstitel.getText());
            kursSeiteStage.setTitle("Kurs");
            kursSeiteStage.initModality(Modality.WINDOW_MODAL);

            Scene scene = new Scene(kursSeiteView);
            kursSeiteStage.setScene(scene);
            kursSeiteStage.showAndWait();

        } catch (IOException ex) {
            Logger.getLogger(App.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    }

    public void setKursbeschreibung(String beschreibung) {
        kursbeschreibung.setText(beschreibung);
    }

    public void setKurstitel(String name) {
        kurstitel.setText(name);
    }

    public void setKursBild(String bildUrl) {
        if (bildUrl != "null") {
            Image image = new Image(bildUrl);
            kursbild.setImage(image);
        }
    }

    /* Legt das ButtonLabel abhängig vom Kurstyp fest */
    public void setBtnName(String typ) {
        String kurstyp = "content";
        System.out.println(typ);
        if (typ.equals(kurstyp)) {
           System.out.println("true");
           kursButton.setText("Kurs anfordern");
           kursAnfordernLabel.setText("Dieser Kurs ist derzeit nicht Teil des Weiterbildungskatalogs");
        } else {
            System.out.println("false");
        }

    }

}
