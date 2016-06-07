package com.techx.autodoor.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by change on 5/5/2016.
 */
public class AutodoorProvider extends ContentProvider {
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private AutodoorDbHelper mOpenHelper;

    static final int USERS = 100;
    static final int USER = 101;
    static final int USER_BY_USER_NAME_AND_PASSWORD = 102;
    static final int ROLES = 200;
    static final int ROLE_FOR_USER = 201;
    static final int OFFICES = 300;
    static final int OFFICE_FOR_USER = 301;



    private static final SQLiteQueryBuilder sOfficeByUserIdQueryBuilder;
    private static final SQLiteQueryBuilder sRoleByUserIdQueryBuilder;

    static{
        sOfficeByUserIdQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //User INNER JOIN Office ON User.office_id = office._id
//        sOfficeByUserIdQueryBuilder.setTables(
//                AutodoorContract.UserEntry.TABLE_NAME + " INNER JOIN " +
//                        AutodoorContract.OfficeEntry.TABLE_NAME +
//                        " ON " + AutodoorContract.UserEntry.TABLE_NAME +
//                        "." + AutodoorContract.UserEntry.COLUMN_OFFICE_ID +
//                        " = " + AutodoorContract.OfficeEntry.TABLE_NAME +
//                        "." + AutodoorContract.OfficeEntry._ID);

        sRoleByUserIdQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //User INNER JOIN Role ON User.role_id = role_id
        sRoleByUserIdQueryBuilder.setTables(
                AutodoorContract.UserEntry.TABLE_NAME + " INNER JOIN " +
                        AutodoorContract.RoleEntry.TABLE_NAME +
                        " ON " + AutodoorContract.UserEntry.TABLE_NAME +
                        "." + AutodoorContract.UserEntry.COLUMN_ROLE_ID +
                        " = " + AutodoorContract.RoleEntry.TABLE_NAME +
                        "." + AutodoorContract.RoleEntry._ID);
    }

    private static final String sUserIdSelection =
            AutodoorContract.UserEntry.TABLE_NAME+
                    "." + AutodoorContract.UserEntry._ID + " = ? ";

    private static final String sUserNameAndPasswordSelection =
            AutodoorContract.UserEntry.TABLE_NAME+
                    "." + AutodoorContract.UserEntry.COLUMN_USERNAME + " = ? AND " +
            AutodoorContract.UserEntry.COLUMN_PASSWORD + " = ? " ;

    private static final String sOfficeIdSelection =
            AutodoorContract.OfficeEntry.TABLE_NAME+
                    "." + AutodoorContract.OfficeEntry._ID + " = ? ";

    private static final String sRoleIdSelection =
            AutodoorContract.RoleEntry.TABLE_NAME+
                    "." + AutodoorContract.RoleEntry._ID + " = ? ";


