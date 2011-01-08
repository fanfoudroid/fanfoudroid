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
package com.ch_linghu.android.fanfoudroid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.WindowManager.LayoutParams;
import android.widget.GridView;
import android.widget.SimpleAdapter;

/**
 * 顶部主菜单切换浮动层
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

	private void initMenu() {
		// 准备要添加的数据条目
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> item_1 = new HashMap<String, Object>();
		Map<String, Object> item_2 = new HashMap<String, Object>();
		Map<String, Object> item_3 = new HashMap<String, Object>();
		Map<String, Object> item_4 = new HashMap<String, Object>();
		item_1.put("imageItem", R.drawable.menu_home);
		item_2.put("imageItem", R.drawable.menu_at);
		item_3.put("imageItem", R.drawable.menu_mail);
		item_4.put("imageItem", R.drawable.menu_star);
		items.add(item_1);
		items.add(item_2);
		items.add(item_3);
		items.add(item_4);
			
		// 实例化一个适配器
		SimpleAdapter adapter = new SimpleAdapter(getContext(), items,
				R.layout.menu_item, new String[] { "imageItem" },
				new int[] { R.id.image_item});
		// 获得GridView实例
		gridview = (GridView) findViewById(R.id.mygridview);
		// 将GridView和数据适配器关联
		gridview.setAdapter(adapter);
	}

}
