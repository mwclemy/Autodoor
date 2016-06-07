package com.techx.autodoor;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.techx.autodoor.data.AutodoorContract;
import com.techx.autodoor.data.AutodoorDbHelper;
import com.techx.autodoor.helper.ConnectionDetector;
import com.techx.autodoor.helper.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    // Progress Dialog
    private ProgressDialog pDialog;
    //JSONParser jsonParser = new JSONParser();
    EditText inputUsername;
    EditText inputPassword;
    private SessionManager session;
    private String serverName = "http://android.autodoor.comli.com";
    Button btnLogin;
    Button btnRequest;
    private AutodoorDbHelper db;
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    // flag for Internet connection status
    Boolean isInternetPresent = false;

    // Connection detector class
    ConnectionDetector cd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Grab the  edit text for login
        inputUsername=(EditText) findViewById(R.id.username);
        inputPassword = (EditText) findViewById(R.id.password);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to manage door activity
            Intent intent = new Intent(MainActivity.this, ManageDoor.class);
            startActivity(intent);
            finish();
        }

        // create buttons
        btnLogin = (Button) findViewById(R.id.username_sign_in_button);

        btnRequest = (Button) findViewById(R.id.request_access_button);

        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());

        // SQLite database handler
        db = new AutodoorDbHelper(getApplicationContext());

        // Login  click event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // get Internet status
                isInternetPresent = cd.isConnectingToInternet();

                String userName= inputUsername.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                // Check for empty data in the form
                if (!userName.isEmpty() && !password.isEmpty()) {
                    new AuthenticateUser().execute(userName,password);
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG)
                            .show();
                }

            }
        });


        // Request access  click event
        btnRequest.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Open the request access activity
                Intent i = new Intent(getApplicationContext(),RequestAccess.class);
                startActivity(i);
                // closing this screen
                finish();

            }
        });

    }
    public void clickHandler(View v){
        if (v.getId() == R.id.forgot_password) {
            startActivity(new Intent(getApplicationContext(), ForgotPassword.class));
        }
    }

    /**
     * Take the String representing the complete Autodoor in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String getAutodoorDataFromJson(String autodoorJsonStr)
            throws JSONException {

        // Now we have a String representing the complete Autodoor Info in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        // These are the names of the JSON objects that need to be extracted.

        final String TAG_SUCCESS = "success";
        final String TAG_MESSAGE = "message";
        // User information
        final String USER_RESULT = "user";
        final String USER_ID ="user_id";
        final String USER_FIRST_NAME = "f_name";
        final String USER_LAST_NAME = "l_name";
        final String USER_EMAIL = "email";
        final String USER_USERNAME= "username";
        final String USER_PASSWORD= "password";
        final String USER_TELEPHONE= "tel";
        final String USER_STATUS ="user_status";


        // Office information
        final String OFFICE_RESULT = "office";
        final String OFFICE_ID = "office_id";
        final String OFFICE_NAME = "office_name";
        final String OFFICE_NUMBER = "office_number";
        final String OFFICE_STATUS = "office_status";

        // Role information
        final String ROLE_RESULT = "role";
        final String ROLE_ID = "role_id";
        final String ROLE_NAME = "role_name";

        // User office information

        final String USER_OFFICE_RESULT = "useroffice";
        final String USER_OFFICE_ID = "user_office_id";

        try {


            JSONObject autodoorJson = new JSONObject(autodoorJsonStr);

            int success = autodoorJson.getInt(TAG_SUCCESS);

            if (success == 0) {
                String message = autodoorJson.getString(TAG_MESSAGE);
                return message;
            }

            JSONObject userJson = autodoorJson.getJSONObject(USER_RESULT); // Json Object
            JSONArray officeJson = autodoorJson.getJSONArray(OFFICE_RESULT);
            JSONObject roleJson = autodoorJson.getJSONObject(ROLE_RESULT);
            JSONArray userOfficeJson = autodoorJson.getJSONArray(USER_OFFICE_RESULT);

            // Get role data
            long roleId = roleJson.getLong(ROLE_ID);
            String roleName = roleJson.getString(ROLE_NAME);

            long roleRowId = db.addRole(roleId,roleName);

            // Insert the new user information into the database
            // Get user data
            long userId = userJson.getLong(USER_ID);
            String firstName = userJson.getString(USER_FIRST_NAME);
            String lastName = userJson.getString(USER_LAST_NAME);
            String userName = userJson.getString(USER_USERNAME);
            String password = userJson.getString(USER_PASSWORD);
            String email = userJson.getString(USER_EMAIL);
            String telephone = userJson.getString(USER_TELEPHONE);
            int userStatus = userJson.getInt(USER_STATUS);

            long userRowId = db.addUser(userId,firstName,lastName,userName,password,email,telephone,userStatus,roleRowId);

            // Get office data

            // Loop over the officeJson JsonArray and add the offices in database
            for (int i=0; i < officeJson.length(); i++) {
                JSONObject office = officeJson.getJSONObject(i);
                long officeId = office.getLong(OFFICE_ID);
                String officeName = office.getString(OFFICE_NAME);
                int officeNumber = office.getInt(OFFICE_NUMBER);
                String officeStatus = office.getString(OFFICE_STATUS);
                long officeRowId = db.addOffice(officeId,officeName,officeNumber,officeStatus);

                //Get User office data

                for(int j=0; j < userOfficeJson.length(); j ++) {
                    JSONObject userOffice = userOfficeJson.getJSONObject(j);
                    long userOfficeId = userOffice.getLong(USER_OFFICE_ID);
                    long userOfficeRowId = db.addUserOffice(userOfficeId,userRowId,officeRowId);
                    Log.d(LOG_TAG, "FetchAutodoorTask Complete. User ID:" + userOfficeRowId);
                }


            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
      return null;
    }

    public class AuthenticateUser extends AsyncTask<String, Void, String>{

        //private final String LOG_TAG = AuthenticateUser.class.getSimpleName();

        //private ArrayAdapter<String> mForecastAdapter;
        //private final Context mContext;



//        public AuthenticateUser(Context context) {
//            mContext = context;
//            //mForecastAdapter = forecastAdapter;
//        }



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

        @Override
        protected String doInBackground(String... params) {
            String message;
            // Verify size of params.
            if (params.length == 0) {
                return null;
            }

            String userName = params[0];
            String password = params[1];

            // get Internet status
            isInternetPresent = cd.isConnectingToInternet();
            if (!isInternetPresent) {

                Cursor c = db.getReadableDatabase().rawQuery(
                        "SELECT * FROM " + AutodoorContract.UserEntry.TABLE_NAME + " WHERE "
                                + AutodoorContract.UserEntry.COLUMN_USERNAME + "='" + userName +"' AND "+AutodoorContract.UserEntry.COLUMN_PASSWORD+"='"+password+"'"+" AND "+AutodoorContract.UserEntry.COLUMN_USER_STATUS+" = "+1,  null);
                c.moveToFirst();
                if (c.getCount()> 0) {

                    // Mark the user as logged in
                    session.setLogin(true);

                    // Store the userId
                    session.setUserId(c.getLong(0));
                    // closing connection
                    c.close();
                    db.close();
                    // Start Manage door activity
                    startManageDoor(userName,password);
                }

                else {
                    message = "Please Check your internet connection.";
                    return message;

                }
            }


            else {
                // failed to authenticate the user

                // These two need to be declared outside the try/catch
                // so that they can be closed in the finally block.
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                // Will contain the raw JSON response as a string.
                String autodoorJsonStr = null;

                try {
                    // Construct the URL for the AutodoorServer query
                    final String AUTODOOR_BASE_URL = serverName+"/login_processing.php?";
                    final String USERNAME_PARAM = "username";
                    final String PASSWORD_PARAM = "password";

                    Uri builtUri = Uri.parse(AUTODOOR_BASE_URL).buildUpon()
                            .appendQueryParameter(USERNAME_PARAM, userName)
                            .appendQueryParameter(PASSWORD_PARAM, password)
                            .build();
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
                    autodoorJsonStr = buffer.toString();
                    Log.v("Json string:", autodoorJsonStr);
                    message = getAutodoorDataFromJson(autodoorJsonStr);
                     if (message!= null) {
                         return message;
                     }

                    Cursor c = db.getReadableDatabase().rawQuery(
                            "SELECT * FROM " + AutodoorContract.UserEntry.TABLE_NAME + " WHERE "
                                    + AutodoorContract.UserEntry.COLUMN_USERNAME + "='" + userName +"' AND "+AutodoorContract.UserEntry.COLUMN_PASSWORD+"='"+password+"'"+" AND "+AutodoorContract.UserEntry.COLUMN_USER_STATUS+" = "+1,  null);
                    c.moveToFirst();
                    if (c.getCount()> 0) {

                        // Mark the user as logged in
                        session.setLogin(true);

                        // Store the userId
                        session.setUserId(c.getLong(0));
                        // closing connection
                        c.close();
                        db.close();

                        // Start Manage door Activity
                        startManageDoor(userName,password);

                    }


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

public void startManageDoor(String userName, String password) {
    // successfully find the user
    Intent i = new Intent(getApplicationContext(),ManageDoor.class);
    i.putExtra("username", userName);
    i.putExtra("password",password);
    startActivity(i);

    // closing this screen
    finish();

}

    }



}
