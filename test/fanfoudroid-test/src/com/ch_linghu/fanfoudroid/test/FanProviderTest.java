package com.ch_linghu.fanfoudroid.test;

import android.content.ContentUris;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import com.ch_linghu.fanfoudroid.provider.FanProvider;
import com.ch_linghu.fanfoudroid.provider.StatusF;

public class FanProviderTest extends ProviderTestCase2<FanProvider> {

    // Contains a reference to the mocked content resolver for the provider
    // under test.
    private MockContentResolver mMockResolver;

    // Contains an SQLite database, used as test data
    private SQLiteDatabase mDb;

    public FanProviderTest() {
        super(FanProvider.class, FanProvider.AUTHORITY);
        // TODO Auto-generated constructor stub
    }

    /*
     * Sets up the test environment before each test method. Creates a mock
     * content resolver, gets the provider under test, and creates a new
     * database for the provider.
     */
    @Override
    protected void setUp() throws Exception {
        // Calls the base class implementation of this method.
        super.setUp();

        // Gets the resolver for this test.
        mMockResolver = getMockContentResolver();

        /*
         * Gets a handle to the database underlying the provider. Gets the
         * provider instance created in super.setUp(), gets the
         * DatabaseOpenHelper for the provider, and gets a database object from
         * the helper.
         */
        mDb = getProvider().getDatabase(getContext());
    }

    /*
     * This method is called after each test method, to clean up the current
     * fixture. Since this sample test case runs in an isolated context, no
     * cleanup is necessary.
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    // dateProvider
    private StatusInfo[] getStatusesForTest() {
        return new StatusInfo[] {
                new StatusInfo(),
                new StatusInfo(),
                new StatusInfo(),
                new StatusInfo(),
        };
    }
    
    private static class StatusInfo {
        
    }
    
    /*
     * Tests the provider's publicly available URIs. If the URI is not one that the provider
     * understands, the provider should throw an exception. It also tests the provider's getType()
     * method for each URI, which should return the MIME type associated with the URI.
     */
    public void testUriAndGetType() {
        // Tests the MIME type for the notes table URI.
        String mimeType = mMockResolver.getType(StatusF.CONTENT_URI);
        assertEquals(FanProvider.CONTENT_TYPE_DIR + StatusF.CONTENT_TYPE, mimeType);

        // Creates a URI with a pattern for note ids. The id doesn't have to exist.
        //Uri noteIdUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_ID_URI_BASE, 1);
    }

}
