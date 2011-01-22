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

import com.ch_linghu.fanfoudroid.DmActivity;
import com.ch_linghu.fanfoudroid.FavoritesActivity;
import com.ch_linghu.fanfoudroid.MentionActivity;
import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.TwitterActivity;

/**
 * 顶部主菜单切换浮动层
 * 
 * @author lds
 * 
 */
public class MenuDialog extends Dialog {

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
	
	private void goTo(Class<?> cls) {
		if (getOwnerActivity().getClass() != cls) {
			dismiss();
			Intent intent = new Intent();
			intent.setClass(getContext(), cls);
			getContext().startActivity(intent);
		} else {
			String msg = getContext().getString(R.string.page_status_same_page);
			Toast.makeText(getContext(), msg,
					Toast.LENGTH_SHORT).show();
		}
	}

	private void initMenu() {
		// 准备要添加的数据条目
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();

        Object[] data = { R.drawable.menu_tweets, "我的空间",
                R.drawable.menu_profile, "个人资料",
                R.drawable.menu_followers, "关注我的",
                R.drawable.menu_following, "我关注的",
                R.drawable.menu_list, "首页",
                R.drawable.menu_mentions, "提到我的",
                R.drawable.menu_listed, "随便看看",
                R.drawable.menu_favorites, "搜索",
                R.drawable.menu_create_list, "我的私信" };
		
        for (int i = 0; i < data.length; i+=2) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("image", data[i]);
			item.put("title", data[i+1] );
			items.add(item);
		}

		// 实例化一个适配器
		SimpleAdapter adapter = new SimpleAdapter(getContext(), 
		        items, // data
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
				case 0:
					goTo(TwitterActivity.class);
					break;
				case 1:
					goTo(MentionActivity.class);
					break;
				case 2:
					goTo(DmActivity.class);
					break;
				case 3:
					goTo(FavoritesActivity.class);
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
