package org.yangbo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;


/**
 * 读取串口数据并绘制成波形图
 */
public class PortWaveApplication extends Application {

    @Override
    public void start(final Stage primaryStage) {

        int maxY = (int) Math.pow(2, 14);
        //        int maxY = 256;
        PortWave portWave = new PortWave(maxY);
        PortReader portReader = new PortReader();


        // 使用较大的缓存来存放数据
        //portReader.eventBasedReadByte(new ReadCallback<>(portWave));
        ReadCallbackChain<Short> callbackChain = new ReadCallbackChain<>();
        ReadCallback<Short> waveChartCallback = new ReadCallback<>(portWave);
        int sampleRate = 9600;
        WaveWriter saveWavFileCallback = new WaveWriter(new File("wav-records/serial-3.wav"), 10*sampleRate, sampleRate);

        callbackChain.getCallbackList().add(waveChartCallback);
        callbackChain.getCallbackList().add(saveWavFileCallback);

        portReader.eventBasedReadShort(callbackChain);
        //portReader.eventBasedRead();

        final Scene scene = new Scene(portWave, 800, 600);
        primaryStage.setTitle(this.getClass().getSimpleName());
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(evt -> System.exit(0));
        primaryStage.show();
    }

    public static void main(final String[] args) {
        Application.launch(args);
    }

}
