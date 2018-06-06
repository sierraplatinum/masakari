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
package biovis.hackebeil.client.gui.output.breakSegmentAnalysis;

import java.io.IOException;

import biovis.hackebeil.client.io.ImageExport;
import biovis.hackebeil.client.utilities.ChartUtilities;
import biovis.hackebeil.common.data.BreakSegment;
import biovislib.javafx.chart.AxisLabeling;
import biovislib.javafx.chart.LogarithmicAxis;
import biovislib.javafx.chart.AxisLabeling.Label;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;

/**
 * @author Alrik Hausdorf
 *
 */
public class BreakSegmentLengthAnalysisController {

	private Boolean isInitialized = false;

	private AnchorPane rootPane;

	@FXML
	private AnchorPane anchorPaneBreakSegmentLengthAnalysis;
	@FXML
	private AnchorPane paneChartLength;
	@FXML
	private BarChart<String, Integer> chartLength;
	@FXML
	private ChoiceBox<String> choiceLengthCode;
	@FXML
	private CheckBox cbLengthYAxisLogScale;

	private int numberOfCodes;
	private List<BreakSegment> breakSegmentData;
	private int maxLength;

	public BreakSegmentLengthAnalysisController() {
	}

	private void setRootPane(
			AnchorPane pane
			) {
		this.rootPane = pane;
	}

	public static BreakSegmentLengthAnalysisController getInstance() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(BreakSegmentLengthAnalysisController.class.getResource("BreakSegmentLengthAnalysis.fxml"));
			AnchorPane rootPane = loader.load();
			BreakSegmentLengthAnalysisController breakSegmentController = loader.<BreakSegmentLengthAnalysisController>getController();
			breakSegmentController.setRootPane(rootPane);
			return breakSegmentController;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 *
	 * @return
	 */
	public AnchorPane loadView() {
		testForReady();

		return rootPane;
	}

	/**
	 * Initialization, called by FXMLLoader.
	 */
	public void initialize() {
		isInitialized = true;
		testForReady();
	}

	private void testForReady() {
		if (isInitialized == true) {
			init();
		}
	}

	public void initView() {
		if (breakSegmentData == null) {
			return;
		}

		redrawLengthsChart();
	}

	/**
	 *
	 */
	public void redraw() {
		if (breakSegmentData == null) {
			return;
		}

		Platform.runLater(() -> {
			updateChoices();
			redrawLengthsChart();
		});
	}

