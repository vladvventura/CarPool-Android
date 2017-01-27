package cs656.carpoolapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.json.JSONObject;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world", "vvv5:vladimir"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError("Invalid Password.");
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError("This UCID is invalid. ");
            focusView = mEmailView;
            cancel = true;
        }

        //attempt to get a network connection going
        if (!isConnected()) {
            if(mEmailView.getError()!=null)
                mEmailView.setError(mEmailView.getError() + "Not connected to Internet");
            else
                mEmailView.setError("Not connected to Internet");
            focusView = mEmailView;
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
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }



    }
    private boolean isConnected()
    {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo!=null && networkInfo.isConnected());

    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.length()>2 && email.length()<9;
    }
    private boolean isUCID(String email) {
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return  password.length()<15 && password.length() > 7;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    private void goToDisplayMessageActivity(String mEmail) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("UCID",mEmail);
        intent.putExtras(bundle);
        startActivity(intent);
    }
    private void goToActivityMain(String mEmail) {
        Intent intent = new Intent(this, MainActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("UCID", mEmail);
        intent.putExtras(bundle);
        startActivity(intent);
    }
    private void goToSubmitSchedule(String mEmail) {
        Intent intent = new Intent(this, SubmitSchedule.class);

        Bundle bundle = new Bundle();
        bundle.putString("UCID", mEmail);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private boolean isDBUp = false;
        private boolean isRegistered = false;
        private boolean isNJITUser = false;
        private boolean isUserInSchedule = false;
        private final String TAG = LoginActivity.class.getSimpleName();



        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {



            isUserPassNJITUser();
            isDBUp = isUserPassRegistered();
            Log.d(TAG, "DB up is: " + String.valueOf(isDBUp));
            isUserInSchedule();
            return isNJITUser;

        }
        private boolean isUserPassNJITUser() {
            try {
                //connect to njit database for authentication using mEmail and mPassword.
                isNJITUser=false;

                URL url = new URL("https://cp4.njit.edu/cp/home/login");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                Log.d(TAG, "Starting to connect to server");

                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(postData(mEmail, mPassword));
                //conn.connect();
                writer.flush();
                writer.close();

                os.close();
                int responseCode=conn.getResponseCode();

                Log.d(TAG, String.valueOf(responseCode));
                if(responseCode==200) {
                    InputStreamReader inputStream = new InputStreamReader(conn.getInputStream());
                    BufferedReader br = new BufferedReader(inputStream);
                    String s = br.readLine();
                    int c =0;
                    while(c!=2) {
                        String row = "row is"+String.valueOf(c);
                        c++;
                        Log.d(TAG,row);
                        Log.d(TAG, s);
                        s=br.readLine();
                    }

                    isNJITUser=s.equalsIgnoreCase("document.location=\"http://cp4.njit.edu/cps/welcome/loginok.html\";");

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
            return true;
        }

        private boolean isUserPassRegistered() {

            try {
                //connect to njit database for authentication using mEmail and mPassword.
                isRegistered=false;
                //remember it's user = ucid, pass=password, uuid:0xACA021

                URL url = new URL("https://web.njit.edu/~vvv5/isUserInDatabase.php");
                HttpsURLConnection conn= (HttpsURLConnection) url.openConnection();
                SSLSocketFactoryEx factory = new SSLSocketFactoryEx();
                conn.setSSLSocketFactory(factory);
                conn.setRequestProperty("charset", "utf-8");

                //conn.setSSLSocketFactory(NoSSLv3Factory);
                Log.d(TAG, "Connecting to DB");

                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.connect();

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(postData(mEmail));
                //conn.connect();
                writer.flush();
                writer.close();

                os.close();
                int responseCode=conn.getResponseCode();

                Log.d(TAG, "Response Code is: " + String.valueOf(responseCode));
                if(responseCode==200 || responseCode == 201) {
                    InputStreamReader inputStream = new InputStreamReader(conn.getInputStream());
                    BufferedReader br = new BufferedReader(inputStream);
                    String s = br.readLine();
                    isRegistered=s.contains("1");
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
            return true;
        }

        public boolean isUserInSchedule() {
            try {
                //connect to njit database for authentication using mEmail and mPassword.
                isUserInSchedule=false;
                //remember it's user = ucid, pass=password, uuid:0xACA021

                URL url = new URL("https://web.njit.edu/~vvv5/isUserInSchedule.php");
                HttpsURLConnection conn= (HttpsURLConnection) url.openConnection();
                SSLSocketFactoryEx factory = new SSLSocketFactoryEx();
                conn.setSSLSocketFactory(factory);
                conn.setRequestProperty("charset", "utf-8");

                //conn.setSSLSocketFactory(NoSSLv3Factory);
                Log.d(TAG, "Connecting to DB");

                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.connect();

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(postData(mEmail));
                //conn.connect();
                writer.flush();
                writer.close();

                os.close();
                int responseCode=conn.getResponseCode();

                Log.d(TAG, "Response Code is: " + String.valueOf(responseCode));
                if(responseCode==200) {
                    InputStreamReader inputStream = new InputStreamReader(conn.getInputStream());
                    BufferedReader br = new BufferedReader(inputStream);
                    String t = br.readLine();
                    Log.d(TAG, "isUserInSchedule: " + t.charAt(0));
                    /*while (t!=null) {
                        Log.d(TAG, "IsUserInSchedule: " + t);
                        t=br.readLine();
                    }*/
                    if(t.contains("1"))
                        isUserInSchedule = true;
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
            return true;
        }

        private String postData(String mEmail, String mPassword) throws UnsupportedEncodingException{
            StringBuilder result = new StringBuilder();
            result.append(URLEncoder.encode("user", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(mEmail, "UTF-8"));
            result.append("&");
            result.append(URLEncoder.encode("pass", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(mPassword, "UTF-8"));
            result.append("&");
            result.append(URLEncoder.encode("uuid", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode("0xACA021", "UTF-8"));
            return result.toString();
        }
        private String postData(String mEmail) throws UnsupportedEncodingException{
            StringBuilder result = new StringBuilder();
            result.append(URLEncoder.encode("user", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(mEmail, "UTF-8"));
            result.append("&");
            result.append(URLEncoder.encode("uuid", "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode("0xACA021", "UTF-8"));
            return result.toString();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);
//success means that the user is in NJIT's database and the code ran through.
            if (success && isDBUp) {
                try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finish();
                //if the user is registered with Carpool app and has finished submitting his schedule, go to his profile.
                //if the user is not registered, go to registration page.
                if (isRegistered && isUserInSchedule) {
                    goToDisplayMessageActivity(mEmail);
                }
                //he is registered but needs to submit schedule:
                else if(isRegistered && !isUserInSchedule) {
                    goToSubmitSchedule(mEmail);
                }
                // he isn't even registered...
                else {
                    goToActivityMain(mEmail);
                }

            } else if (success && !isDBUp) {
                mPasswordView.setError("Sorry, the database is down. Please try again later.");
                mPasswordView.requestFocus();
            } else {
                mPasswordView.setError(getString(R.string.error_login));
                mPasswordView.requestFocus();
            }
        }



        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }


    }
}

