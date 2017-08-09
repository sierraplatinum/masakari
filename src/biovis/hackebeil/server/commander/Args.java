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

/**
 * @author nhinzmann
 *
 */
public class Args {

    private final static String HELP_MESSAGE = "java -jar hackebeilServer.jar "
                                               + "\n"
                                               + "options:\n"
                                               + "\t-port  port of the server to listen for new connections [default: 9753]\n"
                                               //			+ "\t-state  load a precalculated state for further calculations\n"
                                               + "\t-serverConfig  load a config file for the server parameters\n"
                                               + "\t-batch terminate the program after the calculation\n"
                                               + "\t-IOThreads the amount threads that is used during the IO intensive calculations [default: 4]\n" //			+ "\t-clientConfig  load a GUI config file for the server and start the calculation without a client\n"
        //			+ "\t-chunkSizeWindows  chunk size for the calculation of the windows [default: 10000]\n"
        //			+ "\t-chunkSizeQuality  chunk size for the calculation of the quality [default: 1000]\n"
        //			+ "\t-qualitySmart change the algoritm for the peak quality from speed optimized to space optimized[default: speed optimized]\n"
        ;

    private String errors;
    private String serverConfig;
    private Integer port;
    private int ioThreads;

    /**
     * Constructor.
     */
    public Args() {
        errors = "";
        serverConfig = "";
        port = 9753;
        ioThreads = 4;
    }

    /** Parse command line arguments
     *
     * @param args  command line arguments
     */
    public void parse(String[] args) {
        //		if(args.length == 0){
        //			errors += "[ERROR] No Arguments given: required arguemnts are -d, -m, and -p";
        //		}
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-port":
                    i++;
                    if (i < args.length) {
                        port = Integer.parseInt(args[i]);
                    } else {
                        errors += "[ERROR] no argument given for option -p\n";
                    }
                    break;
                case "-serverConfig":
                    i++;
                    if (i < args.length) {
                        serverConfig = args[i];
                    } else {
                        errors += "[ERROR] no argument given for option -l\n";
                    }
                    break;
                case "-IOThreads":
                    i++;
                    if (i < args.length) {
                        ioThreads = Integer.parseInt(args[i]);
                    } else {
                        errors += "[ERROR] no argument given for option -S\n";
                    }
                    break;
                default:
                    errors += "[ERROR] un-recognized option " + args[i] + "\n";
            }
        }
    }

    public String getHelpMessage() {
        return HELP_MESSAGE;
    }

    public Integer getPort() {
        return port;
    }

    public String getServerConfig() {
        return serverConfig;
    }

    public String getErrors() {
        return errors;
    }

    public int getIOThreads() {
        return ioThreads;
    }

}
