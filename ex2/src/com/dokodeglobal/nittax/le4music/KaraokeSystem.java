package com.dokodeglobal.nittax.le4music;
import com.dokodeglobal.nittax.le4music.midiutil.NoteData;
import com.dokodeglobal.nittax.le4music.myutils.NoteNameUtil;
import com.dokodeglobal.nittax.le4music.viewcomponent.NoteBox;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import javafx.scene.paint.Color;
import java.io.*;
import java.util.*;
import javafx.scene.layout.AnchorPane;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import com.dokodeglobal.nittax.le4music.analyzer.SpectrumChartNode;
import com.dokodeglobal.nittax.le4music.analyzer.CalcSubharmonicSumation;
import com.dokodeglobal.nittax.le4music.analyzer.RealTimeSpectrogramChartNode;
import javafx.scene.shape.*;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.Player;
import jp.ac.kyoto_u.kuis.le4music.Recorder;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.MathArrays;

public class KaraokeSystem{
	static public Boolean nowplaying = false;
	static private File audioFile,midiFile, testFile;
	static public Player musicplayer, testFilePlayer;           /*BGM*/
	static public List<NoteData> notelist;      /*MidiCSVからよみこんだNoteDataのリスト*/
	static public List<NoteBox> noteboxlist;    /*表示するノーツのBoxリスト*/
    static public List<NoteBox> scorenotelist;
    static public List<int[]> ScoreOfNote;
    static public AnchorPane notepane;          /*ノーツを表示するPane*/
    static public AnchorPane music_spectrogram_pane, microphone_spectrum_pane;
    static private SpectrumChartNode spectrumChartNode;
    static private RealTimeSpectrogramChartNode realtime_music_spectrogramChartNode;
	static public Line seekbar;                 /*Pane上で現在の位置を表すための棒,シークバー*/
	static public GUIController guicontroller;  /*JavaFXのGUIControllerのクラスのインスタンスへの参照を保持しておく*/
	static public int zurashi;                   /*ノーツのスクロール用.何pixelずらしたか*/
	/*1msを何pixelで表すか*/
	static public double pixel_per_ms = 24.0 / 188.0;
    static ScheduledExecutorService executor;
	static private Recorder recorder;
	static private ScheduledExecutorService spectrogram_executor;
	static private KaraokeThread karaokethread; /*リアルタイム処理をするためのリスト*/
	static public Integer notecounter;          /*何個目まで進んだか*/
    static public double nowscore;
	static public void setaudioFile(File audioFile){
		KaraokeSystem.audioFile = audioFile;
	}
	static public void setMidiFile(File midiFile){
		KaraokeSystem.midiFile = midiFile;
	}
	static public void setTestFile(File testFile){
	    KaraokeSystem.testFile = testFile;
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
		scorenotelist = new ArrayList<NoteBox>();
		for(int i = 0; i < notelist.size(); i++){
			NoteData d = notelist.get(i);
			NoteBox t = new NoteBox(d.notenumber, d.time, d.duration, Color.RED);
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
    //初期化処理をおこなう
    static public void initialize(){
        /*GUI関連*/
        notepane.getChildren().clear();
        music_spectrogram_pane.getChildren().clear();
        microphone_spectrum_pane.getChildren().clear();
        guicontroller.updateStatusLabels("","","","", "");
        guicontroller.updateVolumeProgressBar(0);
        /*データ関連*/
        if(notelist != null) notelist.clear();
        if(noteboxlist != null) noteboxlist.clear();
        if(scorenotelist != null) scorenotelist.clear();
        if(ScoreOfNote != null) ScoreOfNote.clear();
        nowscore = 0;
        notecounter = 0;
        zurashi = 0;
        KaraokeSystem.ScoreOfNote = new ArrayList<>();
    }
    static public void shutdown() {
        karaokethread.stopThread();
        if(musicplayer != null && musicplayer.isActive()) musicplayer.stop();
        if(recorder != null && recorder.isActive()) recorder.stop();
        if(testFilePlayer != null && testFilePlayer.isActive()) testFilePlayer.stop();
        if(spectrogram_executor != null) spectrogram_executor.shutdown();
        KaraokeSystem.nowplaying = false;
    }
	static public void start() throws UnsupportedAudioFileException, IOException, LineUnavailableException{
        /*お手本midiノーツ読み込み・描画*/
        openMidiFile();
        drawMidiNotes();
		/*スレッドを新規作成*/
		karaokethread = new KaraokeThread();
        /*BGMプレイヤー作成*/
        musicplayer = Player.newPlayer(audioFile, 0.1D, 0.4D, null, false);
        /*スペクトラム及びスペクトログラム新規作成・GUIPaneに追加*/
        spectrumChartNode = new SpectrumChartNode();
        realtime_music_spectrogramChartNode = new RealTimeSpectrogramChartNode(musicplayer.getFrameSize(),(int)musicplayer.getSampleRate());
        microphone_spectrum_pane.getChildren().add(spectrumChartNode);
        music_spectrogram_pane.getChildren().add(realtime_music_spectrogramChartNode);

        /*採点対象の音響信号をマイクから取り込む場合とファイルから読み込む場合とで処理を分岐*/
        if(KaraokeSystem.testFile == null) {
            try {
                /*マイクから音響信号を取り込む場合*/
                recorder = Recorder.newRecorder(16000.0D, 0.02D,  guicontroller.getNowSelectMixer(), null);
                /*フレームごとに行う処理を記述*/
                recorder.addAudioFrameListener((frame, position) -> {
                    /*音量計算*/
                    final double sampleRate = recorder.getSampleRate();
                    final double rms = Arrays.stream(frame).map(x -> x * x).average().orElse(0.0);
                    final double logRms = 20.0 * Math.log10(rms);
                    //音量の値をGUIに反映*/
                    int volval = Math.min(Math.max(((int) logRms + 130) * 100 / 130, 0), 100);
                    guicontroller.updateVolumeProgressBar(volval);
                    /*音響信号からスペクトラムを更新*/
                    UpdateChartData(frame, sampleRate);
                    /*音響信号から基本周波数を推定*/
                    double estimatefreq = CalcSubharmonicSumation.estimateFreq(frame, sampleRate);
                    /*現在再生中のMidiノーツや推定した基本周波数をGUIに反映*/
                    guicontroller.updateStatusLabels(
                            Integer.toString(karaokethread.nownotenumber),
                            NoteNameUtil.convertNoteNumtoNoteName(karaokethread.nownotenumber),
                            (estimatefreq < 0 ? "-" : Double.toString(estimatefreq) + "Hz"),
                            (estimatefreq < 0 ? "-" : NoteNameUtil.convertFreqtoNoteName(estimatefreq)),
                            Integer.toString((int)KaraokeSystem.nowscore));
                    /*推定した基本周波数をリストに格納するルーチンを呼ぶ*/
                    final double posInSec = musicplayer.position() / sampleRate;
                    SaitenRoutine(posInSec, estimatefreq);
				});
                /*マイクからの取り込み・BGMの再生・スレッドの開始*/
                recorder.start();
                musicplayer.start();
                karaokethread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            try {
                //testFileをテストデータとする
                testFilePlayer = Player.newPlayer(testFile, 0.01D, 0.02D, null, false);
                testFilePlayer.addAudioFrameListener((frame, position) -> {
                    final double rms = Arrays.stream(frame).map(x -> x * x).average().orElse(00.0);
                    final double logRms = 20.0 * Math.log10(rms);
                    final double sampleRate = testFilePlayer.getSampleRate();
                    final double posInSec = position / sampleRate;
                    int volval = Math.min(Math.max(((int) logRms + 130) * 100 / 130, 0), 100);
                    guicontroller.updateVolumeProgressBar(volval);
                    UpdateChartData(frame, sampleRate);
                    double estimatefreq = CalcSubharmonicSumation.estimateFreq(frame, sampleRate);
                    guicontroller.updateStatusLabels(
                            Integer.toString(karaokethread.nownotenumber),
                            NoteNameUtil.convertNoteNumtoNoteName(karaokethread.nownotenumber),
                            (estimatefreq < 0 ? "-" : Double.toString(estimatefreq) + "Hz"),
                            (estimatefreq < 0 ? "-" : NoteNameUtil.convertFreqtoNoteName(estimatefreq)),
                            Integer.toString((int)KaraokeSystem.nowscore));
                    SaitenRoutine(posInSec, estimatefreq);
                });
                musicplayer.start();
                testFilePlayer.start();
                karaokethread.start();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        /*スペクトログラム更新用のexecutor*/
        spectrogram_executor = Executors.newSingleThreadScheduledExecutor();
        spectrogram_executor.scheduleWithFixedDelay(
                () -> {
                    try {
                        final double[] frame = musicplayer.latestFrame();
                        /*スペクトログラム更新*/
                        realtime_music_spectrogramChartNode.UpdateDataSource(frame, musicplayer.getSampleRate());
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                },
                0L,(long)(Le4MusicUtils.frameDuration * 1000000 / 8.0),
                TimeUnit.MICROSECONDS
        );
        /*再生中フラグを立てる*/
        KaraokeSystem.nowplaying = true;
    }


	static private void UpdateChartData(double[] frame,double sampleRate){
        spectrumChartNode.UpdateDataSource(frame, sampleRate);
    }

    static private void SaitenRoutine(double posInSec, double estimatefreq){
        //現在どのノーツにいるかを決定し,次のノーツに移動していればListにAddする
        int nownotenum = NoteData.GetNoteNumFromTimeandNoteList(posInSec * 1000.0, KaraokeSystem.notelist);
        int nowcode = NoteNameUtil.convertFreqtoNoteNum(estimatefreq)%12;
        if(nownotenum + 1 > KaraokeSystem.ScoreOfNote.size()){
            //スコアリスト追加
            int[] newarray = new int[12];
            newarray[nowcode]++;
            KaraokeSystem.ScoreOfNote.add(newarray);
        }else{
            //スコア追加
            KaraokeSystem.ScoreOfNote.get(nownotenum)[nowcode]++;
        }
    }
	
}
