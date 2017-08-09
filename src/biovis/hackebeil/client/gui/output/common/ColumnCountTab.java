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
package biovis.hackebeil.client.gui.output.common;

import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;

public abstract class ColumnCountTab
    extends Tab
    implements ColumnCountInterface {

    protected ColumnCountController controller = null;

    protected String title;

    protected Scene scene;
    protected AnchorPane anchorPane;

    /**
     *
     * @param title
     */
    public ColumnCountTab(
        String title
    ) {
        this.title = title;
        setText(title);
        setClosable(false);

        controller = ColumnCountController.getInstance();
        controller.initView();
        anchorPane = controller.getAnchorPane();
        setContent(anchorPane);
        scene = new Scene(anchorPane);
    }

    /**
     *
     * @return
     */
    @Override
    public String getLabel() {
        return title;
    }

    /**
     *
     * @return
     */
    @Override
    public String getPostfix() {
        return "_" + title;
    }

    /**
     *
     * @param fileName
     */
    @Override
    public void saveChartToFile(
        String fileName
    ) {
        fileName += getPostfix();
        controller.saveChartToFile(fileName);
    }
}
