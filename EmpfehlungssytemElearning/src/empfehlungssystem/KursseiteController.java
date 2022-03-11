package empfehlungssystem;
import java.util.prefs.Preferences;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;


/** Platzhalter für die Kursseite in Talentsoft */
public class KursseiteController {

    @FXML
    private Button kursabschliessenBtn;

    private String currentuser = "test";

    private Stage stage;

     @FXML
    private Label kurstitelLabel;

    /* Setzt den Kurs auf abgeschlossen */
    public void kursabschliessen(){
        currentuser = Preferences.userRoot().get("username", "User");

           try (RecommenderEngine re = new RecommenderEngine()) {
           re.kursAbgeschlossen(currentuser, kurstitelLabel.getText());
           stage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void setKurstitel(String name){
        kurstitelLabel.setText(name);
    }

    /* Wenn der Kurs den Typ "content" besitzt, setze das Buttonlabel auf "Kurs anfordern" */
    public void setBtnName(String name){

        if(name == "content"){
            kursabschliessenBtn.setText("Kurs anfordern");
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

}
