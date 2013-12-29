package com.tkjelectronics.arduino.colorlightscontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;

public class MainActivity extends Activity {

    TextView statusLabel;
    EditText speedText;

    static int fadeColorsCount = 0;
    TextView fadeColorsCountLabel;

    static int snapColorsCount = 0;
    TextView snapColorsCountLabel;

    static int runColorsCount = 0;
    TextView runColorsCountLabel;

    static int runFadeColorsCount = 0;
    TextView runFadeColorsCountLabel;

    static int previousColor = 0;
    View viewNewColor;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice = null;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    Thread rapidThread;
    volatile boolean stopRapidWorker;
    private Handler serialHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button openButton = (Button) findViewById(R.id.btn_open);
        Button closeButton = (Button) findViewById(R.id.btn_close);
        statusLabel = (TextView) findViewById(R.id.statusText);
        viewNewColor = findViewById(R.id.colorView);
        Button mShow = (Button) findViewById(R.id.btn_show);
        fadeColorsCountLabel = (TextView) findViewById(R.id.fadeCount);
        snapColorsCountLabel = (TextView) findViewById(R.id.snapCount);
        runColorsCountLabel = (TextView) findViewById(R.id.runCount);
        runFadeColorsCountLabel = (TextView) findViewById(R.id.runFadeCount);
        Button disableEffectsButton = (Button) findViewById(R.id.btn_disableEffects);
        Button enableFadeButton = (Button) findViewById(R.id.btn_startFade);
        Button addFadeColorButton = (Button) findViewById(R.id.btn_addFade);
        Button fadeResetButton = (Button) findViewById(R.id.btn_fadeReset);
        Button enableSnapButton = (Button) findViewById(R.id.btn_startSnap);
        Button addSnapColorButton = (Button) findViewById(R.id.btn_addSnap);
        Button snapResetButton = (Button) findViewById(R.id.btn_snapReset);

        Button enableRunButton = (Button) findViewById(R.id.btn_startRun);
        Button addRunColorButton = (Button) findViewById(R.id.btn_addRun);
        Button runResetButton = (Button) findViewById(R.id.btn_runReset);

        Button enableRunFadeButton = (Button) findViewById(R.id.btn_startRunFade);
        Button addRunFadeColorButton = (Button) findViewById(R.id.btn_addRunFade);
        Button runFadeResetButton = (Button) findViewById(R.id.btn_runFadeReset);

        Button setSpeedButton = (Button) findViewById(R.id.btn_setSpeed);
        speedText = (EditText) findViewById(R.id.speedText);
        Button blackOutButton = (Button) findViewById(R.id.btn_blackOut);

