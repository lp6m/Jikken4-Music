package com.dokodeglobal.nittax.le4music;
import com.dokodeglobal.nittax.le4music.analyzer.*;
import java.lang.Thread;
import java.io.File;
import java.net.URL; 
import java.util.stream.IntStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.*;
import javax.sound.sampled.AudioInputStream;
import java.io.BufferedInputStream; 
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.Plot;
import java.io.IOException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.chart.*;

import javafx.scene.*;
import javafx.scene.Scene;
import javafx.scene.chart.*;
public class Ex1 extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("FXMLサンプル");
        AnchorPane root = FXMLLoader.load(getClass().getResource("GUI2.fxml"));
        Scene scene = new Scene(root);      
        stage.setScene(scene);
        stage.show();
		
	}
 
    public static void main(String... args){
        launch(args);
    }


	@FXML
	private Pane chart1pane;
	
    @FXML
    public void showChart(ActionEvent event) {
		try{
			URL audio_path = getClass().getResource("aiueo-danzoku.wav");
			ClassLoader audio_class_loader = javax.sound.sampled.AudioSystem.class.getClassLoader();
			ClassLoader now_context_class_loader = Thread.currentThread().getContextClassLoader();
			AudioInputStream stream = null;
			try {
				Thread.currentThread().setContextClassLoader(audio_class_loader);
				stream = AudioSystem.getAudioInputStream(audio_path);
			} finally{
				Thread.currentThread().setContextClassLoader(now_context_class_loader);
			}
			
			System.out.println(stream);
			final double[] waveform = Le4MusicUtils.readWaveformMonaural(stream);
			System.out.println(waveform.length);//秒数*16kHzの長さ
			final AudioFormat format = stream.getFormat();
			final double sampleRate = format.getSampleRate();
			stream.close();
			
			Node chart = PlotWaveform.createLineChart(waveform, sampleRate);
			
			chart1pane.getChildren().add(chart);
		    
		}catch(Exception e){
			e.printStackTrace();
		}
        
    }
    
}
