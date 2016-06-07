package com.techx.autodoor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * Created by change on 5/16/2016.
 */
public class ManageDoor extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener {


private static String TAG = ManageDoor.class.getSimpleName();

private Toolbar mToolbar;
private FragmentDrawer drawerFragment;

@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_door);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerFragment = (FragmentDrawer)
        getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        // display the first navigation drawer view on app launch
        displayView(0);
        }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
//        if (mDrawerToggle.onOptionsItemSelected(item)) {
//            return true;
//        }
        Intent intent;
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_change_password:
                // Launching the ChangePasswordFragment
               displayView(1);
                return true;
            case R.id.action_logout:
                // Launching the LogoutFragment
                displayView(2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

@Override
public void onDrawerItemSelected(View view, int position) {
        displayView(position);
        }

private void displayView(int position) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (position) {
        case 0:
        fragment = new ManageDoorFragment();
        title = getString(R.string.title_activity_manage_door);
        break;
        case 1:
        fragment = new ChangePasswordFragment();
        title = getString(R.string.title_change_password);
        break;
        case 2:
        fragment = new LogoutFragment();
        title = getString(R.string.title_logout);
        break;
            case 3:
                fragment = new HelpFragment();
                title = getString(R.string.title_help);
                break;
            case 4:
                fragment = new AboutFragment();
                title = getString(R.string.title_about);
                break;
default:
        break;
        }

        if (fragment != null) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_body, fragment);
        fragmentTransaction.commit();

        // set the toolbar title
        getSupportActionBar().setTitle(title);
        }
        }
}

