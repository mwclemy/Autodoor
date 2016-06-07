package com.techx.autodoor.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a local database for Autodoor data.
 */
public class AutodoorDbHelper extends SQLiteOpenHelper{

    private static final String LOG_TAG = AutodoorDbHelper.class.getSimpleName();
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 8;

    static final String DATABASE_NAME = "autodoor.db";
    //SQLiteDatabase db = this.getWritableDatabase();

    public AutodoorDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold roles.  A Role consists of the string supplied in the
        // role name.
        final String SQL_CREATE_ROLE_TABLE = "CREATE TABLE " + AutodoorContract.RoleEntry.TABLE_NAME + " (" +
                AutodoorContract.RoleEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                AutodoorContract.RoleEntry.COLUMN_ROLE_NAME + " TEXT NOT NULL " +
                " );";

        // Create a table to hold offices.  An office consists of the string supplied in the
        // office name, and office number.
        final String SQL_CREATE_OFFICE_TABLE = "CREATE TABLE " + AutodoorContract.OfficeEntry.TABLE_NAME + " (" +
                AutodoorContract.OfficeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                AutodoorContract.OfficeEntry.COLUMN_OFFICE_NAME + " TEXT NOT NULL, " +
                AutodoorContract.OfficeEntry.COLUMN_OFFICE_NUMBER + " INTEGER NOT NULL, " +
                AutodoorContract.OfficeEntry.COLUMN_OFFICE_STATUS + " TEXT NOT NULL " +
                " );";

        final String SQL_CREATE_USER_TABLE = "CREATE TABLE " + AutodoorContract.UserEntry.TABLE_NAME + " (" +
                AutodoorContract.UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                // the ID of the role entry associated with this user data
                AutodoorContract.UserEntry.COLUMN_ROLE_ID + " INTEGER NOT NULL, " +
                AutodoorContract.UserEntry.COLUMN_FIRST_NAME + " TEXT NOT NULL, " +
                AutodoorContract.UserEntry.COLUMN_LAST_NAME + " TEXT NOT NULL, " +
                AutodoorContract.UserEntry.COLUMN_USERNAME + " TEXT NOT NULL, " +
                AutodoorContract.UserEntry.COLUMN_PASSWORD + " TEXT NOT NULL, " +
                AutodoorContract.UserEntry.COLUMN_EMAIL + " TEXT NOT NULL, " +
                AutodoorContract.UserEntry.COLUMN_TEL + " TEXT NOT NULL, " +
                AutodoorContract.UserEntry.COLUMN_USER_STATUS + " INTEGER NOT NULL, " +

                // Set up the role column as a foreign key to role table.
                " FOREIGN KEY (" + AutodoorContract.UserEntry.COLUMN_ROLE_ID + ") REFERENCES " +
                AutodoorContract.RoleEntry.TABLE_NAME + " (" + AutodoorContract.RoleEntry._ID + ") " +

                " );";

        // Create a table to hold user role.
        final String SQL_CREATE_USER_OFFICE_TABLE = "CREATE TABLE " + AutodoorContract.UserOfficeEntry.TABLE_NAME + " (" +
                AutodoorContract.UserOfficeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                AutodoorContract.UserOfficeEntry.COLUMN_USER_ID+ " INTEGER NOT NULL," +
                AutodoorContract.UserOfficeEntry.COLUMN_OFFICE_ID + " INTEGER NOT NULL, " +

