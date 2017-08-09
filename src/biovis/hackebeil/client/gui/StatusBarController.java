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
package biovis.hackebeil.client.gui;

import biovis.hackebeil.client.HackebeilClient;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import biovis.hackebeil.client.commander.ClientCommander;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * @author Alrik Hausdorf
 *
 */
public class StatusBarController {

    private final static double MB = 1024 * 1024.;

    Preferences prefs = Preferences.userNodeForPackage(HackebeilClient.class);

    private static final Logger log = Logger.getLogger(StatusBarController.class.getName());

    private Boolean isInitialized = false;
    private ClientCommander clientCommander;

    @FXML
    private Label labelConnectionState;
    @FXML
    private ProgressIndicator serverState;
    @FXML
    private Label done;
    @FXML
    private Label lastEvent;
    @FXML
    private Label ramUsageState;

    private BorderPane statusBar;

    private String serverUrl = null;
    private String serverPort = null;

    public StatusBarController() {
    }

    public void setPane(BorderPane pane) {
        this.statusBar = pane;
    }

    public static StatusBarController getInstance() {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(StatusBarController.class.getResource("StatusBar.fxml"));
            BorderPane statusBar = (BorderPane) loader.load();
            StatusBarController statusBarController = loader.<StatusBarController>getController();
            statusBarController.setPane(statusBar);
            return statusBarController;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public BorderPane initStatusBar(ClientCommander commander) {
        clientCommander = commander;
        testForReady();

        return statusBar;
    }

    public void initialize() {
        isInitialized = true;
        testForReady();
    }

    public void setConnected(String server, String port) {
        this.serverUrl = server;
        this.serverPort = port;
        this.updateServerState();
    }

    public void updateServerState() {
        // log.info("active:"+this.clientCommander.isActive());
        if (this.clientCommander.isActive()) {
            if (this.serverUrl == null && this.serverPort == null) {
                this.serverUrl = prefs.get("server", "localhost");
                this.serverPort = prefs.get("serverPort", "9753");
            }
            this.labelConnectionState.setText("connected to " + this.serverUrl + ":" + this.serverPort);
            this.labelConnectionState.setTextFill(Color.GREEN);
        } else {
            this.labelConnectionState.setText("not connected ");
            this.labelConnectionState.setTextFill(Color.RED);
        }
    }

    private void testForReady() {
        if (clientCommander != null
            && isInitialized) {
            this.initRamTimer();
        }
    }

    private void initRamTimer() {
        Timeline timer = new Timeline(
            new KeyFrame(Duration.millis(1000),
                         (ActionEvent event) -> {
                             recalculateRamUsage();
                         }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    @FXML
    private void recalculateRamUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalRam = (long) Math.round((runtime.totalMemory() - runtime.freeMemory()) / MB);
        ramUsageState.setText(String.format("%,d", totalRam) + " MB");
    }

    /**
     *
     * @param scommand
     * @param isDone
     */
    public void updateProgress(
        String scommand,
        boolean isDone
    ) {
        Platform.runLater(() -> {
            if (!isDone) {
                serverState.setProgress(-1);
                serverState.setVisible(true);
                done.setVisible(false);
            } else {
                serverState.setVisible(false);
                done.setText("\u2713");
                done.setVisible(true);
            }
            lastEvent.setText(scommand);
        });
    }
}
