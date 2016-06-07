package com.techx.autodoor.data;

import android.net.Uri;
import android.test.AndroidTestCase;

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
public class TestAutodoorContract extends AndroidTestCase {


    // intentionally includes a slash to make sure Uri is getting quoted correctly
    private static final long TEST_OFFICE_ID = 1L;


    /*
        Uncomment this out to test the user office function.
     */
    public void testBuildUserOffice() {
        Uri officeUri = AutodoorContract.OfficeEntry.buildOfficeUri(TEST_OFFICE_ID);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildUserOffice in " +
                        "AutodoorContract.",
                officeUri);
assertEquals("Error: User office Uri doesn't match our expected result",
                officeUri.toString(),
                "content://com.techx.autodoor/office/1");
    }
}