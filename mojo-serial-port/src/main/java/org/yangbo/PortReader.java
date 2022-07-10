package org.yangbo;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;

public class PortReader {

    private static Logger logger = LoggerFactory.getLogger(PortReader.class);


    /**
     * 非阻塞模式读取
     */
    public void nonBlockRead() {
        SerialPort comPort = SerialPort.getCommPorts()[0];
        comPort.openPort();
        try {
            // send H to serial port COM3
            byte[] hello = new byte[]{'h'};

            comPort.writeBytes(hello, hello.length);

            while (true) {
                while (comPort.bytesAvailable() == 0) {
                    Thread.sleep(20);
                }

                byte[] readBuffer = new byte[comPort.bytesAvailable()];
                int numRead = comPort.readBytes(readBuffer, readBuffer.length);
                System.out.println("Read " + numRead + " bytes. " + Arrays.toString(readBuffer));
                System.out.println(new String(readBuffer));

                // 继续送
                comPort.writeBytes(hello, hello.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        comPort.closePort();
    }

    /**
     * 半阻塞模式读取
     */
    public void semiBlockRead() {
        SerialPort comPort = SerialPort.getCommPorts()[0];

        // 设置波特率:
        // comPort.setBaudRate(1_000_000);
        // comPort.setBaudRate(9600);

        comPort.openPort();

        // 设置阻塞模式:
        // 半阻塞读取，只要读到一个字节就会返回，阻塞会超时，超时不会抛出异常
        // comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);

        // 会block住，直到读取完指定长度的字节，但会超时，超时不会抛出异常。适合提起知道要读取的字节长度这种情形。将 newReadTimeout设置为0则不会超时
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);

        // stream 模式读取，超时后会抛出异常

        try {
            while (true) {
                // send H to serial port COM3
                byte[] hello = new byte[]{'h'};
                comPort.writeBytes(hello, hello.length);
                byte[] readBuffer = new byte[14];
                int numRead = comPort.readBytes(readBuffer, readBuffer.length);
                System.out.println("Read " + numRead + " bytes. " + LocalDateTime.now());
                System.out.println("Read: " + new String(readBuffer, 0, numRead));
                //Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        comPort.closePort();
    }

    public void streamRead() {
        SerialPort comPort = SerialPort.getCommPorts()[0];
        comPort.openPort();
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        InputStream in = comPort.getInputStream();
        try {
            comPort.writeBytes(new byte[]{'h'}, 1);
            for (int j = 0; j < 1000; ++j) {
                // 注意，in.read() 并不会返回 -1，只会挂起
                System.out.print((char) in.read());
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        comPort.closePort();
    }

    public void eventBasedRead() {
        SerialPort comPort = SerialPort.getCommPorts()[0];
        comPort.openPort();
        // addDataListener() 会启动一个非daemon线程，读取端口数据，如果没有数据，就会一直等待，不会超时退出
        comPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;
                byte[] newData = new byte[comPort.bytesAvailable()];
                int numRead = comPort.readBytes(newData, newData.length);
                System.out.println("Read " + numRead + " bytes. " + Arrays.toString(newData));
                //System.out.println("Read: " + new String(newData, 0, numRead));
            }
        });
        //comPort.writeBytes(new byte[]{'h'}, 1);
    }

    /**
     * 按字节读取串口数据
     *
     * @param callback 读取到数据时会调用
     */
    public void eventBasedReadByte(Callback<Byte[], Void> callback) {
        SerialPort comPort = SerialPort.getCommPorts()[0];
        logger.debug("Opened Port: {}", comPort.getDescriptivePortName());

        comPort.openPort();
        // addDataListener() 会启动一个非daemon线程，读取端口数据，如果没有数据，就会一直等待，不会超时退出
        comPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                try {
                    if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;
                    byte[] newData = new byte[comPort.bytesAvailable()];
                    int numRead = comPort.readBytes(newData, newData.length);
                    Byte[] newByteData = new Byte[numRead];
                    for (int i = 0; i < numRead; i++) {
                        newByteData[i] = newData[i];
                    }
                    callback.call(newByteData);
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        PortReader portReader = new PortReader();
        portReader.eventBasedReadByte(bytes -> {
            logger.debug("{} bytes：{}", bytes.length, Arrays.toString(bytes));
            return null;
        });
        // portReader.streamRead();
        // portReader.semiBlockRead();
        //portReader.nonBlockRead();
        System.out.println("main thread exit!");
    }

    /**
     * 按 16bits short 方式读取串口数据，数据是 Big-Endian 字节序。
     *
     * @param shortCallback 回调函数，当从串口读到数据时会调用
     */
    public void eventBasedReadShort(Callback<Short[], Void> shortCallback) {
        SerialPort comPort = SerialPort.getCommPorts()[0];
        comPort.openPort();
        // addDataListener() 会启动一个非daemon线程，读取端口数据，如果没有数据，就会一直等待，不会超时退出
        comPort.addDataListener(new SerialPortDataListener() {

            /**
             * 上一次读取的字节，没能组成 short 的最后一个字节，要和下次的第一个字节组成 short
             */
            private byte lastByte;
            /**
             * 是否有上一次剩下的字节
             */
            private boolean hasLastByte;

            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;
                int bytesToRead = comPort.bytesAvailable();
                int bufLen = bytesToRead;
                long offset = 0;
                if (hasLastByte) {
                    bufLen += 1;
                    offset = 1;
                }
                byte[] newData = new byte[bufLen];
                int numRead = comPort.readBytes(newData, bytesToRead, offset);
                // 加上上次遗留的字节
                if (hasLastByte) {
                    newData[0] = lastByte;
                    hasLastByte = false;
                }
                // 转为short
                int shortCount = numRead / 2;
                Short[] newShortData = new Short[shortCount];
                for (int i = 0; i < shortCount; i++) {
                    // big-endian
                    short high = (short) (0x00FF & newData[i * 2]);
                    short low = (short) (0x00FF & newData[i * 2 + 1]);
                    high <<= 8;
                    high = (short) (high | low);
                    newShortData[i] = high;
                }
                if (numRead % 2 == 1) {
                    // 会剩下一个字节
                    hasLastByte = true;
                    lastByte = newData[numRead - 1];
                }
                shortCallback.call(newShortData);
            }
        });
    }
}
