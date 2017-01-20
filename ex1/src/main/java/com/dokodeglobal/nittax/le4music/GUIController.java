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
    
	@FXML
	private Pane waveformpane;
	@FXML
	private Pane spectrogrampane;
	@FXML
	private Pane volumeformpane;
	@FXML
	private Pane spectrumpane;
	@FXML
	private Pane subharmonicsummationpane;
	@FXML
	private ChoiceBox mixerlist;
	@FXML
	private Button filechoose_button;
	@FXML
	private Button realtime_analyze_button;
	@FXML
	private Label volumelabel;
	
	Boolean now_realtime_analyze = false; //現在リアルタイム解析中かどうか
	private Clip activeClip;
	ScheduledExecutorService executor;
	Recorder recorder;
	WaveformChartNode waveformChartNode;
	SpectrumChartNode spectrumChartNode;
	SubharmonicSummationChartNode subharmonicsummationChartNode;
    @Override
    public void initialize(URL location, ResourceBundle resources){
		waveformChartNode = new WaveformChartNode();
		waveformpane.getChildren().add(waveformChartNode);
		spectrumChartNode = new SpectrumChartNode();
		spectrumpane.getChildren().add(spectrumChartNode);
		subharmonicsummationChartNode = new SubharmonicSummationChartNode();
		subharmonicsummationpane.getChildren().add(subharmonicsummationChartNode);
		
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
			mixerlist.getSelectionModel().selectFirst();
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
	    if(now_realtime_analyze == false){
			/*現在選択中のMixerを使用して録音を開始する*/
			ClassLoader audio_class_loader = javax.sound.sampled.AudioSystem.class.getClassLoader();
			ClassLoader now_context_class_loader = Thread.currentThread().getContextClassLoader();
			AudioInputStream stream = null;
			try {
				Thread.currentThread().setContextClassLoader(audio_class_loader);
				recorder = Recorder.newRecorder(16000.0D, 0.4D, getNowSelectMixer() ,null);
				recorder.start();
				now_realtime_analyze = true;
				executor = Executors.newSingleThreadScheduledExecutor();
				executor.scheduleWithFixedDelay(
												() -> {
													final double[] frame = recorder.latestFrame();
													final double sampleRate = recorder.getSampleRate();
													final double rms = Arrays.stream(frame).map(x -> x * x).average().orElse(0.0);
													final double logRms = 20.0 * Math.log10(rms);
													//System.out.printf("RMS %f dB%n", logRms);
													UpdateChartData(frame, sampleRate);
												},
												0L, 20L, TimeUnit.MILLISECONDS
												);
				realtime_analyze_button.setText("Stop");
				filechoose_button.setDisable(true);
				mixerlist.setDisable(true);
			}catch(Exception e){
				System.out.println("Recorderの取得に失敗しました");
			}finally{
				Thread.currentThread().setContextClassLoader(now_context_class_loader);
			}
			
		}else{
			//録音を停止する
			recorder.stop();
			now_realtime_analyze = false;
			executor.shutdown();
			executor = null;
			realtime_analyze_button.setText("Realtime Analyze");
			filechoose_button.setDisable(false);
			mixerlist.setDisable(false);
		}
	}
	public void UpdateChartData(double[] frame, double sampleRate){
		double time = frame.length / sampleRate;
		for(int i = 0; i < frame.length; i++){
			//frame[i] = 1.0 * Math.sin(2 * Math.PI * time * i / 10);
		}
		waveformChartNode.UpdateDataSource(frame, sampleRate);
		spectrumChartNode.UpdateDataSource(frame, sampleRate);
		List<Double> rst = subharmonicsummationChartNode.UpdateDataSourceAndEstimateFreq(frame, sampleRate, volumelabel);
		System.out.println(Double.toString(rst.get(0))+"Hz : " + Double.toString(rst.get(1)));
		double threshold = 0.001;
		if(rst.get(1) > threshold) playSinWave(rst.get(0), 50);
		//		volumelabel.setText(Double.toString(freq) + "Hz");
	}
	public Mixer.Info getNowSelectMixer(){
		Mixer.Info rst = null;
		ClassLoader audio_class_loader = javax.sound.sampled.AudioSystem.class.getClassLoader();
		ClassLoader now_context_class_loader = Thread.currentThread().getContextClassLoader();
		AudioInputStream stream = null;
		try {
			Thread.currentThread().setContextClassLoader(audio_class_loader);
			Mixer.Info[] infoList = AudioSystem.getMixerInfo();
			rst = infoList[(int)mixerlist.getSelectionModel().getSelectedIndex()];
		} finally{
			Thread.currentThread().setContextClassLoader(now_context_class_loader);
		}
		return rst;
	}
	private void playSinWave(double freq, double miliseconds){
		try {
			if(activeClip != null) activeClip.close();
			double num = 44100 * miliseconds / 1000;
			System.out.println(num);
			byte[] data=new byte[(int)num];
			//sin波の作成：
			double a = a=2*Math.PI/(44100.0/freq);
			for(int i=0;i<data.length;i++){
				data[i]=(byte)(120*Math.sin(i*a));
			}
			AudioFormat frmt = new AudioFormat(44100,8,1,true,false);
			//1秒あたりのサンプル数は44100個、
			//音のビット数は8bit=256、
			//チャネル数は1、
			//値は正負型、
			//bigEndianはfalse=リトルエンディアン
			DataLine.Info info=new DataLine.Info(Clip.class,frmt);
			Clip clip=(Clip)AudioSystem.getLine(info);
			clip.open(frmt,data,0,data.length);

			//再生を開始する
			clip.start();
			activeClip = clip;
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/*時間変化するPaneの要素を初期化*/
	private void InitializePane(){
		volumeformpane.getChildren().clear();
		spectrogrampane.getChildren().clear();
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
			
			waveformChartNode.UpdateDataSource(waveform, sampleRate);
			spectrumChartNode.UpdateDataSource(waveform, sampleRate);
			SwingNode volumenode = PlotVolumeform.createPlotVolumeform(waveform, sampleRate,Le4MusicUtils.frameDuration, Le4MusicUtils.shiftDuration);
			volumeformpane.getChildren().add(volumenode);
			SwingNode spectrogramnode = PlotSpectrogram.createSpectrogramChart(waveform, sampleRate, Le4MusicUtils.frameDuration, Le4MusicUtils.shiftDuration);
			spectrogrampane.getChildren().add(spectrogramnode);
			
		}catch(Exception e){
			e.printStackTrace();
		}
        
    }
    
}
