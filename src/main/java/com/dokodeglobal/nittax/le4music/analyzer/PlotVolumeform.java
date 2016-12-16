package com.dokodeglobal.nittax.le4music.analyzer;

import java.util.Arrays;
import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import jp.ac.kyoto_u.kuis.le4music.SingleXYArrayDataset; /*SingleXYArrayDataset*/
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.axis.NumberAxis;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.MathArrays;
import javafx.embed.swing.SwingNode;

public class PlotVolumeform{   
    @SuppressWarnings("unchecked")
		/* 波 形 を 短 時 間 フ ー リ エ 変 換 し ， ス ペ ク ト ロ グ ラ ム を プ ロ ッ ト す る */
	public static final SwingNode createPlotVolumeform(final double[] waveform,final double sampleRate,final double windowDuration,final double windowShift) {
		/* 窓 関 数 と F F T の サ ン プ ル 数 */
		final int windowSize = (int)Math.round(windowDuration * sampleRate);
		final int fftSize = 1 << Le4MusicUtils.nextPow2(windowSize);
		/* シ フ ト の サ ン プ ル 数 */
		final int shiftSize = (int)Math.round(windowShift * sampleRate);
		/* 窓 関 数 を 求 め ， そ れ を 正 規 化 す る */
		final double[] window = MathArrays.normalizeArray(Arrays.copyOf(Le4MusicUtils.hanning(windowSize), fftSize), 1.0);
		/* 各 フ ー リ エ 変 換 係 数 に 対 応 す る 周 波 数 */
		final double[] freqs = IntStream.rangeClosed(0, fftSize / 2).mapToDouble(i -> i * sampleRate / fftSize).toArray();
		/* 短 時 間 フ ー リ エ 変 換 本 体 */
		final Stream<Complex[]> spectrogram = Le4MusicUtils.sliding(waveform, window, shiftSize).map(frame -> Le4MusicUtils.rfft(frame));
		final Complex[][] array = (Complex[][])spectrogram.toArray(Complex[][]::new);
		final double[] result = new double[array.length];
		for(int i = 0; i < array.length; i++){
			double sum = 0;
			int N = array[i].length;
			for(int j = 0; j < N; j++){
				sum += Math.pow(array[i][j].abs(),2.0);
			}
			result[i] = 20.0*Math.log10(Math.sqrt(sum/(double)N));
		}
		/* フ レ ー ム 数 と 各 フ レ ー ム 先 頭 位 置 の 時 刻 */
		final double[] times = IntStream.range(0, array.length).mapToDouble(i -> i * windowShift).toArray();
		
		JFreeChart chart =  ChartFactory.createXYLineChart(null,null,null,new SingleXYArrayDataset(times,result));
		chart.setTitle("Volumeform");
		final XYPlot plot = chart.getXYPlot();
		final NumberAxis xAxis = (NumberAxis)plot.getDomainAxis();
		//xAxis.setRange(0.0, (waveform.length - 1) / sampleRate);
		xAxis.setLabel("Time [sec.]");
		final NumberAxis yAxis = (NumberAxis)plot.getRangeAxis();
		//yAxis.setRange(-1.0, 1.0);
		yAxis.setLabel("Volume[db.]");
		ChartPanel cp = new ChartPanel(chart);
		SwingNode sNode = new SwingNode();
        sNode.setContent(cp);
		return sNode;
	}
}
