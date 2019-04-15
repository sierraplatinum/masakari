module masakari {
	opens biovis.hackebeil.client;
	opens biovis.hackebeil.client.gui;
	opens biovis.hackebeil.client.gui.dialog;
	opens biovis.hackebeil.client.gui.output;
	opens biovis.hackebeil.client.gui.progress;
	opens biovis.hackebeil.client.gui.output.correlation;
	opens biovis.hackebeil.client.gui.output.pwm;
	opens biovis.hackebeil.client.gui.output.motif;
	opens biovis.hackebeil.client.gui.output.fateOfCode;
	opens biovis.hackebeil.client.gui.output.additionalData;
	opens biovis.hackebeil.client.gui.output.breakSegmentAnalysis;
	opens biovis.hackebeil.client.gui.output.common;
	opens biovis.hackebeil.client.gui.output.pairsAnalysis;
	opens biovis.hackebeil.client.gui.output.lengthAnalysis;
	opens biovis.hackebeil.client.gui.output.overview;
	opens biovis.hackebeil.client.gui.output.segmentation;
	
	requires biovislib;
	requires gson;
	requires java.desktop;
	requires java.logging;
	requires java.prefs;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires javafx.swing;
	requires commons.vfs;
	requires commons.io;
}