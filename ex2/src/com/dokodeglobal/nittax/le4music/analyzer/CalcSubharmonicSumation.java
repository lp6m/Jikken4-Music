package com.dokodeglobal.nittax.le4music.analyzer;

import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import org.apache.commons.math3.complex.Complex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class CalcSubharmonicSumation {

    static double threshold = 0.0005;
    //しきい値より小さい場合は-1を返す
    static public double estimateFreq(double[] waveform, double sampleRate){
        List<double[]> rst = new ArrayList<double[]>();
		/*1.フーリエ変換する*/

		/* fftSize = 2ˆp >= waveform.length を 満 た す fftSize を 求 め る
		* 2ˆp は シ フ ト 演 算 で 求 め ら れ る */
        final int fftSize = 1 << Le4MusicUtils.nextPow2(waveform.length);
		/* 信 号 の 長 さ を fftSize に 伸 ば し ， 長 さ が 足 り な い 部 分 は0 で 埋 め る ．
		* 振 幅 を 信 号 長 で 正 規 化 す る ． */
        final double[] src = Arrays.stream(Arrays.copyOf(waveform, fftSize)).map(w -> w / waveform.length).toArray();
		/* 高 速 フ ー リ エ 変 換 を 行 う */
        final Complex[] spectrum = Le4MusicUtils.rfft(src);
		/* 対 数 振 幅 ス ペ ク ト ル を 求 め る */
        double[] specLog = Arrays.stream(spectrum).mapToDouble(c -> c.abs()).toArray();
        //double[] specLog = Arrays.stream(spectrum).mapToDouble(c -> c.abs()).toArray();
		/* 周 波 数 を 求 め る ． 以 下 を 満 た す よ う に 線 型 に
		* freqs[0] = 0Hz
		* freqs[fftSize2 - 1] = sampleRate / 2 (= Nyquist周波数) */
        final double[] freqs = IntStream.rangeClosed(0, fftSize >> 1).mapToDouble(i -> i * sampleRate / fftSize).toArray();

        //		specLog = MyFilter.highPassValueFilter(specLog,(int)((double)specLog.length * 0.01));
        double[] shs = new double[freqs.length];
        for(int f = 0; f < freqs.length; f++){
            double shsum = 0;
            for(int n = 1; n <= 5; n++){
                int nf = (n * f) % freqs.length;
                if(n * f >= freqs.length) continue;
				/*x(nf)を求める*/
                double x = specLog[nf];
				/*w(nf)はhamming窓nfがナイキスト周波数こえる場合は0とする*/
                double w =  0.54 - 0.46 * Math.cos(2 * Math.PI * (double)nf / (double)freqs.length);
                //double w = 1.0;
                double h = Math.pow(0.84, n-1);
                shsum += h * w * x;
            }
            shs[f] = shsum;
            //System.out.println(shsum);
        }
        int bestindex = 0;
        double bestscore = shs[0];
        for(int i = 0; i < shs.length; i++){
            if(bestscore < shs[i]){
                bestscore = shs[i];
                bestindex = i;
            }
        }
        double ans = freqs[bestindex];
        if(ans < threshold) return -1;
        else return ans;
    }
}
