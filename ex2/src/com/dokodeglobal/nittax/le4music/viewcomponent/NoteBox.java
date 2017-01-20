package com.dokodeglobal.nittax.le4music.viewcomponent;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import com.dokodeglobal.nittax.le4music.KaraokeSystem;

public class NoteBox extends Rectangle{
	static public final int height = 6;

	/*一番高い音と一番低い音をセット.これにより各ノートの表示位置を決める*/
	static public int lowest_notenumber = 55; /*初期値は適当*/
	static public int highest_notenumber = 55;
	static public void setNoteRange(int l, int h){
		lowest_notenumber = l;
		highest_notenumber = h;
	}

	/*設計メモ:
	  ノーツ表示部の横幅をおよそ5秒とする
	*/
	
	public NoteBox(int notenumber,long time, int length, Color col){
		int x = (int)(time * KaraokeSystem.pixel_per_ms);
		int y = height * (highest_notenumber - notenumber);
		setX(x);
		setY(y);
		setHeight(height);
		int width = (int)(length * KaraokeSystem.pixel_per_ms);
		setWidth(width);
		setStroke(Color.BLACK);
		setFill(col);
	}
}
