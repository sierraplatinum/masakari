/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biovis.hackebeil.client.gui.progress;

import biovis.hackebeil.client.data.ClientConfiguration;
import biovis.hackebeil.common.data.Motif;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 *
 * @author zeckzer
 */
public class ButtonCellDelete
    extends TableCell<Motif, Boolean> {

    final HBox btngrActions = new HBox();
    final Image imageDelete = new Image(
        ProgressAdditionalMeasurementsController.class.getResourceAsStream("img/fa-trash.png"));

    final Button btnDelete = new Button("", new ImageView(imageDelete));

    ButtonCellDelete(
        ClientConfiguration clientConfiguration
    ) {
        btnDelete.setOnAction((ActionEvent t) -> {
            Motif motifToRemove = (Motif) getTableRow().getItem();

            if (motifToRemove != null) {
                clientConfiguration.removeMotifFromList(motifToRemove);
            }
        });
        btngrActions.getChildren().addAll(btnDelete);
    }

    @Override
    protected void updateItem(Boolean t, boolean empty) {
        super.updateItem(t, empty);
        if (!empty) {
            setGraphic(btngrActions);
        } else {
            setGraphic(null);
        }
    }
}
