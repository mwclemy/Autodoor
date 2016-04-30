package com.techx.autodoor;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // Progress Dialog
    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();
    EditText inputPin;
    EditText inputEmail;
    // url to authenticate the user
    private static String url_anthenticate = "http://techxlab.netau.net/login_processing.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    // flag for Internet connection status
    Boolean isInternetPresent = false;

    // Connection detector class
    ConnectionDetector cd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Grab the  edit text
        inputEmail=(EditText) findViewById(R.id.email);
        inputPin = (EditText) findViewById(R.id.pin);

          // create button
        Button btnLogin = (Button) findViewById(R.id.email_sign_in_button);

        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());

        // Login  click event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // get Internet status
                isInternetPresent = cd.isConnectingToInternet();

                String email= inputEmail.getText().toString();
                String pin = inputPin.getText().toString();

                // check for Internet status
                if (isInternetPresent) {
                    // Internet Connection is Present
                    // make HTTP requests

                    // Authenticating the user  in background thread
                    new AuthenticateUser().execute(email,pin);
                } else {
                    // Internet connection is not present
                    // Ask user to connect to Internet
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    // Setting Dialog Title
                    alertDialog.setTitle("No Internet Connection");
                    alertDialog.setMessage("You don't have internet connection.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();

                }



            }
        });
    }
    public void clickHandler(View v){
        if (v.getId() == R.id.forgot_pin) {
            startActivity(new Intent(getApplicationContext(), ForgotPinActivity.class));
        }
    }
    class AuthenticateUser extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Authenticating..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Authenticating
         * */
        @Override
        protected String doInBackground(String... args) {

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("email", args[0]));
            params.add(new BasicNameValuePair("pin", args[1]));

            // check for success tag
            try {

                // getting JSON Object
                // Note that create product url accepts POST method
                JSONObject json = jsonParser.makeHttpRequest(url_anthenticate,
                        "POST", params);

                // check log cat fro response
                Log.d("Create Response", json.toString());
                if(json != null) {
                    int success = json.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        // successfully authenticated the user
                        Intent i = new Intent(getApplicationContext(), ManageDoorActivity.class);
                        startActivity(i);

                        // closing this screen
                        finish();
                    } else {
                        // failed to authenticate the user
                        Log.d("failed to create user", json.toString());
                        return json.getString(TAG_MESSAGE);
                    }
                }
            } catch (JSONException e) {
                //e.printStackTrace();
                return "Internet connection error.";
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String message) {
            // dismiss the dialog once done
            pDialog.dismiss();
            if (message != null){
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setMessage(message);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }

        }

    }



}
