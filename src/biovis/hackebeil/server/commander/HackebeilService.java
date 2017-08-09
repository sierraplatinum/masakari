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

import java.util.logging.Logger;
import java.util.prefs.Preferences;

import biovis.hackebeil.server.data.ServerCache;
import biovislib.remoteControl.CommandListener;
import biovislib.remoteControl.CommandListenerException;
import java.util.logging.Level;

/**
 * Based on biovis.sierra.server.Commander.SierraService
 *
 * @author nhinzmann
 *
 */
public class HackebeilService {

    Logger log = Logger.getLogger(HackebeilService.class.toString());

    private ServerMapper serverMapper;
    private CommandListener commandListener;
    private ServerDispatcher hackebeilDispatcher;
    private ServerCache cache;
    private int port;
    private boolean isLocalServer;
    Preferences localServerPrefs = Preferences.userNodeForPackage(HackebeilService.class);

    public HackebeilService() {
        this(false);
    }

    public HackebeilService(boolean isLocalServer) {
        this.isLocalServer = isLocalServer;
    }

    public void initServer(String[] args)
        throws CommandListenerException {
        serverMapper = new ServerMapper();
        cache = new ServerCache();
        hackebeilDispatcher = new ServerDispatcher(serverMapper, cache);

        Args argsParser = new Args();
        argsParser.parse(args);
        // TODO: set Server

        String errors = argsParser.getErrors();
        if (errors.contains("ERROR")) {
            log.severe(errors);
            log.severe(argsParser.getHelpMessage());
            return;
        }

        serverMapper.setIOThreads(argsParser.getIOThreads());
        if (!argsParser.getServerConfig().equals("")) {
            // TODO Load ServerConfig
        }

        port = argsParser.getPort();
        startListener();
    }

    /**
     * @throws CommandListenerException
     *
     */
    private void startListener()
        throws CommandListenerException {
        while (true) {
            try {
                log.log(Level.INFO, "try {0} local? {1}", new Object[]{port, isLocalServer});
                commandListener = new CommandListener(port, hackebeilDispatcher);
                commandListener.start();
            } catch (CommandListenerException cle) {
                log.severe("Error while creating commandListener"); //$NON-NLS-1$
                // System.exit(0);
                if (isLocalServer) {
                    port++;
                    continue;
                }
                throw new CommandListenerException("Error while creating commandListener");
            }

            localServerPrefs.putInt("port", port);
            log.log(Level.INFO, "Server Command listener started at port: {0}", port); //$NON-NLS-1$
            try {
                commandListener.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
