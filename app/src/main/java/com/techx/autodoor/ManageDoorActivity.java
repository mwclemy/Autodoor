package com.techx.autodoor;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ManageDoorActivity extends AppCompatActivity {

    // Declaring Your View and Variables
    JSONParser jsonParser = new JSONParser();
    Toolbar toolbar;
    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[] = {"Control Door", "Check Status"};
    int Numboftabs = 2;
    ImageView padLock;
    private static String url_control_door = "http://techxlab.netau.net/control_door.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_COMMAND = "command";
    private static final String TAG_HINT ="hint";

    // flag for Internet connection status
    Boolean isInternetPresent = false;

    // Connection detector class
    ConnectionDetector cd;
    private NetworkChangeReceiver receiver;

    //public static List<String> RESULT = new ArrayList<String>();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managedoor);

        // Creating The Toolbar and setting it as the Toolbar for the activity

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);


        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter = new ViewPagerAdapter(getSupportFragmentManager(), Titles, Numboftabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assigning the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.colorAccent);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

// creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkChangeReceiver();
        registerReceiver(receiver, filter);
    }
    public void clickHandler(View v){
// get Internet status
        isInternetPresent = cd.isConnectingToInternet();

        if (isInternetPresent) {
            switch(v.getId()) {
                case R.id.lock_pad:
                    // setting the background colour
                    v.setBackgroundColor(getResources().getColor(R.color.buttonColor));
                    // send the open command
                    new ControlDoor().execute("open");
                    break;

                case R.id.closed_pad:
                    v.setBackgroundColor(getResources().getColor(R.color.buttonColor));
                    new ControlDoor().execute("closed");
                    break;

                default:
                    break;

            }
        }
        else {
            // Internet connection is not present
            // Ask user to connect to Internet
            Toast.makeText(getApplicationContext(), "No internet connection.",
                    Toast.LENGTH_LONG).show();
        }

        }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
// get Internet status
            isInternetPresent = cd.isConnectingToInternet();
            // check for Internet status
            if (isInternetPresent) {
                // Internet Connection is Present
                // make HTTP requests



            } else {
                // Internet connection is not present
                // Ask user to connect to Internet
                Toast.makeText(getApplicationContext(), "No internet connection.",
                        Toast.LENGTH_LONG).show();

            }

        }
    }

        class ControlDoor extends AsyncTask<String, Void, List<String>> {

        /**
         * Control Door
         * */
        @Override
        protected List<String> doInBackground(String... args) {
            List<String> RESULT = new ArrayList<String>();
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("command", args[0]));
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_control_door,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);
                if ((success == 1)) {
                    // successfully controlled the door
                    Log.d("properly control door", json.toString());
                    RESULT.add(json.getString(TAG_COMMAND));
                    //return   json.getString(TAG_COMMAND);
                    return RESULT;

                } else if ((success == 0)){
                    Log.d("failed to control door", json.toString());
                    //return   json.getString(TAG_COMMAND);
//                    result = errorResult(json.getString(TAG_COMMAND), json.getString(TAG_HINT));
                    RESULT.add(json.getString(TAG_COMMAND));
                    RESULT.add(json.getString(TAG_HINT));

                    return RESULT;

                }

                else {

                }



            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            if(result.get(0).equals("open")) {
                   padLock = (ImageView) findViewById(R.id.lock_pad);
                   padLock.setBackgroundColor(getResources().getColor(R.color.white_greyish));

                notification(R.drawable.open_pad_lock,"The door has been opened.", 9999);

               }
                else if(result.get(0).equals("closed")) {
                   padLock = (ImageView) findViewById(R.id.closed_pad);
                   padLock.setBackgroundColor(getResources().getColor(R.color.white_greyish));
                notification(R.drawable.closed_pad_lock,"The door has been closed.", 8888);
               }
               else if (result.get(0).equals("command failed. ")) {
                padLock = (ImageView) findViewById(R.id.lock_pad);
                padLock.setBackgroundColor(getResources().getColor(R.color.white_greyish));
                padLock = (ImageView) findViewById(R.id.closed_pad);
                padLock.setBackgroundColor(getResources().getColor(R.color.white_greyish));
                notification(R.drawable.closed_pad_lock,"The door failed to be " + result.get(1) , 7777);
            }
               else {
                padLock = (ImageView) findViewById(R.id.lock_pad);
                padLock.setBackgroundColor(getResources().getColor(R.color.white_greyish));
                padLock = (ImageView) findViewById(R.id.closed_pad);
                padLock.setBackgroundColor(getResources().getColor(R.color.white_greyish));
                Toast.makeText(getApplicationContext(), "Oops the " + result.get(1) + " command has already been sent.",
                        Toast.LENGTH_LONG).show();
            }




        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_change_pin:
                // change pin action
                changePin();
                return true;
            case R.id.action_logout:
                // logout action
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    /**
     * Launching new activity
     * */
    private void changePin() {
        Intent i = new Intent(ManageDoorActivity.this, ChangePinActivity.class);
        startActivity(i);
    }

    private void logout() {
        Intent i = new Intent(ManageDoorActivity.this, MainActivity.class);
        startActivity(i);
    }

    public void notification(int icon, String contentText, int notId) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(icon)
                        .setContentTitle("Autodoor")
                        .setAutoCancel(true)
                        .setContentText(contentText);

// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(getApplicationContext(), ManageDoorActivity.class);
        // The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ManageDoorActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(notId, mBuilder.build());
    }
    }
