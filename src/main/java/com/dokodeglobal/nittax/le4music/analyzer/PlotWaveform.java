package com.dokodeglobal.nittax.le4music.analyzer;

import java.io.File;
import java.util.stream.IntStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.Plot;
import java.io.IOException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.*;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.collections.*; 
public class PlotWaveform {
	/*各サンプルの時刻を求める*/
	static private double[] calcTimeforEachFrame(double[] waveform, double sampleRate){
		return IntStream.range(0, waveform.length).mapToDouble(i -> i / sampleRate).toArray();
	}
	
    @SuppressWarnings("unchecked")
	static public LineChart createLineChart(final double[] waveform,final double sampleRate){
		NumberAxis xAxis = new NumberAxis();
		NumberAxis yAxis = new NumberAxis();
		xAxis.setLowerBound(0.0);
		xAxis.setUpperBound((waveform.length - 1) / sampleRate);
		xAxis.setLabel("Time [sec.]");
		yAxis.setLowerBound(-1.0);
		yAxis.setUpperBound(1.0);
		yAxis.setLabel("Amplitude");
 
		double[] times = calcTimeforEachFrame(waveform, sampleRate);
		LineChart<Number, Number> chart = new LineChart<Number, Number>(xAxis, yAxis);
		ObservableList<XYChart.Data<Number, Number>> data = FXCollections.<XYChart.Data<Number, Number>>observableArrayList();
		for (int i = 0; i < waveform.length; i++) data.add(new XYChart.Data<>(times[i],waveform[i]));
		Series series1 = new Series("WaveForm",data);
		chart.getData().add(series1);
	    chart.setCreateSymbols(false);                                                      // シンボルを消去
        series1.getNode().lookup(".chart-series-line").setStyle("-fx-stroke-width: 1px;");  // 線を細く
         
        return chart;
    }
	/*
	public static final void main(final String[] args)
		throws IOException, UnsupportedAudioFileException { if (args.length == 0) {
			System.out.println("no input files");
			return;
		}
		final File wavFile = new File(args[0]);
		
		// 音 響 信 号 読 み 込 み 
		final AudioInputStream stream = AudioSystem.getAudioInputStream(wavFile);
		final double[] waveform = Le4MusicUtils.readWaveformMonaural(stream);
		for(int i = 0; i < waveform.length; i++){
			System.out.println(String.format("%.2f",waveform[i]));
		}
		System.out.println(waveform.length);//秒数*16kHzの長さ
		final AudioFormat format = stream.getFormat();
		final double sampleRate = format.getSampleRate();
		System.out.println(sampleRate);
		stream.close();
		plotWaveform(waveform, sampleRate);
	}*/
}
