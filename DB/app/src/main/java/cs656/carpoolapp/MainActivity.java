package cs656.carpoolapp;

/**
 * Created by Tom Paronis.
 */
//Registering Profile

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

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

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinner;
    private boolean isStored;
    private String USState = "";
    private static final String[]paths = {"NJ", "NY", "CT", "PA"};
    private StoreProfileInDBThread accessDB = null;
    private View mRegistrationFormView, mProgressView;
    private EditText error;

    private String UCID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        UCID = bundle.getString("UCID");
        spinner = (Spinner)findViewById(R.id.Address3_message);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item,paths);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        //create all views
        mRegistrationFormView = (View) findViewById(R.id.registration_form);
        mProgressView = (View) findViewById(R.id.registration_progress);
        error = (EditText) findViewById(R.id.first_name_message);
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
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {

        switch (position) {
            case 0:
                USState="NJ";
                break;
            case 1:
                USState="NY";
                break;
            case 2:
                USState="CT";
                break;
            case 3:
                USState="PA";
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        parent.getEmptyView();
    }

    /** Called when the user clicks the Send button */
    public void sendMessage(View view)
    {// Do something in response to button
        attemptRegistration();
    }

    public void goToSubmitSchedulePage() {
        Intent intent = new Intent(this, SubmitSchedule.class);
        EditText firstName = (EditText) findViewById(R.id.first_name_message);
        String firstNameString = firstName.getText().toString();
        Bundle bundle = new Bundle();
        bundle.putString("UCID",UCID);
        bundle.putString("FirstName", firstNameString);
        intent.putExtras(bundle);
        startActivity(intent);

    }

    private void attemptRegistration(){
        if (accessDB != null) {
            return;
        }

        // Reset errors.
        error.setError(null);

        // Store values at the time of the registration attempt.

        String firstName = ((EditText)findViewById(R.id.first_name_message)).getText().toString();
        String lastName = ((EditText)findViewById(R.id.last_name_message)).getText().toString();
        String street = ((EditText)findViewById(R.id.Address1_message)).getText().toString();
        String city = ((EditText)findViewById(R.id.Address2_message)).getText().toString();
        String zipCode = ((EditText)findViewById(R.id.Address4_message)).getText().toString();
        String cell = ((EditText)findViewById(R.id.Phone_message)).getText().toString();
        String email = ((EditText)findViewById(R.id.email_message)).getText().toString();
        boolean cancel = false;
        View focusView = null;

        if(firstName.isEmpty() || lastName.isEmpty() || street.isEmpty() || city.isEmpty() || zipCode.isEmpty() || cell.isEmpty() || email.isEmpty()) {
            cancel = true;
            focusView = error;
            error.setError("You must complete all fields.");
        }

        //attempt to get a network connection going
        if (!isConnected()) {
            if(error.getError()!=null)
                error.setError(error.getError() + "Not connected to Internet");
            else
                error.setError("Not connected to Internet");
            focusView = error;
            cancel=true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            accessDB = new StoreProfileInDBThread(firstName, lastName, street, city, USState, zipCode, cell, email);
            accessDB.execute((Void) null);
        }


    }
    private boolean isConnected()
    {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo!=null && networkInfo.isConnected());

    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegistrationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegistrationFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegistrationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegistrationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }



    //Background Thread to access the DB.
    public class StoreProfileInDBThread extends AsyncTask<Void, Void, Boolean> {
        private final String firstName, lastName, street, city, state, zipCode, phone, email;
        private final String TAG = MainActivity.class.getSimpleName();
        private boolean isDBUp= false;


        StoreProfileInDBThread(String firstName, String lastName, String street, String city, String state, String zipCode, String phone, String email) {
            this.email = email;
            this.firstName = firstName;
            this.lastName=lastName;
            this.street=street;
            this.city=city;
            this.state=state;
            this.zipCode=zipCode;
            this.phone=phone;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                //connect to njit database for authentication using mEmail and mPassword.


                URL url = new URL("https://web.njit.edu/~vvv5/storeProfileInfo.php");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                SSLSocketFactoryEx factory = new SSLSocketFactoryEx();
                conn.setSSLSocketFactory(factory);
                conn.setRequestProperty("charset", "utf-8");

                Log.d(TAG, "Starting to connect to:storeProfileInfo.php");

                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
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

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
            catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            }   catch (IOException e) {
                e.printStackTrace();
                return false;
            }   catch (Exception e) {
                e.printStackTrace();
                return false;
            }



            //decide what this background thread will do.
            return true;
        }
        private String postData() throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();
            //recall firstName, lastName, street, city, state, zipCode, phone, email
            result.append(URLEncoder.encode("firstName", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(firstName, "UTF-8"));
            result.append("&");
            result.append(URLEncoder.encode("lastName", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(lastName, "UTF-8"));
            result.append("&");
            result.append(URLEncoder.encode("street", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(street, "UTF-8"));
            result.append("&");
            result.append(URLEncoder.encode("city", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(city, "UTF-8"));
            result.append("&");
            result.append(URLEncoder.encode("state", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(state, "UTF-8"));
            result.append("&");
            result.append(URLEncoder.encode("zipCode", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(zipCode, "UTF-8"));
            result.append("&");
            result.append(URLEncoder.encode("phone", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(phone, "UTF-8"));
            result.append("&");
            result.append(URLEncoder.encode("email", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(email, "UTF-8"));
            result.append("&");
            result.append(URLEncoder.encode("UCID", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(UCID, "UTF-8"));
            return result.toString();
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            //decide what to do once this thread is done.
            if(!success && !isDBUp) {
                error.setError("DB is not up. Please try again later.");
                error.requestFocus();
            }
            else if(success && isStored) {
                finish(); //kills thread
                goToSubmitSchedulePage();
            }
            else {
                error.setError("Something went wrong. Ensure all your information is correct.");
                error.requestFocus();
            }
        }


        @Override
        protected void onCancelled() {
            accessDB = null;
            showProgress(false);
        }
    }
}
