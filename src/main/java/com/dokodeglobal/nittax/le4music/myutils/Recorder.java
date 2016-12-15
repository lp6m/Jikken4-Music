//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.dokodeglobal.nittax.le4music.myutilsr;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleUnaryOperator;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.Mixer.Info;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.WavWriter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Recorder {
    private final TargetDataLine line;
    private final WavWriter writer;
    private final AudioFormat format;
    private final int bytesPerSample;
    private final ByteOrder order;
    private final int bufSize;
    private int position;
    private final double frameDuration;
    private final int frameSize;
    private final double[] frame;
    private final ScheduledExecutorService executor;
    private final transient byte[] buffer;
    private final transient ByteBuffer bb;
    private boolean updated = false;

    private Recorder(TargetDataLine line, WavWriter writer, AudioFormat format, double frameDuration, ScheduledExecutorService executor) throws UnsupportedAudioFileException, LineUnavailableException {
        this.line = line;
        this.writer = writer;
        this.format = format;
        this.frameDuration = frameDuration;
        this.executor = executor;
        this.bytesPerSample = format.getSampleSizeInBits() >> 3;
        this.order = format.isBigEndian()?ByteOrder.BIG_ENDIAN:ByteOrder.LITTLE_ENDIAN;
        float sampleRate = format.getSampleRate();
        this.frameSize = (int)(frameDuration * (double)sampleRate);
        this.frame = new double[this.frameSize];
        if(this.bytesPerSample != 2) {
            throw new UnsupportedAudioFileException("this class supports only 16-bit quantization");
        } else {
            if(!line.isOpen()) {
                line.open(format);
            }

            this.bufSize = line.getBufferSize();
            this.buffer = new byte[this.bufSize * this.bytesPerSample];
            this.bb = ByteBuffer.wrap(this.buffer);
            this.bb.order(this.order);
        }
    }

    public static final Recorder newRecorder(double sampleRate, double frameDuration, Info mixerInfo, File wavFile) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        AudioFormat format = new AudioFormat((float)sampleRate, 16, 1, true, false);
        TargetDataLine line = AudioSystem.getTargetDataLine(format, mixerInfo);
        WavWriter writer = wavFile == null?null:WavWriter.newWavWriter(wavFile, format);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            public final Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(false);
                t.setPriority(10);
                return t;
            }
        });
        return new Recorder(line, writer, format, frameDuration, executor);
    }

    public static final Recorder newRecorder() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        return newRecorder(16000.0D, 0.4D, (Info)null, (File)null);
    }

    public final AudioFormat getAudioFormat() {
        return this.format;
    }

    public double getSampleRate() {
        return (double)this.format.getSampleRate();
    }

    public double getFrameDuration() {
        return this.frameDuration;
    }

    public int getFrameSize() {
        return this.frameSize;
    }

    public final boolean isUpdated() {
        boolean var1;
        try {
            var1 = this.updated;
        } finally {
            this.updated = false;
        }

        return var1;
    }

    private void record() {
        while(this.line.available() > 0) {
            int bytesRead = this.line.read(this.buffer, 0, this.line.available());
            if(bytesRead == -1) {
                this.stop();
            }

            int samplesRead = bytesRead / this.bytesPerSample;
            this.position += samplesRead;

            try {
                if(this.writer != null) {
                    this.writer.append(this.buffer, 0, bytesRead);
                }
            } catch (IOException var6) {
                var6.printStackTrace();
            }

            this.bb.limit(bytesRead);
            double[] e = this.frame;
            synchronized(this.frame) {
                int i;
                if(samplesRead < this.frameSize) {
                    System.arraycopy(this.frame, samplesRead, this.frame, 0, this.frameSize - samplesRead);

                    for(i = this.frameSize - samplesRead; i < this.frameSize; ++i) {
                        this.frame[i] = (double)this.bb.getShort() / 32768.0D;
                    }
                } else {
                    this.bb.position((samplesRead - this.frameSize) * this.bytesPerSample);

                    for(i = 0; i < this.frameSize; ++i) {
                        this.frame[i] = (double)this.bb.getShort() / 32768.0D;
                    }
                }
            }

            this.bb.clear();
            this.updated = true;
        }

    }

    public int position() {
        return this.position;
    }

    public double[] latestFrame() {
        double[] var1 = this.frame;
        synchronized(this.frame) {
            return (double[])this.frame.clone();
        }
    }

    public void start(long initialDelay, long updateDelay, TimeUnit timeUnit) {
        this.line.start();
        this.executor.scheduleWithFixedDelay(this::record, initialDelay, updateDelay, timeUnit);
    }

    public void start() {
        this.start(0L, 1L, Recorder.Default.timeUnit);
    }

    public void stop() {
        this.executor.shutdown();
        this.line.drain();
        this.line.stop();
        this.line.close();
    }

    public boolean isOpen() {
        return this.line.isOpen();
    }

    public boolean isRunning() {
        return this.line.isRunning();
    }

    public boolean isActive() {
        return this.line.isActive();
    }

    public static final void main(String[] args) throws IOException, LineUnavailableException, UnsupportedAudioFileException, ParseException {
        Options options = new Options();
        options.addOption("h", "help", false, "display this help and exit");
        options.addOption("m", "mixer", true, "Index of the Mixer object that supplies a TargetDataLine object. To check the proper index, use CheckAudioSystem");
        options.addOption("o", "outfile", true, "output file");
        options.addOption("r", "rate", true, "sampling rate [Hz]");
        options.addOption("f", "frame", true, "duration of frame [seconds]");
        options.addOption("v", "verbose", false, "verbose output");
        CommandLine cmd = (new DefaultParser()).parse(options, args);
        if(cmd.hasOption("h")) {
            HelpFormatter mixerInfo1 = new HelpFormatter();
            mixerInfo1.printHelp("Recorder [OPTION]...", options);
        } else {
            Info mixerInfo = cmd.hasOption("m")?AudioSystem.getMixerInfo()[Integer.parseInt(cmd.getOptionValue("m"))]:null;
            File outFile = cmd.hasOption("o")?new File(cmd.getOptionValue("o")):null;
            double sampleRate = cmd.hasOption("r")?Double.parseDouble(cmd.getOptionValue("r")):16000.0D;
            double frameDuration = cmd.hasOption("f")?Double.parseDouble(cmd.getOptionValue("f")):0.4D;
            Le4MusicUtils.verbose = cmd.hasOption("v");
            Recorder recorder = newRecorder(sampleRate, frameDuration, mixerInfo, outFile);
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleWithFixedDelay(() -> {
                double[] frame = recorder.latestFrame();
                double rms = Arrays.stream(frame).map((x) -> {
                    return x * x;
                }).average().orElse(0.0D);
                double logRms = 20.0D * Math.log10(rms);
                System.out.printf("RMS %f dB%n", new Object[]{Double.valueOf(logRms)});
            }, 0L, 100L, TimeUnit.MILLISECONDS);
            recorder.start();
        }
    }

    public static final class Default {
        public static final long initialDelay = 0L;
        public static final long updateDelay = 1L;
        public static final TimeUnit timeUnit;
        public static final double frameDuration = 0.4D;

        private Default() {
            throw new AssertionError("this class should not be instantiated");
        }

        static {
            timeUnit = TimeUnit.MILLISECONDS;
        }
    }
}
