package com.dokodeglobal.nittax.le4music.analyzer;

import java.io.File;
import java.util.stream.IntStream;
import jp.ac.kyoto_u.kuis.le4music.SingleXYArrayDataset; /*SingleXYArrayDataset*/
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.axis.NumberAxis;
import javafx.embed.swing.SwingNode;
import javax.swing.SwingUtilities;
import org.jfree.chart.ChartPanel;

public class WaveformChartNode extends SwingNode{
	
	public WaveformChartNode(){
		JFreeChart chart =  ChartFactory.createXYLineChart(null,null,null,null);
		ChartPanel cp = new ChartPanel(chart);
        this.setContent(cp);
		setGraphLabel();
	}
  
	public WaveformChartNode(final double[] waveform, final double sampleRate){
		/*各サンプルの時刻を求める*/	
		final double[] times = IntStream.range(0, waveform.length).mapToDouble(i -> i / sampleRate).toArray();
		JFreeChart chart =  ChartFactory.createXYLineChart(null,null,null,new SingleXYArrayDataset(times,waveform));
		ChartPanel cp = new ChartPanel(chart);
        this.setContent(cp);
		final XYPlot plot = chart.getXYPlot();
		final NumberAxis xAxis = (NumberAxis)plot.getDomainAxis();
		xAxis.setRange(0.0, (waveform.length - 1) / sampleRate);
		final NumberAxis yAxis = (NumberAxis)plot.getRangeAxis();
		yAxis.setRange(-1.0, 1.0);
		setGraphLabel();
		UpdateDataSource(waveform, sampleRate);
	}

	public void setGraphLabel(){
		ChartPanel cp = (ChartPanel)this.getContent();
		JFreeChart chart = (JFreeChart)cp.getChart();
		chart.setTitle("Waveform");
		final XYPlot plot = chart.getXYPlot();
		final NumberAxis xAxis = (NumberAxis)plot.getDomainAxis();
		xAxis.setLabel("Time [sec.]");
		final NumberAxis yAxis = (NumberAxis)plot.getRangeAxis();
		yAxis.setLabel("Amplitude");
	}

	public void UpdateDataSource(final double[] waveform, final double sampleRate){
		try{
			ChartPanel cp = (ChartPanel)this.getContent();
			JFreeChart chart = (JFreeChart)cp.getChart();
			XYPlot plot = (XYPlot) chart.getPlot();
			final double[] times = IntStream.range(0, waveform.length).mapToDouble(i -> i / sampleRate).toArray();
			plot.setDataset(new SingleXYArrayDataset(times,waveform));
			/*縦軸は固定*/
			final NumberAxis yAxis = (NumberAxis)plot.getRangeAxis();
			yAxis.setRange(-1.0, 1.0);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
