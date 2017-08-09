/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biovis.hackebeil.client.data;

import biovis.hackebeil.client.gui.progress.ProgressAdditionalMeasurementsController;
import biovis.hackebeil.client.gui.progress.ProgressAddMotifsController;
import biovis.hackebeil.client.gui.progress.ProgressAddPWMController;
import biovis.hackebeil.client.gui.progress.ProgressOverviewController;
import biovis.hackebeil.client.gui.progress.ProgressReferenceGenomeController;
import biovis.hackebeil.client.gui.progress.ProgressSegmentationController;
import biovis.hackebeil.common.data.DataFile;
import biovis.hackebeil.common.data.Motif;
import biovis.hackebeil.common.data.PositionWeightMatrix;
import biovislib.vfsjfilechooser.VFSJFileChooser;
import biovislib.vfsjfilechooser.accessories.DefaultAccessoriesPanel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author zeckzer
 */
public class ClientConfiguration {

    private static final int MIN_SEGMENT_LENGTH = 200;

    /*
     * Remote file chooser
     */
    private transient VFSJFileChooser remoteFileChooser;
    private transient DefaultAccessoriesPanel remoteFileChooserConf;
    private transient File rpath;

    // log information
    private transient List<String> logsRefGenome = new ArrayList<>();

    // progress controller
    private transient BooleanProperty referenceGenomeLoaded = new SimpleBooleanProperty(false);
    private transient BooleanProperty segmentationReady = new SimpleBooleanProperty(false);
    private transient BooleanProperty additionalDataReady = new SimpleBooleanProperty(false);
    private transient BooleanProperty motifsReady = new SimpleBooleanProperty(false);
    private transient BooleanProperty pwmsReady = new SimpleBooleanProperty(false);

    private transient ProgressOverviewController progressOverviewController;
    private transient ProgressReferenceGenomeController progressReferenceGenomeController;
    private transient ProgressSegmentationController progressSegmentationController;
    private transient ProgressAdditionalMeasurementsController progressAddAdditionalDataController;
    private transient ProgressAddMotifsController progressAddMotifsController;
    private transient ProgressAddPWMController progressAddPWMController;
    //private transient ProgressComplexAnalysesController progressComplexAnalysesController;

    // Genome information
    private String selectedGenomeFile;
    private String selectedLoadIndexFile;
    private String selectedSaveIndexFile;

    // Data for segmentation
    private ObservableList<DataFile> referenceDataList = FXCollections.observableList(new ArrayList<>());
    private ObservableList<DataFile> additionalDataList = FXCollections.observableList(new ArrayList<>());
    private ObservableList<Motif> motifList = FXCollections.observableList(new ArrayList<>());
    private ObservableList<PositionWeightMatrix> pwmList = FXCollections.observableList(new ArrayList<>());

    // Minimal Segment Length
    private int minSegmentLength = MIN_SEGMENT_LENGTH;

    /*
     * Preferences
     */
    private transient Preferences prefs;

    /**
     *
     */
    public ClientConfiguration() {
    }

    /**
     *
     * @param newConfiguration
     */
    public void update(ClientConfiguration newConfiguration) {
        this.selectedGenomeFile = newConfiguration.selectedGenomeFile;
        this.selectedLoadIndexFile = newConfiguration.selectedLoadIndexFile;
        this.selectedSaveIndexFile = newConfiguration.selectedSaveIndexFile;

        this.referenceDataList.clear();
        for (DataFile df : newConfiguration.getReferenceDataList()) {
            addReferenceFile(df);
        }

        this.additionalDataList.clear();
        // set new data
        for (DataFile df : newConfiguration.getAdditionalDataList()) {
            addAdditionalMeasurementFile(df);
        }

        this.motifList.clear();
        for (Motif motif : newConfiguration.getMotifList()) {
            motif.setChanged(true);
            motif.setDataListNumber(newConfiguration.getMotifList().size());
            addMotif(motif);
        }

        this.pwmList.clear();
        // set new data
        for (PositionWeightMatrix pwm : newConfiguration.getPWMList()) {
            pwm.setChanged(true);
            pwm.checkNormalization();
            pwm.setDataListNumber(newConfiguration.getPWMList().size());
            addPWM(pwm);
        }

        this.minSegmentLength = newConfiguration.minSegmentLength;
    }

