package com.techx.autodoor;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by change on 12/3/2015.
 */
public class ChangePinActivity extends AppCompatActivity {
    // Progress Dialog
    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();
    EditText inputCurrentPin;
    EditText inputNewPin;

    // url to authenticate the user
    private static String url_pin_change = "http://techxlab.netau.net/change_pin.php";

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
        setContentView(R.layout.change_pin);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        // Grab the  edit text
        inputCurrentPin=(EditText) findViewById(R.id.current_pin);
        inputNewPin=(EditText) findViewById(R.id.new_pin);
        // create button
        Button btnReset = (Button) findViewById(R.id.button_change);

        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());

        // Login  click event
        btnReset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // get Internet status
                isInternetPresent = cd.isConnectingToInternet();

                String currentPin= inputCurrentPin.getText().toString();
                String newPin= inputNewPin.getText().toString();

                // check for Internet status
                if (isInternetPresent) {
                    // Internet Connection is Present
                    // make HTTP requests

                    // Authenticating the user  in background thread
                    new  ChangePin().execute(currentPin,newPin);
                } else {
                    // Internet connection is not present
                    // Ask user to connect to Internet
                    AlertDialog alertDialog = new AlertDialog.Builder(ChangePinActivity.this).create();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

                return super.onOptionsItemSelected(item);

    }
    class ChangePin extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ChangePinActivity.this);
            pDialog.setMessage("Changing pin..");
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
            params.add(new BasicNameValuePair("current_pin", args[0]));
            params.add(new BasicNameValuePair("new_pin", args[1]));
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_pin_change ,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    // successfully changed the pin
//                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
//                    startActivity(i);
//
//                    // closing this screen
//                    finish();
                    Log.d("failed to create user", json.toString());
                    return   json.getString(TAG_MESSAGE);
                } else {
                    // failed to change the pin
                    Log.d("failed to create user", json.toString());
                    return   json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
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
                AlertDialog alertDialog = new AlertDialog.Builder(ChangePinActivity.this).create();
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

