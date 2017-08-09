/**
 *  Copyright 2016 Alrik Hausdorf, Nicole Hinzmann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package biovis.hackebeil.client.gui.progress;

import java.util.logging.Logger;
import java.util.regex.Pattern;

import biovis.hackebeil.client.data.ClientConfiguration;
import biovis.hackebeil.client.gui.RootLayoutController;
import biovis.hackebeil.common.data.Motif;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 *
 * @author Alrik Hausdorf
 *
 */
public class AddMotifDialogController {

    private static final Logger log = Logger.getLogger(AddMotifDialogController.class.getName());

    private Stage stage;

    @FXML
    private TextField textFieldToAddMotif;
    @FXML
    private RadioButton rButtonNormalized;
    @FXML
    private RadioButton rButtonRaw;
    @FXML
    private Button btnAddMotif;
    @FXML
    private Button btnCancel;

    private ClientConfiguration clientConfiguration;

    public AddMotifDialogController() {
    }

    /**
     *
     * @param window
     * @param clientConfiguration
     */
    public static void createDialog(
        Window window,
        ClientConfiguration clientConfiguration
    ) {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(AddMotifDialogController.class.getResource("AddMotifDialog.fxml"));
            AnchorPane anchorAddMotifs = loader.load();

            // Setup stage
            Stage stage = new Stage();
            stage.setScene(new Scene(anchorAddMotifs));
            stage.setTitle("Add Motif");
            stage.getIcons().add(new Image(RootLayoutController.class.getResource("masakari.png").toExternalForm()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(window);

            // create AddMotifDialogController
            AddMotifDialogController addMotifDialogController = loader.<AddMotifDialogController>getController();
            addMotifDialogController.setCache(clientConfiguration);
            addMotifDialogController.setStage(stage);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isNormalized() {
        return rButtonNormalized.isSelected();
    }

    private boolean isMotifValid(String motif) {
        if (!motif.isEmpty()
            && Pattern.matches("[AGTCN]+", motif)) {
            return true;
        } else {
            return false;
        }
    }

    public void initialize() {
    }

    @FXML
    private void onInputChange() {
        String stringMotif = textFieldToAddMotif.getText().toUpperCase();
        if (isMotifValid(stringMotif)) {
            textFieldToAddMotif.getStyleClass().remove("validation-error");
        } else {
            textFieldToAddMotif.getStyleClass().add("validation-error");
        }
    }

    @FXML
    private void handleAddMotif() {
        String stringMotif = textFieldToAddMotif.getText().toUpperCase();
        if (isMotifValid(stringMotif)) {
            Motif motif = new Motif();
            motif.setMotif(stringMotif);
            motif.setIsNormalized(isNormalized());
            clientConfiguration.addMotif(motif);

            textFieldToAddMotif.clear();
            stage.close();
        }
    }

    @FXML
    private void handleCancel() {
        stage.close();
    }

    /**
     *
     * @param clientConfiguration
     */
    public void setCache(
        ClientConfiguration clientConfiguration
    ) {
        this.clientConfiguration = clientConfiguration;
    }

    /**
     *
     * @param stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
