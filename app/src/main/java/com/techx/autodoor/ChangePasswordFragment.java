package com.techx.autodoor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.techx.autodoor.data.AutodoorContract;
import com.techx.autodoor.data.AutodoorDbHelper;
import com.techx.autodoor.helper.ConnectionDetector;
import com.techx.autodoor.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by change on 5/19/2016.
 */
public class ChangePasswordFragment extends Fragment {

    EditText inputOldPassword;
    EditText inputNewPassword;
    EditText inputRetypePassword;
    Button submit;
    private SessionManager session;
    AutodoorDbHelper db;
    ProgressDialog pDialog;
    // flag for Internet connection status
    Boolean isInternetPresent = false;
    // Connection detector class
    ConnectionDetector cd;
    final String TAG_SUCCESS = "success";
    final String TAG_MESSAGE = "message";
    private final String LOG_TAG =ChangePasswordFragment.class.getSimpleName();
    private String serverName = "http://android.autodoor.comli.com";
    public ChangePasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.change_password, container, false);
        session = new SessionManager(getActivity().getApplicationContext());
        // creating connection detector class instance
        cd = new ConnectionDetector(getActivity().getApplicationContext());
        // SQLite database handler
        db = new AutodoorDbHelper(getActivity().getApplicationContext());
// Grab the  edit text for the change password
        inputOldPassword=(EditText) rootView.findViewById(R.id.old_password);
        inputNewPassword = (EditText) rootView.findViewById(R.id.new_password);
        inputRetypePassword = (EditText) rootView.findViewById(R.id.retype_password);
        submit = (Button) rootView.findViewById(R.id.change_password_button);

        // submit  click event
        submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // get Internet status
                //isInternetPresent = cd.isConnectingToInternet();

                String oldPassword = inputOldPassword.getText().toString().trim();
                String newPassword = inputNewPassword.getText().toString().trim();
                String retypePassword = inputRetypePassword.getText().toString().trim();

                // Check for empty data in the form
                if (!oldPassword.isEmpty() && !newPassword.isEmpty() && !retypePassword.isEmpty()) {

                    // check for stored password
                    long userId = session.getUserId();
                    ContentValues user = db.getUserDetailsById(userId);

                    if(!newPassword.equals(retypePassword)) {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Passwords do not match.", Toast.LENGTH_LONG)
                                .show();
                    }

                    else if (!oldPassword.equals(user.get(AutodoorContract.UserEntry.COLUMN_PASSWORD))) {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Old password is incorrect.", Toast.LENGTH_LONG)
                                .show();
                    }

                    else {
                        // send request to the server.
                       new ChangePasswordRequest().execute(Long.toString(userId),newPassword);
                    }


                } else {
                    // one or more fields are empty.
                    Toast.makeText(getActivity().getApplicationContext(),
                            "One or more fields are empty!", Toast.LENGTH_LONG)
                            .show();
                }

            }
        });

                // Inflate the layout for this fragment
                return rootView;
            }

    private  String getResponseDataFromJson(String responseJsonStr,long userId, String newPassword)
            throws JSONException {

        try {
            JSONObject responseJson = new JSONObject(responseJsonStr);

            int success = responseJson.getInt(TAG_SUCCESS);

            if (success == 1) {
                // update the local database
                db.updateUserPassword(userId,newPassword);
                return responseJson.getString(TAG_MESSAGE);
            }
            else {
                return responseJson.getString(TAG_MESSAGE);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Async task to change user's password
     * */
    private class ChangePasswordRequest extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Change Password request...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            long userId = Long.parseLong(params[0]);
            // get Internet status
            isInternetPresent = cd.isConnectingToInternet();
            if (isInternetPresent) {

                // These two need to be declared outside the try/catch
                // so that they can be closed in the finally block.
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                // Will contain the raw JSON response as a string.
                String responseJsonStr = null;

                try {

                    // construct a query string parameter


                    String urlParameters =
                            "userId=" + URLEncoder.encode(params[0])
                                    + "&newPassword=" + URLEncoder.encode(params[1]);

                    // Construct the URL for the AutodoorServer query
                    final String AUTODOOR_BASE_URL = serverName+"/change_password.php";


                    Uri builtUri = Uri.parse(AUTODOOR_BASE_URL).buildUpon().build();
                    URL url = new URL(builtUri.toString());

                    Log.d("The URI", builtUri.toString());

                    // Create the request to AutodoorServer, and open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded");

                    urlConnection.setUseCaches(false);
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);
                    urlConnection.connect();
                    //Send request
                    DataOutputStream wr = new DataOutputStream(
                            urlConnection.getOutputStream());
                    wr.writeBytes(urlParameters);
                    wr.flush();
                    wr.close();


                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging a *lot* easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        return null;
                    }
                    responseJsonStr = buffer.toString();
                    Log.v("Json string:", responseJsonStr);

                    return getResponseDataFromJson(responseJsonStr, userId,params[1]);


                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error ", e);
                    // If the code didn't successfully get the weather data, there's no point in attempting
                    // to parse it.
                    //return null;
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(LOG_TAG, "Error closing stream", e);
                        }
                    }
                }
                // This will only happen if there was an error getting or parsing the AutodoorServer.
                return null;
            } else {
                String message = "Please Check your internet connection.";
                return message;
            }
        }


        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);

            // dismiss the dialog once done
            pDialog.dismiss();
            if (message != null) {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setMessage(message);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();

                // remove the values from the fields
                inputNewPassword.setText("");
                inputOldPassword.setText("");
                inputRetypePassword.setText("");

            }

        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
