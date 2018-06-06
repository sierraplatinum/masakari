/**
 *  Copyright 2016 Dirk Zeckzer
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
package biovis.hackebeil.client.gui.output.common;

import java.io.IOException;
import java.util.logging.Logger;

import biovis.hackebeil.client.io.ImageExport;
import biovis.hackebeil.client.utilities.ChartUtilities;
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
 * @author Dirk Zeckzer
 *
 */
public class CodeDistanceController {

	private static final Logger log = Logger.getLogger(CodeDistanceController.class.getName());

	private Boolean isInitialized = false;

	private AnchorPane rootPane;

	@FXML
	private AnchorPane panePairDistance;
	@FXML
	private AnchorPane paneChartDistance;
	@FXML
	private BarChart<String, Integer> chartDistance;
	@FXML
	private ChoiceBox<String> choiceDistanceCode;
	@FXML
	private CheckBox cbDistanceYAxisLogScale;

	private int numberOfCodes;
	private int[][] distanceCountMatrix;

	public CodeDistanceController() {
	}

	private void setRootPane(
			AnchorPane pane
			) {
		this.rootPane = pane;
	}

	public static CodeDistanceController getInstance() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(CodeDistanceController.class.getResource("CodeDistance.fxml"));
			AnchorPane rootPane = loader.load();
			CodeDistanceController codeDistanceController = loader.<CodeDistanceController>getController();
			codeDistanceController.setRootPane(rootPane);
			return codeDistanceController;
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

	/**
	 *
	 */
	public void redraw() {
		if (distanceCountMatrix == null) {
			return;
		}

		Platform.runLater(() -> {
			updateChoices();
			redrawDistanceChart();
		});
	}

	/**
	 *
	 */
	private void updateChoices() {
		int[] numberOfDistances = new int[numberOfCodes];
		if (distanceCountMatrix != null) {
			for (int code = 0;
					code < distanceCountMatrix.length;
					++code) {
				for (int distance = 0;
						distance < distanceCountMatrix[code].length;
						++distance) {
					numberOfDistances[code] += distanceCountMatrix[code][distance];
				}
			}
		}

		choiceDistanceCode.getItems().clear();
		choiceDistanceCode.getItems().add("All codes");
		for (int i = 0; i < numberOfCodes; i++) {
			choiceDistanceCode.getItems().add((i) + " (" + numberOfDistances[i] + ")");
		}
		choiceDistanceCode.getSelectionModel().selectFirst();
	}

