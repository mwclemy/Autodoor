package com.techx.autodoor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

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
public class ManageDoorFragment extends Fragment {

    AutodoorDbHelper db;
    SessionManager session;
    ListView myList;
    ProgressDialog pDialog;
    // flag for Internet connection status
    Boolean isInternetPresent = false;
    // Connection detector class
    ConnectionDetector cd;
    final String TAG_SUCCESS = "success";
    final String TAG_MESSAGE = "message";
    private final String LOG_TAG =ManageDoorFragment.class.getSimpleName();
    private String serverName = "http://android.autodoor.comli.com";
    public ManageDoorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_control, container, false);

        db = new AutodoorDbHelper(getActivity());
        session = new SessionManager(getActivity());
        // creating connection detector class instance
        cd = new ConnectionDetector(getActivity().getApplicationContext());
        myList = (ListView) rootView.findViewById(R.id.listview_control);

        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    long userId = session.getUserId();
                    long officeId = cursor.getLong(0);
                    String officeStatus =cursor.getString(3);
                    String officeName = cursor.getString(1);

                    // Send the command to the web server.

                    new ControlDoor().execute(Long.toString(userId),Long.toString(officeId),officeName,officeStatus);
                    populateControlListView();
//                    Toast.makeText(getActivity().getApplicationContext(),
//                            officeStatus+","+officeName, Toast.LENGTH_LONG)
//                            .show();
                }
            }
        });

        populateControlListView();
        // Inflate the layout for this fragment
        return rootView;
    }


    private  String getResponseDataFromJson(String responseJsonStr,long officeId)
            throws JSONException {

        final String NEW_OFFICE_STATUS= "newofficestatus";

        try {
            JSONObject responseJson = new JSONObject(responseJsonStr);

            int success = responseJson.getInt(TAG_SUCCESS);

            if (success == 1) {
                String newOfficeStatus = responseJson.getString(NEW_OFFICE_STATUS);
                // update the local database
                db.updateOfficeStatus(officeId,newOfficeStatus);
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
     * Async task to control the door
     * */
    private class ControlDoor extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Control Door...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            long officeId = Long.parseLong(params[1]);
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
                                    + "&officeName=" + URLEncoder.encode(params[2])
                                    + "&officeStatus=" + URLEncoder.encode(params[3]);


                    // Construct the URL for the AutodoorServer query
                    final String AUTODOOR_BASE_URL = serverName+"/control_door.php";


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

                    return getResponseDataFromJson(responseJsonStr, officeId);


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


    public void populateControlListView() {
        long userId = session.getUserId();
        Cursor cursor = db.getUserOffices(userId);
        String [] fromFieldsName = new String[] {AutodoorContract.OfficeEntry.COLUMN_OFFICE_STATUS,AutodoorContract.OfficeEntry.COLUMN_OFFICE_NAME};
        int [] toViewIds = new int [] {R.id.office_status,R.id.office_name};
        SimpleCursorAdapter myCursorAdapter;
        myCursorAdapter = new SimpleCursorAdapter(getActivity().getBaseContext(),R.layout.list_control_item,cursor,fromFieldsName,toViewIds,0);
        myCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) {

                if (aColumnIndex == 3) {
                    String officeStatus = aCursor.getString(aColumnIndex);
                    TextView textView = (TextView) aView;
                    textView.setText(reverseOfficeStatus(officeStatus));
                    return true;
                }

                return false;
            }
        });
        myList.setAdapter(myCursorAdapter);
    }

    public String reverseOfficeStatus (String officeStatus) {

        String reversedStatus;

        if (officeStatus.equals("Open")) {
            reversedStatus="Close";
        }
        else {
            reversedStatus="Open";
        }
     return reversedStatus;
    }
}