    /**
     * clears the Cache
     */
    public void clear() {
        referenceGenomeLoaded.set(false);
        segmentationReady.set(false);
        additionalDataReady.set(false);
        motifsReady.set(false);
        pwmsReady.set(false);

        this.progressReferenceGenomeController.clear();
        this.progressSegmentationController.setDisabled(true);
        this.progressAddAdditionalDataController.setDisabled(true);
        this.progressAddMotifsController.setDisabled(true);
        this.progressAddPWMController.setDisabled(true);
//        this.progressComplexAnalysesController.setDisabled(true);

        this.selectedGenomeFile = "";
        this.selectedLoadIndexFile = "";
        this.selectedSaveIndexFile = "";

        this.referenceDataList.clear();
        this.additionalDataList.clear();
        this.motifList.clear();
        this.pwmList.clear();

        this.minSegmentLength = MIN_SEGMENT_LENGTH;
    }

    /**
     *
     * @param disabled
     */
    public void setBtnsDisabled(boolean disabled) {
        this.progressReferenceGenomeController.setDisabled(disabled);
        this.progressSegmentationController.setDisabled(disabled);
        this.progressAddAdditionalDataController.setDisabled(disabled);
        this.progressAddMotifsController.setDisabled(disabled);
        this.progressAddPWMController.setDisabled(disabled);
//        this.progressComplexAnalysesController.setDisabled(disabled);
    }

    /*
     * Reference Genome functions
     */
    public void updateLogAtRefGenomeController(String log) {
        logsRefGenome.add(log);
        progressReferenceGenomeController.updateLogs(log);
    }

    /*
     * FileChooser
     */
    public void initFileChooser() {
        prefs = Preferences.userNodeForPackage(ClientConfiguration.class);
        remoteFileChooser = new VFSJFileChooser(); // create a file dialog
        remoteFileChooserConf = new DefaultAccessoriesPanel(remoteFileChooser);
        remoteFileChooserConf.setHost(prefs.get("hostname", ""));
        remoteFileChooserConf.setPort(prefs.get("port", "22"));
        remoteFileChooserConf.setUser(prefs.get("user", ""));
        remoteFileChooserConf.setPath(prefs.get("path", ""));
        // configure the file dialog
        remoteFileChooser.setAccessory(remoteFileChooserConf);
        remoteFileChooser.setFileHidingEnabled(false);
        remoteFileChooser.setMultiSelectionEnabled(false);
        remoteFileChooser.setFileSelectionMode(VFSJFileChooser.SELECTION_MODE.FILES_ONLY);
        if (rpath != null) {
            remoteFileChooser.setCurrentDirectory(rpath);
        }
        // System.out.println(this + " " + remoteFileChooser);
    }

    public VFSJFileChooser getRemoteFileChooser() {
        if (remoteFileChooser == null) {
            initFileChooser();
        }
        return remoteFileChooser;
    }

    public void setRemotePathForFileChooser(String remotePath) {
        rpath = new File(remotePath);

    }

    public BooleanProperty getReferenceGenomeLoaded() {
        return referenceGenomeLoaded;
    }

    public void setReferenceGenomeLoaded(boolean referenceGenomeLoaded) {
        this.referenceGenomeLoaded.set(referenceGenomeLoaded);
    }

    public BooleanProperty getSegmentationReady() {
        return segmentationReady;
    }

    public void setSegmentationReady(boolean segmentationReady) {
        this.segmentationReady.set(segmentationReady);
    }

    public BooleanProperty getAdditionalDataReady() {
        return additionalDataReady;
    }