        // Pick color button
        mShow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isBTconnected()) {
                    startRapidData();
                    LiveColorPicker();
                }
            }
        });

        // Open Button
        openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    findBT();
                    openBT();
                } catch (IOException ignored) {
                }
            }
        });

        // Close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    closeBT();
                } catch (IOException ignored) {
                }
            }
        });

        // Disable effects button
        disableEffectsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (isBTconnected())
                        DisableEffects();
                } catch (IOException ignored) {
                }
            }
        });

        // Black out button
        blackOutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (isBTconnected())
                        DisableEffects();
                    BlackOut();
                } catch (IOException ignored) {
                }
            }
        });

        // Enable fade effect button
        enableFadeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (isBTconnected())
                        EnableFadeEffect();
                } catch (IOException ignored) {
                }
            }
        });

        // Add fade color button
        addFadeColorButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isBTconnected())
                    FadeColorAdd();
            }
        });

        // Reset fade colors button
        fadeResetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (isBTconnected())
                        ResetFadeColors();
                    fadeColorsCount = 0;
                    fadeColorsCountLabel.setText(Integer.toString(fadeColorsCount));
                } catch (IOException ignored) {
                }
            }
        });

        // Reset fade colors button
        setSpeedButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (isBTconnected() && speedText.getText() != null)
                        SetSpeed(Integer.valueOf(speedText.getText().toString()));
                } catch (IOException ignored) {
                }
            }
        });

        // Enable snap effect button
        enableSnapButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (isBTconnected())
                        EnableSnapEffect();
                } catch (IOException ignored) {
                }
            }
        });

        // Add snap color button
        addSnapColorButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isBTconnected())
                    SnapColorAdd();
            }
        });

        // Reset snap colors button
        snapResetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (isBTconnected())
                        ResetSnapColors();
                    snapColorsCount = 0;
                    snapColorsCountLabel.setText(Integer.toString(snapColorsCount));
                } catch (IOException ignored) {
                }
            }
        });

        // Enable running effect button
        enableRunButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (isBTconnected())
                        EnableRunEffect();
                } catch (IOException ignored) {
                }
            }
        });

        // Add running color button
        addRunColorButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isBTconnected())
                    RunColorAdd();
            }
        });

        // Reset running colors button
        runResetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (isBTconnected())
                        ResetRunColors();
                    runColorsCount = 0;
                    runColorsCountLabel.setText(Integer.toString(runColorsCount));
                } catch (IOException ignored) {
                }
            }
        });

        // Enable running fade effect button
        enableRunFadeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (isBTconnected())
                        EnableRunFadeEffect();
                } catch (IOException ignored) {
                }
            }
        });

        // Add running fade color button
        addRunFadeColorButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isBTconnected())
                    RunFadeColorAdd();
            }
        });

        // Reset running fade colors button
        runFadeResetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (isBTconnected())
                        ResetRunFadeColors();
                    runFadeColorsCount = 0;
                    runFadeColorsCountLabel.setText(Integer.toString(runFadeColorsCount));
                } catch (IOException ignored) {
                }
            }
        });
    }


    public void LiveColorPicker() {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, (MainActivity.previousColor | 0xFF000000), new OnAmbilWarnaListener() {

            // Executes, when user click Cancel button
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                stopRapidWorker = true;
                //serialHandler.removeCallbacks(mUpdateTimeTask);
            }

            // Executes, when user click OK button
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                //serialHandler.removeCallbacks(mUpdateTimeTask);
                stopRapidWorker = true;
            }

            @Override
            public void onChange(AmbilWarnaDialog dialog, int color) {
                MainActivity.previousColor = color;
                viewNewColor.setBackgroundColor(color);
            }

        });
        dialog.show();
    }

    public void FadeColorAdd() {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, (MainActivity.previousColor | 0xFF000000), new OnAmbilWarnaListener() {

            // Executes, when user click Cancel button
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            // Executes, when user click OK button
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                MainActivity.previousColor = color;
                try {
                    AddFadeColor(color);
                    fadeColorsCount++;
                    fadeColorsCountLabel.setText(Integer.toString(MainActivity.fadeColorsCount));
                } catch (IOException ignored) {
                }
            }

            @Override
            public void onChange(AmbilWarnaDialog dialog, int color) {

            }

        });
        dialog.show();
    }

    public void SnapColorAdd() {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, (MainActivity.previousColor | 0xFF000000), new OnAmbilWarnaListener() {

            // Executes, when user click Cancel button
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            // Executes, when user click OK button
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                MainActivity.previousColor = color;
                try {
                    AddSnapColor(color);
                    snapColorsCount++;
                    snapColorsCountLabel.setText(Integer.toString(MainActivity.snapColorsCount));
                } catch (IOException ignored) {
                }
            }

            @Override
            public void onChange(AmbilWarnaDialog dialog, int color) {

            }

        });
        dialog.show();
    }

    public void RunColorAdd() {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, (MainActivity.previousColor | 0xFF000000), new OnAmbilWarnaListener() {

            // Executes, when user click Cancel button
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            // Executes, when user click OK button
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                MainActivity.previousColor = color;
                try {
                    AddRunColor(color);
                    runColorsCount++;
                    runColorsCountLabel.setText(Integer.toString(MainActivity.runColorsCount));
                } catch (IOException ignored) {
                }
            }

            @Override
            public void onChange(AmbilWarnaDialog dialog, int color) {

            }

        });
        dialog.show();
    }

    public void RunFadeColorAdd() {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, (MainActivity.previousColor | 0xFF000000), new OnAmbilWarnaListener() {

            // Executes, when user click Cancel button
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            // Executes, when user click OK button
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                MainActivity.previousColor = color;
                try {
                    AddRunFadeColor(color);
                    runFadeColorsCount++;
                    runFadeColorsCountLabel.setText(Integer.toString(MainActivity.runFadeColorsCount));
                } catch (IOException ignored) {
                }
            }

            @Override
            public void onChange(AmbilWarnaDialog dialog, int color) {

            }

        });
        dialog.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }


    boolean isBTconnected() {
        return mmDevice != null && mBluetoothAdapter != null && mmSocket != null;
    }

    void findBT() {
        mmDevice = null;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            statusLabel.setText("No bluetooth adapter available");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName() != null && device.getName().equals("quadcopter")) {
                    mmDevice = device;
                    break;
                }
            }
        }
        statusLabel.setText("Bluetooth Device Found");
    }

    void openBT() throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        beginListenForData();

        statusLabel.setText("Bluetooth Opened");
    }

    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            int nbytes = mmInputStream.read(packetBytes, 0, bytesAvailable);
                            for (int i = 0; i < nbytes; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            statusLabel.setText(data);
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    void closeBT() throws IOException {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        statusLabel.setText("Bluetooth Closed");
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            try {
                String msg = Long.toString(previousColor);
                msg += "\n";
                mmOutputStream.write(msg.getBytes());

                serialHandler.postDelayed(mUpdateTimeTask, 100);
            } catch (IOException ex) {
                stopWorker = true;
            }

            //serialHandler.postAtTime(this, start + (((minutes * 60) + seconds + 1) * 1000));
        }
    };

    void startRapidData() {
        stopRapidWorker = false;
        rapidThread = new Thread(new Runnable() {
            public void run() {
                int red;
                int green;
                int blue;

                while (!Thread.currentThread().isInterrupted() && !stopRapidWorker) {
                    try {
                        red = ((previousColor >> 16) & 0xFF);
                        green = ((previousColor >> 8) & 0xFF);
                        blue = (previousColor & 0xFF);
                        //alpha= (int) ((tempColor >> 24) & 0xFF);
                        //String msg = Long.toString(tempColor);
                        String msg = String.format("%d%03d;%03d;%03d;", 0x0F, red, green, blue);  // 0x0A = Color command
                        mmOutputStream.write(msg.getBytes());

                           /*buffer[0] = red;
                           buffer[1] = green;
                           buffer[2] = blue;
                           buffer[3] = 0x0A;*/
                        //mmOutputStream.write(buffer);
                    } catch (IOException ex) {
                        stopRapidWorker = true;
                    }

                    try {
                        Thread.sleep(100, 0);
                    } catch (InterruptedException e) {
                        stopRapidWorker = true;
                    }
                }
            }
        });

        rapidThread.start();
    }

    void BlackOut() throws IOException {
        String msg = String.format("%d%03d;%03d;%03d;", 0x0F, 0, 0, 0);  // 0x0A = Color set command
        mmOutputStream.write(msg.getBytes());
    }

    void AddFadeColor(int color) throws IOException {
        int red = ((color >> 16) & 0xFF);
        int green = ((color >> 8) & 0xFF);
        int blue = (color & 0xFF);
        //int alpha= (int) ((color >> 24) & 0xFF);

        String msg = String.format("%d%03d;%03d;%03d;", 0x01, red, green, blue);  // 0x01 = Fade color command
        mmOutputStream.write(msg.getBytes());
    }

    void DisableEffects() throws IOException {
        byte buffer[] = new byte[1];
        buffer[0] = 0x0E;
        mmOutputStream.write(buffer);
    }

    void EnableFadeEffect() throws IOException {
        byte buffer[] = new byte[1];
        buffer[0] = 0x04;
        mmOutputStream.write(buffer);
    }

    void ResetFadeColors() throws IOException {
        byte buffer[] = new byte[1];
        buffer[0] = 0x02;
        mmOutputStream.write(buffer);
    }

    void SetSpeed(int speed) throws IOException {
        if (speed < 100000) {
            String msg = String.format("%d%05d;", 0x03, speed);  // 0x03 = Speed command
            mmOutputStream.write(msg.getBytes());
        }
    }

    void EnableSnapEffect() throws IOException {
        byte buffer[] = new byte[1];
        buffer[0] = 0x07;
        mmOutputStream.write(buffer);
    }

    void ResetSnapColors() throws IOException {
        byte buffer[] = new byte[1];
        buffer[0] = 0x06;
        mmOutputStream.write(buffer);
    }

    void AddSnapColor(int color) throws IOException {
        int red = ((color >> 16) & 0xFF);
        int green = ((color >> 8) & 0xFF);
        int blue = (color & 0xFF);
        //int alpha= (int) ((color >> 24) & 0xFF);

        String msg = String.format("%d%03d;%03d;%03d;", 0x05, red, green, blue);  // 0x01 = Snap color command
        mmOutputStream.write(msg.getBytes());
    }

    void EnableRunEffect() throws IOException {
        byte buffer[] = new byte[1];
        buffer[0] = 0x0A;
        mmOutputStream.write(buffer);
    }

    void ResetRunColors() throws IOException {
        byte buffer[] = new byte[1];
        buffer[0] = 0x09;
        mmOutputStream.write(buffer);
    }

    void AddRunColor(int color) throws IOException {
        int red = ((color >> 16) & 0xFF);
        int green = ((color >> 8) & 0xFF);
        int blue = (color & 0xFF);
        //int alpha= (int) ((color >> 24) & 0xFF);

        String msg = String.format("%d%03d;%03d;%03d;", 0x08, red, green, blue);  // 0x01 = Run color command
        mmOutputStream.write(msg.getBytes());
    }

    void EnableRunFadeEffect() throws IOException {
        byte buffer[] = new byte[1];
        buffer[0] = 0x0D;
        mmOutputStream.write(buffer);
    }

    void ResetRunFadeColors() throws IOException {
        byte buffer[] = new byte[1];
        buffer[0] = 0x0C;
        mmOutputStream.write(buffer);
    }

    void AddRunFadeColor(int color) throws IOException {
        int red = ((color >> 16) & 0xFF);
        int green = ((color >> 8) & 0xFF);
        int blue = (color & 0xFF);
        //int alpha= (int) ((color >> 24) & 0xFF);

        String msg = String.format("%d%03d;%03d;%03d;", 0x0B, red, green, blue);  // 0x01 = Run color command
        mmOutputStream.write(msg.getBytes());
    }

}

