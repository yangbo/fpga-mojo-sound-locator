package org.yangbo;

import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;

import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;

/**
 * 用 Java Sound API 把串口数据写入 wave 文件。不可用，会导致 JVM 退出。
 */
@Deprecated
public class WaveWriterByJSA implements Callback<Short[], Void> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private File waveFile;

    // 输入缓存，接收从 PortReader 得到的数据，送给 AudioInputStream.
    private PipedOutputStream pipedOutputStream = new PipedOutputStream();
    private InputStream inputStream = new PipedInputStream(pipedOutputStream);
    private AudioInputStream audioInputStream;

    private Thread writeThread;

    /**
     * 用指定的 wave 文件构造 WaveWriter 对象
     *
     * @param waveFile 要写入的 wave 文件
     */
    public WaveWriterByJSA(File waveFile) throws IOException {
        this.waveFile = waveFile;
        float sampleRate = 250;
        int sampleSizeInBits = 16;
        int channels = 1;
        int frameSize = 2; // 等于 sample bytes
        float frameRate = sampleRate;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(PCM_SIGNED, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);
        audioInputStream = new AudioInputStream(inputStream, format, AudioSystem.NOT_SPECIFIED);
    }

    /**
     * 启动线程，开始写入数据
     */
    public void startWrite() {
        // 启动线程写入
        //this.writeThread = new Thread(() -> {
            try {
                logger.debug("启动写入wave线程...");
                AudioFileFormat.Type fileType = getWaveType();
                if (fileType == null) {
                    logger.error("不能获取 Wave Type！");
                    return;
                }
                if (AudioSystem.isFileTypeSupported(fileType, audioInputStream)) {
                    AudioSystem.write(audioInputStream, fileType, this.waveFile);
                    logger.debug("wave 写入结束！");
                }
            } catch (Throwable e) {
                logger.error("写 wave 文件 {} 失败！", this.waveFile, e);
            }
        //}, "wave-writer");
//        this.writeThread.setDaemon(true);
//        this.writeThread.start();
    }

    private AudioFileFormat.Type getWaveType() {
        for (AudioFileFormat.Type type : AudioSystem.getAudioFileTypes()) {
            if (type.toString().equalsIgnoreCase("wave")) {
                return type;
            }
        }
        return null;
    }

    /**
     * 写入收到的 samples
     *
     * @param samples 收到的采样数据
     */
    @Override
    public Void call(Short[] samples) {
        try {
            logger.debug("写入 samples {} 个", samples.length);
            for (Short sample : samples) {
                // 按 big-endian 写入
                this.pipedOutputStream.write((sample >> 8) & 0x00FF);
                this.pipedOutputStream.write(sample & 0x00FF);
            }
        } catch (Exception exp) {
            logger.error("写入 PipedOutputStream 失败！", exp);
        }
        return null;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        for (AudioFileFormat.Type type : AudioSystem.getAudioFileTypes()) {
            if (type.toString().equalsIgnoreCase("wave")) {
                System.out.println(type);
            }
        }
        // 测试写入
        WaveWriterByJSA waveWriterByJSA = new WaveWriterByJSA(new File("test.wav"));
        waveWriterByJSA.startWrite();
        waveWriterByJSA.call(new Short[]{0x1234, 0x5678});
        Thread.sleep(10000);
    }
}
