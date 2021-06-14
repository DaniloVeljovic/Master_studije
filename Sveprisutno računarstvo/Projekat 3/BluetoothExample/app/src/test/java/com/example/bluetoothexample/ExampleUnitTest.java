package com.example.bluetoothexample;

import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    public static int LIMIT = 16;

    @Before
    public void before() throws IOException {
        OutputStream outputStream = new FileOutputStream("C:\\Users\\danil\\AndroidStudioProjects\\BluetoothExample\\app\\src\\test\\java\\com\\example\\bluetoothexample\\test.txt");
        Random r = new Random();
        byte[] toWrite;
        int i = 0;
        while(i < LIMIT) {
            toWrite = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(r.nextFloat() * 1000).array();
            outputStream.write(toWrite);
            outputStream.flush();
            i++;
        }
        outputStream.close();
    }

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void readData_isCorrect() throws IOException {
        InputStream stream = new FileInputStream("C:\\Users\\danil\\AndroidStudioProjects\\BluetoothExample\\app\\src\\test\\java\\com\\example\\bluetoothexample\\test.txt");
        MainActivity.readData(stream);
    }
}