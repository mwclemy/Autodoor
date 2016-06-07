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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(AutodoorDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(AutodoorContract.RoleEntry.TABLE_NAME);
        tableNameHashSet.add(AutodoorContract.OfficeEntry.TABLE_NAME);
        tableNameHashSet.add(AutodoorContract.UserEntry.TABLE_NAME);
        mContext.deleteDatabase(AutodoorDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new AutodoorDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext());

        // if this fails, it means that the database doesn't contain both the data entry tables
        assertTrue("Error: Your database was created without data entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + AutodoorContract.OfficeEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> officeColumnHashSet = new HashSet<String>();
        officeColumnHashSet.add(AutodoorContract.OfficeEntry._ID);
        officeColumnHashSet.add(AutodoorContract.OfficeEntry.COLUMN_OFFICE_NAME);
        officeColumnHashSet.add(AutodoorContract.OfficeEntry.COLUMN_OFFICE_NUMBER);
        officeColumnHashSet.add(AutodoorContract.OfficeEntry.COLUMN_OFFICE_STATUS);
        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            officeColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required office
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                officeColumnHashSet.isEmpty());
        db.close();
    }

    /*
       Here is where we will build code to test that we can insert and query the
        Role table.
    */
    public void testRoleTable() {
        insertRole();
    }

    /*
      Here is where we will build code to test that we can insert and query the
       Office table.
   */
    public void testOfficeTable() {
        insertOffice();
    }

    /*
        Here is where we will build code to test that we can insert and query the
        user table.
     */
    public void testUserTable() {
        // First insert the Role, and Office then use the roleRowId and officeRowId to insert
        // the user.
        long roleRowId = insertRole();

        // Make sure we have a valid row ID.
        assertFalse("Error: Role Not Inserted Correctly", roleRowId == -1L);

        long officeRowId = insertOffice();

        // Make sure we have a valid row ID.
        assertFalse("Error: Role Not Inserted Correctly", officeRowId == -1L);

        // First step: Get reference to writable database
        AutodoorDbHelper dbHelper= new AutodoorDbHelper(mContext);
        SQLiteDatabase db= dbHelper.getWritableDatabase();
        // Create ContentValues of what we want to insert
        ContentValues userValues = com.techx.autodoor.data.TestUtilities.createUserValues(roleRowId,officeRowId);
        // Insert ContentValues into database and get a row ID back
        long userRowId = db.insert(AutodoorContract.UserEntry.TABLE_NAME, null, userValues);
        assertTrue(userRowId != -1);
        // Query the database and receive a Cursor back
        Cursor userCursor = db.query(
                AutodoorContract.UserEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );
        // Move the cursor to a valid database row
        assertTrue("Error: No record return from the user query", userCursor.moveToFirst());
        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        com.techx.autodoor.data.TestUtilities.validateCurrentRecord("testInsertReadDb userEntry failed to validate",
                userCursor, userValues);
        // Finally, close the cursor and database
        userCursor.close();
        db.close();
    }


    /*
         This is a helper method for inserting a role.
     */
    public long insertRole() {
        // First step: Get reference to writable database
        AutodoorDbHelper dbHelper= new AutodoorDbHelper(mContext);
        SQLiteDatabase db= dbHelper.getWritableDatabase();
        // Create ContentValues of what we  want to insert
        ContentValues testValues = com.techx.autodoor.data.TestUtilities.createRoleValues();
        // Insert ContentValues into database and get a row ID back
        long roleRowId ;
        roleRowId= db.insert(AutodoorContract.RoleEntry.TABLE_NAME,null,testValues);
        // Verify  we got a row back
        assertTrue(roleRowId != -1);
        // Query the database and receive a Cursor back
        Cursor cursor = db.query(
                AutodoorContract.RoleEntry.TABLE_NAME,
                null, // all columns
                null, // Columns of the where clause
                null, // Values of the where clause
                null,// Columns to group by
                null,// columns to filter by row groups
                null // sort order


        ) ;
        // Move the cursor to a valid database row
        assertTrue("Error: No record return from the Role query", cursor.moveToFirst());
        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        com.techx.autodoor.data.TestUtilities.validateCursor("Error: Location query validation failed", cursor, testValues);
        assertFalse("Error: More than one record return from Role query",cursor.moveToNext());
        // Finally, close the cursor and database
        cursor.close();
        db.close();

        return roleRowId;
    }

    /*
         This is a helper method for inserting a role.
     */
    public long insertOffice() {
        // First step: Get reference to writable database
        AutodoorDbHelper dbHelper= new AutodoorDbHelper(mContext);
        SQLiteDatabase db= dbHelper.getWritableDatabase();
        // Create ContentValues of what we  want to insert
        ContentValues testValues = com.techx.autodoor.data.TestUtilities.createOfficeValues();
        // Insert ContentValues into database and get a row ID back
        long officeRowId ;
        officeRowId= db.insert(AutodoorContract.OfficeEntry.TABLE_NAME,null,testValues);
        // Verify  we got a row back
        assertTrue(officeRowId != -1);
        // Query the database and receive a Cursor back
        Cursor cursor = db.query(
                AutodoorContract.OfficeEntry.TABLE_NAME,
                null, // all columns
                null, // Columns of the where clause
                null, // Values of the where clause
                null,// Columns to group by
                null,// columns to filter by row groups
                null // sort order


        ) ;
        // Move the cursor to a valid database row
        assertTrue("Error: No record return from the Role query", cursor.moveToFirst());
        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        com.techx.autodoor.data.TestUtilities.validateCursor("Error: Office query validation failed", cursor, testValues);
        assertFalse("Error: More than one record return from Office query",cursor.moveToNext());
        // Finally, close the cursor and database
        cursor.close();
        db.close();

        return officeRowId;
    }
}