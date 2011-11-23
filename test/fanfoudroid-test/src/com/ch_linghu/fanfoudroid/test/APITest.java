package com.ch_linghu.fanfoudroid.test;

import java.util.Date;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

import com.ch_linghu.fanfoudroid.UserTimelineActivity;
import com.ch_linghu.fanfoudroid.fanfou.Weibo;

public class APITest extends ActivityInstrumentationTestCase2<UserTimelineActivity> {

	private static final String TAG = "WeiboTest";

	private Context context;
	private UserTimelineActivity activity;
	private String msg;
	
	/**
	 * Weibo客户端(帐号密码)
	 */
	private Weibo fanfou;

	/**
	 * 好友帐号ID， 必须是好友关系(与已登录帐号互相关注)
	 */
	private static final String TO_USER_ID = "lds2012";
	private String AT_TO;

	public APITest() {
		super("tk.sandin.android.fanfandroid.test", UserTimelineActivity.class);
		fanfou = new Weibo("172339248@qq.com", "12345678");
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		msg = "test status " + new Date();
//		AT_TO = "@" + fanfou.showUser(TO_USER_ID).getName() + " ";
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	/*
	// 显示随便看看的消息
	public void testGetPublicTimeLine() throws Exception{
		List<Status> status = fanfou.getPublicTimeline();
		for (Status s : status) {
			assertNotNull(s.getText());
		}
	}
	
	// 显示用户和好友的消息
	public void testGetFrientsTimeline() throws Exception {
		// page 1, count 10
		List<Status> status_10 = fanfou.getFriendsTimeline(1, 10);
		assertEquals(10, status_10.size());
		
		// page 2, count 5
		List<Status> status_5 = fanfou.getFriendsTimeline(2, 5);
		assertEquals(5, status_5.size());
		
		// 取page 1, count 10的第6条 等同于 取page 2, count5的第1条
		assert(status_10.get(5).equals(status_5.get(0)));
	}
	
	private void updateStatuses() throws Exception {
		for (int i = 0; i < 150; i++) {
			fanfou.updateStatus(msg);
			System.out.println(i + " " + msg);
			Thread.currentThread().sleep(300);
		}
	}
	
	public void testGetFrientsTimelineBySinceId() throws Exception {
		// 此测试需要大量信息存在. 
		// update a lot of statuses for this test. 
//		updateStatuses();	
		
		// 读取回来的所有结果
		HashMap<String, Status> map = new HashMap<String, Status>();
		
		// 读取一个很古老的status id(60条/页 * 3页 = 第180条)作为since_id参数 
		String sinceId = fanfou.getFriendsTimeline(3, 60).get(59).getId();
		
		for (int i = 1; ; i++) {
			
			// 以 sinceId不变, 循环进行分页读取(每页20条数据), 直到读取结果为空(即表示没有比sinceId更旧的信息了).
			List<Status> status = fanfou.getFriendsTimeline( new Paging(i,20, sinceId) ); // page = i ; count = 20; since_id = sinceId
			if (0 == status.size()) break;
			
			for (Status s: status) {
				
				// 确保没有读取到重复项
				assertTrue(! map.containsKey(s.getId()) );
				// 将结果保存
				map.put(s.getId(), s);
				
				System.out.println(map.size() + " " + s.getId() + "--> " + s.getText());
			}
		}
		
		// print result
		Set<String> keys = map.keySet();
		for (String key : keys ) {
			Status s = map.get(key);
			//System.out.println(s.getId() + "--> " + s.getText());
		}
		System.out.println(map.size());
		// 需要加上since_id这条消息才等于总消息数
		Assert.assertEquals(180 , map.size() + 1);
		
	}
	
	// 显示用户的消息
	public void testGetUserTimeline() throws Exception {
		
		// update status list
		List<Status> update = new ArrayList<Status>();
		
		// update 3 test status
		for (int i = 3; i > 0; i--) {
			update.add(fanfou.updateStatus(msg));
		}
		assertEquals(3, update.size());
		
		// get user time line by user(myself) id
		String userid = fanfou.getUserId();
		List<Status> status_20 = fanfou.getUserTimeline(userid);
		
		assertFalse(status_20.isEmpty());
		assertTrue(status_20.size() > 3);
		assertEquals(msg, status_20.get(0).getText());
		
		// test getUserTimeline(page, count)
		List<Status> status_2 = fanfou.getUserTimeline(1, 3);
		assertEquals(3, status_2.size());
		
		// clear up
		for (Status s : update) {
			fanfou.destroyStatus(s.getId());
		}
	}
	
	// 显示指定消息
	public void testShowStatus() throws Exception {
		// get one status by status id
		Status status = fanfou.updateStatus(msg);
		Status status_show = fanfou.showStatus(status.getId());
		
		assertTrue(status.equals(status_show));
		
		// clean up
		fanfou.destroyStatus(status.getId());
	}
	
	
	// 显示发给当前用户的消息
	public void testGetMentions() throws Exception {
		
		User myself = fanfou.showUser(fanfou.getUserId());
		
		// repay to user(myself)
		String message = "@" + myself.getScreenName()  + " "+ msg;
		
		for (int i = 0; i<10; i++) {
			Status new_mention = fanfou.updateStatus(message);
		}
		
		// 延时以等待服务器更新数据 
		Thread.currentThread().sleep(5000);
		
		
		// 确保当前帐号内至少有count条mentions用于此测试
		int count = 10;
		
		// get mention status of user
		List<Status> status = fanfou.getMentions(new Paging(1, count));
		for (Status s : status) {
			System.out.println(s.getText());
			Log.i("LDS", s.getText());
		}
		String since_id = status.get(status.size()-1).getId();
		
		assertEquals(count, status.size());
		
		// get mentions list by since_id
		List<Status> new_mentions = fanfou.getMentions(since_id);
		assertEquals(count-1, new_mentions.size());
		
		// cleanup
		
	}
	
	// 发布消息
	public void testUpdateStatus() throws Exception {

		Status status = fanfou.updateStatus(msg);
		String text = status.getText();
		String id = status.getId();
				
		assertTrue(msg.equals(text));
		
		User user = fanfou.showUser(TO_USER_ID);
//		System.out.println(user.getStatusText());
		
		// reply to someone
		Status repay = fanfou.updateStatus("@" + user.getName() + " " + msg, user.getStatusId());
		assertEquals(
			user.getStatusId(),
			repay.getInReplyToStatusId()
		);
		assertEquals(user.getId(), repay.getInReplyToUserId());
		
		// clean up
		fanfou.destroyStatus(id);
		fanfou.destroyStatus(repay.getId());
	}
	
	// 转发消息
	public void testRepost() throws Exception {
		User user = fanfou.showUser(fanfou.getUserId());
		String repost_status_id = user.getStatusId();
		String repost_status_text = user.getStatusText();
		String message = msg + "repost to " + TO_USER_ID;
		
		Status repost = fanfou.repost(repost_status_id, message);
		String text = repost.getText();
		
//		System.out.println(text);
//		System.out.println(repost);
//		System.out.println(repost_status_text);
//		System.out.println("123".contains("123"));
		
		// new status contains the origin status text
		assertTrue(text.contains(repost_status_text));
		assertTrue(text.contains("@" + user.getName()));
		
		// clean up
		fanfou.destroyStatus(repost.getId());
	}
	
	// 删除消息
	public void testDestroyStatus() throws Exception {
		
		// update some status in case have no status
		fanfou.updateStatus(msg);
		fanfou.updateStatus(msg);
		
		User myself = fanfou.showUser(fanfou.getUserId());
		List<Status> status = fanfou.getUserTimeline();
		int all_status_count = myself.getStatusesCount();
		int page = (int) Math.ceil( all_status_count / 20.0 );
		
		int delete_count = 0;
		
		// delete status as much as API can
//		for (int i = 1; i <= page; i++) {
		for (int i = 1; i <= 1; i++) {
			List<Status> status_per = fanfou.getUserTimeline(i, 20);
			for (Status s : status_per) {
//				System.out.println(s.getText());
				fanfou.destroyStatus(s.getId());
				delete_count++;
			}
		}
		
		List<Status> status_left = fanfou.getUserTimeline();
//		System.out.println(status.size() - status_left.size() == delete_count );
//		System.out.println(status.size());
//		System.out.println(status_left.size());
//		System.out.println(delete_count);
		assertTrue(delete_count > 0);
		
		// update one status in case 
		fanfou.updateStatus("clean up on " + new Date());
	}
	
	
	// 照片上传
	public void testUploadPhoto() throws Exception {
		
//		Resources resources = getInstrumentation().getContext().getResources();
//		AssetManager am = resources.getAssets();
//		InputStream fin = am.open("fanfou.jpg");
		
		// NOTE: 确保SD根目录下有"fanfou.jpg"的文件,用于此测试
		File SDCard = Environment.getExternalStorageDirectory(); 	
		String filename = SDCard.getParent() + SDCard.getName() + "/fanfou.jpg";
		File image = new File(filename);
		assertTrue(image.exists());
		
		// 上传照片
		Status status = fanfou.uploadPhoto(msg, image);
		assertNotNull(status);
		Log.i(TAG, status.toString());
		
		String original_pic = status.getOriginal_pic();
		String bmiddle_pic  = status.getBmiddle_pic();
		String thumbail_pic = status.getThumbnail_pic();
		
		assertTrue( original_pic.length() > 0 );
		assertTrue( bmiddle_pic.length()  > 0 );
		assertTrue( thumbail_pic.length() > 0 );
		
		// clean up
		fanfou.destroyStatus(status.getId());
	}
	
	
	// 公开搜索
	public void testSearch() throws Exception {
		
		QueryResult result = fanfou.search(new Query("哈"));
		
		// search result is not null
		List<Status> status = result.getStatus();
		assertTrue(!status.isEmpty());
	
		assertTrue(status.size() > 0);
		for (Status s : status) {
			assertTrue( s.getId().length() > 0);
//			System.out.println(s.getText());
		}
	}
	
	// 热词
	public void testGetTrends() throws Exception {
		Trends trends = fanfou.getTrends();
		assertTrue(trends.getAsOf() instanceof Date);
		
		Trend[] trend_array = trends.getTrends();
		assertTrue( trend_array.length > 0 );
		
		for (Trend t : trend_array) {
			String name = t.getName();
			String url = t.getUrl();
			String query = t.getQuery();
			
			assertTrue( url.length() > 0 );
			assertTrue( query.length() > 0 );
			assertTrue( name.length() > 0 );
		}
	}
	
	// 显示好友列表
	public void testGetFirendStatus() throws Exception {
		List<User> users = fanfou.getFriendsStatuses();
		
		// The user has more then one friend
		assertTrue( users.size() > 1 );
		assertTrue( users.get(0).getId().length() > 0 );
		
		// all users' name
//		for (User u : users) {
//			System.out.println(u.getName());
//		}
		
		// Get the other user's friends 
		User san = fanfou.showUser(TO_USER_ID);
		
		// Test by user id
		
		List<User> users_by_id = fanfou.getFriendsStatuses(san.getId());
		
		assertTrue(users_by_id.size() > 1);
		for (User u : users_by_id) {
//			System.out.println(u.getName());
			assertTrue( u.getName().length() > 0 );
		}
		
		// Test by user id and paging
		
		User fan = fanfou.showUser("wangxing");
		assertTrue( fan.getName().length() > 0 );
		
	    int friend_count = fan.getFriendsCount();
		
		List<User> users_by_page_1 = fanfou.getFriendsStatuses(fan.getId(), 
				new Paging(1));
		
		// All friends' name
//		for (User u : users_by_page_1 ) {
//			System.out.println(u.getName());
//		}
		
		// The user(王兴) has 50 friends at least
//		System.out.println(users_by_page_1.size());
		assertTrue( users_by_page_1.size() > 50 );
	}
	
	// 显示关注者列表
	public void testGetFollowersStatuses() throws Exception {
		
		// default user 
		List<User> followers = fanfou.getFollowersStatuses();
		
		// I only have few followers(more then one)
//		assertTrue(followers.size() > 1);
		
		// User(fanfou)
		User fan = fanfou.showUser("fanfou");
		
		List<User> followers_of_fanfou = fanfou.getFollowersStatuses(fan.getId());
		List<User> followers_of_fanfou_page_2 = fanfou.getFollowersStatuses(fan.getId(), new Paging(2));
		
		// He has a lot of followers
		assertTrue(followers_of_fanfou.size() > 50);
		assertTrue( followers_of_fanfou.get(5).getName().length() > 0 );
		
		// He definitely has two page of followers
		assertTrue(followers_of_fanfou_page_2.size() > 50);
	}
	
	// 显示用户详细信息
	public void testShowUser() throws Exception {
		
		// update a status for test user's last status
		Status status = fanfou.updateStatus(msg);
		
		// need to test(not all)
		Map<String, Object> expect = new HashMap<String, Object>();
		expect.put("id", "aFanfou");
		expect.put("name", "aFanfou");
		expect.put("screen_name", "aFanfou");
		expect.put("location", "湖北 武汉");
		expect.put("gender", "男");
		expect.put("birthday", "2011-01-01");
		expect.put("description", "fanfou android app");
		expect.put("url", new URL("http://www.sandin.tk"));
		expect.put("created_at", new Date("Fri Dec 10 06:41:43 +0000 2010"));
		expect.put("utc_offset", 28800);
		expect.put("following", false);
		expect.put("status_text", msg);
		expect.put("is_protected", false);
		expect.put("notifications", false);
		
		// not for sure
		expect.put("profile_image_url", new URL("http://avatar1.fanfou.com/s0/00/00/00.jpg")); //default photo
		expect.put("source", "API");
//		expect.put("friends_count", 3);
//		expect.put("followers_count", 0);
//		expect.put("favourites_count", 0);
//		expect.put("statuses_count", 16);
		
		// get user info
		User myself = fanfou.showUser(fanfou.getUserId());
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("id", myself.getId());
		map.put("screen_name", myself.getScreenName());
		map.put("location",  myself.getLocation());
		map.put("gender", myself.getGender());
		map.put("birthday", myself.getBirthday());
		map.put("description",  myself.getDescription());
		map.put("profile_image_url", myself.getProfileImageURL());
		map.put("url", myself.getURL());
		map.put("followers_count", myself.getFollowersCount());
		map.put("favourites_count",  myself.getFavouritesCount());
		map.put("statuses_count", myself.getStatusesCount());
		map.put("is_protected",  myself.isProtected());
		map.put("created_at", myself.getCreatedAt());
		map.put("following", myself.isFollowing());
		map.put("notifications", myself.isNotificationEnabled());
		map.put("utc_offset", myself.getUtcOffset());
		
		//The last status
		map.put("status_created_at", myself.getStatusCreatedAt());
		map.put("status_id", myself.getStatusId());
		map.put("status_text", myself.getStatusText());
		map.put("status_source", myself.getStatusSource());
		map.put("status_trencated", myself.isStatusTruncated());
		map.put("status_in_reply_to_status_id", myself.getStatusInReplyToStatusId());
		map.put("status_in_reply_to_user_id", myself.getStatusInReplyToUserId());
		map.put("status_in_reply_to_screen_name", myself.getStatusInReplyToScreenName());
		map.put("status_favorited", myself.isStatusFavorited());
		
		// TEST ALL
		Iterator it = map.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Object value = map.get(key);
			Object expect_value = expect.get(key);
			if (value != null && expect_value != null ) {
				assertTrue(expect.get(key).equals(value));
//				System.out.println(expect.get(key).equals(value));
//				System.out.println(expect_value);
//				System.out.println(value);
//				System.out.println("--------------------------------");
			} else if ( value == null ) {
//				System.out.println(key + " is NULL...............");
//				System.out.println("--------------------------------");
			} else if (expect_value == null) {
//				System.out.println(key + " is not been tested.");
//				System.out.println("--------------------------------");
			} else {
//				System.out.println(key + " [skip].");
//				System.out.println("--------------------------------");
				fail(key + " is skip.");
			}
		}
		
		// clean up
		fanfou.destroyStatus(status.getId());
	}
	
	// 发送私信
	public void testSendDirectMessage() throws Exception {

		User sender = fanfou.showUser(fanfou.getUserId());
		
		DirectMessage message = fanfou.sendDirectMessage(TO_USER_ID, msg);
		String msg_id = message.getId();
		String msg_text = message.getText();
		
		assertTrue( msg_id.length() > 0);
		assertTrue( msg_text.length() > 0);

		assertTrue( message.getSender().getId().equals(sender.getId()) );
		assertTrue( message.getRecipient().getId().equals(TO_USER_ID) );
		
		// clean up
		fanfou.destroyDirectMessage(message.getId());
	}
	
	// 显示用户收到的私信
	public void testGetDirectMessage() throws Exception {
		
		List<DirectMessage> messages =  fanfou.getDirectMessages();
		
//		for (DirectMessage m : messages) {
//			System.out.println(messages);
//		}
		
		// more then one message
		assertTrue( messages.size() > 0);
		assertTrue( messages.get(0).getText().length() > 0 );
	}
		
	// 显示用户发送的私信
	public void testGetSendDirectMessage() throws Exception {
		
		User to = fanfou.showUser(TO_USER_ID);
		
		// send a message for test
		DirectMessage message = fanfou.sendDirectMessage(TO_USER_ID, msg);
		
		// get all sent message
		List<DirectMessage> messages =  fanfou.getSentDirectMessages();
		
		// more then one message 
		assertTrue( messages.size() > 1);
		
		// the last message's target is TO_USER_ID
		User getTo = messages.get(0).getRecipient();
		assertTrue( getTo.equals(to) );
		assertTrue( messages.get(0).equals(message));
		
		// clean up
		fanfou.destroyDirectMessage(message.getId());
	}
	
	// 删除私信
	public void testDestoryDirectMessages() throws Exception {
//		for (int i =0 ; i < 10; i++) {
//			fanfou.sendDirectMessage(TO_USER_ID, msg);
//			Thread.sleep(1000);
//		} 
		
		DirectMessage last_message = fanfou.getSentDirectMessages().get(0); 
		
		DirectMessage send = fanfou.sendDirectMessage(TO_USER_ID, msg);
		assertTrue( msg.equals(fanfou.getSentDirectMessages().get(0).getText()) ); 
		
		DirectMessage message = fanfou.destroyDirectMessage(send.getId());
		assertTrue( last_message.equals(fanfou.getSentDirectMessages().get(0)) ); 
	}
	
	// 显示用户的收藏列表
	public void testGetFavorites() throws Exception {
		
		// my favorites status list
		List<Status> favorites = fanfou.getFavorites();
		assertTrue(favorites.size() > 0);
		assertTrue( favorites.get(0).getId().length() > 0);
		
		// get favorites status list by user id
		List<Status> f_by_id = fanfou.getFavorites(fanfou.getUserId());
		assertEquals(f_by_id.size(), favorites.size());
		
		// get by paging
		List<Status> favorites_1 = fanfou.getFavorites(new Paging(1,1));
		assertEquals(1, favorites_1.size());
	}
	
	// 收藏某条消息/删除收藏
	public void testCreateFavorite() throws Exception {
		
		// save the last favorite for test destroy favorite
		Status the_last_favorite = fanfou.getFavorites().get(0);
		
		// create a favorite
		String to_favorites = fanfou.showUser(TO_USER_ID).getStatusId();
		Status status = fanfou.createFavorite(to_favorites);
		
		// test
		List<Status> favorites = fanfou.getFavorites();
		assertEquals(status.getId(), favorites.get(0).getId());
		
		// destroy a favorite and test
		fanfou.destroyFavorite(to_favorites);
		assertEquals(the_last_favorite, fanfou.getFavorites().get(0));
	}
	
	// 好友关系方法
	public void testFriendship() throws Exception {
		
		// 判断好友关系是否存在
		User fan = fanfou.showUser("fanfou");
		User myself = fanfou.showUser(fanfou.getUserId());
		
		if ( fanfou.existsFriendship(myself.getId(), fan.getId()) ) {
			// "fanfou" is a friend of mine
			
			// 删除好友
			fanfou.destroyFriendship(fan.getId());
			assertTrue(! fanfou.existsFriendship(myself.getId(), fan.getId()));
		} else {
			// "fanfou" is not a friend of mine
			
			// 添加好友
			fanfou.createFriendship(fan.getId());
			assertTrue( fanfou.existsFriendship(myself.getId(), fan.getId()));
		}

	}
	
	// 好友和关注者方法
	public void testGetFriendsIDs() throws Exception {
		
		User myself = fanfou.showUser(fanfou.getUserId());
		User my_friend = fanfou.showUser(TO_USER_ID);
		
		// my_friend must be a friend of mine. 
		assertTrue(fanfou.existsFriendship(myself.getId(), my_friend.getId()) );
		
		String[] id_arr = fanfou.getFriendsIDs().getIDs();
		List id_list = Arrays.asList(id_arr);
		for (String i: id_arr) {
			Log.i("LDS", i);
		}
		
		assertTrue(id_list.size() > 0);
		assertTrue(id_list.contains(my_friend.getId()));
	}
	
	public void testGetFollowersIDs() throws Exception {
		IDs ids = fanfou.getFollowersIDs();
		assertNotNull(ids);
	}
	
	// 黑名单
	public void testBlock() throws Exception {
		
		// 两用户为好友
		User user = fanfou.showUser("fanfou");
		User myself = fanfou.showUser(fanfou.getUserId());
		if (! fanfou.existsFriendship(myself.getId(), user.getId()) ) {
			fanfou.createFriendship(user.getId());
		}
		
		// 加入黑名单, 好友关系自动取消
		fanfou.createBlock(user.getId());
		assertTrue(! fanfou.existsFriendship(myself.getId(), user.getId()) );
		
		// 移除黑名单
		fanfou.destroyBlock(user.getId());
		// 手动恢复关注
		fanfou.createFriendship(user.getId());
		
		assertTrue( fanfou.existsFriendship(myself.getId(), user.getId()) );
		
	}
	
	// 验证用户
	// 错误用户名/密码情况的测试单独在UserTest中
	public void testVerifyCredentials() {
		
		try {
			User user = fanfou.verifyCredentials();
			assertTrue( user.getId().length() > 0 );
		} catch (WeiboException e) {
			// 成功返回code 200
			assertEquals(200, e.getStatusCode());
			fail("用户名/密码错误，登录失败。lds");
		}
		
	}
	
	// 测试API是否正常
	public void testTest() throws Exception {
		assertTrue(fanfou.test());
	}
	
	// 保存搜索相关的方法
	public void testSavedSearch() throws Exception {
		
		// query word
		String word = String.valueOf(msg.hashCode());
		
		// create/show saved search
		SavedSearch save = fanfou.createSavedSearch(word);
		SavedSearch save_search = fanfou.showSavedSearch(save.getId());
		assertEquals(save, save_search);
		
		// get saved search
		List<SavedSearch> search = fanfou.getSavedSearches();
		assertTrue(search.size() > 0);
//		Log.i("LDS", word);
//		Log.i("LDS", search.get(search.size()-1).getQuery());
		assertEquals(word, search.get(search.size()-1).getQuery());
		
		// destroy saved search
		for (SavedSearch s : search) {
//			System.out.println(s.getName() + s.getId());
			fanfou.destroySavedSearch(s.getId());
		}
	}
	
	*/

}
