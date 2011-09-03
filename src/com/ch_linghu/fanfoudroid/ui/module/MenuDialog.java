/*
 * Copyright (C) 2009 Google Inc.
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
package com.ch_linghu.fanfoudroid.ui.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.ch_linghu.fanfoudroid.BrowseActivity;
import com.ch_linghu.fanfoudroid.DmActivity;
import com.ch_linghu.fanfoudroid.FavoritesActivity;
import com.ch_linghu.fanfoudroid.FollowersActivity;
import com.ch_linghu.fanfoudroid.FollowingActivity;
import com.ch_linghu.fanfoudroid.MentionActivity;
import com.ch_linghu.fanfoudroid.ProfileActivity;
import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.TwitterActivity;
import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.UserTimelineActivity;

/**
 * 顶部主菜单切换浮动层
 * 
 * @author lds
 * 
 */
public class MenuDialog extends Dialog {

	private static final int PAGE_MINE = 0;
	private static final int PAGE_PROFILE = 1;
	private static final int PAGE_FOLLOWERS = 2;
	private static final int PAGE_FOLLOWING = 3;
	private static final int PAGE_HOME = 4;
	private static final int PAGE_MENTIONS = 5;
	private static final int PAGE_BROWSE = 6;
	private static final int PAGE_FAVORITES = 7;
	private static final int PAGE_MESSAGE = 8;

	private List<int[]> pages = new ArrayList<int[]>();
	{
		pages.add(new int[] { R.drawable.menu_tweets, R.string.pages_mine });
		pages.add(new int[] { R.drawable.menu_profile, R.string.pages_profile });
		pages.add(new int[] { R.drawable.menu_followers,
				R.string.pages_followers });
		pages.add(new int[] { R.drawable.menu_following,
				R.string.pages_following });
		pages.add(new int[] { R.drawable.menu_list, R.string.pages_home });
		pages.add(new int[] { R.drawable.menu_mentions, R.string.pages_mentions });
		pages.add(new int[] { R.drawable.menu_listed, R.string.pages_browse });
		pages.add(new int[] { R.drawable.menu_favorites, R.string.pages_search });
		pages.add(new int[] { R.drawable.menu_create_list,
				R.string.pages_message });
	};

	private GridView gridview;

	public MenuDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}

	public MenuDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	public MenuDialog(Context context) {
		super(context, R.style.Theme_Transparent);

		setContentView(R.layout.menu_dialog);
		// setTitle("Custom Dialog");
		setCanceledOnTouchOutside(true);

		// 设置window属性
		LayoutParams a = getWindow().getAttributes();
		a.gravity = Gravity.TOP;
		a.dimAmount = 0; // 去背景遮盖
		getWindow().setAttributes(a);

		initMenu();
	}

	public void setPosition(int x, int y) {
		LayoutParams a = getWindow().getAttributes();
		if (-1 != x)
			a.x = x;
		if (-1 != y)
			a.y = y;
		getWindow().setAttributes(a);
	}

	private void goTo(Class<?> cls, Intent intent) {
		if (getOwnerActivity().getClass() != cls) {
			dismiss();
			intent.setClass(getContext(), cls);
			getContext().startActivity(intent);
		} else {
			String msg = getContext().getString(R.string.page_status_same_page);
			Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
		}
	}

	private void goTo(Class<?> cls) {
		Intent intent = new Intent();
		goTo(cls, intent);
	}

	private void initMenu() {
		// 准备要添加的数据条目
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();

		for (int[] item : pages) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("image", item[0]);
			map.put("title", getContext().getString(item[1]));
			items.add(map);
		}

		// 实例化一个适配器
		SimpleAdapter adapter = new SimpleAdapter(getContext(), items, // data
				R.layout.menu_item, // grid item layout
				new String[] { "title", "image" }, // data's key
				new int[] { R.id.item_text, R.id.item_image }); // item view id

		// 获得GridView实例
		gridview = (GridView) findViewById(R.id.mygridview);
		// 将GridView和数据适配器关联
		gridview.setAdapter(adapter);

	}

	public void bindEvent(Activity activity) {
		setOwnerActivity(activity);

		// 绑定监听器
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				switch (position) {
				case PAGE_MINE:
					String user = TwitterApplication.getMyselfId(false);
					String name = TwitterApplication.getMyselfName(false);
					Intent intent = UserTimelineActivity.createIntent(user,
							name);
					goTo(UserTimelineActivity.class, intent);
					break;
				case PAGE_PROFILE:
					goTo(ProfileActivity.class);
					break;
				case PAGE_FOLLOWERS:
					goTo(FollowersActivity.class);
					break;
				case PAGE_FOLLOWING:
					goTo(FollowingActivity.class);
					break;
				case PAGE_HOME:
					goTo(TwitterActivity.class);
					break;
				case PAGE_MENTIONS:
					goTo(MentionActivity.class);
					break;
				case PAGE_BROWSE:
					goTo(BrowseActivity.class);
					break;
				case PAGE_FAVORITES:
					goTo(FavoritesActivity.class);
					break;
				case PAGE_MESSAGE:
					goTo(DmActivity.class);
					break;
				}
			}
		});

		Button close_button = (Button) findViewById(R.id.close_menu);
		close_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}

}
