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

package com.ch_linghu.fanfoudroid;

import com.ch_linghu.fanfoudroid.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.LayoutInflater;
import android.view.View;

public class AboutDialog {

  public static void show(Activity activity) {
    View view = LayoutInflater.from(activity).inflate(R.layout.about, null);

    ComponentName comp = new ComponentName(activity, activity.getClass());
    PackageInfo pinfo = null;
	try {
		pinfo = activity.getPackageManager().getPackageInfo(comp.getPackageName(), 0);
	} catch (NameNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    String title = String.format("%s ver.%s", activity.getString(R.string.app_name), pinfo.versionName);
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setView(view);
    builder.setCancelable(true);
    builder.setTitle(title);
    builder.setPositiveButton(R.string.about_label_ok, null);
    builder.create().show();
  }  
 
}
