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
package biovis.hackebeil.client.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

/**
 * @author Alrik Hausdorf, Dirk Zeckzer
 *
 */
public class ImageExport {

    private static final Logger log = Logger.getLogger(ImageExport.class.getName());

    /**
     *
     * @param pane
     * @param path
     */
    public static void exportPaneToPng(
        Pane pane,
        String path
    ) {
        if (path == null) {
            // Set extension filter
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("png files (*.png", "*.png");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setTitle("Save figure as png file");

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                savePaneToPng(pane, file);
            }
        } else {
            savePaneToPng(pane, new File(path));
        }

    }

    private static void savePaneToPng(
        Pane pane,
        File file
    ) {
        if (!file.getName().endsWith(".png")) {
            file = new File(file.getAbsoluteFile() + ".png");
        }
        WritableImage snapshot = pane.snapshot(new SnapshotParameters(), null);
        BufferedImage image = javafx.embed.swing.SwingFXUtils.fromFXImage(snapshot, null);
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
