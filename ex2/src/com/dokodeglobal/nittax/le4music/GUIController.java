package com.dokodeglobal.nittax.le4music;
import java.io.*;
import java.net.URL;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.control.ChoiceBox;
import javafx.fxml.Initializable;
import java.util.*;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.scene.control.ProgressBar;

public class GUIController implements Initializable{
	@FXML ChoiceBox mixerlist;
	@FXML Button startbutton, stopbutton, resetbutton;
	@FXML Slider musicSlider;
	@FXML ProgressBar volumeprogressbar;
	@FXML AnchorPane notepane;
	@FXML AnchorPane music_spectrogram_pane, microphone_spectrum_pane;
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
		KaraokeSystem.microphone_spectrum_pane = this.microphone_spectrum_pane;
		KaraokeSystem.music_spectrogram_pane = this.music_spectrogram_pane;
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

	public void updateTimeLabelText(long time){
		long minute = time / 1000 / 60;
		long sec = (time / 1000) % 60;
		//int milisec = time % 1000
		Platform.runLater( () -> timelabel.setText(String.format("%02d",(int)minute)+":"+String.format("%02d",(int)sec)));
	}

	public void updateVolumeProgressBar(int val){
        Platform.runLater( () -> volumeprogressbar.setProgress((double)val / 100.0));
    }
}
