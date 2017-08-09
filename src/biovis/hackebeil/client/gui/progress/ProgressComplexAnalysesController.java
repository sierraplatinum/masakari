package biovis.hackebeil.client.gui.progress;

import java.io.IOException;
import java.util.logging.Logger;

import com.google.gson.Gson;

import biovis.hackebeil.client.commander.ClientCommander;
import biovis.hackebeil.client.data.ClientConfiguration;
import biovis.hackebeil.common.data.Messages;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;

public class ProgressComplexAnalysesController {

    Logger log = Logger.getLogger(ProgressComplexAnalysesController.class.toString());

    private ClientConfiguration clientConfiguration;
    private ClientCommander clientCommander;
    private AnchorPane anchorProgressComplexAnalyses;

    @FXML
    private Button btnStartComputing;

    // Begin Correlaction Line
    @FXML
    private CheckBox cbCorrelation;
    @FXML
    private ProgressIndicator progressCorrelation;

    public ProgressComplexAnalysesController() {
    }

    /**
     *
     * @param rootPane
     */
    public void setPane(AnchorPane rootPane) {
        this.anchorProgressComplexAnalyses = rootPane;
    }

    /**
     *
     * @return
     */
    public static ProgressComplexAnalysesController getInstance() {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(ProgressComplexAnalysesController.class.getResource("ProgressComplexAnalyses.fxml"));
            AnchorPane anchorProgressComplexAnalyses = loader.load();
            ProgressComplexAnalysesController progressComplexAnalysesController = loader
                .<ProgressComplexAnalysesController>getController();
            progressComplexAnalysesController.setPane(anchorProgressComplexAnalyses);
            return progressComplexAnalysesController;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @param clientConfiguration
     * @param commander
     * @return
     */
    public AnchorPane loadProgressComplexAnalyses(
        ClientConfiguration clientConfiguration,
        ClientCommander commander
    ) {
        this.clientConfiguration = clientConfiguration;
        this.clientCommander = commander;
        testForReady();

        return anchorProgressComplexAnalyses;
    }

    private void testForReady() {
        if (clientConfiguration != null && clientCommander != null && this.btnStartComputing != null) {
            init();
        }
    }

    private void init() {
//        clientConfiguration.setProgressComplexAnalysesController(this);
    }

    @FXML
    public void startComputing() {
//		log.info("cbCorrelation" + this.cbCorrelation.isSelected());
        if (this.cbCorrelation.isSelected()) {
            this.progressCorrelation.setProgress(-1);
            this.progressCorrelation.setVisible(true);
            Object[] command = new Object[2];
            command[0] = Messages.SERVER_startCorrelation;
            Gson gson = new Gson();
            String dataFileList = gson.toJson("");
            command[1] = dataFileList;
            clientCommander.sendCommand(command);

        }
    }

    public void setDisabled(boolean b) {
        this.btnStartComputing.setDisable(b);
    }

    public void clear() {

    }

    public void setCorrelationDone(boolean b) {
        if (b) {
            this.progressCorrelation.setProgress(100.0);
        } else {
            this.progressCorrelation.setProgress(-1.0);
        }

    }
}
