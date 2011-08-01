package com.ch_linghu.fanfoudroid;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ch_linghu.fanfoudroid.app.Preferences;
import com.ch_linghu.fanfoudroid.R;

public class AboutActivity extends Activity {
	//反馈信息
	private String versionName = null;
	private String deviceModel = null;
	private String versionRelease = null;
	private String feedback = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		deviceModel=android.os.Build.MODEL;
		versionRelease=android.os.Build.VERSION.RELEASE;

		if (TwitterApplication.mPref.getBoolean(
				Preferences.FORCE_SCREEN_ORIENTATION_PORTRAIT, false)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		// set real version
		ComponentName comp = new ComponentName(this, getClass());
		PackageInfo pinfo = null;
		try {
			pinfo = getPackageManager()
					.getPackageInfo(comp.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TextView version = (TextView) findViewById(R.id.version);
		version.setText(String.format("v %s", pinfo.versionName));
		
		versionName = pinfo.versionName;

		// bind button click event
		Button okBtn = (Button) findViewById(R.id.ok_btn);
		okBtn.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		feedback = "@忽然兔 "+"#安能饭否#"+"版本"+versionName+",型号"+deviceModel+",系统"+versionRelease+" ";
				
		Button chkUpdateBtn = (Button) findViewById(R.id.checkupdate_btn);
		chkUpdateBtn.setOnClickListener(new Button.OnClickListener() {

			
			@Override
			public void onClick(View v) {
				Intent intent = WriteActivity.createNewTweetIntent(feedback);
				startActivity(intent);
			}
		});
	}

	

}
