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
public class GUIController implements Initializable{
    
	@FXML
	private Pane waveformpane;
	@FXML
	private Pane spectrogrampane;
	@FXML
	private Pane volumeformpane;
	@FXML
	private Pane spectrumpane;
	@FXML
	private ChoiceBox mixerlist;

	Recorder recorder;
	
    @Override
    public void initialize(URL location, ResourceBundle resources){
		try{
			recorder = Recorder.newRecorder();
		}catch(Exception e){
		}
		mixerlist.setItems(FXCollections.observableArrayList());
		mixerlist.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
				@Override public void changed(ObservableValue<? extends String> selected, String oldParam, String newParam) {
					System.out.println(newParam);
				}
			});
		ClassLoader audio_class_loader = javax.sound.sampled.AudioSystem.class.getClassLoader();
		ClassLoader now_context_class_loader = Thread.currentThread().getContextClassLoader();
		AudioInputStream stream = null;
		try {
			Thread.currentThread().setContextClassLoader(audio_class_loader);
			Mixer.Info[] infoList = AudioSystem.getMixerInfo(); 
			for(int i = 0; i < infoList.length; i++){
				mixerlist.getItems().add(infoList[i].getName());
			}
		} finally{
			Thread.currentThread().setContextClassLoader(now_context_class_loader);
		}
    }

	/*ファイル選択ボタンを呼ばれた時に呼び出される*/
	@FXML
	public void OnFileOpenButtonPressed(ActionEvent event){
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Audio Files", "*.wav"));
		Stage stage = new Stage();
		File audioFile = fileChooser.showOpenDialog(stage);
		if (audioFile != null) {
			showChart(audioFile);
		}
	}
	@FXML
	public void OnRealTimeAnalyzeButtonPressed(ActionEvent event){
		recorder.start();
		ClassLoader audio_class_loader = javax.sound.sampled.AudioSystem.class.getClassLoader();
		ClassLoader now_context_class_loader = Thread.currentThread().getContextClassLoader();
		AudioInputStream stream = null;
		try {
			Thread.currentThread().setContextClassLoader(audio_class_loader);
			recorder.start();
		} finally{
			Thread.currentThread().setContextClassLoader(now_context_class_loader);
		}
		final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleWithFixedDelay(
										() -> {
											final double[] frame = recorder.latestFrame();
											final double rms = Arrays.stream(frame).map(x -> x * x).average().orElse(0.0);
											final double logRms = 20.0 * Math.log10(rms);
											System.out.printf("RMS %f dB%n", logRms);
										},
										0L, 100L, TimeUnit.MILLISECONDS
										);
	}
	/*各タブの要素を初期化*/
	private void InitializePane(){
		waveformpane.getChildren().clear();
		volumeformpane.getChildren().clear();
		spectrumpane.getChildren().clear();
		spectrogrampane.getChildren().clear();
		mixerlist.getItems().addAll("item1", "item2", "item3");
		
	}
	/*各タブにプロット結果を表示*/
    private void showChart(File audioFile) {
		InitializePane();
		/*sbtではContextClassLoaderというClassLoaderを使用しているが,AudioInputStreamはこれに対応していないため,
		  一度AudioInputStreamのClassLoaderに書き換えて音声ファイルをロードし,その後ContextClassLoaderに書き戻す*/
		/*参考:http://stackoverflow.com/questions/31727385/sbt-scala-audiosystem*/
		try{
			//URL audio_path = getClass().getResource("aiueo-danzoku.wav");
			ClassLoader audio_class_loader = javax.sound.sampled.AudioSystem.class.getClassLoader();
			ClassLoader now_context_class_loader = Thread.currentThread().getContextClassLoader();
			AudioInputStream stream = null;
			try {
				Thread.currentThread().setContextClassLoader(audio_class_loader);
				//stream = AudioSystem.getAudioInputStream(audio_path);
			    stream = AudioSystem.getAudioInputStream(audioFile);
			} finally{
				Thread.currentThread().setContextClassLoader(now_context_class_loader);
			}
			final double[] waveform = Le4MusicUtils.readWaveformMonaural(stream);
			final AudioFormat format = stream.getFormat();
			final double sampleRate = format.getSampleRate();
			stream.close();
			
			SwingNode swingnode = PlotWaveform.createWaveformChart(waveform, sampleRate);
			waveformpane.getChildren().add(swingnode);
			SwingNode volumenode = PlotVolumeform.createPlotVolumeform(waveform, sampleRate,Le4MusicUtils.frameDuration, Le4MusicUtils.shiftDuration);
			volumeformpane.getChildren().add(volumenode);
			SwingNode spectrumnode = PlotSpectrum.createSpectrumchart(waveform, sampleRate);
			spectrumpane.getChildren().add(spectrumnode);
			SwingNode spectrogramnode = PlotSpectrogram.createSpectrogramChart(waveform, sampleRate, Le4MusicUtils.frameDuration, Le4MusicUtils.shiftDuration);
			spectrogrampane.getChildren().add(spectrogramnode);
		}catch(Exception e){
			e.printStackTrace();
		}
        
    }
    
}
