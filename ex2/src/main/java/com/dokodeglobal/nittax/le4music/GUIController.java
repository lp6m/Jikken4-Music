package com.dokodeglobal.nittax.le4music;
import com.dokodeglobal.nittax.le4music.analyzer.*;
import com.dokodeglobal.nittax.le4music.myutils.*;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javax.sound.sampled.*;
public class GUIController implements Initializable{
	@FXML ChoiceBox mixerlist;
	@FXML Button startbutton, stopbutton, resetbutton;

	@FXML
	void OnAudioFileOpenButtonPressed(){
	}
	@FXML
	void OnMidiFileOpenButtonPressed(){
	}
	
    @Override
    public void initialize(URL location, ResourceBundle resources){
	}
	
}