	private void redrawDistanceChart() {
		if (distanceCountMatrix == null) {
			return;
		}

		if (choiceDistanceCode.getValue() == null) {
			return;
		}

		SortedMap<Integer, Integer> distances = new TreeMap<>();

		// Start Computing
		Platform.runLater(() -> {
			int maxDistance = 0;

			if (choiceDistanceCode.getValue().equals("All codes")) {
				int value = 0;
				for (int code = 0;
						code < distanceCountMatrix.length;
						++code) {
					for (int j = 0;
							j < distanceCountMatrix[code].length;
							++j) {
						value = distanceCountMatrix[code][j];
						if (value != 0
								&& maxDistance < j) {
							maxDistance = j;
						}
						Integer oldDistances = distances.get(j);
						if (oldDistances != null) {
							value += oldDistances;
						}
						distances.put(j, value);
					}
				}
			} else {
				int value = 0;
				int code = Integer.parseInt(choiceDistanceCode.getValue().split(" ")[0]);
				for (int j = 0;
						j < distanceCountMatrix[code].length;
						++j) {
					value = distanceCountMatrix[code][j];
					distances.put(j, value);
					if (value != 0) {
						maxDistance = j;
					}
				}
			}

			XYChart.Series<String, Integer> series1 = new XYChart.Series<>();
			if (distances.size() > 0) {
				long[] blockstarts = ChartUtilities.createBins((long) 0, (long) (numberOfCodes - 1), numberOfCodes, false);
				List<XYChart.Data<String, Integer>> chartData
				= ChartUtilities.createChartDataforBins(distances, blockstarts, 0, (numberOfCodes - 1));
				for (XYChart.Data<String, Integer> a : chartData) {
					series1.getData().add(a);
					if (a.getYValue() > maxDistance) {
						maxDistance = a.getYValue();
					}
				}
			}
			final int finalMax = maxDistance;
			chartDistance.getStylesheets().clear();
			chartDistance.getStylesheets().add(getClass().getResource("histogram.css").toExternalForm());
			createDistanceChart((finalMax));
			chartDistance.getData().clear();
			chartDistance.getData().add(series1);
			for (final Series<String, Integer> series : chartDistance.getData()) {
				for (final Data<String, Integer> data : series.getData()) {
					Tooltip tooltip = ChartUtilities.createTooltip(data);
					Tooltip.install(data.getNode(), tooltip);
				}
			}
		});
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void createDistanceChart(double maxValue) {
		// System.out.println("createChart");
		this.paneChartDistance.getChildren().clear();
		CategoryAxis xAxis = new CategoryAxis();
		if (maxValue <= 0) {
			chartDistance = new BarChart(xAxis, new NumberAxis());
			chartDistance.setTitle("No data available");
			chartDistance.setTitleSide(Side.TOP);
		} else if (this.cbDistanceYAxisLogScale.isSelected()) {
			chartDistance = new BarChart(xAxis, new LogarithmicAxis(0.4, maxValue, true));
			chartDistance.getYAxis().setLabel("occurrence (log)");
		} else {
			AxisLabeling x = AxisLabeling.base10();
			x.loose= false;
			Label lab = x.search(0.0, maxValue, 10);
			double max = lab.getMax();
			if(lab.getMax() <= maxValue)
			{
				max += lab.getStep();
			}
			chartDistance = new BarChart(xAxis, new NumberAxis(0.0, max, lab.getStep()));
			chartDistance.getYAxis().setLabel("occurrence (lin)");
		}
		chartDistance.getStylesheets().clear();
		chartDistance.getStylesheets().add(getClass().getResource("histogram.css").toExternalForm());
		chartDistance.setLegendVisible(false);

		chartDistance.getXAxis().setAnimated(false);

		chartDistance.getXAxis().setLabel("distances (lin)");
		chartDistance.getYAxis().setAnimated(false);
		chartDistance.getYAxis().setTickLength(1);
		chartDistance.setCategoryGap(0);
		chartDistance.setBarGap(1);
		chartDistance.setAnimated(false);
		MenuItem cmItem1 = new MenuItem("Save as image");
		cmItem1.setOnAction((ActionEvent e) -> {
			ImageExport.exportPaneToPng(paneChartDistance, null);
		});
		final ContextMenu cm = new ContextMenu(cmItem1);
		paneChartDistance.setOnMouseClicked((event) -> {
			if (event.getButton() == MouseButton.SECONDARY) {
				cm.show(paneChartDistance, event.getScreenX(), event.getScreenY());
			}
		});
		AnchorPane.setBottomAnchor(chartDistance, 0.0);
		AnchorPane.setTopAnchor(chartDistance, 50.0);
		AnchorPane.setLeftAnchor(chartDistance, 0.0);
		AnchorPane.setRightAnchor(chartDistance, 0.0);
		this.paneChartDistance.getChildren().add(chartDistance);
	}

	private void init() {
		cbDistanceYAxisLogScale.selectedProperty().addListener((ov, before, after) -> {
			redrawDistanceChart();
		});

		choiceDistanceCode.valueProperty().addListener((ov, before, after) -> {
			redrawDistanceChart();
		});
	}

	private void testForReady() {
		if (isInitialized == true) {
			init();
		}
	}

	/**
	 *
	 * @param numberOfCodes
	 * @param distanceCountMatrix
	 */
	public void setData(
			int numberOfCodes,
			int[][] distanceCountMatrix
			) {
		this.numberOfCodes = numberOfCodes;
		this.distanceCountMatrix = distanceCountMatrix;
		redraw();
	}

	private void saveDistanceChartToFile(String path) {
		ImageExport.exportPaneToPng(this.paneChartDistance, path);
	}

	/**
	 *
	 * @param path
	 */
	public void exportToPng(String path) {
		path += "Distance";
		this.saveDistanceChartToFile(path);
	}
}
