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

import biovis.hackebeil.client.data.ClientConfiguration;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import biovislib.vfsjfilechooser.VFSJFileChooser;
import biovislib.vfsjfilechooser.VFSJFileChooser.RETURN_TYPE;
import biovislib.vfsjfilechooser.filechooser.AbstractVFSFileFilter;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;

/**
 * @author Alrik Hausdorf, Dirk Zeckzer
 *
 */
public class DataIO {

    private static final Logger log = Logger.getLogger(DataIO.class.getName());

    /**
     *
     * @param clientConfiguration
     * @param path
     */
    public static void exportClientConfigurationToFile(
        ClientConfiguration clientConfiguration,
        String path
    ) {
        if (path == null) {
            // Set extension filter
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("hconf files (*.hconf", "*.hconf");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setTitle("Save config to file");
            File file;
            file = fileChooser.showSaveDialog(null);
            if (file != null) {
                saveClientConfigurationToFile(clientConfiguration, file);
            }
        } else {
            saveClientConfigurationToFile(clientConfiguration, new File(path));
        }
    }

    /**
     *
     * @param file
     * @return client configuration
     */
    public static ClientConfiguration importClientConfigurationFromFile(
        File file
    ) {
        if (file == null) {
            ClientConfiguration tempClientConfiguration = new ClientConfiguration();
            VFSJFileChooser remoteFileChooser = tempClientConfiguration.getRemoteFileChooser();
            remoteFileChooser.setDialogTitle("Load config from file");

            AbstractVFSFileFilter extFilter = new AbstractVFSFileFilter() {
                @Override
                public String getDescription() {
                    return "hconf Files and Folder";
                }

                @Override
                public boolean accept(FileObject f) {
                    try {
                        if (f.getType() == FileType.FOLDER) {
                            return true;
                        }
                    } catch (FileSystemException e) {
                        e.printStackTrace();
                        return true;
                    }
                    return f.getName().getPath().endsWith(".hconf");
                }
            };
            remoteFileChooser.addChoosableFileFilter(extFilter);

            RETURN_TYPE answer = remoteFileChooser.showOpenDialog(null);
            if (answer == RETURN_TYPE.APPROVE) {

                final FileObject aFileObject = remoteFileChooser.getSelectedFileObject();
                String pathOfSelectedFile = aFileObject.getName().getPath();
                log.info(pathOfSelectedFile);
                return loadClientConfigurationFromFile(new File(pathOfSelectedFile));
            } else {
                return null;
            }
        } else {
            return loadClientConfigurationFromFile(file);
        }
    }

    private static ClientConfiguration loadClientConfigurationFromFile(File file) {
        try {
            class ObservableListDeserializer implements JsonDeserializer<ObservableList<Object>> {

                @Override
                public ObservableList<Object> deserialize(JsonElement json, Type type,
                                                          JsonDeserializationContext context) throws JsonParseException {
                    Gson gson = new Gson();
                    List<Object> tasks = gson.fromJson(json.getAsJsonArray().toString(), type);
                    return FXCollections.observableArrayList(tasks);
                }
            }

            Gson gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(ObservableList.class, new ObservableListDeserializer()).create();

            JsonReader reader = new JsonReader(new FileReader(file));
            Type resultsType = new TypeToken<ClientConfiguration>() {
            }.getType();
            ClientConfiguration clientConfiguration = gson.fromJson(reader, resultsType);
            log.log(Level.INFO, "{0}", clientConfiguration);
            return clientConfiguration;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private static void saveClientConfigurationToFile(
        ClientConfiguration clientConfiguration,
        File file
    ) {
        if (!file.getName().endsWith(".hconf")) {
            file = new File(file.getAbsoluteFile() + ".hconf");
        }
        try (FileOutputStream out = new FileOutputStream(file)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse((String) gson.toJson(clientConfiguration));
            String prettyJsonString = gson.toJson(je);
            out.write(prettyJsonString.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