	private void redrawLengthsChart() {
		if (breakSegmentData == null) {
			return;
		}

		SortedMap<Integer, Integer> lengths = new TreeMap<>();
		// Start Computing
		Platform.runLater(() -> {
			for (BreakSegment dp : breakSegmentData) {
				if (choiceLengthCode.getValue() != null
						&& !choiceLengthCode.getValue().equals("All codes")
						&& Integer.parseInt(choiceLengthCode.getValue().split(" ")[0]) != dp.getBeforeCode()) {
					continue;
				}
				Integer key = dp.getLength();
				if (lengths.containsKey(key)) {
					lengths.replace(key, lengths.get(key) + 1);
				} else {
					lengths.put(key, 1);
				}
			}

			int maxValue = 0;
			XYChart.Series<String, Integer> series1 = new XYChart.Series<>();
			if (lengths.size() > 0) {
				long[] blockstarts = ChartUtilities.createBins(1, maxLength, 100, false);
				List<XYChart.Data<String, Integer>> chartData = ChartUtilities.createChartDataforBins(lengths, blockstarts,
						1, maxLength);
				for (XYChart.Data<String, Integer> a : chartData) {
					series1.getData().add(a);
					if (a.getYValue() > maxValue) {
						maxValue = a.getYValue();
					}
				}
			}
			final int finalMax = maxValue;

			createLengthChart((finalMax));
			chartLength.getData().clear();
			chartLength.getData().add(series1);
			for (final Series<String, Integer> series : chartLength.getData()) {
				for (final Data<String, Integer> data : series.getData()) {
					Tooltip tooltip = ChartUtilities.createTooltip(data);
					Tooltip.install(data.getNode(), tooltip);
				}
			}
		});
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void createLengthChart(double maxValue) {
		// System.out.println("createChart");
		this.paneChartLength.getChildren().clear();
		CategoryAxis xAxis = new CategoryAxis();
		if (maxValue <= 0) {
			chartLength = new BarChart(xAxis, new NumberAxis());
			chartLength.setTitle("No data available");
			chartLength.setTitleSide(Side.TOP);
		} else if (this.cbLengthYAxisLogScale.isSelected()) {
			chartLength = new BarChart(xAxis, new LogarithmicAxis(0.4, maxValue, true));
			chartLength.getYAxis().setLabel("occurrence (log)");
		} else {
			AxisLabeling x = AxisLabeling.base10();
			x.loose= false;
			Label lab = x.search(0.0, maxValue, 10);
			double max = lab.getMax();
			if(lab.getMax() <= maxValue)
			{
				max += lab.getStep();
			}
			chartLength = new BarChart(xAxis, new NumberAxis(0.0, max, lab.getStep()));
			chartLength.getYAxis().setLabel("occurrence (lin)");
		}
		chartLength.setLegendVisible(false);

		chartLength.getXAxis().setAnimated(false);

		chartLength.getXAxis().setLabel("length (lin)");
		chartLength.getYAxis().setAnimated(false);
		chartLength.getYAxis().setTickLength(1);
		chartLength.setCategoryGap(0);
		chartLength.setBarGap(1);
		chartLength.setAnimated(false);
		MenuItem cmItem1 = new MenuItem("Save as image");
		cmItem1.setOnAction((ActionEvent e) -> {
			ImageExport.exportPaneToPng(paneChartLength, null);
		});
		final ContextMenu cm = new ContextMenu(cmItem1);
		paneChartLength.setOnMouseClicked((event) -> {
			if (event.getButton() == MouseButton.SECONDARY) {
				cm.show(paneChartLength, event.getScreenX(), event.getScreenY());
			}
		});
		AnchorPane.setBottomAnchor(chartLength, 0.0);
		AnchorPane.setTopAnchor(chartLength, 50.0);
		AnchorPane.setLeftAnchor(chartLength, 0.0);
		AnchorPane.setRightAnchor(chartLength, 0.0);
		this.paneChartLength.getChildren().add(chartLength);
	}

	/**
	 *
	 */
	private void updateChoices() {
		int[] length = new int[numberOfCodes];
		if (breakSegmentData != null
				&& !breakSegmentData.isEmpty()) {
			for (BreakSegment bp : breakSegmentData) {
				length[bp.getBeforeCode()]++;
			}
		}
		choiceLengthCode.getItems().clear();
		choiceLengthCode.getItems().add("All codes");
		for (int i = 0; i < numberOfCodes; i++) {
			choiceLengthCode.getItems().add((i) + " (" + length[i] + ")");
		}
		choiceLengthCode.getSelectionModel().selectFirst();
	}

	private void init() {
		cbLengthYAxisLogScale.selectedProperty().addListener((ov, before, after) -> {
			redrawLengthsChart();
		});

		choiceLengthCode.valueProperty().addListener((ov, before, after) -> {
			redrawLengthsChart();
		});
	}

	/**
	 *
	 * @param numberOfCodes
	 * @param breakSegmentData
	 * @param maxLength
	 */
	public void setBreakSegmentData(
			int numberOfCodes,
			List<BreakSegment> breakSegmentData,
			int maxLength
			) {
		if (breakSegmentData == null
				|| breakSegmentData.isEmpty()) {
			rootPane.setDisable(true);
			return;
		}

		rootPane.setDisable(false);
		this.numberOfCodes = numberOfCodes;
		this.breakSegmentData = breakSegmentData;
		this.maxLength = maxLength;

		redraw();
	}

	private void saveLengthChartToFile(String path) {
		ImageExport.exportPaneToPng(this.paneChartLength, path);
	}

	public void exportToPng(String path) {
		this.saveLengthChartToFile(path);
	}
}
