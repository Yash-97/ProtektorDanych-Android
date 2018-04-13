package com.example.dionysus.selectfile;

/**
 * Created by Navneet on 25-03-2018.
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {


    private Context context;
    String TAG = "sMess";

    // Constructor
    public FingerprintHandler(Context mContext) {
        context = mContext;
    }


    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
        CancellationSignal cancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }


    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        this.update("Fingerprint Authentication error\n" + errString, false);
    }


    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        this.update("Fingerprint Authentication help\n" + helpString, false);
    }


    @Override
    public void onAuthenticationFailed() {
        this.update("Fingerprint Authentication failed.", false);
    }


    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        this.update("Fingerprint Authentication succeeded.", true);
        //get files
        String filename="temp";
        String key = "Cpp12365Cpp12349";

        String filename1 = "file_paths.csv";
        List<String> items = new ArrayList<String>();
        try {
            FileInputStream fis = context.openFileInput(filename1);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            Log.d(TAG, "***************output - " + sb);
            Toast.makeText(context,sb,Toast.LENGTH_LONG).show();
           // txt.setText(sb);
            items = Arrays.asList(sb.toString().split("\\s*;\\s*"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Magic//
        int n =  items.size();
        while (n!=0) {
            filename = items.get(n-1).toString();
            System.out.println(filename);
            String[] tempstr = filename.split("\\.");
            filename = tempstr[0];
            System.out.println(filename);

            System.out.println("________________--------- decryp start ---____________"+filename+"------------");


        String encrypted = MainActivity.readFile(filename+"EN.txt");
        String enc = encrypted;

        String decrypted="";
        try{
            byte[] bb = new byte[enc.length()];
            for (int i=0; i<enc.length(); i++) {
                bb[i] = (byte) enc.charAt(i);
            }
            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");

            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            decrypted = new String(cipher.doFinal(bb));
            System.out.println(decrypted);
            new MainActivity().writeFile(filename +"DEC.txt", decrypted);
            System.out.println(decrypted);
            //success delete files
           // new MainActivity().deleteMyFile(filename+"EN.txt");
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("________________----------- decry end ---____________");
        n--;
    }
    }


    public void update(String e, Boolean success){
        TextView textView = (TextView) ((Activity)context).findViewById(R.id.errorText);
        textView.setText(e);
        if(success){
            textView.setTextColor(ContextCompat.getColor(context,R.color.colorPrimaryLight));
        }
    }
}