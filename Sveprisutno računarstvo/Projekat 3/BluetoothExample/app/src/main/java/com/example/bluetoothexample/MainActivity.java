package com.example.bluetoothexample;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static void readData(InputStream inputStream) {
        byte[] bytesHumidity = new byte[4];
        byte[] bytesTemperature = new byte[4];
        while (true) {
            try {
                inputStream.read(bytesHumidity);
                inputStream.read(bytesTemperature);
            } catch (IOException e) {
                e.printStackTrace();
            }
            float humidity = ByteBuffer.wrap(bytesHumidity).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            //float temperature = ByteBuffer.wrap(bytesTemperature).getFloat();
            float temperature = ByteBuffer.wrap(bytesTemperature).order(ByteOrder.LITTLE_ENDIAN).getFloat();

            System.out.println("Humidity is " + humidity);
            System.out.println("Temperature is " + temperature);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        System.out.println(btAdapter.getBondedDevices());

        BluetoothDevice hc05 = btAdapter.getRemoteDevice("00:21:13:02:B6:5B");
        System.out.println(hc05.getName());

        BluetoothSocket btSocket = null;
        int counter = 0;
        do {
            try {
                btSocket = hc05.createRfcommSocketToServiceRecord(mUUID);
                System.out.println(btSocket);
                btSocket.connect();
                System.out.println(btSocket.isConnected());
            } catch (IOException e) {
                e.printStackTrace();
            }
            counter++;
        } while (!btSocket.isConnected() && counter < 3);


        /*try {
            OutputStream outputStream = btSocket.getOutputStream();
            outputStream.write(48);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        InputStream inputStream = null;
        try {
            inputStream = btSocket.getInputStream();
            inputStream.skip(inputStream.available());

            readData(inputStream);
            /*for (int i = 0; i < 26; i++) {

                byte b = (byte) inputStream.read();
                System.out.println((char) b);

            }*/

        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            btSocket.close();
            System.out.println(btSocket.isConnected());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}