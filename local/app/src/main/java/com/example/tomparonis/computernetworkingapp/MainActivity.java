package com.example.tomparonis.computernetworkingapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.MESSAGE";
    ArrayList<String> carAppList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        carAppList = new ArrayList<>();
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id)
        {
            case R.id.login:
                setContentView(R.layout.login);
                return true;
            case R.id.action_settings:
                setContentView(R.layout.activity_main);
                return true;
            case R.id.findRide:
                setContentView(R.layout.find_ride);
                return true;
            case R.id.scheduling:
                setContentView(R.layout.to_and_from);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** Called when the user clicks the Send button */
    public void sendMessage(View view)
    {// Do something in response to button
        carAppList.clear();
        Intent intent = new Intent(this, DisplayMessageActivity.class);

        EditText editText = (EditText) findViewById(R.id.name_message);
        carAppList.add(editText.getText().toString());

        editText = (EditText) findViewById(R.id.Phone_message);
        carAppList.add(editText.getText().toString());

        editText = (EditText) findViewById(R.id.email_message);
        carAppList.add(editText.getText().toString());

        editText = (EditText) findViewById(R.id.Address1_message);
        carAppList.add(editText.getText().toString());

        editText = (EditText) findViewById(R.id.Address2_message);
        carAppList.add(editText.getText().toString());

        editText = (EditText) findViewById(R.id.Address3_message);
        carAppList.add(editText.getText().toString());

        editText = (EditText) findViewById(R.id.Address4_message);
        carAppList.add(editText.getText().toString());

        String message = carAppList.toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void sendLogin(View view)
    {// Do something in response to button
        carAppList.clear();
        Intent intent = new Intent(this, DisplayMessageActivity.class);

        EditText editText = (EditText) findViewById(R.id.Student_ID);
        carAppList.add(editText.getText().toString());

        editText = (EditText) findViewById(R.id.SocialSecurity_number);
        carAppList.add(editText.getText().toString());

        String message = carAppList.toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void findRide(View view)
    {// Do something in response to button
        carAppList.clear();
        Intent intent = new Intent(this, DisplayMessageActivity.class);

        EditText editText = (EditText) findViewById(R.id.Current_coordinates);
        carAppList.add(editText.getText().toString());

        editText = (EditText) findViewById(R.id.Destination);
        carAppList.add(editText.getText().toString());

        String message = carAppList.toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
}
