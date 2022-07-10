package org.yangbo;

import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * 读取单字节数据的 callback.
 * <p>负责写入缓存，并更新 DataSet。</p>
 */
class ReadCallback<T extends Number> implements Callback<T[], Void> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    protected PortWave portWave;
    protected int BUF_LEN = 1024;
    protected double xValues[] = new double[BUF_LEN];
    protected double yValues[] = new double[BUF_LEN];
    protected double errors[] = new double[BUF_LEN];
    protected int filled = 0;

    public ReadCallback(PortWave portWave) {
        this.portWave = portWave;
    }

    @Override
    public Void call(T[] samples) {
        logger.debug("读到{}数据", samples.length);
        // 本次可以写入多少元素
        int writable = samples.length;
        if (filled + samples.length >= BUF_LEN) {
            // 溢出缓存，只能取部分写入
            writable = BUF_LEN - filled;
        }
        // 缓存还未满，继续积累
        for (int i = 0; i < writable; i++) {
            xValues[filled + i] = filled + i;
            yValues[filled + i] = samples[i].doubleValue();
        }
        filled += samples.length;
        if (filled >= BUF_LEN) {
            // 缓存已满，写入数据集
            portWave.getDataSet().set(xValues, yValues, errors, errors);
            // 清 0
            Arrays.fill(xValues, 0);
            Arrays.fill(yValues, 0);
            filled = 0;
        }
        return null;
    }
}
