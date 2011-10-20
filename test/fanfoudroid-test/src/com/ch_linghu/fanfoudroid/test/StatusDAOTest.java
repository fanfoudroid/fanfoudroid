package com.ch_linghu.fanfoudroid.test;

import com.ch_linghu.fanfoudroid.dao.StatusDAO;
import com.ch_linghu.fanfoudroid.data2.Status;
import com.ch_linghu.fanfoudroid.db2.FanDatabase;

import android.test.AndroidTestCase;

public class StatusDAOTest extends AndroidTestCase {

	StatusDAO statusDao;
	FanDatabase db;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		statusDao = new StatusDAO(getContext());
		db = statusDao.__getDb();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testIsExistsInStatus() {
		fail("Not yet implemented");
	}

	public void testIsExistsInPropertyString() {
		fail("Not yet implemented");
	}

	public void testIsExistsInPropertyStringStringInt() {
		fail("Not yet implemented");
	}

	public void testInsertSingleStatusStatus() {
		fail("Not yet implemented");
	}

	public void testInsertSingleStatusStatusInt() {
		fail("Not yet implemented");
	}

	public void testDeleteSingleStatus() {
		fail("Not yet implemented");
	}

	public void testSetFavorited() {
		fail("Not yet implemented");
	}

	public void testFetchStatus() {
		fail("Not yet implemented");
	}

	public void testGetMaxStatusId() {
		fail("Not yet implemented");
	}

}
