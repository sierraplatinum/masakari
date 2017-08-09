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
package biovis.hackebeil.client;

import biovis.hackebeil.client.gui.RootLayoutController;
import java.io.IOException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * @author nhinzmann
 *
 */
public class HackebeilClient extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.getIcons().add(new Image(RootLayoutController.class.getResourceAsStream("masakari.png")));
        primaryStage.setTitle("Masakari");
        // Here init RootLayout
        RootLayoutController rootController = RootLayoutController.getInstance();
        rootController.setPrimaryStage(primaryStage);
        Scene scene = rootController.initRootLayout();
        primaryStage.setScene(scene);
        primaryStage.show();

        // Kill all stages
        primaryStage.setOnCloseRequest((event) -> System.exit(0));

        primaryStage.setMinWidth(820.0);
        primaryStage.setMinHeight(660.0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
