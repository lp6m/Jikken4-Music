package com.dokodeglobal.nittax.le4music;
import com.dokodeglobal.nittax.le4music.midiutil.NoteData;
import com.dokodeglobal.nittax.le4music.myutils.NoteNameUtil;
import com.dokodeglobal.nittax.le4music.viewcomponent.NoteBox;

import java.lang.Thread;
import java.io.*;
import java.util.*;
import javafx.scene.layout.AnchorPane;
import javax.sound.sampled.AudioSystem;
import com.dokodeglobal.nittax.le4music.myutils.Recorder;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import com.dokodeglobal.nittax.le4music.analyzer.SpectrumChartNode;
import com.dokodeglobal.nittax.le4music.analyzer.CalcSubharmonicSumation;
import javafx.scene.shape.*;
import jp.ac.kyoto_u.kuis.le4music.Player;

public class KaraokeSystem{
	static public Boolean nowplaying = false;
	static private File audioFile,midiFile;
	static public Player musicplayer;           /*BGM*/
	static public List<NoteData> notelist;      /*MidiCSVからよみこんだNoteDataのリスト*/
	static public List<NoteBox> noteboxlist;    /*表示するノーツのBoxリスト*/
    static public AnchorPane notepane;          /*ノーツを表示するPane*/
    static public AnchorPane music_spectrogram_pane, microphone_spectrum_pane;
    static private SpectrumChartNode spectrumChartNode;
	static public Line seekbar;                 /*Pane上で現在の位置を表すための棒,シークバー*/
	static public GUIController guicontroller;  /*JavaFXのGUIControllerのクラスのインスタンスへの参照を保持しておく*/
	static public int zurashi;                   /*ノーツのスクロール用.何pixelずらしたか*/
	/*1msを何pixelで表すか*/
	static public double pixel_per_ms = 24.0 / 188.0;
    static ScheduledExecutorService executor;
	static private Recorder recorder;
	static private KaraokeThread karaokethread; /*リアルタイム処理をするためのリスト*/
	static public Integer notecounter;          /*何個目まで進んだか*/
	//static public long starttime;
	//static private long nowtime;
	//static private int timercnt;
	//static private ScheduledExecutorService executor;
	static public void setaudioFile(File audioFile){
		KaraokeSystem.audioFile = audioFile;
	}
	static public void setMidiFile(File midiFile){
		KaraokeSystem.midiFile = midiFile;
		openMidiFile();
		drawMidiNotes();
	}
	static private Boolean openMidiFile(){
		Boolean ok = true;
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(midiFile);
			br = new BufferedReader(fr);
			notelist = new ArrayList<NoteData>();
			int lowest_notenumber = 999;
			int highest_notenumber = -1;
			String line;
			while ((line = br.readLine()) != null) {
				String[] splitted = line.split(",");
				if(splitted.length < 3) throw new IllegalArgumentException();
				NoteData note = new NoteData();
				note.time = Long.parseLong(splitted[0]);
				note.notenumber = Integer.parseInt(splitted[1]);
				note.duration = Integer.parseInt(splitted[2]);
				notelist.add(note);
				lowest_notenumber = Math.min(lowest_notenumber, note.notenumber);
				highest_notenumber = Math.max(highest_notenumber, note.notenumber);
			}
			NoteBox.lowest_notenumber = lowest_notenumber;
			NoteBox.highest_notenumber = highest_notenumber;
		}catch(Exception e) {
			ok = false;
			e.printStackTrace();
		}finally{
			try {
				br.close();
				fr.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		return ok;
	}
	
	static private void drawMidiNotes(){
		noteboxlist = new ArrayList<NoteBox>();
		for(int i = 0; i < notelist.size(); i++){
			NoteData d = notelist.get(i);
			NoteBox t = new NoteBox(d.notenumber, d.time, d.duration);
			noteboxlist.add(t);
			KaraokeSystem.notepane.getChildren().add(t);
		}
		
		seekbar = new Line();
		seekbar.setStartX(0.0f);
		seekbar.setStartY(0.0f);
		seekbar.setEndX(0.0f);
		seekbar.setEndY(notepane.getHeight());
		KaraokeSystem.notepane.getChildren().add(seekbar);
	}

	/*スタート可能かどうかをかえす*/
	static public Boolean canStart(){
		if(audioFile != null && midiFile != null) return true;
		else return false;
	}

	static public void start(){
		//starttime = System.nanoTime();
		/*timercnt = 0;
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleWithFixedDelay(
										() -> {
											//1ミリ秒ごとに実行されるコードをかく
											//1ミリ秒ごとにカウンタを増加させると誤差がひどくなるのでSystem.nanoTimeを利用する
										},
										0L, 1L, TimeUnit.MILLISECONDS
										);*/

        try {
            musicplayer = Player.newPlayer(audioFile);
            musicplayer.start();
        }catch(Exception e){
            e.printStackTrace();
        }
		notecounter = 0;
		zurashi = 0;
		karaokethread = new KaraokeThread();
		karaokethread.start();
        spectrumChartNode = new SpectrumChartNode();
        microphone_spectrum_pane.getChildren().add(spectrumChartNode);
		try {
            recorder = Recorder.newRecorder(16000.0D, 0.4D, AudioSystem.getMixerInfo()[0], null);
            recorder.start();
            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleWithFixedDelay(
                    () -> {
                        final double[] frame = recorder.latestFrame();
                        final double sampleRate = recorder.getSampleRate();
                        final double rms = Arrays.stream(frame).map(x -> x * x).average().orElse(0.0);
                        final double logRms = 20.0 * Math.log10(rms);
                        //-130 ~ 0まで
                        int volval = Math.min(Math.max(((int)logRms + 130) * 100 / 130, 0),100);
                        guicontroller.updateVolumeProgressBar(volval);
                        UpdateChartData(frame, sampleRate);

                        double estimatefreq = CalcSubharmonicSumation.estimateFreq(frame, sampleRate);
                        guicontroller.updateStatusLabels(
                                Integer.toString(karaokethread.nownotenumber),
                                NoteNameUtil.convertNoteNumtoNoteName(karaokethread.nownotenumber),
                                (estimatefreq < 0 ? "-" : Double.toString(estimatefreq) + "Hz"),
                                (estimatefreq < 0 ? "-" : NoteNameUtil.convertFreqtoNoteName(estimatefreq)));
                    },
                    0L, 100L, TimeUnit.MILLISECONDS
            );
        }catch(Exception e){
		    e.printStackTrace();
        }

        KaraokeSystem.nowplaying = true;
    }
	static public void stop(){
		if(KaraokeSystem.nowplaying != true) return;
		karaokethread.stopThread();
	}
	static public void reset(){
	}

	static private void UpdateChartData(double[] frame,double sampleRate){
        spectrumChartNode.UpdateDataSource(frame, sampleRate);
    }
	
}
