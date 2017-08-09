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
package biovis.hackebeil.client.gui.dialog;

import biovis.hackebeil.client.gui.RootLayoutController;
import java.util.logging.Logger;

import biovis.hackebeil.common.data.DataFile;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 *
 * @author Alrik Hausdorf
 *
 */
public class AddAdditionalDataDialogController {

    private static final Logger log = Logger.getLogger(AddAdditionalDataDialogController.class.getName());

    @FXML
    private TextField tfName;
    @FXML
    private Label lbPath;
    @FXML
    private CheckBox cbScore;
    @FXML
    private Button btnAddMotif;
    @FXML
    private Button btnCancel;

    private Stage stage;

    private DataFile dataFile;

    public AddAdditionalDataDialogController() {
    }

    /**
     *
     * @param window
     * @param dataFile
     */
    public static void createDialog(
        Window window,
        DataFile dataFile
    ) {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(AddAdditionalDataDialogController.class.getResource("AddAdditionalDataDialog.fxml"));
            VBox root = loader.load();

            // Setup stage
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.getIcons().add(new Image(RootLayoutController.class.getResource("masakari.png").toExternalForm()));
            stage.setTitle("Edit additional data");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(window);

            // create AddAdditionalDataDialog
            AddAdditionalDataDialogController addAdditionalDataDialogController = loader.<AddAdditionalDataDialogController>getController();
            addAdditionalDataDialogController.setDataFile(dataFile);
            addAdditionalDataDialogController.setStage(stage);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialize() {
    }

    @FXML
    private void handleCancel() {
        stage.close();
    }

    @FXML
    private void handleSave() {
        this.dataFile.setDataSetName(tfName.getText());
        stage.close();
    }

    private void setStage(Stage stage) {
        this.stage = stage;
    }

    private void setDataFile(DataFile dataFile) {
        this.dataFile = dataFile;
        lbPath.setText(dataFile.getFilePath());
        tfName.setText(dataFile.getDataSetName());
        cbScore.setDisable(true);
        cbScore.setSelected(dataFile.getUseScore());
    }
}
