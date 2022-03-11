package empfehlungssystem;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ein Listitem für einen Kurs
 *
 */
public class KursListViewCell extends ListCell<Kurs> {

    @FXML
    private Label kursTitel;

    private FXMLLoader mLLoader;

    @FXML
    private AnchorPane anchor;

    @Override
    protected void updateItem(Kurs kurs, boolean empty) {
        super.updateItem(kurs, empty);

        if (empty || kurs == null) {

        } else {
            if (mLLoader == null) {
                mLLoader = new FXMLLoader(getClass().getResource("testItem.fxml"));
                mLLoader.setController(this);

                try {
                    mLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            kursTitel.setText(kurs.getName());

            /* Öffnet die entsprechende Kursübersicht */
            anchor.setOnMouseClicked(new EventHandler<MouseEvent>() {

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
                        controller.setKursbeschreibung(kurs.getBeschreibung());
                        controller.setKurstitel(kurs.getName());
                        controller.setKursBild(kurs.getBild());
                        controller.setBtnName(kurs.getTyp());
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

            setText(null);
            setGraphic(anchor);

        }

    }

}
