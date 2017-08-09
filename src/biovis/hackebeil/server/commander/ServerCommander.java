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
package biovis.hackebeil.server.commander;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import biovislib.remoteControl.AddressContainer;
import biovislib.remoteControl.Commander;
import biovislib.remoteControl.CommanderException;

/**
 * Based on biovis.sierra.server.Commander.PeakCommander
 *
 * @author nhinzmann
 *
 */
public class ServerCommander {

    private Logger log = Logger.getLogger("ServerCommander");

    private String serverHost;
    private int hostPort;
    private HashMap<String, Commander> nCommanders = new HashMap<>();
    private Commander commander = null;

    private int active = 0;
    private double oldProgress = 0.0;
    private int counter = 0;

    /**
     * Initialize commander.
     */
    private void initCommander(String hash) {
        AddressContainer host = new AddressContainer(serverHost, hostPort);
        try {
            commander = new Commander(host);
            nCommanders.put(hash, commander);
            active++;
        } catch (CommanderException cEx) {
            cEx.printStackTrace();
        }
    }

    /**
     * Set server parameters.
     *
     * @param url server url
     * @param port server port
     * @param hash server hash
     */
    public synchronized void setServer(String url, int port, String hash) {
        this.serverHost = url;
        this.hostPort = port;
        initCommander(hash);
    }

    /**
     * Send command.
     *
     * @param command command
     */
    public synchronized void sendCommand(Object command) {
        Object[] check = (Object[]) command;
        if (check[0].equals("setProgress")) {
            counter++;
            if ((counter % 1000) == 0
                || ((Double) check[1] - oldProgress) > 0.0009) {
                // Exporter.printHTML("hackebeil.htm", (Double) check[1]);
                oldProgress = (Double) check[1];
            }
        }

        Set<Entry<String, Commander>> entrySet = nCommanders.entrySet();
        ArrayList<Entry<String, Commander>> entryList = new ArrayList<>();
        entryList.addAll(entrySet);
        // log.info("entryList-size: "+Integer.toString(entryList.size()));
        for (int i = 0; i < entryList.size(); i++) {
            Commander com = entryList.get(i).getValue();
            try {
                com.executeCommand(command);
            } catch (CommanderException | IOException e) {
                log.log(Level.WARNING, "Lost connection to {0}", com.getHost());
                nCommanders.remove(entryList.get(i).getKey());
                if (nCommanders.isEmpty()) {
                    active--;
                }
            }
        }
    }

    /**
     * Send command to host-
     *
     * @param command command
     * @param host host
     */
    public synchronized void sendCommand(Object command, String host) {
        try {
            nCommanders.get(host).executeCommand(command);
        } catch (Exception e) {
            // System.err.println("+++++++++++++++++++++++++++++++++++");
            // logger.log(Level.WARNING, "Lost connection to {0}", host);
            // logger.log(Level.WARNING, "Clients {0}",clients.size());
            // logger.log(Level.WARNING, "commanders {0}",commanders.size());
            // System.err.println("+++++++++++++++++++++++++++++++++++");
            nCommanders.remove(host);
            if (nCommanders.isEmpty()) {
                active--;
            }
        }
    }

    /**
     * Is server active?
     *
     * @return true iff server is active
     */
    public int isActive() {
        return active;
    }

    /**
     * Set server state to active.
     */
    public void setActive() {
        Object[] c = {"ECHO", "ECHO"};
        sendCommand(c);
    }

    /**
     * Reset progress counter.
     */
    public void resetProgress() {
        oldProgress = 0.0;
    }
}
