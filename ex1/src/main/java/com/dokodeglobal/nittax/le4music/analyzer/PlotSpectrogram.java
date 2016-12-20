package com.dokodeglobal.nittax.le4music.analyzer;

import java.util.Arrays;
import java.io.File;
import java.util.stream.IntStream;
import jp.ac.kyoto_u.kuis.le4music.SingleXYArrayDataset; /*SingleXYArrayDataset*/
import jp.ac.kyoto_u.kuis.le4music.HotPaintScale;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.MathArrays;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.Plot;
import java.io.IOException;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.axis.NumberAxis;
import javafx.embed.swing.SwingNode;
import javax.swing.SwingUtilities;
import java.util.stream.Stream;
import java.util.stream.IntStream;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.MatrixSeries;
import org.jfree.data.xy.MatrixSeriesCollection;
import org.jfree.chart.util.ExportUtils;
import javax.swing.JFrame;

public class PlotSpectrogram{
	
		/* 波 形 を 短 時 間 フ ー リ エ 変 換 し ， ス ペ ク ト ロ グ ラ ム を プ ロ ッ ト す る */
	public static final SwingNode createSpectrogramChart(final double[] waveform,final double sampleRate,final double windowDuration,final double windowShift) {
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
		/* 複 素 ス ペ ク ト ロ グ ラ ム を 対 数 振 幅 ス ペ ク ト ロ グ ラ ム に */
		final double[][] specLog = spectrogram.map(sp -> Arrays.stream(sp).mapToDouble(c -> 20.0 * Math.log10(c.abs())).toArray()).toArray(n -> new double[n][]);
		/* フ レ ー ム 数 と 各 フ レ ー ム 先 頭 位 置 の 時 刻 */
		final double[] times = IntStream.range(0, specLog.length).mapToDouble(i -> i * windowShift).toArray();

		/* プ ロ ッ ト */
		 
		final JFreeChart chart = PlotSpectrogram.plotimage(specLog, times, freqs,"Time [sec.]", "Frequency [Hz]",-100.0, 0.0, true);
		chart.setTitle("Spectrogram");
 
		ChartPanel cp = new ChartPanel(chart);
		SwingNode sNode = new SwingNode();
		sNode.setContent(cp);
		return sNode;

	}
	//le4music.jar Plot.javaより引用
	private static final JFreeChart plotimage(final double[][] z,
											  final double[] x,
											  final double[] y,
											  final String xAxisTitle,
											  final String yAxisTitle,
											  final double zMin,
											  final double zMax,
											  final boolean transpose
											  )
	{
		final int size1 = x.length;
		final int size2 = y.length;
		final MatrixSeries ms = buildMatrixSeries(z, size1, size2, transpose);
		final MatrixSeriesCollection msc = new MatrixSeriesCollection(ms);
 
		/* プロットの軸 */
		final NumberAxis axis1a = new NumberAxis(xAxisTitle + "-index");
		axis1a.setLowerMargin(0.0);
		axis1a.setUpperMargin(0.0);
 
		final NumberAxis axis2a = new NumberAxis(yAxisTitle + "-index");
		axis2a.setLowerMargin(0.0);
		axis2a.setUpperMargin(0.0);
 
		final NumberAxis axis1b = new NumberAxis(xAxisTitle);
		axis1b.setLowerBound(x[0]);
		axis1b.setUpperBound(x[size1 - 1]);
     
		final NumberAxis axis2b = new NumberAxis(yAxisTitle);
		axis2b.setLowerBound(y[0]);
		axis2b.setUpperBound(y[size2 - 1]);
     
		/* レンダラ（値と色の対応関係） */
		final XYBlockRenderer renderer = new XYBlockRenderer();
		renderer.setPaintScale(new HotPaintScale(zMin, zMax));
 
		/* JFreeChartの軸表示に関するバグへの対策 */
		final XYPlot plot = new XYPlot(msc, axis1a, axis2a, renderer);
		final String axisType = "NORMAL"; /* NORMAL | INDEX | BOTH */
		switch (axisType) {
		case "INDEX":
			break;
		case "BOTH":
			plot.setDomainAxis(1, axis1b);
			plot.setDomainAxisLocation(1, AxisLocation.BOTTOM_OR_LEFT);
			plot.setRangeAxis(1, axis2b);
			plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_LEFT);
			break;
		case "NORMAL":
			plot.setDomainAxis(1, axis1b);
			plot.setDomainAxisLocation(1, AxisLocation.BOTTOM_OR_LEFT);
			plot.setRangeAxis(1, axis2b);
			plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_LEFT);
			plot.getDomainAxis().setVisible(false);
			plot.getRangeAxis().setVisible(false);
			break;
		}
 
		final JFreeChart chart = new JFreeChart(plot);
		chart.removeLegend();
		return chart;
	}
	private static final MatrixSeries buildMatrixSeries(
														final double[][] z,
														final int size1,
														final int size2,
														final boolean transpose
														) {
		MatrixSeries ms;
		if (!transpose) {
			ms = new MatrixSeries("", size1, size2);
			for (int i = 0; i < size1; i++)
				for (int j = 0; j < size2; j++)
					ms.update(i, j, z[i][j]);
		} else {
			ms = new MatrixSeries("", size2, size1);
			for (int i = 0; i < size1; i++)
				for (int j = 0; j < size2; j++)
					ms.update(j, i, z[i][j]);
		}
		return ms;
	}
}

 

