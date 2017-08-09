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
package biovis.hackebeil.client.commander;

import java.io.IOException;
import java.util.logging.Logger;

import biovis.hackebeil.client.gui.RootLayoutController;
import biovis.hackebeil.client.gui.StatusBarController;
import biovis.hackebeil.common.data.Messages;
import biovislib.remoteControl.AddressContainer;
import biovislib.remoteControl.CommandListener;
import biovislib.remoteControl.CommandListenerException;
import biovislib.remoteControl.Commander;
import biovislib.remoteControl.CommanderException;
import java.util.logging.Level;

/**
 * Based on biovis.sierra.client.Commander.PeakCommander
 * @author nhinzmann
 *
 */
public class ClientCommander {

    private static final Logger log = Logger.getLogger(ClientCommander.class.getName());

    private Commander commander = null;
    private String clientHost;
    private int clientPort;
    private boolean active = false;
    private CommandListener commandListener;
    private ClientDispatcher clientDispatcher;
    private int serverPort;
    private RootLayoutController rootLayoutController;

    //private  String hash;
    /**
     *
     * @param controller
     */
    public ClientCommander(RootLayoutController controller) {
        this.rootLayoutController = controller;
        //hash = new RandomDNA().generateStrand();
//		System.out.println(this);
    }

    /**
     *
     */
    private boolean initCommander() {
        System.out.println("Starte neuen Commander");
        AddressContainer host = new AddressContainer(clientHost, serverPort);

        try {
            commander = new Commander(host);
            //active = true;
            setActive(true);
            rootLayoutController.getStatusBarController().setConnected(clientHost, String.valueOf(serverPort));
            return true;
        } catch (CommanderException cEx) {
            commander = null;
            System.out.println("No server connection was initiated");
        }
        return false;

    }

    private void initDispatcher() {
        clientDispatcher = new ClientDispatcher(rootLayoutController);
        try {
            commandListener = new CommandListener(clientPort, clientDispatcher);
            commandListener.start();
        } catch (CommandListenerException cle) {
            System.out.println("Error while creating commandListener -> terminating.");
            if (clientHost.equals("localhost")) {
                System.out.println("Set new clientPort");
                clientPort++;
                initDispatcher();
            }
            //				System.exit(0);
        }
        System.out.println("Command listener started");
        System.out.flush();
    }

    /**
     *
     * @param url
     * @param serverPort
     * @param clientPort
     * @return
     */
    public boolean setServer(String url, int serverPort, int clientPort) {
        //System.err.println("Set Server");
        System.out.println("Set Server");
        this.clientHost = url;
        this.serverPort = serverPort;
        this.clientPort = clientPort;
        if (initCommander()) {
            initDispatcher();
            Object[] command = new Object[2];
            command[0] = Messages.SERVER_setClient;
            Object[] payload = new Object[2];
            payload[0] = clientPort;
            command[1] = payload;

            sendCommand(command);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return active
     */
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
//		System.out.println("clientCommander.setActive " + active);
        this.active = active;
    }

    /**
     * @param command
     */
    public void sendCommand(Object command) {
        try {
            if (commander == null) {
//				System.out.println("Init Commander");
                initCommander();
            }
            Object[] c = (Object[]) command;
            log.log(Level.INFO, "command {0}", c[0]);
            this.displayState(c[0].toString());
//			System.out.println(commander);
            commander.executeCommand(command);

        } catch (CommanderException | IOException e) {
            System.out.println("Exception while sending command: " + e);
            active = false;
            e.printStackTrace();
        }
    }

    private void displayState(String command) {
        StatusBarController controller = rootLayoutController.getStatusBarController();
        switch (command) {
            case Messages.SERVER_loadIndex:
                controller.updateProgress("Index load started", false);
                break;
            case Messages.SERVER_startCorrelation:
                controller.updateProgress("Correlation started", false);
                break;
            case Messages.SERVER_startSegmentation:
                controller.updateProgress("Segmentation started", false);
                break;
            case Messages.SERVER_addAdditionalData:
                controller.updateProgress("Additional data started", false);
                break;
            case Messages.SERVER_addMotif:
                controller.updateProgress("Motif search started", false);
                break;
            case Messages.SERVER_addPWM:
                controller.updateProgress("PWM computation started", false);
                break;
            case Messages.SERVER_credentials:
                controller.updateProgress("Connected and ready", true);
                break;
            default:
                controller.updateProgress(command, true);
        }
    }

    public void stopListener() {
        if (commandListener != null) {
            commandListener.closeAll();
            commandListener = null;
        }
        if (commander != null) {
            commander.closeAll();
            commander = null;
        }
    }

    /**
     * @param controller
     */
    public void setRootLayoutController(RootLayoutController controller) {
        this.rootLayoutController = controller;
    }

//	public String getHash()
//	{
//		return hash;
//	}
    public void ack() {
        Object[] c = {Messages.QUIT, "ACK"};
        sendCommand(c);
        commander = null;
        System.out.println("closed Commander");
    }
}
