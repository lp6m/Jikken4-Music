package com.dokodeglobal.nittax.le4music;

import java.lang.Thread;
import javax.sound.sampled.*;
import javax.sound.midi.*;
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
	@Override
	public void run(){
		long starttime = System.nanoTime();
		long oldontime = starttime;
		int oldseekbarpos = -1;
		//int offcounter = 0;
		while(this.isActive){

			long nowtime = System.nanoTime();
			//long elapsedtime = nowtime - starttime;
			double elapsedtime = (double)KaraokeSystem.musicplayer.position() / KaraokeSystem.musicplayer.getSampleRate() * 1000;
			/*Midiノートを鳴らす*/
			if(KaraokeSystem.notecounter < KaraokeSystem.notelist.size() && elapsedtime  > KaraokeSystem.notelist.get(KaraokeSystem.notecounter).time){
				//note on
				oldontime = nowtime;
				int notenumber = KaraokeSystem.notelist.get(KaraokeSystem.notecounter).notenumber;
                this.nownotenumber = notenumber;
				playMidi(true, notenumber);
				KaraokeSystem.notecounter++;
			}
			/*if(KaraokeSystem.notecounter != 0 && offcounter < KaraokeSystem.notelist.size() && (nowtime - oldontime) > KaraokeSystem.notelist.get(offcounter).duration * 1000000){
				//note off
				playMidi(false, KaraokeSystem.notelist.get(KaraokeSystem.notecounter).notenumber);
				System.out.println(Integer.toString(offcounter) + ": off");
				offcounter++;
				}*/
			
			/*棒を動かす,棒が最後までいけば表示されてるノーツも左に*/
		   int seekbarposx = (int)(elapsedtime * KaraokeSystem.pixel_per_ms) - KaraokeSystem.zurashi;
		   int panewidth = (int)KaraokeSystem.notepane.getWidth();
		   if(seekbarposx > panewidth){
				KaraokeSystem.zurashi += panewidth;
				for(NoteBox n : KaraokeSystem.noteboxlist){
					n.setX(n.getX() - panewidth);
				}
			}
			if(oldseekbarpos != seekbarposx){
		       //KaraokeSystem.seekbar.setStartX(seekbarposx);
		       //KaraokeSystem.seekbar.setEndX(seekbarposx);
                oldseekbarpos = seekbarposx;
            }

		}
	}
}
