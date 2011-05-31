package com.ch_linghu.fanfoudroid;

import com.ch_linghu.fanfoudroid.app.Preferences;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        
        if (TwitterApplication.mPref.getBoolean(
                Preferences.FORCE_SCREEN_ORIENTATION_PORTRAIT, false)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        
        //set real version
        ComponentName comp = new ComponentName(this, getClass());
        PackageInfo pinfo = null;
    	try {
    		pinfo = getPackageManager().getPackageInfo(comp.getPackageName(), 0);
    	} catch (NameNotFoundException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	
    	TextView version = (TextView)findViewById(R.id.version);
    	version.setText(String.format("v %s", pinfo.versionName));
    	
    	//bind button click event
    	Button okBtn = (Button)findViewById(R.id.ok_btn);
    	okBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
        
	}

}
