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
package biovis.hackebeil.client.gui.output.lengthAnalysis;

import biovis.hackebeil.client.data.ClientConfiguration;
import biovis.hackebeil.client.io.ImageExport;
import biovis.hackebeil.client.utilities.ChartUtilities;
import biovislib.javafx.chart.AxisLabeling;
import biovislib.javafx.chart.AxisLabeling.Label;
import biovislib.javafx.chart.LogarithmicAxis;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.SortedMap;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/**
 * @author Alrik Hausdorf, Dirk Zeckzer
 *
 */
public class LengthAnalysisSegmentationController
implements LengthAnalysisControllerInterface {

	private static final Logger log = Logger.getLogger(LengthAnalysisSegmentationController.class.getName());

	private final static int DEFAULT_BLOCK_COUNT = 100;

	private ClientConfiguration clientConfiguration;
	private LengthAnalysisRootController parent;
	private SortedMap<Integer, Integer> unmodifiedSegmentLengths;
	private SortedMap<Integer, Integer> modifiedSegmentLengths;

	private Boolean isInitialized = false;

	private int xAxisBlockCount = DEFAULT_BLOCK_COUNT;

	private AnchorPane anchorPane;

	@FXML
	private GridPane baseLayoutGrid;
	@FXML
	private HBox logScaleSettings;
	@FXML
	private CheckBox cbXAxisLogScale;
	@FXML
	private CheckBox cbYAxisLogScale;
	@FXML
	private HBox rangeSettings;
	@FXML
	private Button btnReset;
	@FXML
	private TextField tfFrom;
	@FXML
	private TextField tfTo;
	@FXML
	private AnchorPane chartPane;

	private BarChart<String, Integer> chart;

	private boolean ignore = false;

	public LengthAnalysisSegmentationController() {
	}

	/**
	 *
	 * @param pane
	 */
	private void setAnchorPane(
			AnchorPane pane
			) {
		this.anchorPane = pane;
	}

	/**
	 *
	 * @return
	 */
	public static LengthAnalysisSegmentationController getInstance() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(LengthAnalysisSegmentationController.class.getResource("LengthAnalysisSegmentation.fxml"));
			AnchorPane anchorPane = loader.load();
			LengthAnalysisSegmentationController lengthAnalysisSegmentationController = loader.<LengthAnalysisSegmentationController>getController();
			lengthAnalysisSegmentationController.setAnchorPane(anchorPane);
			return lengthAnalysisSegmentationController;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 *
	 * @param clientConfiguration
	 * @param parent
	 * @param unmodifiedSegmentLengths
	 * @param modifiedSegmentLengths
	 * @return  anchorPane
	 */
	public AnchorPane loadView(
			ClientConfiguration clientConfiguration,
			LengthAnalysisRootController parent,
			SortedMap<Integer, Integer> unmodifiedSegmentLengths,
			SortedMap<Integer, Integer> modifiedSegmentLengths
			) {
		this.clientConfiguration = clientConfiguration;
		this.parent = parent;
		this.unmodifiedSegmentLengths = unmodifiedSegmentLengths;
		this.modifiedSegmentLengths = modifiedSegmentLengths;
		testForReady();

		return anchorPane;
	}

	public void initialize() {
		isInitialized = true;
		testForReady();
	}

	@FXML
	public void redraw() {
		if (modifiedSegmentLengths == null) {
			return;
		}
		// Start Computing
		Platform.runLater(() -> {
			long from = 0;
			if (tfFrom.getText().equals("")) {
				if (modifiedSegmentLengths.firstKey() < unmodifiedSegmentLengths.firstKey()) {
					from = modifiedSegmentLengths.firstKey();
				} else {
					from = unmodifiedSegmentLengths.firstKey();
				}
			} else {
				from = Integer.parseInt(tfFrom.getText());
			}
			long to = 1000000;
			if (tfTo.getText().equals("")) {
				if (modifiedSegmentLengths.lastKey() > unmodifiedSegmentLengths.lastKey()) {
					to = modifiedSegmentLengths.lastKey();
				} else {
					to = unmodifiedSegmentLengths.lastKey();
				}
			} else {
				to = Integer.parseInt(tfTo.getText());
			}
			// Calc Blockstarts
			long[] blockstarts = ChartUtilities.createBins(from, to,
					xAxisBlockCount,
					cbXAxisLogScale.isSelected());

			int maxValue = 0;

			// Series 1
			XYChart.Series<String, Integer> series1 = new XYChart.Series<>();
			List<XYChart.Data<String, Integer>> chartData = ChartUtilities.createChartDataforBins(modifiedSegmentLengths,
					blockstarts,
					from,
					to);
			for (XYChart.Data<String, Integer> a : chartData) {
				series1.getData().add(a);
				if (a.getYValue() > maxValue) {
					maxValue = a.getYValue();
				}
			}

			// Series 2
			XYChart.Series<String, Integer> series2 = new XYChart.Series<>();
			chartData = ChartUtilities.createChartDataforBins(unmodifiedSegmentLengths,
					blockstarts,
					from,
					to);

			for (XYChart.Data<String, Integer> a : chartData) {
				series2.getData().add(a);
				if (a.getYValue() > maxValue) {
					maxValue = a.getYValue();
				}
			}

			// Maximum as final
			final int finalMax = maxValue;

			// log.info("DONE adding Data(" + series1.getData().size() +
			// ")");
			Platform.runLater(() -> {
				createChart((finalMax));
				chart.getData().clear();
				chart.getData().add(series1);
				chart.getData().add(series2);
				for (Series<String, Integer> serie : chart.getData()) {
					for (XYChart.Data<String, Integer> item : serie.getData()) {
						Tooltip tooltip = ChartUtilities.createTooltip(item);
						Tooltip.install(item.getNode(), tooltip);
						item.getNode().setOnMousePressed((MouseEvent event) -> {
							if (event.getClickCount() > 1) {
								if (item.getXValue().contains("-")) {
									String[] temp = item.getXValue().split("-");
									if (Integer.parseInt(temp[1]) - Integer.parseInt(temp[0]) < DEFAULT_BLOCK_COUNT) {
										int target = Integer.parseInt(temp[0]) + (int) Math.round(
												(Math.round(Integer.parseInt(temp[1]) - Integer.parseInt(temp[0])))
												/ 2.0);
										Platform.runLater(() -> {
											setlocalRange((target - 50), (target + 49));
										});
									} else {
										Platform.runLater(() -> {
											setlocalRange(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]));
										});
									}
								} else {
									int target = Integer.parseInt(item.getXValue());
									Platform.runLater(() -> {
										setlocalRange((target - 50), (target + 49));
									});
								}
							}
						});
					}
				}
			});

		});
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void createChart(double maxValue) {
		// System.out.println("createChart");
		this.chartPane.getChildren().clear();
		CategoryAxis xAxis = new CategoryAxis();
		if (maxValue <= 0) {
			chart = new BarChart(xAxis, new NumberAxis());
			chart.setTitle("No data available");
			chart.setTitleSide(Side.TOP);
		} else if (this.cbYAxisLogScale.isSelected()) {
			chart = new BarChart(xAxis, new LogarithmicAxis(0.4, maxValue, true));
			chart.getYAxis().setLabel("occurrence (log)");
		} else {
			AxisLabeling x = AxisLabeling.base10();
			x.loose= false;
			Label lab = x.search(0.0, maxValue, 10);
			double max = lab.getMax();
			if(lab.getMax() <= maxValue)
			{
				max += lab.getStep();
			}
			chart = new BarChart(xAxis, new NumberAxis(0.0, max, lab.getStep()));
			chart.getYAxis().setLabel("occurrence (lin)");
		}
		chart.getXAxis().setAnimated(false);
		if (this.cbXAxisLogScale.isSelected()) {
			chart.getXAxis().setLabel("lengths (log)");
		} else {
			chart.getXAxis().setLabel("lengths (lin)");
		}
		chart.getStylesheets().clear();
		chart.getStylesheets().add(getClass().getResource("histogram.css").toExternalForm());
		chart.setLegendVisible(false);
		chart.getYAxis().setAnimated(false);
		chart.setCategoryGap(0);
		chart.setBarGap(1);
		chart.setAnimated(false);
		MenuItem cmItem1 = new MenuItem("Save as image");
		cmItem1.setOnAction((ActionEvent e) -> {
			ImageExport.exportPaneToPng(chartPane, null);
		});
		final ContextMenu cm = new ContextMenu(cmItem1);
		chartPane.setOnMouseClicked((event) -> {
			if (event.getButton() == MouseButton.SECONDARY) {
				cm.show(chartPane, event.getScreenX(), event.getScreenY());
			}
		});
		AnchorPane.setBottomAnchor(chart, 0.0);
		AnchorPane.setTopAnchor(chart, 50.0);
		AnchorPane.setLeftAnchor(chart, 0.0);
		AnchorPane.setRightAnchor(chart, 0.0);
		this.chartPane.getChildren().add(chart);
	}

	private void init() {
		cbXAxisLogScale.selectedProperty().addListener((ov) -> {
			if (ignore) {
				return;
			}
			setLocalXAxisLogScale();
		});
		cbYAxisLogScale.selectedProperty().addListener((ov) -> {
			if (ignore) {
				return;
			}
			setLocalYAxisLogScale();
		});
	}

	@Override
	public void setXAxisLogScale(boolean enable) {
		ignore = true;
		this.cbXAxisLogScale.setSelected(enable);
		ignore = false;
		redraw();
	}

	@Override
	public void setYAxisLogScale(boolean enable) {
		ignore = true;
		this.cbYAxisLogScale.setSelected(enable);
		ignore = false;
		redraw();
	}

	private void setLocalXAxisLogScale() {
		redraw();
	}

	private void setLocalYAxisLogScale() {
		redraw();
	}

	@Override
	public void setRange(int from, int to) {
		this.tfFrom.setText((from == -1) ? "" : from + "");
		this.tfTo.setText((to == -1) ? "" : to + "");
		btnReset.setDisable(false);
		redraw();
	}

	@FXML
	private void updateLocalRange() {
		int from = -1;
		try {
			from = Integer.parseInt(tfFrom.getText());
		} catch (NumberFormatException e) {
			from = -1;
		}
		int to = -1;
		try {
			to = Integer.parseInt(tfTo.getText());
		} catch (NumberFormatException e) {
			to = -1;
		}
		setlocalRange(from, to);
	}

	private void setlocalRange(int from, int to) {
		this.tfFrom.setText((from == -1) ? "" : from + "");
		this.tfTo.setText((to == -1) ? "" : to + "");
		btnReset.setDisable(false);
		redraw();
	}

	@Override
	public void resetRange() {
		tfFrom.setText("");
		tfTo.setText("");
		redraw();
	}

	@FXML
	private void resetLocalRange() {
		if (modifiedSegmentLengths == null || modifiedSegmentLengths.isEmpty()) {
			return;
		}

		Platform.runLater(() -> {
			tfFrom.setText("");
			tfTo.setText("");
			redraw();
		});
	}

	private void testForReady() {
		if (clientConfiguration != null
				&& isInitialized == true) {
			this.init();
		}
	}

	@Override
	public void initView() {
		redraw();
	}

	public void saveChartToFile(String path) {
		ImageExport.exportPaneToPng(chartPane, path);
	}
}
