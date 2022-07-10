package org.yangbo;

import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yangbo.wav.WavFile;

import java.io.File;

/**
 * 使用 WavFile 类写入定长声音采样数据.
 * <p>
 * 初始化时指定 WAV 数据长度，一旦读取到足够的数据就会生成 WAV 文件。
 * </p>
 */
public class WaveWriter<T extends Number> implements Callback<T[], Void> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final File wavFile;
    private final int wavSampleCount;
    // 采样率
    private final int sampleRate;
    private int[] samples;
    // 写入 samples 数组的偏移量，每次写入后都会移动
    private int fillOffset;
    // 是否已经写过 wav 文件了，如果写过了(true)就不要再写入
    private boolean written;

    /**
     * 指定 WAV 采样个数，一旦读取到足够的数据就会生成 WAV 文件。
     *
     * @param wavFile     生成的 WAV 文件路径，如果存在则覆盖
     * @param wavSampleCount 要写入的 sample 个数
     */
    public WaveWriter(File wavFile, int wavSampleCount, int sampleRate) {
        this.wavFile = wavFile;
        this.wavSampleCount = wavSampleCount;
        this.sampleRate = sampleRate;
        samples = new int[wavSampleCount];
    }

    @Override
    public Void call(T[] newSamples) {
        int writeCount = this.fillOffset + newSamples.length >= this.wavSampleCount ?
                // 超了，只能取部分数据
                this.wavSampleCount - this.fillOffset :
                // 没超，取全部数据
                newSamples.length;
        // 写入缓存
        for (int i = 0; i < writeCount; i++) {
            // 2 倍增益
            this.samples[this.fillOffset++] = newSamples[i].intValue();
        }
        // 如果数量足够就写文件
        if (fillOffset == wavSampleCount && !written) {
            writeWavFile();
            written = true;
            System.exit(0);
        }
        return null;
    }

    private void writeWavFile() {
        logger.debug("开始写 WAV 文件：{}", this.wavFile);
        try {
            // Calculate the number of frames required for specified duration
            // sample count == frame count
            long numFrames = this.wavSampleCount;

            // Create a wav file with the name specified as the first argument
            WavFile wavFile = WavFile.newWavFile(this.wavFile, 1, numFrames, 16, sampleRate);

            // Write the buffer
            wavFile.writeFrames(this.samples, this.wavSampleCount);

            // Close the wavFile
            wavFile.close();
        } catch (Exception e) {
            logger.error("写 WAV 文件 {} 失败！", this.wavFile, e);
        }
    }
}
