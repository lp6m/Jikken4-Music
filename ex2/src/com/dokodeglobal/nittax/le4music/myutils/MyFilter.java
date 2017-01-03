package com.dokodeglobal.nittax.le4music.myutils;

import java.util.*;
import java.util.Map.Entry; 

//http://vstcpp.wpblog.jp/?page_id=523#Robert Bristow-Johnson Audio EQ Cookbook
public class MyFilter{

	public static double[] LowPassFilter(double input[], double cutoff_freq, double sampleRate){
		// それぞれの変数は下記のとおりとする
		// float samplerate … サンプリング周波数
		// float freq … カットオフ周波数
		// float q    … フィルタのQ値
		double q = 1.0 / Math.sqrt(2);
		double omega = 2.0 * Math.PI *  cutoff_freq / sampleRate;
		double alpha = Math.sin(omega) / (2.0 * q);
 
		double a0 =  1.0 + alpha;
		double a1 = -2.0 * Math.cos(omega);
		double a2 =  1.0 - alpha;
		double b0 = (1.0 - Math.cos(omega)) / 2.0;
		double b1 =  1.0 - Math.cos(omega);
		double b2 = (1.0 - Math.cos(omega)) / 2.0;

		// それぞれの変数は下記のとおりとする
		// 　float input[]  …入力信号の格納されたバッファ。
		// 　flaot output[] …フィルタ処理した値を書き出す出力信号のバッファ。
		// 　int   size     …入力信号・出力信号のバッファのサイズ。
		// 　float in1, in2, out1, out2  …フィルタ計算用のバッファ変数。初期値は0。
		// 　float a0, a1, a2, b0, b1, b2 …フィルタの係数。 別途算出する。
		double[] output = new double[input.length];
		double in1,in2,out1,out2;
		in1 = in2 = out1 = out2 = 0;
		for(int i = 0; i < input.length; i++){
			// 入力信号にフィルタを適用し、出力信号として書き出す。
			output[i] = b0/a0 * input[i] + b1/a0 * in1  + b2/a0 * in2
				- a1/a0 * out1 - a2/a0 * out2;
 
			in2  = in1;       // 2つ前の入力信号を更新
			in1  = input[i];  // 1つ前の入力信号を更新
 
			out2 = out1;      // 2つ前の出力信号を更新
			out1 = output[i]; // 1つ前の出力信号を更新
		}
		return output;
	}
	private static int[] sortedIndex(double[] array) {
        int len = array.length;
        int[] order = new int[len];
        Map<Integer, Double> map = new HashMap<Integer, Double>();
        for (int i = 0; i < len; i++) {
            map.put(i, array[i]);
        }
        List<Map.Entry<Integer, Double>> entries = new ArrayList<Map.Entry<Integer, Double>>(
                map.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<Integer, Double>>() {
            @Override
            public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        int i = 0;
        for (Map.Entry<Integer, Double> entry : entries) {
            order[i++] = entry.getKey();
        }
        return order;
    }

	/*大きい順にnum個は元の値+最小値でそれ以外は0にする狂気のフィルタ*/
	public static double[] highPassValueFilter(double[] array, int num){
		if(array.length < num) return array;
		int[] sindex = sortedIndex(array);
		double[] rst = new double[array.length];
		//Arrays.fill(rst,array[sindex[array.length-1]]);
		Arrays.fill(rst,0);
		double min = array[sindex[array.length-1]];
		for(int i = 0; i < num; i++){
			rst[sindex[i]] = array[sindex[i]] - min;
		}
		return rst;
	}
}