                // Set up the office column as a foreign key to office table.
                " FOREIGN KEY (" + AutodoorContract.UserOfficeEntry.COLUMN_OFFICE_ID + ") REFERENCES " +
                AutodoorContract.OfficeEntry.TABLE_NAME + " (" + AutodoorContract.OfficeEntry._ID + "), " +
                // Set up the user column as a foreign key to user table.
                " FOREIGN KEY (" + AutodoorContract.UserOfficeEntry.COLUMN_USER_ID + ") REFERENCES " +
                AutodoorContract.UserEntry.TABLE_NAME + " (" + AutodoorContract.UserEntry._ID + ") " +

                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_ROLE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_OFFICE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_USER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_USER_OFFICE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if we change the version number for our database.
        // It does NOT depend on the version number for our application.
        // If we want to update the schema without wiping data, commenting out the next 2 lines
        // should be our top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AutodoorContract.RoleEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AutodoorContract.OfficeEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AutodoorContract.UserEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    /**
     * Helper method to handle insertion of a new office in the Autodoor database.
     *
     * @param officeName the name of the office.
     * @param officeNumber the number representing an office.
     * @param officeStatus the status of the office. example: Open/Close
     * @return the row ID of the added office.
     */
    public long addOffice(long officeId,String officeName, int officeNumber, String officeStatus) {

        long officeRowId;

        SQLiteDatabase db = this.getReadableDatabase();

        // check if there's no office with this id officeId.
        Cursor c = db.rawQuery(
                "SELECT * FROM " + AutodoorContract.OfficeEntry.TABLE_NAME + " WHERE "
                        + AutodoorContract.OfficeEntry._ID + " = " + officeId ,  null);
        // Move to first row

        if ( c.moveToFirst()) {
            c.close();
            db.close(); // Closing database connection
            return officeId;
        }

        else {

            ContentValues officeValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            officeValues.put(AutodoorContract.OfficeEntry._ID, officeId);
            officeValues.put(AutodoorContract.OfficeEntry.COLUMN_OFFICE_NAME, officeName);
            officeValues.put(AutodoorContract.OfficeEntry.COLUMN_OFFICE_NUMBER, officeNumber);
            officeValues.put(AutodoorContract.OfficeEntry.COLUMN_OFFICE_STATUS, officeStatus);

            // Finally, insert office data into the database.

            officeRowId = db.insert(AutodoorContract.OfficeEntry.TABLE_NAME, null, officeValues);
            db.close(); // Closing database connection

            Log.d(LOG_TAG, "New office inserted into sqlite: " + officeRowId);

            return officeRowId;
        }

    }

    public void updateOfficeStatus(long officeId, String newOfficeStatus) {

        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(AutodoorContract.OfficeEntry.COLUMN_OFFICE_STATUS, newOfficeStatus);
        db.update(AutodoorContract.OfficeEntry.TABLE_NAME, cv, AutodoorContract.OfficeEntry._ID + "= ?", new String[] {Long.toString(officeId)});


    }

    /**
     * Helper method to handle insertion of a new role in the Autodoor database.
     *
     * @param roleName the name of the role.
     * @return the row ID of the added role.
     */
    public long addRole(long roleId,String roleName) {

        long roleRowId;

        SQLiteDatabase db = this.getReadableDatabase();

        // First, check if the role with this roleId exists in the db

        Cursor c = db.rawQuery(
                "SELECT * FROM " + AutodoorContract.RoleEntry.TABLE_NAME + " WHERE "
                        + AutodoorContract.RoleEntry._ID + " = " + roleId ,  null);
        // Move to first row
        c.moveToFirst();
        if (c.getCount() > 0) {
            c.close();
            db.close(); // Closing database connection
           return roleId;
        }

        else {

            // First create a ContentValues object to hold the data you want to insert.
            ContentValues roleValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            roleValues.put(AutodoorContract.RoleEntry._ID, roleId);
            roleValues.put(AutodoorContract.RoleEntry.COLUMN_ROLE_NAME, roleName);
            // Finally, insert role data into the database.
            roleRowId = db.insert(
                    AutodoorContract.RoleEntry.TABLE_NAME,
                    null,
                    roleValues
            );

            db.close(); // Closing database connection

            Log.d(LOG_TAG, "New role inserted into sqlite: " + roleRowId);

            return roleRowId;
        }
        }