    public void setAdditionalDataReady(boolean additionalDataReady) {
        this.additionalDataReady.set(additionalDataReady);
    }

    public BooleanProperty getMotifsReady() {
        return motifsReady;
    }

    public void setMotifsReady(boolean motifsReady) {
        this.motifsReady.set(motifsReady);
    }

    public BooleanProperty getPWMsReady() {
        return pwmsReady;
    }

    public void setPwmsReady(boolean pwmsReady) {
        this.pwmsReady.set(pwmsReady);
    }
    //--------------------------------------------------------------------------
    // Progress
    //--------------------------------------------------------------------------

    public ProgressOverviewController getProgressOverviewController() {
        return progressOverviewController;
    }

    public void setProgressOverviewController(ProgressOverviewController progressOverviewController) {
        this.progressOverviewController = progressOverviewController;
    }

    //--------------------------------------------------------------------------
    // Genome information
    //--------------------------------------------------------------------------
    public String getSelectedGenomeFilePath() {
        return selectedGenomeFile;
    }

    public void setSelectedGenomeFile(String selectedGenomeFile) {
        this.selectedGenomeFile = selectedGenomeFile;
    }

    public String getSelectedLoadIndexFilePath() {
        return selectedLoadIndexFile;
    }

    public void setSelectedLoadIndexFilePath(String selectedIndexFile) {
        this.selectedLoadIndexFile = selectedIndexFile;
    }

    public String getSelectedSaveIndexFilePath() {
        return this.selectedSaveIndexFile;
    }

    public void setSelectedSaveIndexFilePath(String saveFile) {
        this.selectedSaveIndexFile = saveFile;
    }

    public void addReferenceFile(DataFile referenceFile) {
        referenceDataList.add(referenceFile);
    }

    public void clearReferenceDataList() {
        referenceDataList.clear();
    }

    public ObservableList<DataFile> getReferenceDataList() {
        return referenceDataList;
    }

    /**
     * Additional Data functions
     * @param dataFileToRemove
     */
    public void removeAdditionalMeasurementFile(DataFile dataFileToRemove) {
        int columnNr = dataFileToRemove.getColumnNumber();
        int dataListNr = dataFileToRemove.getDataListNumber();

        if (columnNr >= 0) {
            // update column number and datalist number
            for (int i = 0; i < additionalDataList.size(); i++) {
                if (additionalDataList.get(i).getColumnNumber() > columnNr) {
                    additionalDataList.get(i).setColumnNumber(additionalDataList.get(i).getColumnNumber() - 1);
                }
                if (additionalDataList.get(i).getDataListNumber() > dataListNr) {
                    additionalDataList.get(i).setDataListNumber(additionalDataList.get(i).getDataListNumber() - 1);
                }
            }
        }

        additionalDataList.remove(dataFileToRemove);
    }

    /**
     *
     * @param dataFile
     */
    public void addAdditionalMeasurementFile(
        DataFile dataFile
    ) {
        dataFile.setDataListNumber(additionalDataList.size());
        dataFile.setColumnNumber(3 + additionalDataList.size());
        dataFile.setChanged(true);
        additionalDataList.add(dataFile);
    }

    /**
     *
     * @param index
     * @return
     */
    public DataFile getAdditionalDataFile(
        int index
    ) {
        return additionalDataList.get(index);
    }

    public void clearAdditionalDataList() {
        additionalDataList.clear();
    }

    public ObservableList<DataFile> getAdditionalDataList() {
        return additionalDataList;
    }

    /**
     *
     * @param motif
     */
    public void removeMotifFromList(Motif motif) {
        int columnNr = motif.getColumnNumber();
        int dataListNr = motif.getDataListNumber();

        if (columnNr >= 0) {
            // update columnnumber and datalistnumber
            for (int i = 0; i < motifList.size(); i++) {
                if (motifList.get(i).getColumnNumber() > columnNr) {
                    motifList.get(i).setColumnNumber(motifList.get(i).getColumnNumber() - 1);
                }
                if (motifList.get(i).getDataListNumber() > dataListNr) {
                    motifList.get(i).setDataListNumber(motifList.get(i).getDataListNumber() - 1);
                }
            }
        }

        motifList.remove(motif);
    }

