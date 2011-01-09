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
package com.ch_linghu.android.fanfoudroid.ui.widget;

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

import com.ch_linghu.android.fanfoudroid.R;
import com.ch_linghu.android.fanfoudroid.ui.DmActivity;
import com.ch_linghu.android.fanfoudroid.ui.MentionActivity;
import com.ch_linghu.android.fanfoudroid.ui.TwitterActivity;

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
			Intent intent = new Intent();
			intent.setClass(getContext(), cls);
			getContext().startActivity(intent);
			dismiss();
		} else {
			String msg = "您正在此页.";
			Toast.makeText(getContext(), msg,
					Toast.LENGTH_SHORT).show();
		}
	}

	private void initMenu() {
		// 准备要添加的数据条目
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();

		int[] resources = {R.drawable.menu_home,
						   R.drawable.menu_at,
						   R.drawable.menu_mail,
						   R.drawable.menu_star};
		
		for (int resource : resources) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("imageItem", resource);
			items.add(item);
		}

		// 实例化一个适配器
		SimpleAdapter adapter = new SimpleAdapter(getContext(), items,
				R.layout.menu_item, new String[] { "imageItem" },
				new int[] { R.id.image_item });
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
					//TODO: goto(FavActivity.class);
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
