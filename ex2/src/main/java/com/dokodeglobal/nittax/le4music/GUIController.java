package com.dokodeglobal.nittax.le4music;
import com.dokodeglobal.nittax.le4music.analyzer.*;
import com.dokodeglobal.nittax.le4music.myutils.*;
import com.dokodeglobal.nittax.le4music.midiutil.*;
import java.lang.Thread;
import java.io.*;
import java.net.URL; 
import java.util.stream.IntStream;
import javafx.application.Platform;
import javax.sound.sampled.UnsupportedAudioFileException;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.scene.chart.*;
import javafx.stage.FileChooser.ExtensionFilter;
import org.jfree.chart.JFreeChart;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.fxml.Initializable;
import javax.sound.sampled.Mixer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.*;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javax.sound.sampled.*;
import javax.sound.midi.*;

public class GUIController implements Initializable{
	@FXML ChoiceBox mixerlist;
	@FXML Button startbutton, stopbutton, resetbutton;
	@FXML Slider musicSlider;
	@FXML AnchorPane notepane;
	@FXML Label timelabel;
	@FXML
	void OnAudioFileOpenButtonPressed(){
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Audio File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Audio Files", "*.wav"));
		Stage stage = new Stage();
		File audioFile = fileChooser.showOpenDialog(stage);
		if(audioFile != null){
			KaraokeSystem.setaudioFile(audioFile);
		}
	}
	
	@FXML
	void OnMidiFileOpenButtonPressed(){
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open MIDI File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("MIDI CSV Files", "*.csv"));
		Stage stage = new Stage();
		File midiFile = fileChooser.showOpenDialog(stage);
		if(midiFile != null){
			KaraokeSystem.setMidiFile(midiFile);
		}
	}
	
    @Override
    public void initialize(URL location, ResourceBundle resources){
		KaraokeSystem.notepane = this.notepane;
		//KaraokeSystem.timelabel = this.timelabel;
		KaraokeSystem.guicontroller = this;
	}

	@FXML
	public void OnStartButtonPressed() throws Exception{
		if(KaraokeSystem.nowplaying == false && KaraokeSystem.canStart()){
			KaraokeSystem.nowplaying = true;
			KaraokeSystem.start();
		}
	}
	/*
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
	*/
	public void updateTimeLabelText(long time){
		long minute = time / 1000 / 60;
		long sec = (time / 1000) % 60;
		//int milisec = time % 1000
		Platform.runLater( () -> timelabel.setText(String.format("%02d",(int)minute)+":"+String.format("%02d",(int)sec)));
	}
}
