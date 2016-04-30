package com.techx.autodoor;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
public class ForgotPinActivity extends AppCompatActivity {
// Progress Dialog
private ProgressDialog pDialog;
        JSONParser jsonParser = new JSONParser();
        EditText inputEmailReset;

// url to authenticate the user
private static String url_pin_reset = "http://techxlab.netau.net/forgot_pin.php";

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
        setContentView(R.layout.forgot_pin);
    Toolbar myToolbar = (Toolbar) findViewById(R.id.forgot_pin_toolbar);
    setSupportActionBar(myToolbar);

    ActionBar ab = getSupportActionBar();

    // Enable the Up button
    ab.setDisplayHomeAsUpEnabled(true);
    
        // Grab the  edit text
        inputEmailReset=(EditText) findViewById(R.id.email_reset);

        // create button
        Button btnReset = (Button) findViewById(R.id.reset_pin);

    // creating connection detector class instance
    cd = new ConnectionDetector(getApplicationContext());

        // Login  click event
    btnReset.setOnClickListener(new View.OnClickListener() {

@Override
public void onClick(View view) {
        String emailReset= inputEmailReset.getText().toString();
    // get Internet status
    isInternetPresent = cd.isConnectingToInternet();

    // check for Internet status
    if (isInternetPresent) {
        // Internet Connection is Present
        // make HTTP requests
        // Authenticating the user  in background thread
        new  ResetPin().execute(emailReset);
    } else {
        // Internet connection is not present
        // Ask user to connect to Internet
        AlertDialog alertDialog = new AlertDialog.Builder(ForgotPinActivity.this).create();
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

class ResetPin extends AsyncTask<String, String, String> {

    /**
     * Before starting background thread Show Progress Dialog
     * */

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(ForgotPinActivity.this);
        pDialog.setMessage("Resetting pin..");
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
        params.add(new BasicNameValuePair("email_reset", args[0]));
        // getting JSON Object
        // Note that create product url accepts POST method
        JSONObject json = jsonParser.makeHttpRequest(url_pin_reset ,
                "POST", params);

        // check log cat fro response
        Log.d("Create Response", json.toString());

        // check for success tag
        try {
            int success = json.getInt(TAG_SUCCESS);
            if (success == 1) {
                // successfully authenticated the user
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);

                // closing this screen
                finish();
            } else {
                // failed to authenticate the user
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
            AlertDialog alertDialog = new AlertDialog.Builder(ForgotPinActivity.this).create();
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

