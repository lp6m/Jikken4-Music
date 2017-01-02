package com.dokodeglobal.nittax.le4music;

import java.lang.Thread;
import javax.sound.sampled.*;
import javax.sound.midi.*;

public class KaraokeThread extends Thread{
	private Boolean isActive = true; /*スレッドがアクティブの時だけtrue*/

	private Receiver receiver;

	public KaraokeThread(){
		try {
			ClassLoader midi_class_loader = javax.sound.midi.MidiSystem.class.getClassLoader();
			ClassLoader now_context_class_loader = Thread.currentThread().getContextClassLoader(); 
			Thread.currentThread().setContextClassLoader(midi_class_loader);
			receiver = MidiSystem.getReceiver();
			Thread.currentThread().setContextClassLoader(now_context_class_loader);
		}catch(Exception e){
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
		int offcounter = 0;
		while(this.isActive){
			long nowtime = System.nanoTime();
			if(KaraokeSystem.notecounter < KaraokeSystem.notelist.size() && (nowtime - starttime) > KaraokeSystem.notelist.get(KaraokeSystem.notecounter).time * 1000000){
				//note on
				oldontime = nowtime;
				playMidi(true, KaraokeSystem.notelist.get(KaraokeSystem.notecounter).notenumber);
				System.out.println(Integer.toString(KaraokeSystem.notecounter) + ": on");
				KaraokeSystem.notecounter++;
			}
			/*if(KaraokeSystem.notecounter != 0 && offcounter < KaraokeSystem.notelist.size() && (nowtime - oldontime) > KaraokeSystem.notelist.get(offcounter).duration * 1000000){
				//note off
				playMidi(false, KaraokeSystem.notelist.get(KaraokeSystem.notecounter).notenumber);
				System.out.println(Integer.toString(offcounter) + ": off");
				offcounter++;
				}*/
			
		}
	}
}
