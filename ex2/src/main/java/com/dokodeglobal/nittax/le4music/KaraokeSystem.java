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

public class KaraokeSystem{
	static public Boolean nowplaying = false;
	static private File audioFile, midiFile;
	static public List<NoteData> notelist;
    static public AnchorPane notepane;
	static public GUIController guicontroller;

	static private KaraokeThread karaokethread;
	static public Integer notecounter;
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
		for(int i = 0; i < notelist.size(); i++){
			NoteData d = notelist.get(i);
			NoteBox t = new NoteBox(d.notenumber, d.time, d.duration);
			KaraokeSystem.notepane.getChildren().add(t);
		}
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
		notecounter = 0;
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
