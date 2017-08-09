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

import java.io.IOException;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import biovis.hackebeil.client.commander.ClientCommander;
import biovis.hackebeil.client.commander.LocalServer;
import biovis.hackebeil.client.HackebeilClient;
import biovis.hackebeil.client.gui.RootLayoutController;
import biovis.hackebeil.common.data.Messages;
import biovis.hackebeil.server.commander.HackebeilService;
import biovislib.utils.MD5Generator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Based on biovis.sierra.client.GUI.ServerDialog
 * @author nhinzmann
 *
 */
public class ConnectionManagerController {

    private static final Logger log = Logger.getLogger(ConnectionManagerController.class.toString());

    private Preferences prefs = Preferences.userNodeForPackage(HackebeilClient.class);
    private Preferences localServerPrefs = Preferences.userNodeForPackage(HackebeilService.class);

    private RootLayoutController rootLayoutController;
    private Stage connectionManager;
    private AnchorPane anchorPane;

    @FXML
    private TextField textFieldServer;
    @FXML
    private TextField textFieldServerPort;
    @FXML
    private TextField textFieldClientPort;
    @FXML
    private TextField textFieldPassword;
    @FXML
    private CheckBox checkBoxStartLocal;
    @FXML
    private Button connect;

    public ConnectionManagerController() {
    }

    public void setPane(AnchorPane rootPane) {
        this.anchorPane = rootPane;
    }

    public static ConnectionManagerController getInstance() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ConnectionManagerController.class.getResource("ConnectionManager.fxml"));
            AnchorPane anchorPane = (AnchorPane) loader.load();
            ConnectionManagerController controller = loader.<ConnectionManagerController>getController();
            controller.setPane(anchorPane);
            return controller;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void createServerDialog(
        RootLayoutController rootLayoutController
    ) {
        this.rootLayoutController = rootLayoutController;

        connectionManager = new Stage();
        connectionManager.initModality(Modality.APPLICATION_MODAL);
        connectionManager.setResizable(false);
        connectionManager.getIcons().add(new Image(RootLayoutController.class.getResourceAsStream("masakari.png")));

        Scene scene = new Scene(anchorPane);
        connectionManager.setScene(scene);
        connectionManager.setTitle("Connection Manager");
        connectionManager.show();

        Platform.runLater(() -> {
            connectionManager.toFront();
        });
    }

    public void initialize() {
        //System.out.println(textFieldHost);
        //System.out.println(prefs.get("server","localhost"));
        textFieldServer.setText(prefs.get("server", "localhost"));
        textFieldServerPort.setText(prefs.get("serverPort", "9753"));
        textFieldClientPort.setText(prefs.get("clientPort", "9754"));
        textFieldPassword.setText(prefs.get("password", "Swordfish"));
        checkBoxStartLocal.setSelected(prefs.getBoolean("localServer", true));
        this.testLocalServer();
    }

    @FXML
    private void handleConnect() {
        connectToServer();

        connectionManager = (Stage) connect.getScene().getWindow();
        connectionManager.close();
    }

    /*
     *
     */
    private void connectToServer() {
        String host = textFieldServer.getText();
        int serverPort = Integer.parseInt(textFieldServerPort.getText());
        int clientPort = Integer.parseInt(textFieldClientPort.getText());

        if (checkBoxStartLocal.isSelected()) {
            if (!LocalServer.isRunning()) {
                LocalServer.startLocalServer(true);
            }
            host = "localhost";
            serverPort = localServerPrefs.getInt("port", 9753);
            clientPort = serverPort + 1;
        }

        if (LocalServer.isRunning()
            || !checkBoxStartLocal.isSelected()) {
            ClientCommander commander = rootLayoutController.getClientCommander();
            commander.stopListener();
            //commander = new ClientCommander(rootLayoutController);
//			System.out.println();
//			System.out.println("Serverangaben:");
//			System.out.println(textFieldHost.getText());
//			System.out.println(Integer.valueOf(textFieldServerPort.getText()));
//			System.out.println(Integer.valueOf(textFieldClientPort.getText()));
//			System.out.println();

            boolean setServer = commander.setServer(host, serverPort, clientPort);
//			System.out.println(setServer);
            if (setServer) {
                //rootLayoutController.setClientCommander(commander);

                Object[] command = new Object[2];
                command[0] = Messages.SERVER_credentials;
                Object[] payload = new Object[2];
                String password = new MD5Generator().getMD5(textFieldPassword.getText());
                payload[0] = password;
//				payload[1] = commander.getHash();
                command[1] = payload;
                commander.sendCommand(command);

                prefs.put("server", textFieldServer.getText());
                prefs.put("serverPort", textFieldServerPort.getText());
                prefs.put("clientPort", textFieldClientPort.getText());
                prefs.put("password", textFieldPassword.getText());
                prefs.putBoolean("localServer", checkBoxStartLocal.isSelected());
            } else {
                //System.out.println("Connection to Server failed!");
                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error Dialog");
                    alert.setHeaderText("Error occurred");
                    alert.setContentText("Connection to server failed!");
                    alert.showAndWait();

                    ConnectionManagerController reconnect = ConnectionManagerController.getInstance();
                    reconnect.createServerDialog(rootLayoutController);
                });
            }
        } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText("Error occurred");
            alert.setContentText("Local server is already running!");
            alert.showAndWait();
            //System.out.println("Local server is already running!");
        }
    }

    public void testLocalServer() {
        if (checkBoxStartLocal.isSelected()) {
            textFieldServer.setDisable(true);
            textFieldServerPort.setDisable(true);
            textFieldClientPort.setDisable(true);
            textFieldPassword.setDisable(true);
        } else {
            textFieldServer.setDisable(false);
            textFieldServerPort.setDisable(false);
            textFieldClientPort.setDisable(false);
            textFieldPassword.setDisable(false);
        }
    }

    public void CloseConnectionManager() {
        connectionManager = (Stage) connect.getScene().getWindow();
        connectionManager.close();
    }
}
