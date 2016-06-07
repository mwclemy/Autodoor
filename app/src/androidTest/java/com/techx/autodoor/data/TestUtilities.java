package com.techx.autodoor.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.techx.autodoor.Utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/*
    Students: These are functions and some test data to make it easier to test your database and
    Content Provider.  Note that you'll want your WeatherContract class to exactly match the one
    in our solution to use these as-given.
 */
public class TestUtilities extends AndroidTestCase {
    static final String TEST_ROLE_NAME = "Admin";
    static final String TEST_OFFICE_NAME = "Account Office";


    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    /*
        Students: Use this to create some default weather values for your database tests.
     */
    static ContentValues createUserValues(long roleRowId, Long officeRowId) {
        ContentValues userValues = new ContentValues();
        userValues.put(AutodoorContract.UserEntry.COLUMN_ROLE_ID, roleRowId);
        //userValues.put(AutodoorContract.UserEntry.COLUMN_OFFICE_ID, officeRowId);
        userValues.put(AutodoorContract.UserEntry.COLUMN_FIRST_NAME, "clement");
        userValues.put(AutodoorContract.UserEntry.COLUMN_LAST_NAME, "Mwimo");
        userValues.put(AutodoorContract.UserEntry.COLUMN_USERNAME, "kiki");
        userValues.put(AutodoorContract.UserEntry.COLUMN_PASSWORD, "kiki");
        userValues.put(AutodoorContract.UserEntry.COLUMN_EMAIL, "mwimoclement@gmail.com");
        userValues.put(AutodoorContract.UserEntry.COLUMN_TEL, "0788258075");

        return userValues;
    }

    /*
        We can uncomment this helper function once we have finished creating the
        RoleEntry part of the AutodoorContract.
     */
    static ContentValues createRoleValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(AutodoorContract.RoleEntry.COLUMN_ROLE_NAME, TEST_ROLE_NAME);
        return testValues;
    }

    /*
        We can uncomment this helper function once we have finished creating the
        OfficeEntry part of the AutodoorContract.
     */
    static ContentValues createOfficeValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(AutodoorContract.OfficeEntry.COLUMN_OFFICE_NAME, TEST_OFFICE_NAME);
        testValues.put(AutodoorContract.OfficeEntry.COLUMN_OFFICE_NUMBER, 1);
        testValues.put(AutodoorContract.OfficeEntry.COLUMN_OFFICE_STATUS, "Open");
        return testValues;
    }

    static long insertOfficeValues(Context context) {
        // insert our test records into the database
        AutodoorDbHelper dbHelper = new AutodoorDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createOfficeValues();

        long officeRowId;
        officeRowId = db.insert(AutodoorContract.OfficeEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert office Values", officeRowId != -1);

        return officeRowId;
    }

    static long insertRoleValues(Context context) {
        // insert our test records into the database
        AutodoorDbHelper dbHelper = new AutodoorDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createRoleValues();

        long roleRowId;
        roleRowId = db.insert(AutodoorContract.RoleEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert Role Values", roleRowId != -1);

        return roleRowId;
    }
    /*
        The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.
        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}