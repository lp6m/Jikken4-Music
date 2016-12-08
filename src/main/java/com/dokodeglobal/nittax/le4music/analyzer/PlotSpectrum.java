package com.dokodeglobal.nittax.le4music.analyzer;

import java.io.File;
import java.util.Arrays;
import java.util.stream.IntStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.swing.JFrame;
import org.apache.commons.math3.complex.Complex;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.axis.NumberAxis;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.SingleXYArrayDataset; /*SingleXYArrayDataset*/
import java.io.IOException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javafx.embed.swing.SwingNode;
import org.jfree.chart.ChartFactory;

public final class PlotSpectrum {
	/* 波 形 を フ ー リ エ 変 換 し ， ス ペ ク ト ル を 対 数(dB)ス ケ ー ル で プ ロ ッ ト す る */
	public static final SwingNode createSpectrumchart(final double[] waveform,final double sampleRate) {
		/* fftSize = 2ˆp >= waveform.length を 満 た す fftSize を 求 め る
		* 2ˆp は シ フ ト 演 算 で 求 め ら れ る */
		final int fftSize = 1 << Le4MusicUtils.nextPow2(waveform.length);
		System.out.println(waveform.length);
		System.out.println(fftSize);
		/* 信 号 の 長 さ を fftSize に 伸 ば し ， 長 さ が 足 り な い 部 分 は0 で 埋 め る ．
		* 振 幅 を 信 号 長 で 正 規 化 す る ． */
		final double[] src = Arrays.stream(Arrays.copyOf(waveform, fftSize)).map(w -> w / waveform.length).toArray();
		/* 高 速 フ ー リ エ 変 換 を 行 う */
		final Complex[] spectrum = Le4MusicUtils.rfft(src);
		System.out.println(src.length);
		System.out.println(spectrum.length);
		System.out.println("hoge");
		/* 対 数 振 幅 ス ペ ク ト ル を 求 め る */
		final double[] specLog = Arrays.stream(spectrum).mapToDouble(c -> 20.0 * Math.log10(c.abs())).toArray();
		/* 周 波 数 を 求 め る ． 以 下 を 満 た す よ う に 線 型 に
		* freqs[0] = 0Hz
		* freqs[fftSize2 - 1] = sampleRate / 2 (= Nyquist周波数) */
		final double[] freqs = IntStream.rangeClosed(0, fftSize >> 1).mapToDouble(i -> i * sampleRate / fftSize).toArray();
		/* プ ロ ッ ト す る */
		JFreeChart chart =  ChartFactory.createXYLineChart(null,null,null,new SingleXYArrayDataset(freqs,specLog));
		chart.setTitle("Spectrum");
		final XYPlot plot = chart.getXYPlot();
		final NumberAxis xAxis = (NumberAxis)plot.getDomainAxis();
		xAxis.setRange(0.0, 0.5 * sampleRate);
		xAxis.setLabel("Frequency [Hz]");
		final NumberAxis yAxis = (NumberAxis)plot.getRangeAxis();
		yAxis.setLabel("Amplitude [dB]");
		yAxis.setAutoRangeIncludesZero(false);
		ChartPanel cp = new ChartPanel(chart);
		SwingNode sNode = new SwingNode();
		sNode.setContent(cp);
		return sNode;
	} 
}
