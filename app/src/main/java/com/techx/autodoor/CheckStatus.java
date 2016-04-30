package com.techx.autodoor;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by change on 11/27/2015.
 */
public class CheckStatus  extends Fragment {

    // Json parser
    JSONParser jsonParser = new JSONParser();
    // url to authenticate the user
    private static String url_check_status = "http://techxlab.netau.net/check_status.php";
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    //Declaring a TextView
    private TextView doorStatus;

@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    new  StatusCheck().execute("check_status");
}
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.check_status,container,false);
        return v;
    }

    class StatusCheck extends AsyncTask<String, String, String> {

        /**
         * Control Door
         * */
        @Override
        protected String doInBackground(String... args) {

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("command", args[0]));
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_check_status ,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);
                if ((success == 1)) {
                    // successfully controlled the door
                    Log.d("failed to control door", json.toString());
                    return   json.getString(TAG_MESSAGE);

                } else{
                    Log.d("failed to control door", json.toString());
                    return   json.getString(TAG_MESSAGE);

                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String message) {
            doorStatus = (TextView) getView().findViewById(R.id.door_status);
            doorStatus.setText(message.toUpperCase());
        }
    }
}