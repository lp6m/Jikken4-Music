package com.dokodeglobal.nittax.le4music;
import com.dokodeglobal.nittax.le4music.midiutil.NoteData;
import com.dokodeglobal.nittax.le4music.viewcomponent.NoteBox;

import java.lang.Thread;
import java.io.*;
import java.util.*;
import javafx.scene.layout.AnchorPane;
import javafx.animation.*;
import javafx.application.Application;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.util.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javafx.scene.shape.*;
import jp.ac.kyoto_u.kuis.le4music.Player;

public class KaraokeSystem{
	static public Boolean nowplaying = false;
	static private File audioFile,midiFile;
	static private Player musicplayer;           /*BGM*/
	static public List<NoteData> notelist;      /*MidiCSVからよみこんだNoteDataのリスト*/
	static public List<NoteBox> noteboxlist;    /*表示するノーツのBoxリスト*/
    static public AnchorPane notepane;          /*ノーツを表示するPane*/
	static public Line seekbar;                 /*Pane上で現在の位置を表すための棒,シークバー*/
	static public GUIController guicontroller;  /*JavaFXのGUIControllerのクラスのインスタンスへの参照を保持しておく*/
	static public int zurashi;                   /*ノーツのスクロール用.何pixelずらしたか*/
	/*1msを何pixelで表すか*/
	static public double pixel_per_ms = 24.0 / 188.0;
	
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
		KaraokeSystem.nowplaying = true;

	}
	static public void stop(){
		if(KaraokeSystem.nowplaying != true) return;
		karaokethread.stopThread();
	}
	static public void reset(){
	}
	
}
