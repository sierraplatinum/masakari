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

import biovis.hackebeil.server.commander.HackebeilServer;
import biovislib.remoteControl.CommandListenerException;

/**
 * Based on biovis.sierra.client.Commander.LocalServer
 *
 * @author nhinzmann
 *
 */
public class LocalServer {

    private static Thread server = new Thread();

    /**
     *
     * @param isLocalServer
     */
    public static void startLocalServer(boolean isLocalServer) {

        server = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("starting local server");
                try {
                    HackebeilServer.main(new String[0], isLocalServer);
                } catch (CommandListenerException ex) {
                    System.err.println("Exception received! " + ex.toString());
                }
            }
        });

        server.start();
        try {
            server.join(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // System.err.println("This thread is running?" +isRunning());
    }

    /**
     *
     * @return
     */
    public static boolean isRunning() {
        return server.isAlive();
    }
}
