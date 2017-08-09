/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biovis.hackebeil.client.gui.progress;

import biovis.hackebeil.common.data.Motif;
import biovis.hackebeil.common.data.MotifBase;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.MouseButton;

/**
 *
 * @author zeckzer
 */
public class CheckBoxCellNormalized
    extends CheckBoxTableCell<Motif, Boolean> {

    public CheckBoxCellNormalized(
        TableView<Motif> tableView
    ) {
        super();
        this.setOnMouseClicked((event) -> {
            ObservableList<? extends MotifBase> items = tableView.getItems();
            if (items.size() > getIndex()) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    MotifBase element = items.get(getIndex());
                    if (element.isChanged()) {
                        element.changeIsNormalized();
                    }
                }
            }
        });
    }

    @Override
    public void updateSelected(boolean selected) {
        super.updateSelected(selected);
    }
}