    private Cursor getUserById(Uri uri, String[] projection, String sortOrder) {
        String userId = AutodoorContract.UserEntry.getUserIdFromUri(uri);

        String[] selectionArgs;
        String selection;
        selection = sUserIdSelection;
        selectionArgs = new String[]{userId};

        return  mOpenHelper.getReadableDatabase().query(AutodoorContract.OfficeEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }


    private Cursor getUserByUsernameAndPassword(Uri uri, String[] projection, String sortOrder) {
        String userName = AutodoorContract.UserEntry.getUserNameFromUri(uri);
        String password = AutodoorContract.UserEntry.getPasswordFromUri(uri);

        String[] selectionArgs;
        String selection;
        selection = sUserNameAndPasswordSelection;
        selectionArgs = new String[]{userName,password};

        return  mOpenHelper.getReadableDatabase().query(AutodoorContract.OfficeEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getOfficeById(Uri uri, String[] projection, String sortOrder) {
        String officeId = AutodoorContract.OfficeEntry.getOfficeIdFromUri(uri);

        String[] selectionArgs;
        String selection;
        selection = sOfficeIdSelection;
        selectionArgs = new String[]{officeId};

        return  mOpenHelper.getReadableDatabase().query(AutodoorContract.OfficeEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getRoleById(Uri uri, String[] projection, String sortOrder) {
        String roleId = AutodoorContract.RoleEntry.getRoleIdFromUri(uri);

        String[] selectionArgs;
        String selection;
        selection = sRoleIdSelection;
        selectionArgs = new String[]{roleId};

        return  mOpenHelper.getReadableDatabase().query(AutodoorContract.RoleEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    /*
        Here is where we need to create the UriMatcher. This UriMatcher will
        match each URI to the USER, USER_WITH_LOCATION, USER_WITH_ROLE, ROLE
        and OFFICE integer constants defined above.  we can test this by uncommenting the
        testUriMatcher test within TestUriMatcher.
     */
    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = AutodoorContract.CONTENT_AUTHORITY;

        // 2) Use the addURI function to match each of the types.  Use the constants from
        // AutodoorContract to help define the types to the UriMatcher.
        matcher.addURI(authority, AutodoorContract.PATH_USER,USERS);
        matcher.addURI(authority, AutodoorContract.PATH_USER + "/#",USER);
        matcher.addURI(authority, AutodoorContract.PATH_USER + "/*/*",USER_BY_USER_NAME_AND_PASSWORD);
        matcher.addURI(authority, AutodoorContract.PATH_OFFICE,OFFICES);
        matcher.addURI(authority, AutodoorContract.PATH_OFFICE + "/#",OFFICE_FOR_USER);
        matcher.addURI(authority, AutodoorContract.PATH_ROLE,ROLES);
        matcher.addURI(authority, AutodoorContract.PATH_ROLE + "/#",ROLE_FOR_USER);

        // 3) Return the new matcher!

        return matcher;
    }

    /*
        We just create a new AutodoorDbHelper for later use
        here.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new AutodoorDbHelper(getContext());
        return true;
    }

    /*
        Here's where we'll code the getType function that uses the UriMatcher.  we can
        test this by uncommenting testGetType in TestProvider.
     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {

            case USERS:
                return AutodoorContract.UserEntry.CONTENT_TYPE;
            case USER:
                return AutodoorContract.UserEntry.CONTENT_ITEM_TYPE;
            case USER_BY_USER_NAME_AND_PASSWORD:
                return AutodoorContract.UserEntry.CONTENT_ITEM_TYPE;
            case OFFICES:
                return AutodoorContract.OfficeEntry.CONTENT_TYPE;
            case OFFICE_FOR_USER:
                return AutodoorContract.OfficeEntry.CONTENT_ITEM_TYPE;
            case ROLES:
                return AutodoorContract.RoleEntry.CONTENT_TYPE;
            case ROLE_FOR_USER:
                return AutodoorContract.RoleEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {

            // "User/*"
            case USER: {
                retCursor = getUserById(uri, projection, sortOrder);
                break;
            }
            // "User/*/*"
            case USER_BY_USER_NAME_AND_PASSWORD: {
                retCursor = getUserByUsernameAndPassword(uri, projection, sortOrder);
                break;
            }
            // "Office/*"
            case OFFICE_FOR_USER:
            {
                retCursor = getOfficeById(uri, projection, sortOrder);
                break;
            }
            // "Role/*"
            case ROLE_FOR_USER: {
                retCursor = getRoleById(uri, projection, sortOrder);
                break;
            }
            // "Users"
            case USERS: {
                retCursor= mOpenHelper.getReadableDatabase().query(
                        AutodoorContract.UserEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                break;
            }
            // "Offices"
            case OFFICES: {
                retCursor=mOpenHelper.getReadableDatabase().query(
                        AutodoorContract.OfficeEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            // "Roles"
            case ROLES: {
                retCursor=mOpenHelper.getReadableDatabase().query(
                        AutodoorContract.RoleEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case USERS: {
                long _id = db.insert(AutodoorContract.UserEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = AutodoorContract.UserEntry.buildUserUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            case OFFICES: {
                long _id = db.insert(AutodoorContract.OfficeEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = AutodoorContract.OfficeEntry.buildOfficeUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case ROLES: {
                long _id = db.insert(AutodoorContract.RoleEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = AutodoorContract.RoleEntry.buildRoleUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Start by getting a writable database
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        // Use the uriMatcher to match the USER, OFFICE and ROLE URI's we are going to
        // handle.  If it doesn't match these, throw an UnsupportedOperationException.
        final int match = sUriMatcher.match(uri);
        int numRows;
        if (selection == null) selection ="1";
        switch (match) {
            case USERS: {
                numRows = db.delete(AutodoorContract.UserEntry.TABLE_NAME,selection,selectionArgs);
                break;
            }
            case OFFICES: {
                numRows = db.delete(AutodoorContract.OfficeEntry.TABLE_NAME,selection,selectionArgs);
                break;
            }
            case ROLES: {
                numRows = db.delete(AutodoorContract.RoleEntry.TABLE_NAME,selection,selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // A null value deletes all rows.  In my implementation of this, I only notified
        // the uri listeners (using the content resolver) if the rowsDeleted != 0 or the selection
        // is null.
        // Oh, and you should notify the listeners here.
        if (numRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // return the actual rows deleted
        return numRows;
    }


    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // This is a lot like the delete function.  We return the number of rows impacted
        // by the update.
        // Start by getting a writable database
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        // Use the uriMatcher to match the USER, OFFICE and ROLE URI's we are going to
        // handle.  If it doesn't match these, throw an UnsupportedOperationException.
        final int match = sUriMatcher.match(uri);
        int numRows;
        if (selection == null) selection ="1";
        switch (match) {
            case USERS: {
                numRows = db.update(AutodoorContract.UserEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }

            case OFFICES: {
                numRows = db.update(AutodoorContract.OfficeEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case ROLES: {
                numRows = db.update(AutodoorContract.RoleEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // A null value Updates all rows.  In my implementation of this, I only notified
        // the uri listeners (using the content resolver) if the rowsDeleted != 0 or the selection
        // is null.
        // Oh, and you should notify the listeners here.
        if (numRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);

        }
        // Student: return the actual rows deleted
        return numRows;

    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case USER:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(AutodoorContract.UserEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }

}
