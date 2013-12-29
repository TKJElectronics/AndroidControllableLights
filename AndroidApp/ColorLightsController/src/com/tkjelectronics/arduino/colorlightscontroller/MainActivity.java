package com.tkjelectronics.arduino.colorlightscontroller;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;

public class MainActivity extends Activity {
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // Standard SerialPortService ID

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
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
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
                openBT();
            }
        });

        // Close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                closeBT();
            }
        });

        // Disable effects button
        disableEffectsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isBTconnected())
                    DisableEffects();
            }
        });

        // Black out button
        blackOutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isBTconnected()) {
                    DisableEffects();
                    BlackOut();
                }
            }
        });

        // Enable fade effect button
        enableFadeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isBTconnected())
                    EnableFadeEffect();
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
                if (isBTconnected())
                    ResetFadeColors();
                fadeColorsCount = 0;
                fadeColorsCountLabel.setText(Integer.toString(fadeColorsCount));
            }
        });

        // Reset fade colors button
        setSpeedButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isBTconnected() && speedText.getText() != null)
                    SetSpeed(Integer.valueOf(speedText.getText().toString()));
            }
        });

        // Enable snap effect button
        enableSnapButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isBTconnected())
                    EnableSnapEffect();
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
                if (isBTconnected())
                    ResetSnapColors();
                snapColorsCount = 0;
                snapColorsCountLabel.setText(Integer.toString(snapColorsCount));
            }
        });

        // Enable running effect button
        enableRunButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isBTconnected())
                    EnableRunEffect();
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
                if (isBTconnected())
                    ResetRunColors();
                runColorsCount = 0;
                runColorsCountLabel.setText(Integer.toString(runColorsCount));
            }
        });

        // Enable running fade effect button
        enableRunFadeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isBTconnected())
                    EnableRunFadeEffect();
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
                if (isBTconnected())
                    ResetRunFadeColors();
                runFadeColorsCount = 0;
                runFadeColorsCountLabel.setText(Integer.toString(runFadeColorsCount));
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
        return mmSocket != null && mmOutputStream != null && mmInputStream != null;
    }

    void openBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            statusLabel.setText("No Bluetooth adapter available");
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        closeBT(); // Close current connection

        final ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
        final ArrayAdapter<String> deviceNames = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);

        final Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                devices.add(device);
                deviceNames.add(device.getName());
            }
        } else
            return;

        new AlertDialog.Builder(this)
                .setTitle("Select device: ")
                .setAdapter(deviceNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index) {
                        BluetoothDevice device = devices.get(index);
                        if (device != null) {
                            dialog.dismiss();
                            String toast;
                            if (connectBT(device))
                                toast = "Connected to: " + device.getName();
                            else
                                toast = "Could not connect to device";

                            if (getApplicationContext() != null)
                                Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .create().show();
    }

    boolean connectBT(BluetoothDevice device) {
        BluetoothSocket tmpSock;
        InputStream tmpIn;
        OutputStream tmpOut;

        try {
            tmpSock = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException ex) {
            return false;
        }
        mmSocket = tmpSock;

        try {
            mmSocket.connect();
        } catch (IOException ex) {
            try {
                mmSocket.close();
            } catch (IOException ignored) {
            }
            return false;
        }

        try {
            tmpOut = mmSocket.getOutputStream();
            tmpIn = mmSocket.getInputStream();
        } catch (IOException ex) {
            return false;
        }
        mmOutputStream = tmpOut;
        mmInputStream = tmpIn;

        beginListenForData();

        statusLabel.setText("Bluetooth Opened");
        return true;
    }

    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        final byte[] readBuffer = new byte[1024];

        workerThread = new Thread(new Runnable() {
            public void run() {
                byte[] packetBytes = new byte[1024];

                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        if (mmInputStream.available() > 0) {
                            int bytes = mmInputStream.read(packetBytes);
                            for (int i = 0; i < bytes; i++) {
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

    void closeBT() {
        stopWorker = true;
        if (mmOutputStream != null) {
            try {
                mmOutputStream.close();
                mmOutputStream = null;
            } catch (Exception ignored) {
            }
        }
        if (mmInputStream != null) {
            try {
                mmInputStream.close();
                mmInputStream = null;
            } catch (Exception ignored) {
            }
        }
        if (mmSocket != null) {
            try {
                mmSocket.close();
                mmSocket = null;
            } catch (Exception ignored) {
            }
        }
        statusLabel.setText("Bluetooth Closed");
    }
/*
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            String msg = Long.toString(previousColor);
            msg += "\n";
            if (!write(msg))
                stopWorker = true;

            serialHandler.postDelayed(this, 100);

            //serialHandler.postAtTime(this, start + (((minutes * 60) + seconds + 1) * 1000));
        }
    };
*/
    void startRapidData() {
        stopRapidWorker = false;
        rapidThread = new Thread(new Runnable() {
            public void run() {
                int red;
                int green;
                int blue;

                while (!Thread.currentThread().isInterrupted() && !stopRapidWorker) {
                    red = ((previousColor >> 16) & 0xFF);
                    green = ((previousColor >> 8) & 0xFF);
                    blue = (previousColor & 0xFF);
                    //alpha= (int) ((tempColor >> 24) & 0xFF);
                    //String msg = Long.toString(tempColor);
                    String msg = String.format("%d%03d;%03d;%03d;", 0x0F, red, green, blue);  // 0x0A = Color command
                    if (!write(msg))
                        stopRapidWorker = true;

                    /*buffer[0] = red;
                    buffer[1] = green;
                    buffer[2] = blue;
                    buffer[3] = 0x0A;
                    write(buffer);*/

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

    void BlackOut() {
        String msg = String.format("%d%03d;%03d;%03d;", 0x0F, 0, 0, 0);  // 0x0A = Color set command
        write(msg);
    }

    void AddFadeColor(int color) {
        int red = ((color >> 16) & 0xFF);
        int green = ((color >> 8) & 0xFF);
        int blue = (color & 0xFF);
        //int alpha= (int) ((color >> 24) & 0xFF);

        String msg = String.format("%d%03d;%03d;%03d;", 0x01, red, green, blue);  // 0x01 = Fade color command
        write(msg);
    }

    void DisableEffects() {
        byte buffer[] = new byte[1];
        buffer[0] = 0x0E;
        write(buffer);
    }

    void EnableFadeEffect() {
        byte buffer[] = new byte[1];
        buffer[0] = 0x04;
        write(buffer);
    }

    void ResetFadeColors() {
        byte buffer[] = new byte[1];
        buffer[0] = 0x02;
        write(buffer);
    }

    void SetSpeed(int speed) {
        if (speed < 100000) {
            String msg = String.format("%d%05d;", 0x03, speed);  // 0x03 = Speed command
            write(msg);
        }
    }

    void EnableSnapEffect() {
        byte buffer[] = new byte[1];
        buffer[0] = 0x07;
        write(buffer);
    }

    void ResetSnapColors() {
        byte buffer[] = new byte[1];
        buffer[0] = 0x06;
        write(buffer);
    }

    void AddSnapColor(int color) {
        int red = ((color >> 16) & 0xFF);
        int green = ((color >> 8) & 0xFF);
        int blue = (color & 0xFF);
        //int alpha= (int) ((color >> 24) & 0xFF);

        String msg = String.format("%d%03d;%03d;%03d;", 0x05, red, green, blue);  // 0x01 = Snap color command
        write(msg);
    }

    void EnableRunEffect() {
        byte buffer[] = new byte[1];
        buffer[0] = 0x0A;
        write(buffer);
    }

    void ResetRunColors() {
        byte buffer[] = new byte[1];
        buffer[0] = 0x09;
        write(buffer);
    }

    void AddRunColor(int color) {
        int red = ((color >> 16) & 0xFF);
        int green = ((color >> 8) & 0xFF);
        int blue = (color & 0xFF);
        //int alpha= (int) ((color >> 24) & 0xFF);

        String msg = String.format("%d%03d;%03d;%03d;", 0x08, red, green, blue);  // 0x01 = Run color command
        write(msg);
    }

    void EnableRunFadeEffect() {
        byte buffer[] = new byte[1];
        buffer[0] = 0x0D;
        write(buffer);
    }

    void ResetRunFadeColors() {
        byte buffer[] = new byte[1];
        buffer[0] = 0x0C;
        write(buffer);
    }

    void AddRunFadeColor(int color) {
        int red = ((color >> 16) & 0xFF);
        int green = ((color >> 8) & 0xFF);
        int blue = (color & 0xFF);
        //int alpha= (int) ((color >> 24) & 0xFF);

        String msg = String.format("%d%03d;%03d;%03d;", 0x0B, red, green, blue);  // 0x01 = Run color command
        write(msg);
    }

    boolean write(String string) {
        return write(string.getBytes());
    }

    boolean write(byte[] buffer) {
        if (mmOutputStream == null)
            return false;
        try {
            mmOutputStream.write(buffer);
        } catch (IOException ignored) {
            return false;
        }
        return true;
    }

}

