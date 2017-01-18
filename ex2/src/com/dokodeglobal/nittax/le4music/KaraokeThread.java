package com.dokodeglobal.nittax.le4music;

import java.lang.Thread;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javax.sound.midi.*;
import com.dokodeglobal.nittax.le4music.midiutil.NoteData;
import com.dokodeglobal.nittax.le4music.myutils.NoteNameUtil;
import com.dokodeglobal.nittax.le4music.viewcomponent.NoteBox;
public class KaraokeThread extends Thread{
	private Boolean isActive = true; /*スレッドがアクティブの時だけtrue*/
    public int nownotenumber;
	private Receiver receiver;

	public KaraokeThread(){
	    try {
            receiver = MidiSystem.getReceiver();
        }catch(Exception e) {
            e.printStackTrace();
        }
	}
			
	public void stopThread(){
		this.isActive = false;
	}

	private void playMidi(Boolean onflag, int notenumber){
		try {
			ClassLoader midi_class_loader = javax.sound.midi.MidiSystem.class.getClassLoader();
			ClassLoader now_context_class_loader = Thread.currentThread().getContextClassLoader(); 
			Thread.currentThread().setContextClassLoader(midi_class_loader);
			//Receiver receiver = MidiSystem.getReceiver();
			ShortMessage message = new ShortMessage();
			if(onflag) message.setMessage(ShortMessage.NOTE_ON, notenumber, 127);
			else message.setMessage(ShortMessage.NOTE_OFF, notenumber, 127);
			receiver.send(message, -1);
			Thread.currentThread().setContextClassLoader(now_context_class_loader);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	//notenumのノーツについて採点を行う
	private int DoSaiten(int notenum) {
	    if(notenum < 0) return -1;
        try {
            int[] level = KaraokeSystem.ScoreOfNote.get(notenum);
            int maxcode = -1;
            int bestscore = -1;
            for (int i = 0; i < level.length; i++) {
                if (bestscore < level[i]) {
                    maxcode = i;
                    bestscore = level[i];
                }
            }
            return maxcode;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
	@Override
	public void run(){
		long starttime = System.nanoTime();
		int oldseekbarpos = -1;
		int offcounter = 0;
		while(this.isActive){

			long nowtime = System.nanoTime();
			//long elapsedtime = nowtime - starttime;
			double elapsedtime = (double)KaraokeSystem.musicplayer.position() / KaraokeSystem.musicplayer.getSampleRate() * 1000;
			/*Midiノートを鳴らす*/
			if(KaraokeSystem.notecounter < KaraokeSystem.notelist.size()
               && elapsedtime  > KaraokeSystem.notelist.get(KaraokeSystem.notecounter).time){
				//note on
				int notenumber = KaraokeSystem.notelist.get(KaraokeSystem.notecounter).notenumber;
                this.nownotenumber = notenumber;
				playMidi(true, notenumber);
				KaraokeSystem.notecounter++;
			}

			if(offcounter < KaraokeSystem.notelist.size()
               && elapsedtime > KaraokeSystem.notelist.get(offcounter).duration + KaraokeSystem.notelist.get(offcounter).time){
				//note off 判定を行う
                int rst = DoSaiten(offcounter);
                if(rst >= 0){
                    //ノーツ追加
                    NoteData d = KaraokeSystem.notelist.get(offcounter);
                    int saiten_rst = NoteData.GetSaitenNotenumber(rst, d.notenumber);
                    //スコア更新
                    if(saiten_rst == d.notenumber) {
                        KaraokeSystem.nowscore += 100.0 / (double)KaraokeSystem.notelist.size();
                    }
                    NoteBox n = new NoteBox(saiten_rst, d.time, d.duration, Color.YELLOW);
                    Platform.runLater(() -> KaraokeSystem.notepane.getChildren().add(n));
                    n.setX(KaraokeSystem.noteboxlist.get(offcounter).getX());
                    KaraokeSystem.scorenotelist.add(n);
                }
                offcounter++;
            }
			
			/*棒を動かす,棒が最後までいけば表示されてるノーツも左に*/
		   int seekbarposx = (int)(elapsedtime * KaraokeSystem.pixel_per_ms) - KaraokeSystem.zurashi;
		   int panewidth = (int)KaraokeSystem.notepane.getWidth();
		   if(seekbarposx > panewidth){
				KaraokeSystem.zurashi += panewidth;
				for(NoteBox n : KaraokeSystem.noteboxlist){
					n.setX(n.getX() - panewidth);
				}
				for(int i = 0; i < KaraokeSystem.scorenotelist.size(); i++){
				    NoteBox n = KaraokeSystem.scorenotelist.get(i);
                    n.setX(n.getX() - panewidth);
                }
			}
			if(oldseekbarpos != seekbarposx){
		       //KaraokeSystem.seekbar.setStartX(seekbarposx);
		       //KaraokeSystem.seekbar.setEndX(seekbarposx);
                oldseekbarpos = seekbarposx;
            }

            /*musicplayerが終われば自動終了するようにする*/
			if(!KaraokeSystem.musicplayer.isActive()){
				this.isActive = false;
				KaraokeSystem.shutdown();
			}
		}
	}
}
