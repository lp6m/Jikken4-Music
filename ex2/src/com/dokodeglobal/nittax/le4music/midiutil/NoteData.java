package com.dokodeglobal.nittax.le4music.midiutil;

import java.util.*;
public class NoteData{
	public long time;
	public int notenumber;
	public int duration;
	static int tolerance = 2; /*カラオケ採点における許容誤差*/

	//NoteDataのリストを受け取り現在どのノーツかを返す
    public static int GetNoteNumFromTimeandNoteList(double time, List<NoteData> notelist){
        //System.out.println(time);
        for(int i = 0; i < notelist.size(); i++){
            NoteData n = notelist.get(i);
            //System.out.println(n.time);
            if(n.time < time && time < n.time + n.duration) return i;
        }
        return -1;
    }

    //採点で多数決されたコードから表示用にノートナンバーを決定
    public static int GetSaitenNotenumber(int saitenrst, int correct_notenumber){
        if(Math.abs(correct_notenumber % 12 - saitenrst) <= tolerance){
            //全く同じor許容誤差なら正しいnotenumberを返す
            return correct_notenumber;
        }
        int lowrst = (correct_notenumber / 12) * 12 + saitenrst;
        int highrst = (correct_notenumber / 12 + 1) * 12 + saitenrst;
        //lowestとhighrstのうちcorrect_notenumberに近い結果を返す
        if(Math.abs(lowrst - correct_notenumber) < Math.abs(highrst - correct_notenumber)){
            return lowrst;
        }else{
            return highrst;
        }
    }
}
