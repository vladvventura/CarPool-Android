package cs656.carpoolapp;
/*
Created by Vladimir Ventura
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

public class DisplayMessageActivity extends AppCompatActivity {

    private String UCID;
    private final String TAG = MainActivity.class.getSimpleName();
    private boolean hasGroups = false; //to show if the user has any groups or not suggested to them.
        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_display_message);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        UCID = bundle.getString("UCID");
        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(UCID);
        setContentView(textView);
        String json = getJSON();

    }
    public String getJSON() {
        HttpsURLConnection c =null;
        StringBuilder sb = new StringBuilder();
        try {

/*

            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(postData());
            //conn.connect();
            writer.flush();
            writer.close();
            os.close();
            int responseCode=conn.getResponseCode();

            Log.d(TAG, String.valueOf(responseCode));
            if(responseCode==200 || responseCode == 201) {
                InputStreamReader inputStream = new InputStreamReader(conn.getInputStream());
                BufferedReader br = new BufferedReader(inputStream);
                String s = br.readLine();
                isStored=s.contains("1");
                Log.d(TAG, "isStored = " + String.valueOf(isStored));
                conn.disconnect();


            }
            else
                conn.disconnect();
        }


            //////////////////////*/

            URL url = new URL("https://web.njit.edu/~vvv5/getSuggestedGroups.php");
             c = (HttpsURLConnection) url.openConnection();
            SSLSocketFactoryEx factory = new SSLSocketFactoryEx();
            c.setSSLSocketFactory(factory);
            c.setRequestProperty("charset", "utf-8");

            Log.d(TAG, "Starting to connect to:getSuggestedGroups.php");

            c.setReadTimeout(10000 /*milliseconds*/);
            c.setConnectTimeout(15000);
            c.setRequestMethod("POST");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setDoInput(true);
            c.setDoOutput(true);
            c.connect();



            OutputStream os = c.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(postData());
            //conn.connect();
            writer.flush();
            writer.close();
            os.close();
            int responseCode=c.getResponseCode();

            Log.d(TAG, String.valueOf(responseCode));
            if(responseCode==200 || responseCode == 201) {
                InputStreamReader inputStream = new InputStreamReader(c.getInputStream());
                BufferedReader br = new BufferedReader(inputStream);



                //////////////

                String line;
                String s=null;
                while ((line = br.readLine()) != null) {
                    if (s==null)
                        s=line;

                    sb.append(line+"\n");
                }
                if(s==null || s.isEmpty())
                    hasGroups=false;
                else
                    hasGroups = true;

                br.close();
                Log.d(TAG, "The JSON code is: " + sb.toString());
                return sb.toString();


            }




        } catch (MalformedURLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
                return sb.toString();
            }
        }
        return null;
    }

    private String postData() throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        //recall firstName, lastName, street, city, state, zipCode, phone, email

        result.append(URLEncoder.encode("UCID", "UTF-8"));
        result.append("=");
        result.append(URLEncoder.encode(UCID, "UTF-8"));

        return result.toString();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

}
