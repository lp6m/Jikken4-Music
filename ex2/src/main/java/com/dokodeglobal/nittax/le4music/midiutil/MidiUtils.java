package com.dokodeglobal.nittax.le4music.midiutil;

import javax.sound.sampled.*;
import java.io.*;
import javax.sound.midi.*;

public class MidiUtils{
	private Track t;
	private MidiEvent e;
	private byte[] m;
	
	public MidiUtils(File f, int track) throws IOException, InvalidMidiDataException{
		Sequence s = MidiSystem.getSequence(f);
		System.out.println(s.getMi
		this.t = s.getTracks()[track];
	}
	public void showMidiEvent(){
		for(int i=0;;){
			try {
				e = t.get(i++);
				m = e.getMessage().getMessage();
			}catch(ArrayIndexOutOfBoundsException ex){
				break;
			}
			System.out.println(e.getTick() + ", " + (0xFF & m[0]) + ", " + (0xFF & m[1]) + ", " + (0xFF & m[2]));
		}
	}
}
