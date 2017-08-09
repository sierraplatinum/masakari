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
package biovis.hackebeil.client.gui.output.motif;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import biovis.hackebeil.client.data.ClientConfiguration;
import biovis.hackebeil.common.data.Motif;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * @author Alrik Hausdorf<alrik@gurkware.de>, nhinzmann, Dirk Zeckzer
 *
 */
public class MotifTabPaneController {

    private Tab rootTab;
    private ClientConfiguration clientConfiguration;
    private Boolean isInitialized = false;

    // Elements of the primary information tab
    @FXML
    private TabPane tabs;

    // Tabs
    private List<MotifTab> motifDataTabs = new ArrayList<>();

    private boolean isUpdatingMotifDataTabs = false;

    /**
     */
    public MotifTabPaneController() {
    }

    /**
     *
     * @param rootTab
     */
    public void setRootTab(Tab rootTab) {
        this.rootTab = rootTab;
    }

    /**
     *
     * @return
     */
    public static MotifTabPaneController getInstance() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MotifTabPaneController.class.getResource("MotifTabPane.fxml"));
            Tab rootTab = loader.load();
            MotifTabPaneController statisticalInformationController = loader.<MotifTabPaneController>getController();
            statisticalInformationController.setRootTab(rootTab);
            return statisticalInformationController;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Initialization, called by FXMLLoader.
     */
    public void initialize() {
        isInitialized = true;
        testForReady();
    }

    /**
     * Check if all pre-conditions for using this tab are fulfilled.
     */
    private void testForReady() {
        if (clientConfiguration != null && isInitialized == true) {
            this.init();
        }
    }

    /**
     * Complete initialization.
     */
    private void init() {
    }

    /**
     * Initialize the different sub-tabs of this tab.
     */
    public void initView() {
    }

    /**
     *
     * @param clientConfiguration
     * @return
     */
    public Tab loadView(
        ClientConfiguration clientConfiguration
    ) {
        this.clientConfiguration = clientConfiguration;
        testForReady();

        return rootTab;
    }

    /**
     * @param motifs
     */
    public void updateTabs(
        Map<String, List<Double>> motifs
    ) {
        if (isUpdatingMotifDataTabs) {
            return;
        }
        isUpdatingMotifDataTabs = true;

        Platform.runLater(() -> {
            rootTab.setDisable(false);
            if (clientConfiguration.getMotifList().size() != motifDataTabs.size()) {
                for (Tab t : motifDataTabs) {
                    tabs.getTabs().remove(t);
                }
                int insert = 0;
                for (Motif motif : clientConfiguration.getMotifList()) {
                    MotifTab motifTab = new MotifTab(motif, motifs);
                    motifDataTabs.add(motifTab);

                    tabs.getTabs().add(insert, motifTab);
                    insert++;
                }
            }
        });
        isUpdatingMotifDataTabs = false;
    }

    /**
     *
     * @param string
     */
    public void exportToPng(String string) {
        string += "-Motif";
        for (MotifTab motifTab : motifDataTabs) {
            motifTab.saveChartToFile(string);
        }
    }
}
