package com.dokodeglobal.nittax.le4music.myutils;

import java.util.Map;
public class NoteNameUtil{
	public static String convertNoteNumtoNoteName(int notenumber){
		int t = notenumber % 12;
		if(t == 0) return "C";
		if(t == 11) return "B";
		if(t == 10) return "A#";
		if(t == 9) return "A";
		if(t == 8) return "G#";
		if(t == 7) return "G";
		if(t == 6) return "F#";
		if(t == 5) return "F";
		if(t == 4) return "E";
		if(t == 3) return "D#";
		if(t == 2) return "D";
		if(t == 1) return "C#";
		return "";
	}
	public static int convertFreqtoNoteNum(double freq){
		double y = freq / 440.0;
		double rst = 69.0 + 12 * Math.log(y)/Math.log(2.0);
		return (int)Math.round(rst);
	}
	public static String convertFreqtoNoteName(double freq){
		return convertNoteNumtoNoteName(convertFreqtoNoteNum(freq));
	}
}
