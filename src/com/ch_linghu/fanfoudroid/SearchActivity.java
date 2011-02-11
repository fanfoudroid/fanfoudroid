package com.ch_linghu.fanfoudroid;

import java.util.ArrayList;

import com.commonsware.cwac.merge.MergeAdapter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class SearchActivity extends Activity {
	
	private EditText mSearchEdit;
	private ListView mSearchSectionList;
	private MergeAdapter mSearchSectionAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        
        mSearchEdit = (EditText)findViewById(R.id.edt_search);
        mSearchEdit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onSearchRequested();
			}
		});
        
        mSearchSectionAdapter = new MergeAdapter();
        
        TextView trendsTitle = (TextView)getLayoutInflater().inflate(R.layout.search_section_header, null);
        trendsTitle.setText("热门话题");

        TextView savedSearchTitle = (TextView)getLayoutInflater().inflate(R.layout.search_section_header, null);
        savedSearchTitle.setText("关注的话题");
        
        mSearchSectionList = (ListView)findViewById(R.id.search_section_list);
        mSearchSectionList.setAdapter(mSearchSectionAdapter);
        
        
        //FIXME: 以下代码均为示例代码(buggy, should use more stable library to implement)
        ArrayList trends = new ArrayList();
        trends.add("情人节");
        trends.add("下雪");
        trends.add("爱迪生");
        trends.add("跳槽季节");
        trends.add("旭日阳刚");
        ArrayAdapter trendsAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, trends);
        
        ArrayList savedSearch = new ArrayList();
        savedSearch.add("安能饭否");
        savedSearch.add("android");
        ArrayAdapter savedSearchesAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, savedSearch);       

        mSearchSectionAdapter.addView(trendsTitle);
        mSearchSectionAdapter.addAdapter(trendsAdapter);
        mSearchSectionAdapter.addView(savedSearchTitle);
        mSearchSectionAdapter.addAdapter(savedSearchesAdapter);

	}

}
