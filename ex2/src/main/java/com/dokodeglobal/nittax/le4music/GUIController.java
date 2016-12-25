package com.dokodeglobal.nittax.le4music;
import com.dokodeglobal.nittax.le4music.analyzer.*;
import com.dokodeglobal.nittax.le4music.myutils.*;
import com.dokodeglobal.nittax.le4music.midiutil.*;
import java.lang.Thread;
import java.io.File;
import java.io.IOException;
import java.io.BufferedInputStream; 
import java.net.URL; 
import java.util.stream.IntStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.Plot;

import javax.sound.sampled.UnsupportedAudioFileException;
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
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.jfree.chart.JFreeChart;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javax.swing.JButton;
import javax.swing.SwingUtilities; 
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javax.sound.sampled.Mixer;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.*;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javax.sound.sampled.*;
import java.io.*;
import javax.sound.midi.*;

public class GUIController implements Initializable{
	@FXML ChoiceBox mixerlist;
	@FXML Button startbutton, stopbutton, resetbutton;
	@FXML Slider musicSlider;
	@FXML
	void OnAudioFileOpenButtonPressed(){
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Audio File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Audio Files", "*.wav"));
		Stage stage = new Stage();
		File audioFile = fileChooser.showOpenDialog(stage);
		if(audioFile != null){
		}
	}
	
	@FXML
	void OnMidiFileOpenButtonPressed(){
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open MIDI File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("MIDI Files", "*.mid"));
		Stage stage = new Stage();
		File midiFile = fileChooser.showOpenDialog(stage);
		if(midiFile != null){
		}
	}
	
    @Override
    public void initialize(URL location, ResourceBundle resources){
	}

	@FXML
	public void OnStartButtonPressed() throws Exception{
		ClassLoader midi_class_loader = javax.sound.midi.MidiSystem.class.getClassLoader();
		ClassLoader now_context_class_loader = Thread.currentThread().getContextClassLoader();

		try {
			Thread.currentThread().setContextClassLoader(midi_class_loader);
			Receiver receiver = MidiSystem.getReceiver();
			ShortMessage message = new ShortMessage();

			message.setMessage(ShortMessage.NOTE_ON, 60, 127);
			receiver.send(message, -1);
		} finally{
			Thread.currentThread().setContextClassLoader(now_context_class_loader);
		}
	}
	
}
