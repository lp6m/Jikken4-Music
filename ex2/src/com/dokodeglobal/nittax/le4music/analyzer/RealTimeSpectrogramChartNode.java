package com.dokodeglobal.nittax.le4music.analyzer;

import java.io.File;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import javafx.embed.swing.SwingNode;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.HelpFormatter;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.MathArrays;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.general.DatasetChangeEvent;

import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;

import jp.ac.kyoto_u.kuis.le4music.Plot;
import jp.ac.kyoto_u.kuis.le4music.HotColorMap;
import jp.ac.kyoto_u.kuis.le4music.Player;
import jp.ac.kyoto_u.kuis.le4music.ColorMapIntARGB;

import java.io.IOException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.LineUnavailableException;
import org.apache.commons.cli.ParseException;
import java.awt.Dimension;
public final class RealTimeSpectrogramChartNode extends SwingNode{

    private int framesize;
    public RealTimeSpectrogramChartNode(int framesize, int sampleRate){
        this.framesize = framesize;
        final double frameDuration = Le4MusicUtils.frameDuration;
		/* ã‚·ãƒ•ãƒˆé•·ï¼ãƒ•ãƒ¬ãƒ¼ãƒ é•·ã®8åˆ†ã®1 */
        final double shiftDuration = frameDuration / 8.0;
        final double duration = Default.duration;
        final double freqMax =  Default.freqMax;

        final int frames = (int)(duration / shiftDuration);

        final int fftSize = 1 << Le4MusicUtils.nextPow2(framesize);
        final int bins = (int)(fftSize * freqMax / sampleRate);

        final NumberAxis timeAxis = new NumberAxis("Time-index");
        timeAxis.setLowerMargin(0.0);
        timeAxis.setUpperMargin(0.0);

        final NumberAxis freqAxis = new NumberAxis("Frequency-index");
        freqAxis.setLowerMargin(0.0);
        freqAxis.setUpperMargin(0.0);

        final NumberAxis timeAxis1 = new NumberAxis("Time [sec.]");
        timeAxis1.setLowerBound((-frames + 1) * shiftDuration);
        timeAxis1.setUpperBound(0.0);

        final NumberAxis freqAxis1 = new NumberAxis("Frequency [Hz]");
        freqAxis1.setLowerBound(0.0);
        freqAxis1.setUpperBound(freqMax);

        final NumberAxis freqAxis2 = new NumberAxis("Frequency [Hz]");
        freqAxis2.setLowerBound(0.0);
        freqAxis2.setUpperBound(freqMax);

        final ColorMapIntARGB colormap = new jp.ac.kyoto_u.kuis.le4music.HotColorMapIntARGB(Default.specLogMin, Default.specLogMax);
        final OnlineSpectrogramPlot spgPlot = new OnlineSpectrogramPlot(timeAxis, freqAxis, frames, bins, colormap);
        final String axisType = "NORMAL"; /* NORMAL | INDEX | BOTH */
        switch (axisType) {
            case "INDEX":
                break;
            case "BOTH":
                spgPlot.setDomainAxis(1, timeAxis1);
                spgPlot.setDomainAxisLocation(1, AxisLocation.BOTTOM_OR_LEFT);
                spgPlot.setRangeAxis(1, freqAxis1);
                spgPlot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_LEFT);
                break;
            case "NORMAL":
                spgPlot.setDomainAxis(1, timeAxis1);
                spgPlot.setDomainAxisLocation(1, AxisLocation.BOTTOM_OR_LEFT);
                spgPlot.setRangeAxis(1, freqAxis1);
                spgPlot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_LEFT);
                spgPlot.getDomainAxis().setVisible(false);
                spgPlot.getRangeAxis().setVisible(false);
                break;
        }

        final JFreeChart spgChart = new JFreeChart(spgPlot);
        //spgChart.removeLegend();
        final ChartPanel spgPanel = Plot.createChartPanel(spgChart);
        spgPanel.setPreferredSize(new Dimension(800,400));
        this.setContent(spgPanel);
    }
    public static final class Default {
        public static final double duration = 5.0;   /* seconds */
        public static final double freqMax = 2000.0;//4000.0; /* Hz */
        public static final double specLogMin = -100.0;
        public static final double specLogMax = 0.0;
    }

    private static final class OnlineSpectrogramPlot extends XYPlot {

        private static final long serialVersionUID = 3023546826798901921L;

        private final DatasetChangeEvent event = new DatasetChangeEvent(this, null);

        private final int frames;
        private final int bins;
        private final ColorMapIntARGB colormap;

        private final BufferedImage image;
        private final int[] dataElements;

        private OnlineSpectrogramPlot(
                final NumberAxis timeAxis,
                final NumberAxis freqAxis,
                final int frames,
                final int bins,
                final ColorMapIntARGB colormap
        ) {
            super(
				  /* dataset    = */ null,
				  /* domainAxis = */ timeAxis,
				  /* rangeAxis  = */ freqAxis,
				  /* renderer   = */ null
            );
            this.frames = frames;
            this.bins = bins;
            this.colormap = colormap;
            this.image = new BufferedImage(
										   /* width      = */ frames,
										   /* height     = */ bins,
										   /* imageType  = */ BufferedImage.TYPE_INT_ARGB
            );
            this.dataElements = new int[frames * bins];

            setDataset(new DefaultXYDataset()); /* render() ã‚’å‘¼ã³å‡ºã•ã›ã‚‹ãŸã‚ã®ãƒ€ãƒŸãƒ¼ */
        }

        public final void addSpectrum(final Complex[] spectrum) {
            final int[] specImg = Arrays.stream(spectrum)
                    .limit(bins)
                    .mapToDouble(Complex::abs)
                    .map(x -> 20.0 * Math.log10(x))
                    .mapToInt(colormap::map)
                    .toArray();
            for (int i = 0; i < bins; i++) {
                System.arraycopy(dataElements, (i * frames) + 1,
                        dataElements, (i * frames) + 0,
                        frames - 1);
                dataElements[(i * frames) + frames - 1] = specImg[bins - 1 - i];
            }
            datasetChanged(event); /* render() ã‚’å‘¼ã³å‡ºã™ */
        }

        @Override /* XYPlot */
        public final boolean render(
                final Graphics2D g2,
                final Rectangle2D dataArea,
                final int index,
                final org.jfree.chart.plot.PlotRenderingInfo info,
                final org.jfree.chart.plot.CrosshairState crosshairState
        ) {
            image.getRaster().setDataElements(
											  /* x = */ 0,
											  /* y = */ 0,
											  /* w = */ frames,
											  /* h = */ bins,
											  /* inData = */ dataElements
            );
            return g2.drawImage(
								/* img   = */ image,
								/* xform = */ new AffineTransform(
																  /* m00 = */ dataArea.getWidth() / frames,
																  /* m10 = */ 0.0,
																  /* m01 = */ 0.0,
																  /* m11 = */ dataArea.getHeight() / bins,
																  /* m02 = */ dataArea.getX(),
																  /* m12 = */ dataArea.getY()
                    ),
								/* obs   = */ null
            );
        }

    }

    public void UpdateDataSource(final double[] waveform, final double sampleRate) {
        try {
            ChartPanel cp = (ChartPanel)this.getContent();
            JFreeChart chart = (JFreeChart)cp.getChart();
            OnlineSpectrogramPlot plot = (OnlineSpectrogramPlot) chart.getPlot();
            final double[] window = MathArrays.normalizeArray(Le4MusicUtils.hanning(this.framesize), 1.0);
            final double[] wframe = MathArrays.ebeMultiply(waveform, window);
            final int fftSize = 1 << Le4MusicUtils.nextPow2(this.framesize);
            final Complex[] spectrum = Le4MusicUtils.rfft(Arrays.copyOf(wframe, fftSize));
            plot.addSpectrum(spectrum);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
