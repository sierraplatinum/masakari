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
 * Based on biovis.sierra.server.Commander.ServerMapper
 * @author nhinzmann
 *
 */
public class ServerMapper {

    private String password = "";
    private int IOThreads = 4;
    private boolean locked = false;
    //private DataMapper mapper = new DataMapper();

    public String getPassword() {
        return password;
    }

    public void setPassword(String passwd) {
        this.password = passwd;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getIOThreads() {
        return IOThreads;
    }

    public void setIOThreads(int iOThreads) {
        IOThreads = iOThreads;
    }
}
