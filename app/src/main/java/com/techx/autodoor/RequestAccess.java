package com.techx.autodoor;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.techx.autodoor.data.AutodoorDbHelper;
import com.techx.autodoor.helper.ConnectionDetector;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by change on 5/23/2016.
 */
public class RequestAccess extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {

    EditText inputFirstName;
    EditText inputLastName;
    EditText inputEmail;
    EditText inputPhoneNumber;
    String role;
    String office;
    // Spinner elements
    Spinner roleSpinner;
    Spinner officeSpinner;
    // flag for Internet connection status
    Boolean isInternetPresent = false;
    // Connection detector class
    ConnectionDetector cd;
    private ArrayList<String> rolesList;
    private ArrayList<String> officesList;
    ProgressDialog pDialog;
    private final String LOG_TAG =RequestAccess.class.getSimpleName();
    private String serverName = "http://android.autodoor.comli.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_access);

        // Spinner element
        roleSpinner = (Spinner) findViewById(R.id.role_spinner);
        officeSpinner = (Spinner) findViewById(R.id.office_spinner);
        //Grab the  edit text for Request Access
        inputFirstName=(EditText) findViewById(R.id.first_name);
        inputLastName = (EditText) findViewById(R.id.last_name);
        inputEmail= (EditText) findViewById(R.id.email);
        inputPhoneNumber = (EditText) findViewById(R.id.phone_number);

        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());

        rolesList = new ArrayList<String>();
        officesList= new ArrayList<String>();
        // Spinner click listener
        roleSpinner.setOnItemSelectedListener(this);
        officeSpinner.setOnItemSelectedListener(this);
        // get Internet status
        isInternetPresent = cd.isConnectingToInternet();
        if (!isInternetPresent) {
            // Loading spinner data from Sqlite database
            loadSpinnerData();
        }
        else {
            // Fetch Roles from the Backend Server.
            new GetRolesAndOffices().execute();

        }

        Button btnRequest = (Button) findViewById(R.id.request_submit_button);



        // Request  click event
        btnRequest.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String firstName= inputFirstName.getText().toString().trim();
                String lastName = inputLastName.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String phoneNumber = inputPhoneNumber.getText().toString().trim();

                // Check for empty data in the form
                if (!firstName.isEmpty() && !lastName.isEmpty() && !email.isEmpty() && !phoneNumber.isEmpty() && !role.isEmpty() && !office.isEmpty()) {
                   // make a new access request
                    new MakeNewRequest().execute(firstName,lastName,email,phoneNumber,role,office);
                } else {
                    // Display the error message
                    Toast.makeText(getApplicationContext(),
                            "One or more fields are empty", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {

        Spinner spinner = (Spinner) parent;

        if(spinner.getId() == R.id.role_spinner) {
            // On selecting a role spinner item
            role = parent.getItemAtPosition(position).toString();

        } else if (spinner.getId() == R.id.office_spinner) {
            // On selecting an office spinner item
            office = parent.getItemAtPosition(position).toString();

        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }

    private  String getResponseDataFromJson(String responseJsonStr)
            throws JSONException {


        final String TAG_SUCCESS = "success";
        final String TAG_MESSAGE = "message";

        try {
            JSONObject responseJson = new JSONObject(responseJsonStr);

            int success = responseJson.getInt(TAG_SUCCESS);

            if (success == 0) {
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

    private  Void getRolesAndOfficesDataFromJson(String rolesOfficesJsonStr)
            throws JSONException {


        final String TAG_SUCCESS = "success";
        final String TAG_MESSAGE = "message";

        // Role information
        final String ROLE_RESULT = "roles";
        final String ROLE_NAME = "role_name";


        // Role information
        final String OFFICE_RESULT = "offices";
        final String OFFICE_NAME = "office_name";

        try {
            JSONObject rolesOfficesJson = new JSONObject(rolesOfficesJsonStr);

            int success = rolesOfficesJson.getInt(TAG_SUCCESS);

            if (success == 1) {
                //Get roles
                JSONArray roles = rolesOfficesJson.getJSONArray(ROLE_RESULT);
                for (int i = 0; i < roles.length(); i++) {
                    JSONObject role = roles.getJSONObject(i);
                    String roleName = role.getString(ROLE_NAME);
                    rolesList.add(roleName);
                }

                //Get offices
                JSONArray offices = rolesOfficesJson.getJSONArray(OFFICE_RESULT);
                for (int j = 0; j < offices.length(); j++) {
                    JSONObject office = offices.getJSONObject(j);
                    String officeName = office.getString(OFFICE_NAME);
                    officesList.add(officeName);
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Async task to get all roles and offices
     * */
    private class GetRolesAndOffices extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String roleJsonStr = null;

            try {
                // Construct the URL for the AutodoorServer query
                final String AUTODOOR_BASE_URL = serverName+"/get_roles_offices.php";


                Uri builtUri = Uri.parse(AUTODOOR_BASE_URL).buildUpon().build();
                URL url = new URL(builtUri.toString());

                Log.d("The URI", builtUri.toString());

                // Create the request to AutodoorServer, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

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
                roleJsonStr = buffer.toString();
                Log.v("Json string:", roleJsonStr);

                getRolesAndOfficesDataFromJson(roleJsonStr);


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

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            populateSpinners();
        }
    }

    /**
     * Async task to get all roles and offices
     * */
    private class MakeNewRequest extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RequestAccess.this);
            pDialog.setMessage("Make a new request...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

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
                            "fName=" + URLEncoder.encode(params[0])
                                    + "&lName=" + URLEncoder.encode(params[1])
                                    + "&email=" + URLEncoder.encode(params[2])
                                    + "&phoneNumber=" + URLEncoder.encode(params[3])
                                    + "&role=" + URLEncoder.encode(params[4])
                                    + "&office=" + URLEncoder.encode(params[5]);

                    // Construct the URL for the AutodoorServer query
                    final String AUTODOOR_BASE_URL = serverName+"/make_request.php";


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

                    return getResponseDataFromJson(responseJsonStr);


                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error ", e);
                    // If the code didn't successfully get the weather data, there's no point in attempting
                    // to parse it.
                    //return null;
              }
                catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
                finally {
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
            }
            else {
                String message = "Please Check your internet connection.";
                return message;
            }
            }


        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);

            // dismiss the dialog once done
            pDialog.dismiss();
            if (message != null){
                AlertDialog alertDialog = new AlertDialog.Builder(RequestAccess.this).create();
                alertDialog.setMessage(message);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();

                // remove the values from the fields
                inputFirstName.setText("");
                inputLastName.setText("");
                inputEmail.setText("");
                inputPhoneNumber.setText("");
            }

        }
    }
    /**
     * Adding spinner data
     * */
    private void populateSpinners() {

        // Creating adapter for role spinner
        ArrayAdapter<String> roleSpinnerAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, rolesList);

        // Drop down layout style - list view with radio button
        roleSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        roleSpinner.setAdapter(roleSpinnerAdapter);


        // Creating adapter for office spinner
        ArrayAdapter<String> officeSpinnerAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, officesList);

        // Drop down layout style - list view with radio button
        officeSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        officeSpinner.setAdapter(officeSpinnerAdapter);

    }



    /**
     * Function to load the spinner data from SQLite database
     * */
    private void loadSpinnerData() {
        // database handler for roles
        AutodoorDbHelper db = new AutodoorDbHelper(getApplicationContext());

        // Spinner Drop down elements
        List<String> roles = db.getAllRoles();

        // Creating adapter for spinner
        ArrayAdapter<String> roleDataAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, roles);

        // Drop down layout style - list view with radio button
        roleDataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        roleSpinner.setAdapter(roleDataAdapter);

        // database handler for offices

        // Spinner Drop down elements
        List<String> offices = db.getAllOffices();

        // Creating adapter for spinner
        ArrayAdapter<String> officeDataAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, offices);

        // Drop down layout style - list view with radio button
        officeDataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        officeSpinner.setAdapter(officeDataAdapter);
    }
}