    public void addMotif(
        Motif motif
    ) {
        motif.setDataListNumber(motifList.size());
        motif.setColumnNumber(3 + additionalDataList.size() + motifList.size());
        motif.setChanged(true);
        motifList.add(motif);
    }

    public ObservableList<Motif> getMotifList() {
        return motifList;
    }

    /**
     *
     * @param pwm
     */
    public void removePWMFromList(PositionWeightMatrix pwm) {
        int columnNr = pwm.getColumnNumber();
        int dataListNr = pwm.getDataListNumber();

        if (columnNr >= 0) {
            // update columnnumber and datalistnumber
            for (int i = 0; i < pwmList.size(); i++) {
                if (pwmList.get(i).getColumnNumber() > columnNr) {
                    pwmList.get(i).setColumnNumber(pwmList.get(i).getColumnNumber() - 1);
                }
                if (pwmList.get(i).getDataListNumber() > dataListNr) {
                    pwmList.get(i).setDataListNumber(pwmList.get(i).getDataListNumber() - 1);
                }
            }
        }

        pwmList.remove(pwm);
    }

    public void addPWM(
        PositionWeightMatrix pwm
    ) {
        pwm.setDataListNumber(pwmList.size());
        pwm.setColumnNumber(3 + additionalDataList.size() + motifList.size() + pwmList.size());
        pwm.setChanged(true);
        pwmList.add(pwm);
    }

    public ObservableList<PositionWeightMatrix> getPWMList() {
        return pwmList;
    }

    public int getMinSegmentLength() {
        return minSegmentLength;
    }

    public void setMinSegmentLength(int minSegmentLength) {
        this.minSegmentLength = minSegmentLength;
    }

    public ProgressReferenceGenomeController getProgressReferenceGenomeController() {
        return this.progressReferenceGenomeController;
    }

    public void setProgressReferenceGenomeController(ProgressReferenceGenomeController refGenomeController) {
        this.progressReferenceGenomeController = refGenomeController;
    }

    /**
     * @return the additionalDataController
     */
    public ProgressAdditionalMeasurementsController getProgressAddAdditionalDataController() {
        return progressAddAdditionalDataController;
    }

    public void setProgressAdditionalMeasurementsController(ProgressAdditionalMeasurementsController additionalDataController) {
        this.progressAddAdditionalDataController = additionalDataController;
    }

    public ProgressSegmentationController getProgressSegmentationController() {
        return this.progressSegmentationController;
    }

    public void setProgressSegmentationController(ProgressSegmentationController progressSegmentationController) {
        this.progressSegmentationController = progressSegmentationController;
    }

    public ProgressAddMotifsController getProgressAddMotifsController() {
        return this.progressAddMotifsController;
    }

    public void setProgressAddMotifsController(ProgressAddMotifsController progressAddMotifsController) {
        this.progressAddMotifsController = progressAddMotifsController;
    }

    public ProgressAddPWMController getProgressAddPWMController() {
        return this.progressAddPWMController;
    }

    public void setProgressAddPWMController(ProgressAddPWMController progressAddPWMController) {
        this.progressAddPWMController = progressAddPWMController;
    }

    /**
     *
     * @param dfNew data file
     */
    public void updateSegmentationLength(DataFile dfNew) {
        for (DataFile df : getReferenceDataList()) {
            if (df.getId() == dfNew.getId()) {
                df.setBlockLengths(dfNew.getBlockLengths());
                df.setThickLengths(dfNew.getThickLengths());
                df.setChromLengths(dfNew.getChromLengths());
            }
        }
    }

}
