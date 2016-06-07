package com.techx.autodoor.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Defines table and column names for the weather database.
 */
public class AutodoorContract {
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.techx.autodoor";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.techx.autodoor/user/ is a valid path for
    // looking at user data. content://com.techx.autodoor/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_USER = "user";
    public static final String PATH_OFFICE = "office";
    public static final String PATH_ROLE = "role";
    public static final String PATH_USER_ROLE="user_role";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    /*
        Inner class that defines the table contents of the office table
        This is where we will add the strings.  (Similar to what has been
        done for UserEntry)
     */
    public static final class OfficeEntry implements BaseColumns {
        public static final String TABLE_NAME = "office";

        public static final String COLUMN_OFFICE_NAME="office_name";

        public static final String COLUMN_OFFICE_NUMBER="office_number";
        public static String COLUMN_OFFICE_STATUS="office_status";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_OFFICE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_OFFICE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_OFFICE;

        public static Uri buildOfficeUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getOfficeIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }

    /* Inner class that defines the table contents of the user table */
    public static final class UserEntry implements BaseColumns {

        public static final String TABLE_NAME = "user";

        // Column with the foreign key into the role table
        public static final String COLUMN_ROLE_ID = "role_id";

        // First and Last Names for the user
        public static final String COLUMN_FIRST_NAME = "first_name";
        public static final String COLUMN_LAST_NAME = "last_name";

        // Username for the user
        public static final String COLUMN_USERNAME = "username";

        // Password for the user
        public static final String COLUMN_PASSWORD= "password";

        // Email for the user
        public static final String COLUMN_EMAIL = "email";

        // Phone number for the user
        public static final String COLUMN_TEL = "telephone";
        public static final String COLUMN_USER_STATUS="user_status";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_USER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER;


        public static Uri buildUserUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildUserNameWithPassword(
                String userName, String password) {

            return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_USERNAME, userName)
                    .appendQueryParameter(COLUMN_PASSWORD, password)
                    .build();
        }
        public static String getUserIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
        public static String getUserNameFromUri(Uri uri) {
            String userName = uri.getQueryParameter(COLUMN_USERNAME);

            if (null != userName && userName.length() > 0)
                return userName;
            else
                return null;
        }

        public static String getPasswordFromUri(Uri uri) {
            String password = uri.getQueryParameter(COLUMN_PASSWORD);

            if (null != password && password.length() > 0)
                return password;
            else
                return null;
        }

    }


    /*
       Inner class that defines the table contents of the office table
       This is where we will add the strings.  (Similar to what has been
       done for UserEntry)
    */
    public static final class RoleEntry implements BaseColumns {
        public static final String TABLE_NAME = "role";

        public static final String COLUMN_ROLE_NAME="role_name";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ROLE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ROLE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ROLE;


        public static Uri buildRoleUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);

        }
        public static String getRoleIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }

    /*
       Inner class that defines the table contents of the office table
       This is where we will add the strings.  (Similar to what has been
       done for UserEntry)
    */
    public static final class UserOfficeEntry implements BaseColumns {
        public static final String TABLE_NAME = "user_office";

        public static final String COLUMN_USER_ID="user_id";
        public static final String COLUMN_OFFICE_ID="office_id";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_USER_ROLE).build();
    }

    }


