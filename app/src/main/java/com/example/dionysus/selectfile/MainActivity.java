package com.example.dionysus.selectfile;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "sMess";
    Button button;
    Button read_fl;
    TextView txt;
    String filename1 = "file_paths.csv";
    String each_file_path = "";
    private static final int FILE_SELECT_CODE = 0;
    File file;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];
    double aX,aY,aZ;
    boolean wait = false;

    private Button decrypt,stopservice,startservice;
    private String input="";
    private String filename="temp";
    private String key = "Cpp12365Cpp12349"; // 128 bit key //CLOUD
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt = findViewById(R.id.textView);
        button = findViewById(R.id.button);
        read_fl = findViewById(R.id.button2);
        decrypt = (Button)findViewById(R.id.dec);
        stopservice = (Button)findViewById(R.id.stopservice);
        startservice = (Button)findViewById(R.id.startservice);

        //Toast.makeText(getApplicationContext(),"Background service started",Toast.LENGTH_SHORT).show();

        final Intent intent = new Intent(this, BackgroundService.class);

        startservice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"Background service started",Toast.LENGTH_SHORT).show();
                startService(intent);
            }
        });

        decrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intendB = new Intent(MainActivity.this, activity_fingerprint.class);
                startActivity(intendB);
            }
        });
        stopservice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"Background service is stopped",Toast.LENGTH_SHORT).show();
                stopService(intent);
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });

        read_fl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "button 2 clicked");
                //file = new File(filename);
                file = new File(getApplicationContext().getFilesDir(), filename1);

                if (file.exists()) {
                    Log.d(TAG, "dir exists");
                    try {
                        FileInputStream fis = getApplicationContext().openFileInput(filename1);
                        InputStreamReader isr = new InputStreamReader(fis);
                        BufferedReader bufferedReader = new BufferedReader(isr);
                        StringBuilder sb = new StringBuilder();
                        String line;
                        line="";
                        while ((line = bufferedReader.readLine()) != null) {
                            sb.append(line);
                        }
                        Log.d(TAG, "***************output - " + sb);
                        txt.setText(sb);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "exiting button 2");
                } else {
                    Log.d(TAG, "dir not exists");
                }
            }
        });
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d(TAG, "File Uri: " + uri.toString());
                    // Get the path
                    try {
                        each_file_path = getPath(this, uri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "File Path: " + each_file_path);
                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload
                    appendFile();
                }
                break;
        }
        txt.setText(each_file_path);

        super.onActivityResult(requestCode, resultCode, data);
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }finally {
                cursor.close();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private boolean appendFile() {
        //file = new File(filename);
        file = new File(getApplicationContext().getFilesDir(), filename1);

        if (!file.exists()) {
            Log.d(TAG, "file does not exists,  crating file.....");
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!file.exists()) {
            Log.d(TAG, "Locha hai boss");
        }else{
            Log.d(TAG,"ALL good");
        }
        FileOutputStream outputStream;
        try {
            Log.d(TAG,"writing to file......" + each_file_path);
            outputStream = openFileOutput(filename1, Context.MODE_APPEND);
            outputStream.write(each_file_path.getBytes());
            outputStream.write(";".getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            FileInputStream fis = getApplicationContext().openFileInput(filename1);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            Log.d(TAG, file.getAbsolutePath()+"***************output - " + sb);
            txt.setText(sb);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResume()");
        // mSensorManager.registerListener(this, mAccelerometerUnc, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        //    mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("onPause()");
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        deleteMyFile(filename1);
        Log.d(TAG,"onDestroy(){"+ filename1);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        System.out.println("onAccuracyChanged(Sensor sensor, int accuracy)");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        /*try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(wait){*/
        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            //System.out.println("event.sensor.getType()==Sensor.TYPE_ACCELEROMETER");
            aX=event.values[0];
            aY=event.values[1];
            aZ=event.values[2];

            final float alpha = 0.8f;

            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
            double x = Math.sqrt(aX*aX + aY*aY + aZ*aZ);

            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];

           // System.out.println("******linear_acceleration[2]  :  "+linear_acceleration[2]);

            if(x<1 && x>=0){

                //    tx4.setText(String.valueOf(linear_acceleration[2]));
                List<String> items = new ArrayList<String >();
                file = new File(getApplicationContext().getFilesDir(), filename1);

                if (file.exists()) {
                    Log.d(TAG, "dir exists");

                    try {
                        FileInputStream fis = getApplicationContext().openFileInput(filename1);
                        InputStreamReader isr = new InputStreamReader(fis);
                        BufferedReader bufferedReader = new BufferedReader(isr);
                        StringBuilder sb = new StringBuilder();
                        String line;
                        line="";
                        while ((line = bufferedReader.readLine()) != null) {
                            sb.append(line);
                        }
                        //Log.d(TAG, "***************output - in interrupt" + sb);
                        items = Arrays.asList(sb.toString().split("\\s*;\\s*"));
                      //  txt.setText(sb);
                        Log.d(TAG, "***************output - in interrupt" + items.toString());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "exiting button 2");
                } else {
                    Log.d(TAG, "dir not exists");
                }

                //encrypt --------- navneet

                //Magic//
                int n =  items.size();
                int flag=0;
                Toast.makeText(getApplicationContext(),"Encrypting",Toast.LENGTH_SHORT).show();
                while (n!=0) {
                    filename = items.get(n-1).toString();
                    System.out.println(filename);
                    //String[] tempstr = filename.split("/");
                    //filename = tempstr[tempstr.length-1];


                    String[] tempstr = filename.split("\\.");
                    filename = tempstr[0];
                    System.out.println(filename);

                    System.out.println("________________--------- enc start ---____________"+filename+"------------");
                    input = readFile(filename+".txt");
                    String res = encrypt(input, key);
                    //set result
                    //txt.setText(res);
                    System.out.println("________________--------- writing ---____________"+filename+"EN.txt------------");
                    System.out.println(res);
                    writeFile(filename + "EN" +".txt", res);
                        //flag++;
                       //deleteMyFile(filename+".txt");
                    System.out.println("________________----------- enc end ---____________");
                    n--;
                }
                //if(flag==items.size())
                //{
                //}*/
//                items.clear();
            }

            if(linear_acceleration[2]<=5){
              //  tx5.setText(String.valueOf(linear_acceleration[2]));
            }

            /*if(gravity[2]>x){
                x = gravity[2];
                tx6.setText(String.valueOf(gravity[2]));
            }

            if(gravity[2]<y){
                y = gravity[2];
                tx7.setText(String.valueOf(gravity[2]));
            }
*/      }
        //}

    }
    public static String readFile(String fname){
        BufferedReader br = null;
        String response = null;
        try {
            StringBuffer output = new StringBuffer();
            String fpath = fname;
            br = new BufferedReader(new FileReader(fpath));
            String line = "";
            while ((line = br.readLine()) != null) {
                output.append(line);
            }
            response = output.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return response;
    }

    public void deleteMyFile(String fname){
        File file = new File(fname);
        if(file.delete()){
            Log.d(TAG,"for "+ fname + " delete success");
        }else{
            Log.d(TAG,"for "+ fname + " delete success");
        }

        if(file.exists()){
            Log.d(TAG,"**************locha");
        }
    }
    public static boolean writeFile(String fname, String fcontent){
        try {

            String fpath = fname;

            File file = new File(fpath);

            // If file does not exists, then create it
            if (!file.exists()) {

                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(fcontent);
            bw.close();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String encrypt(String input, String key){
        String enc="";
        try {
            // Create key and cipher
            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            // encrypt the text
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encrypted = cipher.doFinal(input.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : encrypted) {
                sb.append((char) b);
            }
            // the encrypted String
            enc = sb.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return enc;
    }

}