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

import biovis.hackebeil.client.data.ClientConfiguration;
import biovis.hackebeil.client.gui.RootLayoutController;
import biovis.hackebeil.common.data.PositionWeightMatrix;
import biovislib.vfsjfilechooser.VFSJFileChooser;
import biovislib.vfsjfilechooser.filechooser.VFSFileFilter;
import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.vfs2.FileObject;

/**
 *
 * @author zeckzer, Alrik Hausdorf
 *
 */
public class AddPWMDialogController {

    private static final Logger log = Logger.getLogger(AddPWMDialogController.class.getName());

    @FXML
    private Button btnAddFile;
    @FXML
    private Label lAddPWM;
    @FXML
    private RadioButton rButtonMedian;
    @FXML
    private RadioButton rButtonMax;
    @FXML
    private RadioButton rButtonCutoff;
    @FXML
    private RadioButton rButtonNormalized;
    @FXML
    private RadioButton rButtonRaw;
    @FXML
    private Label lCuttoffValue;
    @FXML
    private TextField tfCutoffValue;
    @FXML
    private Button btnAddPWM;
    @FXML
    private Button btnCancel;

    private String filePathValue;
    private String dataSetNameValue;

    private ClientConfiguration clientConfiguration;
    private Stage stage;

    public AddPWMDialogController() {
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
            loader.setLocation(AddPWMDialogController.class.getResource("AddPWMDialog.fxml"));
            VBox vbox = loader.load();

            // Setup stage
            Stage stage = new Stage();
            stage.setScene(new Scene(vbox));
            stage.setTitle("Add PWM");
            stage.getIcons().add(new Image(RootLayoutController.class.getResource("masakari.png").toExternalForm()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(window);

            // create AddPWMDialogController
            AddPWMDialogController addPWMDialogController = loader.<AddPWMDialogController>getController();
            addPWMDialogController.setCache(clientConfiguration);
            addPWMDialogController.setStage(stage);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isNormalized() {
        return rButtonNormalized.isSelected();
    }

    public void initialize() {
    }

    @FXML
    private void selectMedian() {
        rButtonRaw.setSelected(true);
        rButtonRaw.setDisable(true);
    }

    @FXML
    private void selectMax() {
        rButtonRaw.setSelected(true);
        rButtonRaw.setDisable(true);
    }

    @FXML
    private void selectCutoff() {
        rButtonRaw.setDisable(false);
    }

    @FXML
    private void handleAddFile() {
        clientConfiguration.initFileChooser();
        VFSJFileChooser remoteFileChooser = clientConfiguration.getRemoteFileChooser();
        remoteFileChooser.setDialogTitle("Load PWM");
        remoteFileChooser.addChoosableFileFilter(new VFSFileFilter("Matrix Format (*.mat)", "mat"));
        remoteFileChooser.setMultiSelectionEnabled(false);
        VFSJFileChooser.RETURN_TYPE answer = remoteFileChooser.showOpenDialog(null);
        if (answer == VFSJFileChooser.RETURN_TYPE.APPROVE) {
//			log.info("ListSize before:" + cache.getAdditionalDataList().size());
            final FileObject aFileObject = remoteFileChooser.getSelectedFileObject();
            if (aFileObject != null) {
                Platform.runLater(() -> {
                    filePathValue = aFileObject.getName().getPath();
                    dataSetNameValue = aFileObject.getName().getBaseName();
                    lAddPWM.setText(dataSetNameValue);
                });
            }
//			log.info("ListSize after:" + cache.getAdditionalDataList().size());
        }
    }

    @FXML
    private void handleAddPWM() {
        PositionWeightMatrix pwm = new PositionWeightMatrix();
        pwm.setPWM(filePathValue, dataSetNameValue);
        if (rButtonMedian.isSelected()) {
            pwm.setComputationMethod(PositionWeightMatrix.COMPUTATION_METHOD_MEDIAN);
        } else if (rButtonMax.isSelected()) {
            pwm.setComputationMethod(PositionWeightMatrix.COMPUTATION_METHOD_MAX);
        } else {
            pwm.setComputationMethod(PositionWeightMatrix.COMPUTATION_METHOD_CUTOFF);
        }
        pwm.setIsNormalized(isNormalized());
        pwm.setCutoffValue(Double.parseDouble(tfCutoffValue.getText()));
        clientConfiguration.addPWM(pwm);

        lAddPWM.setText("");
        stage.close();
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

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
