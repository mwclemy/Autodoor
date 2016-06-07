/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.techx.autodoor.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;


/*
    This is not a complete set of tests of the Autodoor ContentProvider, but it does test
    that at least the basic functionality has been implemented correctly.

 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.

     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                AutodoorContract.UserEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                AutodoorContract.OfficeEntry.CONTENT_URI,
                null,
                null
        );

        mContext.getContentResolver().delete(
                AutodoorContract.RoleEntry.CONTENT_URI,
                null,
                null
        );
        Cursor cursor = mContext.getContentResolver().query(
                AutodoorContract.UserEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from User table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                AutodoorContract.OfficeEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Office table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                AutodoorContract.RoleEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Role table during delete", 0, cursor.getCount());
        cursor.close();
    }

    /*
       This helper function deletes all records from both database tables using the database
       functions only.  This is designed to be used to reset the state of the database until the
       delete functionality is available in the ContentProvider.
     */
    public void deleteAllRecordsFromDB() {
        AutodoorDbHelper dbHelper = new AutodoorDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(AutodoorContract.UserEntry.TABLE_NAME, null, null);
        db.delete(AutodoorContract.OfficeEntry.TABLE_NAME, null, null);
        db.delete(AutodoorContract.RoleEntry.TABLE_NAME, null, null);
        db.close();
    }


    public void deleteAllRecords() {
        deleteAllRecordsFromDB();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.

     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // AutodoorProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                AutodoorProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: AutodoorProvider registered with authority: " + providerInfo.authority +
                    " instead of authority: " + AutodoorContract.CONTENT_AUTHORITY,
                    providerInfo.authority, AutodoorContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: AutodoorProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
            This test doesn't touch the database.  It verifies that the ContentProvider returns
            the correct type for each type of URI that it can handle.

         */
    public void testGetType() {
        // content://com.techx.autodoor/user/
        String type = mContext.getContentResolver().getType(AutodoorContract.UserEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.techx.autodoor/user
        assertEquals("Error: the UserEntry CONTENT_URI should return UserEntry.CONTENT_TYPE",
                AutodoorContract.UserEntry.CONTENT_TYPE, type);
        // content://com.techx.autodoor/office/
        type = mContext.getContentResolver().getType(AutodoorContract.OfficeEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.techx.autodoor/office
        assertEquals("Error: the OfficeEntry CONTENT_URI should return OfficeEntry.CONTENT_TYPE",
                AutodoorContract.OfficeEntry.CONTENT_TYPE, type);

        // content://com.techx.autodoor/role/
        type = mContext.getContentResolver().getType(AutodoorContract.OfficeEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.techx.autodoor/role
        assertEquals("Error: the UserEntry CONTENT_URI should return OfficeEntry.CONTENT_TYPE",
                AutodoorContract.OfficeEntry.CONTENT_TYPE, type);

        long testUserId = 1L;
        // content://com.techx.autodoor/office/1
        type = mContext.getContentResolver().getType(
                AutodoorContract.OfficeEntry.buildOfficeUri(testUserId));
        // vnd.android.cursor.item/com.techx.autodoor/office/1
        assertEquals("Error: the OfficeEntry CONTENT_URI with userid should return OfficeEntry.CONTENT_ITEM_TYPE",
                AutodoorContract.OfficeEntry.CONTENT_ITEM_TYPE, type);

        // content://com.techx.autodoor/role/1
        type = mContext.getContentResolver().getType(
                AutodoorContract.RoleEntry.buildRoleUri(testUserId));
        // vnd.android.cursor.item/com.techx.autodoor/role/1
        assertEquals("Error: the RoleEntry CONTENT_URI with userid should return RoleEntry.CONTENT_ITEM_TYPE",
                AutodoorContract.RoleEntry.CONTENT_ITEM_TYPE, type);

    }


    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.
     */
    public void testBasicUserQuery() {
        // insert our test records into the database
        AutodoorDbHelper dbHelper = new AutodoorDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long officeRowId = com.techx.autodoor.data.TestUtilities.insertOfficeValues(mContext);
        long roleRowId = com.techx.autodoor.data.TestUtilities.insertRoleValues(mContext);

        // Fantastic.  Now that we have an office and role , we can add a user!
        ContentValues userValues = com.techx.autodoor.data.TestUtilities.createUserValues(roleRowId,officeRowId);

        long userRowId = db.insert(AutodoorContract.UserEntry.TABLE_NAME, null, userValues);
        assertTrue("Unable to Insert UserEntry into the Database", userRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor userCursor = mContext.getContentResolver().query(
                AutodoorContract.UserEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        com.techx.autodoor.data.TestUtilities.validateCursor("testBasicAutodoorQuery", userCursor, userValues);
    }

    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.
     */
    public void testBasicOfficeQueries() {
        // insert our test records into the database
        ContentValues testValues = com.techx.autodoor.data.TestUtilities.createOfficeValues();
        long officeRowId = com.techx.autodoor.data.TestUtilities.insertOfficeValues(mContext);

        // Test the basic content provider query
        Cursor officeCursor = mContext.getContentResolver().query(
                AutodoorContract.OfficeEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        com.techx.autodoor.data.TestUtilities.validateCursor("testBasicOfficeQueries, office query", officeCursor, testValues);

        // Has the NotificationUri been set correctly? --- we can only test this easily against API
        // level 19 or greater because getNotificationUri was added in API level 19.
        if ( Build.VERSION.SDK_INT >= 19 ) {
            assertEquals("Error: Office Query did not properly set NotificationUri",
                    officeCursor.getNotificationUri(), AutodoorContract.OfficeEntry.CONTENT_URI);
        }
    }


    /*
       This test uses the database directly to insert and then uses the ContentProvider to
       read out the data.
    */
    public void testBasicRoleQueries() {
        // insert our test records into the database
        ContentValues testValues = com.techx.autodoor.data.TestUtilities.createRoleValues();
        long roleRowId = com.techx.autodoor.data.TestUtilities.insertRoleValues(mContext);

        // Test the basic content provider query
        Cursor roleCursor = mContext.getContentResolver().query(
                AutodoorContract.RoleEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        com.techx.autodoor.data.TestUtilities.validateCursor("testBasicRoleQueries, role query", roleCursor, testValues);

        // Has the NotificationUri been set correctly? --- we can only test this easily against API
        // level 19 or greater because getNotificationUri was added in API level 19.
        if ( Build.VERSION.SDK_INT >= 19 ) {
            assertEquals("Error: role Query did not properly set NotificationUri",
                    roleCursor.getNotificationUri(), AutodoorContract.RoleEntry.CONTENT_URI);
        }
    }

    /*
        This test uses the provider to insert and then update the data.
     */
    public void testUpdateOffice() {
        // Create a new map of values, where column names are the keys
        ContentValues values = com.techx.autodoor.data.TestUtilities.createOfficeValues();

        Uri officeUri = mContext.getContentResolver().
                insert(AutodoorContract.OfficeEntry.CONTENT_URI, values);
        long officeRowId = ContentUris.parseId(officeUri);

        // Verify we got a row back.
        assertTrue(officeRowId != -1);
        Log.d(LOG_TAG, "New row id: " + officeRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(AutodoorContract.OfficeEntry._ID, officeRowId);
        updatedValues.put(AutodoorContract.OfficeEntry.COLUMN_OFFICE_NAME, "CEO office");
        updatedValues.put(AutodoorContract.OfficeEntry.COLUMN_OFFICE_NUMBER, 2);

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor officeCursor = mContext.getContentResolver().query(AutodoorContract.OfficeEntry.CONTENT_URI, null, null, null, null);

        com.techx.autodoor.data.TestUtilities.TestContentObserver tco = com.techx.autodoor.data.TestUtilities.getTestContentObserver();
        officeCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                AutodoorContract.OfficeEntry.CONTENT_URI, updatedValues, AutodoorContract.OfficeEntry._ID + "= ?",
                new String[] { Long.toString(officeRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        //
        // If our code is failing here, it means that our content provider
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();

        officeCursor.unregisterContentObserver(tco);
        officeCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                AutodoorContract.OfficeEntry.CONTENT_URI,
                null,   // projection
                AutodoorContract.OfficeEntry._ID + " = " + officeRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        com.techx.autodoor.data.TestUtilities.validateCursor("testUpdateOffice.  Error validating office entry update.",
                cursor, updatedValues);

        cursor.close();
    }


    // Make sure we can still delete after adding/updating stuff
    //

    public void testInsertReadProvider() {
        ContentValues officeValues = com.techx.autodoor.data.TestUtilities.createOfficeValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        com.techx.autodoor.data.TestUtilities.TestContentObserver tco = com.techx.autodoor.data.TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(AutodoorContract.OfficeEntry.CONTENT_URI, true, tco);
        Uri officeUri = mContext.getContentResolver().insert(AutodoorContract.OfficeEntry.CONTENT_URI, officeValues);

        // Did our content observer get called?  If this fails, Our insert office
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long officeRowId = ContentUris.parseId(officeUri);

        // Verify we got a row back.
        assertTrue(officeRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor officeCursor = mContext.getContentResolver().query(
                AutodoorContract.OfficeEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        com.techx.autodoor.data.TestUtilities.validateCursor("testInsertReadProvider. Error validating OfficeEntry.",
                officeCursor, officeValues);

        ContentValues roleValues = com.techx.autodoor.data.TestUtilities.createRoleValues();
        // The TestContentObserver is a one-shot class
        tco = com.techx.autodoor.data.TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(AutodoorContract.RoleEntry.CONTENT_URI, true, tco);
        Uri roleUri = mContext.getContentResolver().insert(AutodoorContract.RoleEntry.CONTENT_URI, roleValues);

        // Did our content observer get called?  If this fails, Our insert role
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long roleRowId = ContentUris.parseId(roleUri);

        // Verify we got a row back.
        assertTrue(roleRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor roleCursor = mContext.getContentResolver().query(
                AutodoorContract.RoleEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        com.techx.autodoor.data.TestUtilities.validateCursor("testInsertReadProvider. Error validating RoleEntry.",
                roleCursor, roleValues);

        // Fantastic.  Now that we have a office and role we can add user!
        ContentValues userValues = com.techx.autodoor.data.TestUtilities.createUserValues(roleRowId,officeRowId);
        // The TestContentObserver is a one-shot class
        tco = com.techx.autodoor.data.TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(AutodoorContract.UserEntry.CONTENT_URI, true, tco);

        Uri userInsertUri = mContext.getContentResolver()
                .insert(AutodoorContract.UserEntry.CONTENT_URI, userValues);
        assertTrue(userInsertUri != null);

        // Did our content observer get called? If this fails, our insert user
        // in our ContentProvider isn't calling
        // getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor userCursor = mContext.getContentResolver().query(
                AutodoorContract.UserEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        com.techx.autodoor.data.TestUtilities.validateCursor("testInsertReadProvider. Error validating UserEntry insert.",
                userCursor, userValues);


    }

    // Make sure we can still delete after adding/updating stuff


    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our office delete.
        com.techx.autodoor.data.TestUtilities.TestContentObserver officeObserver = com.techx.autodoor.data.TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(AutodoorContract.OfficeEntry.CONTENT_URI, true, officeObserver);


        // Register a content observer for our role delete.
        com.techx.autodoor.data.TestUtilities.TestContentObserver roleObserver = com.techx.autodoor.data.TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(AutodoorContract.RoleEntry.CONTENT_URI, true, roleObserver);

        // Register a content observer for our user delete.
        com.techx.autodoor.data.TestUtilities.TestContentObserver userObserver = com.techx.autodoor.data.TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(AutodoorContract.UserEntry.CONTENT_URI, true, userObserver);

        deleteAllRecordsFromProvider();

        // If either of these fail, we most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        officeObserver.waitForNotificationOrFail();
        roleObserver.waitForNotificationOrFail();
        userObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(officeObserver);
        mContext.getContentResolver().unregisterContentObserver(roleObserver);
        mContext.getContentResolver().unregisterContentObserver(userObserver);
    }


    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    static ContentValues[] createBulkInsertUserValues(long roleRowId, Long officeRowId ) {

        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++) {
            ContentValues userValues = new ContentValues();
            userValues.put(AutodoorContract.UserEntry.COLUMN_ROLE_ID, roleRowId);
            //userValues.put(AutodoorContract.UserEntry.COLUMN_OFFICE_ID, officeRowId);
            userValues.put(AutodoorContract.UserEntry.COLUMN_FIRST_NAME, "clement");
            userValues.put(AutodoorContract.UserEntry.COLUMN_LAST_NAME, "Mwimo");
            userValues.put(AutodoorContract.UserEntry.COLUMN_USERNAME, "kiki");
            userValues.put(AutodoorContract.UserEntry.COLUMN_PASSWORD, "kiki");
            userValues.put(AutodoorContract.UserEntry.COLUMN_EMAIL, "mwimoclement@gmail.com");
            userValues.put(AutodoorContract.UserEntry.COLUMN_TEL, "0788258075");
            returnContentValues[i] = userValues;
        }
        return returnContentValues;
    }

    // Note that this test will work with the built-in (default) provider
    // implementation, which just inserts records one-at-a-time.
    public void testBulkInsert() {
        // first, let's create a an office value
        ContentValues officeValues = TestUtilities.createOfficeValues();
        Uri officeUri = mContext.getContentResolver().insert(AutodoorContract.OfficeEntry.CONTENT_URI, officeValues);
        long officeRowId = ContentUris.parseId(officeUri);

        // Verify we got a row back.
        assertTrue(officeRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor officeCursor = mContext.getContentResolver().query(
                AutodoorContract.OfficeEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testBulkInsert. Error validating OfficeEntry.",
                officeCursor, officeValues);


        // let's create a role value
        ContentValues roleValues = TestUtilities.createRoleValues();
        Uri roleUri = mContext.getContentResolver().insert(AutodoorContract.RoleEntry.CONTENT_URI, roleValues);
        long roleRowId = ContentUris.parseId(roleUri);

        // Verify we got a row back.
        assertTrue(roleRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor roleCursor = mContext.getContentResolver().query(
                AutodoorContract.RoleEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testBulkInsert. Error validating RoleEntry.",
                roleCursor, roleValues);

        // Now we can bulkInsert some user.  In fact, we only implement BulkInsert for user
        // entries.  With ContentProviders, we really only have to implement the features we
        // use, after all.
        ContentValues[] bulkInsertContentValues = createBulkInsertUserValues(roleRowId, officeRowId);

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver userObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(AutodoorContract.UserEntry.CONTENT_URI, true, userObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(AutodoorContract.UserEntry.CONTENT_URI, bulkInsertContentValues);

        // If this fails, it means that we most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in our BulkInsert
        // ContentProvider method.
        userObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(userObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        Cursor userCursor = mContext.getContentResolver().query(
                AutodoorContract.UserEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null
        );

        // we should have as many records in the database as we've inserted
        assertEquals(userCursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        userCursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, userCursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating UserEntry " + i,
                    userCursor, bulkInsertContentValues[i]);
        }
        userCursor.close();
    }
}