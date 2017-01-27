package cs656.carpoolapp;

/**
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
import android.widget.TimePicker;

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


public class SubmitSchedule extends AppCompatActivity {

    //private Spinner spinner;
    private boolean isStored;
    private StoreScheduleInDBThread accessDB = null;
    private View mScheduleFormView, mProgressView;
    private EditText errorTo, errorFrom;

    private String UCID, firstNameString;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.to_and_from);
        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        UCID = bundle.getString("UCID");
        firstNameString = bundle.getString("FirstName");

        mScheduleFormView = (View) findViewById(R.id.schedule_form);
        mProgressView = (View) findViewById(R.id.schedule_progress);
        errorFrom = (EditText) findViewById(R.id.From_Monday);
        errorTo = (EditText) findViewById(R.id.To_Monday);
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



    /** Called when the user clicks the Send button */
    public void sendMessage(View view)
    {// Do something in response to button
        attemptScheduleSubmit();
    }

    public void goToProfilePage() {
        Intent intent = new Intent(this, DisplayMessageActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("UCID",UCID);
        bundle.putString("FirstName", firstNameString);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void attemptScheduleSubmit(){
        if (accessDB != null) {
            return;
        }

        // Reset errors.
        errorTo.setError(null);
        errorFrom.setError(null);

        // Store values at the time of the registration attempt.

        String toMonday = ((EditText)findViewById(R.id.To_Monday)).getText().toString();
        String toTuesday = ((EditText)findViewById(R.id.To_Tuesday)).getText().toString();
        String toWednesday = ((EditText)findViewById(R.id.To_Wednesday)).getText().toString();
        String toThursday= ((EditText)findViewById(R.id.To_Thursday)).getText().toString();
        String toFriday = ((EditText)findViewById(R.id.To_Friday)).getText().toString();
        String toSaturday = ((EditText)findViewById(R.id.To_Saturday)).getText().toString();
        String toSunday = ((EditText)findViewById(R.id.To_Sunday)).getText().toString();
        String fromMonday = ((EditText)findViewById(R.id.From_Monday)).getText().toString();
        String fromTuesday = ((EditText)findViewById(R.id.From_Tuesday)).getText().toString();
        String fromWednesday = ((EditText)findViewById(R.id.From_Wednesday)).getText().toString();
        String fromThursday= ((EditText)findViewById(R.id.From_Thursday)).getText().toString();
        String fromFriday = ((EditText)findViewById(R.id.From_Friday)).getText().toString();
        String fromSaturday = ((EditText)findViewById(R.id.From_Saturday)).getText().toString();
        String fromSunday = ((EditText)findViewById(R.id.From_Sunday)).getText().toString();
        boolean cancel = false;
        View focusView = null;

        if(toMonday.isEmpty() && toTuesday.isEmpty() && toWednesday.isEmpty() && toThursday.isEmpty() && toFriday.isEmpty() && toSaturday.isEmpty() && toSunday.isEmpty()) {
            cancel = true;
            focusView = errorTo;
            errorTo.setError("You must fill in at least one day you're going to school...");
        }
        if(fromMonday.isEmpty() && fromTuesday.isEmpty() && fromWednesday.isEmpty() && fromThursday.isEmpty() && fromFriday.isEmpty() && fromSaturday.isEmpty() && fromSunday.isEmpty()) {
            cancel = true;
            focusView = errorFrom;
            errorFrom.setError("You must fill in at least one day you're coming from school...");
        }
        // if any pair have an Empty it returns true.
        // this is only true if every single pair have at least 1 empty.
        //if even one pair is filled, it returns false automatically.
        if((fromMonday.isEmpty() || toMonday.isEmpty()) && (fromTuesday.isEmpty() || toTuesday.isEmpty()) &&
                (toWednesday.isEmpty() || fromWednesday.isEmpty()) && (toThursday.isEmpty() || fromThursday.isEmpty()) &&
                (toFriday.isEmpty() || fromFriday.isEmpty()) && (toSaturday.isEmpty() || fromSaturday.isEmpty()) &&
                (toSunday.isEmpty() || fromSunday.isEmpty()))
        {
            cancel = true;
            focusView = errorFrom;
            errorFrom.setError("You have to fill in a pair of the same day. ");
        }


        //attempt to get a network connection going
        if (!isConnected()) {
            if(errorFrom.getError()!=null)
                errorFrom.setError(errorFrom.getError() + "Not connected to Internet");
            else
                errorFrom.setError("Not connected to Internet");
            focusView = errorFrom;
            cancel=true;
        }

        if (cancel) {
            // There was an error; don't attempt to continue and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            accessDB = new StoreScheduleInDBThread(toMonday,toTuesday,toWednesday,toThursday,toFriday,toSaturday,toSunday,fromMonday,fromTuesday,fromWednesday,fromThursday,fromFriday,fromSaturday,fromSunday);
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

            mScheduleFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mScheduleFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mScheduleFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mScheduleFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }



    //Background Thread to access the DB.
    public class StoreScheduleInDBThread extends AsyncTask<Void, Void, Boolean> {
        private final String toMonday, toTuesday, toWednesday, toThursday, toFriday, toSaturday, toSunday, fromMonday, fromTuesday, fromWednesday, fromThursday, fromFriday, fromSaturday, fromSunday;
        private final String TAG = SubmitSchedule.class.getSimpleName();
        private boolean isDBUp= false;


        StoreScheduleInDBThread(String toMonday, String toTuesday, String toWednesday, String toThursday, String toFriday, String toSaturday, String toSunday,
                                String fromMonday, String fromTuesday, String fromWednesday, String fromThursday, String fromFriday, String fromSaturday, String fromSunday) {
            this.toMonday=toMonday;
            this.toTuesday=toTuesday;
            this.toWednesday=toWednesday;
            this.toThursday=toThursday;
            this.toFriday=toFriday;
            this.toSaturday=toSaturday;
            this.toSunday=toSunday;
            this.fromMonday=fromMonday;
            this.fromTuesday=fromTuesday;
            this.fromWednesday=fromWednesday;
            this.fromThursday=fromThursday;
            this.fromFriday=fromFriday;
            this.fromSaturday=fromSaturday;
            this.fromSunday=fromSunday;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                //connect to njit database for authentication using mEmail and mPassword.


                URL url = new URL("https://web.njit.edu/~vvv5/storeSchoolTimes.php");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                SSLSocketFactoryEx factory = new SSLSocketFactoryEx();
                conn.setSSLSocketFactory(factory);
                conn.setRequestProperty("charset", "utf-8");

                Log.d(TAG, "Starting to connect to:storeSchoolTimes.php");

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
                    /*while (s!=null) {
                        Log.d(TAG, "SubmitSchedule: " + s);
                        s = br.readLine();
                    }
//*/
                    isStored=s.contains("1");
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

            result.append(URLEncoder.encode("UCID", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(UCID, "UTF-8"));
            result.append("&");

            result.append(URLEncoder.encode("fromMonday", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(fromMonday, "UTF-8"));
            result.append("&");

            result.append(URLEncoder.encode("fromTuesday", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(fromTuesday, "UTF-8"));
            result.append("&");

            result.append(URLEncoder.encode("fromWednesday", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(fromWednesday, "UTF-8"));
            result.append("&");

            result.append(URLEncoder.encode("fromThursday", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(fromThursday, "UTF-8"));
            result.append("&");

            result.append(URLEncoder.encode("fromFriday", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(fromFriday, "UTF-8"));
            result.append("&");

            result.append(URLEncoder.encode("fromSaturday", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(fromSaturday, "UTF-8"));
            result.append("&");

            result.append(URLEncoder.encode("fromSunday", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(fromSunday, "UTF-8"));
            result.append("&");

            result.append(URLEncoder.encode("toMonday", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(toMonday, "UTF-8"));
            result.append("&");

            result.append(URLEncoder.encode("toTuesday", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(toTuesday, "UTF-8"));
            result.append("&");

            result.append(URLEncoder.encode("toWednesday", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(toWednesday, "UTF-8"));
            result.append("&");

            result.append(URLEncoder.encode("toThursday", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(toThursday, "UTF-8"));
            result.append("&");

            result.append(URLEncoder.encode("toFriday", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(toFriday, "UTF-8"));
            result.append("&");

            result.append(URLEncoder.encode("toSaturday", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(toSaturday, "UTF-8"));
            result.append("&");

            result.append(URLEncoder.encode("toSunday", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(toSunday, "UTF-8"));

            return result.toString();
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            //decide what to do once this thread is done.
            if(!success && !isDBUp) {
                errorFrom.setError("DB is not up. Please try again later.");
                errorFrom.requestFocus();
            }
            else if(success && isStored) {
                finish(); //kills thread
                goToProfilePage();
            }
            else {
                errorFrom.setError("Something went wrong. Ensure all your information is correct.");
                errorFrom.requestFocus();
            }
        }


        @Override
        protected void onCancelled() {
            accessDB = null;
            showProgress(false);
        }
    }
}
