package com.ch_linghu.fanfoudroid;

import java.util.ArrayList;

import com.ch_linghu.fanfoudroid.data.User;
import com.ch_linghu.fanfoudroid.ui.module.UserArrayAdapter;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class FollowersActivity extends Activity {
	
	private ListView mUserList;
	private UserArrayAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        setContentView(R.layout.follower);
        
        //FIXME: 以下代码仅用于示例用途
        ArrayList<User> users = new ArrayList<User>();
        for(int i = 0; i < 32; ++i){
        	User user = new User();
        	user.id = "test" + i;
        	user.screenName = "测试" + i;
        	user.lastStatus = "谢谢万能的饭否 转@葡萄球君 万能的饭否告诉您 是位于埃及西奈半岛南端的一座城市，在红海与西奈山之间的海岸地带。距伊斯梅利亚大约四小时车程，距塔巴三小时车程。 转@和菜头 沙姆沙伊赫是哪里？";
        	users.add(user);
        }
        
        mAdapter = new UserArrayAdapter(this);
        mUserList = (ListView)findViewById(R.id.follower_list);
        mUserList.setAdapter(mAdapter);
        mAdapter.refresh(users);
        
	}

}