    /**
     * Helper method to handle insertion of a new user in the Autodoor database.
     *
     * @param firstName the first name of the user .
     * @param lastName the lastname of the user.
     * @param userName the username
     * @param password the password
     * @param email the email
     * @param telephone the telephone
     * @param roleId the role id
     * @return the row ID of the added user.
     */
    public long addUser(long userId, String firstName,String lastName,String userName,String password, String email,String telephone,int userStatus, Long roleId) {

        long userRowId;

        SQLiteDatabase db = this.getReadableDatabase();

        // First, check if the user with this userId exists in the db
        Cursor c = db.rawQuery(
                "SELECT * FROM " + AutodoorContract.UserEntry.TABLE_NAME + " WHERE "
                        + AutodoorContract.UserEntry._ID + " = " + userId ,  null);
        // Move to first row
        c.moveToFirst();
        if (c.getCount() > 0) {
            c.close();
            db.close(); // Closing database connection
            return userId;
        }

        else {
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues userValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            userValues.put(AutodoorContract.UserEntry._ID, userId);
            userValues.put(AutodoorContract.UserEntry.COLUMN_FIRST_NAME, firstName);
            userValues.put(AutodoorContract.UserEntry.COLUMN_LAST_NAME, lastName);
            userValues.put(AutodoorContract.UserEntry.COLUMN_USERNAME, userName);
            userValues.put(AutodoorContract.UserEntry.COLUMN_PASSWORD, password);
            userValues.put(AutodoorContract.UserEntry.COLUMN_EMAIL, email);
            userValues.put(AutodoorContract.UserEntry.COLUMN_TEL, telephone);
            userValues.put(AutodoorContract.UserEntry.COLUMN_USER_STATUS, userStatus);
            userValues.put(AutodoorContract.UserEntry.COLUMN_ROLE_ID, roleId);

            // Finally, insert user data into the database.
            userRowId = db.insert(
                    AutodoorContract.UserEntry.TABLE_NAME,
                    null,
                    userValues
            );

            db.close(); // Closing database connection

            Log.d(LOG_TAG, "New user inserted into sqlite: " + userRowId);

            return userRowId;
        }
    }

    /**
     * Helper method to handle insertion of a new user office in the Autodoor database.
     *
     * @param userOfficeId
     * @param userId
     * @param officeId
     * @return the row ID of the added user office.
     */
    public long addUserOffice(long userOfficeId,long userId, long officeId) {

        long userOfficeRowId;

        SQLiteDatabase db = this.getReadableDatabase();

        // First, check if the user office with this userOfficeId exists in the db

        Cursor c = db.rawQuery(
                "SELECT * FROM " + AutodoorContract.UserOfficeEntry.TABLE_NAME + " WHERE "
                        + AutodoorContract.UserOfficeEntry._ID + "  =  " + userOfficeId ,  null);
        // Move to first row
        c.moveToFirst();
        if (c.getCount() > 0) {
            c.close();
            db.close(); // Closing database connection
            return userOfficeId;
        }

        else {

            // First create a ContentValues object to hold the data you want to insert.
            ContentValues userOfficeValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            userOfficeValues.put(AutodoorContract.UserOfficeEntry._ID, userOfficeId);
            userOfficeValues.put(AutodoorContract.UserOfficeEntry.COLUMN_USER_ID, userId);
            userOfficeValues.put(AutodoorContract.UserOfficeEntry.COLUMN_OFFICE_ID, officeId);
            // Finally, insert user office data into the database.
            userOfficeRowId = db.insert(
                    AutodoorContract.UserOfficeEntry.TABLE_NAME,
                    null,
                    userOfficeValues
            );

            db.close(); // Closing database connection

            Log.d(LOG_TAG, "New user office inserted into sqlite: " + userOfficeRowId);

            return userOfficeRowId;
        }
    }

