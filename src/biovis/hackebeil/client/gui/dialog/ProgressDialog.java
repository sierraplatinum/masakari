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
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Alrik Hausdorf
 *
 */
public class ProgressDialog {

    private final Stage dialogStage;

    public ProgressDialog(String text) {
        dialogStage = new Stage();
        dialogStage.getIcons().add(new Image(RootLayoutController.class.getResource("masakari.png").toExternalForm()));
        dialogStage.initStyle(StageStyle.DECORATED);
        dialogStage.setResizable(false);
        dialogStage.initModality(Modality.NONE);

        final Label label = new Label();
        label.setText(text);

        final VBox vb = new VBox();
        vb.setSpacing(5);
        vb.setAlignment(Pos.CENTER);
        vb.getChildren().add(label);

        Label lb = new Label();
        lb.setText("Please wait.");
        vb.getChildren().add(lb);

        Scene scene = new Scene(vb);
        dialogStage.setScene(scene);
    }

    public void start() {
        dialogStage.show();
    }

    public void close() {
        dialogStage.hide();
        dialogStage.close();
    }
}
