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
package biovis.hackebeil.client.gui.output.pwm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import biovis.hackebeil.client.data.ClientConfiguration;
import biovis.hackebeil.common.data.PositionWeightMatrix;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * @author Alrik Hausdorf<alrik@gurkware.de>, nhinzmann, Dirk Zeckzer
 *
 */
public class PWMTabPaneController {

    private Tab rootTab;
    private ClientConfiguration clientConfiguration;
    private Boolean isInitialized = false;

    // Elements of the primary information tab
    @FXML
    private TabPane tabs;

    // Tabs
    private List<PWMTab> pwmDataTabs = new ArrayList<>();

    private boolean isUpdatingPWMDataTabs = false;

    /**
     */
    public PWMTabPaneController() {
    }

    /**
     *
     * @param rootTab
     */
    public void setRootPane(Tab rootTab) {
        this.rootTab = rootTab;
    }

    /**
     *
     * @return
     */
    public static PWMTabPaneController getInstance() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(PWMTabPaneController.class.getResource("PWMTabPane.fxml"));
            Tab rootTab = loader.load();
            PWMTabPaneController statisticalInformationController = loader.<PWMTabPaneController>getController();
            statisticalInformationController.setRootPane(rootTab);
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
     * @param pwmValues
     */
    public void updateTabs(
        Map<String, List<Double>> pwmValues
    ) {
        if (isUpdatingPWMDataTabs) {
            return;
        }
        isUpdatingPWMDataTabs = true;

        Platform.runLater(() -> {
            rootTab.setDisable(false);
            if (clientConfiguration.getPWMList().size() != pwmDataTabs.size()) {
                for (Tab t : pwmDataTabs) {
                    tabs.getTabs().remove(t);
                }
                int insert = 0;
                for (PositionWeightMatrix pwm : clientConfiguration.getPWMList()) {
                    PWMTab pwmTab = new PWMTab(pwm, pwmValues);
                    pwmDataTabs.add(pwmTab);

                    tabs.getTabs().add(insert, pwmTab);
                    insert++;
                }
            }
        });

        isUpdatingPWMDataTabs = false;
    }

    /**
     *
     * @param string
     */
    public void exportToPng(String string) {
        string += "-PWM";
        for (PWMTab pwmTab : pwmDataTabs) {
            pwmTab.saveChartToFile(string);
        }
    }
}