    /**
     * Getting user's office data from database
     * */
    public ContentValues getUserDetails(String userName, String password) {
        ContentValues user = new ContentValues();
        String selectQuery = "SELECT  * FROM " + AutodoorContract.UserEntry.TABLE_NAME + " WHERE " +
                AutodoorContract.UserEntry.COLUMN_USERNAME + " = '" + userName + "' AND " +
                AutodoorContract.UserEntry.COLUMN_PASSWORD + " = '" + password+ "' "+" AND "+AutodoorContract.UserEntry.COLUMN_USER_STATUS+" = "+1;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put(AutodoorContract.UserEntry._ID, cursor.getLong(1));
            user.put(AutodoorContract.UserEntry.COLUMN_ROLE_ID, cursor.getLong(2));
            user.put(AutodoorContract.UserEntry.COLUMN_FIRST_NAME, cursor.getString(3));
            user.put(AutodoorContract.UserEntry.COLUMN_LAST_NAME, cursor.getString(4));
            user.put(AutodoorContract.UserEntry.COLUMN_USERNAME, cursor.getString(5));
            user.put(AutodoorContract.UserEntry.COLUMN_PASSWORD, cursor.getString(6));
            user.put(AutodoorContract.UserEntry.COLUMN_EMAIL, cursor.getString(7));
            user.put(AutodoorContract.UserEntry.COLUMN_TEL, cursor.getString(8));
            user.put(AutodoorContract.UserEntry.COLUMN_USER_STATUS, cursor.getString(9));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(LOG_TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    public void updateUserPassword(long userId, String newPassword) {

        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(AutodoorContract.UserEntry.COLUMN_PASSWORD, newPassword);
        db.update(AutodoorContract.UserEntry.TABLE_NAME, cv, AutodoorContract.UserEntry._ID + "= ?", new String[] {Long.toString(userId)});


    }

    /**
     * Getting user's details by idfrom database
     * */
    public ContentValues getUserDetailsById(long userId) {
        ContentValues user = new ContentValues();
        String selectQuery = "SELECT  * FROM " + AutodoorContract.UserEntry.TABLE_NAME + " WHERE " +
                AutodoorContract.UserEntry._ID +" = "+ userId;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put(AutodoorContract.UserEntry._ID, cursor.getLong(0));
            user.put(AutodoorContract.UserEntry.COLUMN_ROLE_ID, cursor.getLong(1));
            user.put(AutodoorContract.UserEntry.COLUMN_FIRST_NAME, cursor.getString(2));
            user.put(AutodoorContract.UserEntry.COLUMN_LAST_NAME, cursor.getString(3));
            user.put(AutodoorContract.UserEntry.COLUMN_USERNAME, cursor.getString(4));
            user.put(AutodoorContract.UserEntry.COLUMN_PASSWORD, cursor.getString(5));
            user.put(AutodoorContract.UserEntry.COLUMN_EMAIL, cursor.getString(6));
            user.put(AutodoorContract.UserEntry.COLUMN_TEL, cursor.getString(7));
            user.put(AutodoorContract.UserEntry.COLUMN_USER_STATUS, cursor.getInt(8));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(LOG_TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }


    /**
     * Getting user's details by id from database
     * */
    public Cursor getUserOffices(long userId) {
        String selectQuery = "SELECT * "+
                " FROM " + AutodoorContract.OfficeEntry.TABLE_NAME + " INNER JOIN " +AutodoorContract.UserOfficeEntry.TABLE_NAME +
                " ON " +AutodoorContract.OfficeEntry.TABLE_NAME+"."+AutodoorContract.OfficeEntry._ID+ " = "+ AutodoorContract.UserOfficeEntry.TABLE_NAME+"."+AutodoorContract.UserOfficeEntry.COLUMN_OFFICE_ID+
                " INNER JOIN " +AutodoorContract.UserEntry.TABLE_NAME +
                " ON " + AutodoorContract.UserOfficeEntry.TABLE_NAME+"."+AutodoorContract.UserOfficeEntry.COLUMN_USER_ID+ " = "+ AutodoorContract.UserEntry.TABLE_NAME+"."+AutodoorContract.UserEntry._ID+
                " WHERE "+AutodoorContract.UserEntry.TABLE_NAME+"."+AutodoorContract.UserEntry._ID +" = "+ userId;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }
    /**
     * Getting all roles
     * returns list of roles
     * */
    public List<String> getAllRoles(){
        List<String> roles = new ArrayList<String>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + AutodoorContract.RoleEntry.TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                roles.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();
        db.close();

        // returning roles
        return roles;
    }


    /**
     * Getting all roles
     * returns list of roles
     * */
    public List<String> getAllOffices(){
        List<String> offices = new ArrayList<String>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + AutodoorContract.OfficeEntry.TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                offices.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();
        db.close();

        // returning roles
        return offices;
    }
}
