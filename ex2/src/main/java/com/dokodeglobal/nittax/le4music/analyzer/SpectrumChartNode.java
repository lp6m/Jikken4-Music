package com.dokodeglobal.nittax.le4music.analyzer;

import com.dokodeglobal.nittax.le4music.myutils.*;
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

public final class SpectrumChartNode extends SwingNode{

	public SpectrumChartNode(){
		JFreeChart chart =  ChartFactory.createXYLineChart(null,null,null,null);
		ChartPanel cp = new ChartPanel(chart);
        this.setContent(cp);
		setGraphLabel();
	}
	/* 波 形 を フ ー リ エ 変 換 し ， ス ペ ク ト ル を 対 数(dB)ス ケ ー ル で プ ロ ッ ト す る */
	public SpectrumChartNode(final double[] waveform,final double sampleRate) {
		/* プ ロ ッ ト す る */
		JFreeChart chart =  ChartFactory.createXYLineChart(null,null,null, createSpectrumData(waveform, sampleRate));
		final XYPlot plot = chart.getXYPlot();
		final NumberAxis xAxis = (NumberAxis)plot.getDomainAxis();
		xAxis.setRange(0.0, 0.5 * sampleRate);
		final NumberAxis yAxis = (NumberAxis)plot.getRangeAxis();
		yAxis.setAutoRangeIncludesZero(false);
		ChartPanel cp = new ChartPanel(chart);
		this.setContent(cp);
		setGraphLabel();
	}

	private void setGraphLabel(){
		ChartPanel cp = (ChartPanel)this.getContent();
		JFreeChart chart = (JFreeChart)cp.getChart();
		chart.setTitle("Spectrum");
		final XYPlot plot = chart.getXYPlot();
		final NumberAxis xAxis = (NumberAxis)plot.getDomainAxis();
		xAxis.setLabel("Frequency [Hz]");
		final NumberAxis yAxis = (NumberAxis)plot.getRangeAxis();
		yAxis.setLabel("Amplitude [dB]");

	}
	/*フーリエ変換したデータを作成*/
	private SingleXYArrayDataset createSpectrumData(final double[] waveform, final double sampleRate){
		/* fftSize = 2ˆp >= waveform.length を 満 た す fftSize を 求 め る
		* 2ˆp は シ フ ト 演 算 で 求 め ら れ る */
		final int fftSize = 1 << Le4MusicUtils.nextPow2(waveform.length);
		/* 信 号 の 長 さ を fftSize に 伸 ば し ， 長 さ が 足 り な い 部 分 は0 で 埋 め る ．
		* 振 幅 を 信 号 長 で 正 規 化 す る ． */
		final double[] src = Arrays.stream(Arrays.copyOf(waveform, fftSize)).map(w -> w / waveform.length).toArray();
		/* 高 速 フ ー リ エ 変 換 を 行 う */
		final Complex[] spectrum = Le4MusicUtils.rfft(src);
		/* 対 数 振 幅 ス ペ ク ト ル を 求 め る */
		//		double[] specLog = Arrays.stream(spectrum).mapToDouble(c -> 20.0 * Math.log10(c.abs())).toArray();
		double[] specLog = Arrays.stream(spectrum).mapToDouble(c -> c.abs()).toArray();
		/* 周 波 数 を 求 め る ． 以 下 を 満 た す よ う に 線 型 に
		* freqs[0] = 0Hz
		* freqs[fftSize2 - 1] = sampleRate / 2 (= Nyquist周波数) */
		
		double[] freqs = IntStream.rangeClosed(0, fftSize >> 1).mapToDouble(i -> i * sampleRate / fftSize).toArray();
		
		return new SingleXYArrayDataset(freqs, specLog);
	}
	
	public void UpdateDataSource(final double[] waveform, final double sampleRate){
		try{
			ChartPanel cp = (ChartPanel)this.getContent();
			JFreeChart chart = (JFreeChart)cp.getChart();
			XYPlot plot = (XYPlot) chart.getPlot();
			final NumberAxis xAxis = (NumberAxis)plot.getDomainAxis();
			xAxis.setRange(0.0, 0.5 * sampleRate);
			final NumberAxis yAxis = (NumberAxis)plot.getRangeAxis();
			//			yAxis.setRange(-160.0,0);
			final double[] times = IntStream.range(0, waveform.length).mapToDouble(i -> i / sampleRate).toArray();
			plot.setDataset(createSpectrumData(waveform,sampleRate));
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	
}
