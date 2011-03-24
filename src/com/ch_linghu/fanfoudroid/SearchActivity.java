package com.ch_linghu.fanfoudroid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.helper.Utils;
import com.ch_linghu.fanfoudroid.task.GenericTask;
import com.ch_linghu.fanfoudroid.task.TaskAdapter;
import com.ch_linghu.fanfoudroid.task.TaskListener;
import com.ch_linghu.fanfoudroid.task.TaskParams;
import com.ch_linghu.fanfoudroid.task.TaskResult;
import com.ch_linghu.fanfoudroid.ui.base.WithHeaderActivity;
import com.ch_linghu.fanfoudroid.weibo.SavedSearch;
import com.ch_linghu.fanfoudroid.weibo.Trend;
import com.ch_linghu.fanfoudroid.weibo.WeiboException;
import com.commonsware.cwac.merge.MergeAdapter;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends WithHeaderActivity {

	private static final String TAG = SearchActivity.class.getSimpleName();
	private static final int LOADING = 1;
	private static final int NETWORKERROR = 2;
	private static final int SUCCESS = 3;

	private EditText mSearchEdit;
	private ListView mSearchSectionList;
	private TextView trendsTitle;
	private TextView savedSearchTitle;

	private MergeAdapter mSearchSectionAdapter;
	private SearchAdapter trendsAdapter;
	private SearchAdapter savedSearchesAdapter;

	private ArrayList<SearchItem> trends;
	private ArrayList<SearchItem> savedSearch;
	private String initialQuery;

	private GenericTask trendsAndSavedSearchesTask;
	private TaskListener trendsAndSavedSearchesTaskListener = new TaskAdapter() {

		@Override
		public String getName() {
			return "trendsAndSavedSearchesTask";
		}

		@Override
		public void onPreExecute(GenericTask task) {
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {

			if (result == TaskResult.OK) {
				refreshSearchSectionList(SearchActivity.SUCCESS);
			} else if (result == TaskResult.IO_ERROR) {
				refreshSearchSectionList(SearchActivity.NETWORKERROR);

				Toast.makeText(
						SearchActivity.this,
						getResources()
								.getString(
										R.string.login_status_network_or_connection_error),
						Toast.LENGTH_SHORT).show();
			}

		}

	};

	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate()...");
		if (super._onCreate(savedInstanceState)) {
			setContentView(R.layout.search);
			initHeader(HEADER_STYLE_SEARCH);

			mSearchEdit = (EditText) findViewById(R.id.search_edit);
			mSearchEdit.setOnKeyListener(enterKeyHandler);

			trendsTitle = (TextView) getLayoutInflater().inflate(
					R.layout.search_section_header, null);
			trendsTitle
					.setText(getResources().getString(R.string.trends_title));

			savedSearchTitle = (TextView) getLayoutInflater().inflate(
					R.layout.search_section_header, null);
			savedSearchTitle.setText(getResources().getString(
					R.string.saved_search_title));
			mSearchSectionAdapter = new MergeAdapter();
			mSearchSectionList = (ListView) findViewById(R.id.search_section_list);

			initSearchSectionList();

			refreshSearchSectionList(SearchActivity.LOADING);

			doGetSavedSearches();

			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "onResume()...");
		super.onResume();
	}
	
	private void doGetSavedSearches(){
		if (trendsAndSavedSearchesTask != null 
				&& trendsAndSavedSearchesTask.getStatus() == GenericTask.Status.RUNNING){
			return;
		}else{
			trendsAndSavedSearchesTask = new TrendsAndSavedSearchesTask();
			trendsAndSavedSearchesTask
					.setListener(trendsAndSavedSearchesTaskListener);
			trendsAndSavedSearchesTask.execute();			
		}
	}

	private void initSearchSectionList() {
		trends = new ArrayList<SearchItem>();
		savedSearch = new ArrayList<SearchItem>();

		trendsAdapter = new SearchAdapter(this);
		savedSearchesAdapter = new SearchAdapter(this);

		mSearchSectionAdapter.addView(savedSearchTitle);
		mSearchSectionAdapter.addAdapter(savedSearchesAdapter);
		mSearchSectionAdapter.addView(trendsTitle);
		mSearchSectionAdapter.addAdapter(trendsAdapter);
		mSearchSectionList.setAdapter(mSearchSectionAdapter);
	}

	/**
	 * 辅助计算位置的类
	 * @author jmx
	 *
	 */
	class PositionHelper{
		/**
		 * 返回指定位置属于哪一个小节
		 * @param position 绝对位置
		 * @return 小节的序号，0是第一小节，1是第二小节, -1为无效位置
		 */
		public int getSectionIndex(int position){
			int[] contentLength = new int[2];
			contentLength[0] = savedSearchesAdapter.getCount();
			contentLength[1] = trendsAdapter.getCount();
			
			if (position > 0 && position < contentLength[0]+1){
				return 0;
			} else if (position > contentLength[0]+1 
					&& position < (contentLength[0]+contentLength[1]+1)+1){
				return 1;
			} else {
				return -1;
			}
		}
		
		/**
		 * 返回指定位置在自己所在小节的相对位置
		 * @param position 绝对位置
		 * @return 所在小节的相对位置，-1为无效位置
		 */
		public int getRelativePostion(int position){
			int[] contentLength = new int[2];
			contentLength[0] = savedSearchesAdapter.getCount();
			contentLength[1] = trendsAdapter.getCount();
			
			int sectionIndex = getSectionIndex(position);
			int offset = 0;
			for (int i = 0; i < sectionIndex; ++i) {
				offset += contentLength[i] + 1;
			}
			return position - offset - 1;
		}
	}
	/**
	 * flag: loading;network error;success
	 */
	PositionHelper pos_helper = new PositionHelper();
	private void refreshSearchSectionList(int flag) {

		AdapterView.OnItemClickListener searchSectionListListener = new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				MergeAdapter adapter = (MergeAdapter)(adapterView.getAdapter());
				SearchAdapter subAdapter = (SearchAdapter)adapter.getAdapter(position);
				
				//计算针对subAdapter中的相对位置
				int relativePos = pos_helper.getRelativePostion(position);
				
				SearchItem item = (SearchItem)(subAdapter.getItem(relativePos));
				initialQuery = item.query;
				startSearch();
			}
		};

		if (flag == SearchActivity.LOADING) {
			mSearchSectionList.setOnItemClickListener(null);
			savedSearch.clear();
			trends.clear();
			savedSearchesAdapter.refresh(getString(R.string.search_loading));
			trendsAdapter.refresh(getString(R.string.search_loading));
		} else if (flag == SearchActivity.NETWORKERROR) {
			mSearchSectionList.setOnItemClickListener(null);
			savedSearch.clear();
			trends.clear();
			savedSearchesAdapter.refresh(getString(R.string.login_status_network_or_connection_error));
			trendsAdapter.refresh(getString(R.string.login_status_network_or_connection_error));
		} else {
			savedSearchesAdapter.refresh(savedSearch);
			trendsAdapter.refresh(trends);
		}

		if (flag == SearchActivity.SUCCESS) {
			mSearchSectionList
					.setOnItemClickListener(searchSectionListListener);
		}
	}

	@Override
	protected boolean startSearch() {
		if (!Utils.isEmpty(initialQuery)) {
			// 以下这个方法在7可用，在8就报空指针
			// triggerSearch(initialQuery, null);
			Intent i = new Intent(this, SearchResultActivity.class);
			i.putExtra(SearchManager.QUERY, initialQuery);
			startActivity(i);
		} else if (Utils.isEmpty(initialQuery)) {
			Toast.makeText(this,
					getResources().getString(R.string.search_box_null),
					Toast.LENGTH_SHORT).show();
			return false;
		}
		return false;
	}

	@Override
	protected void addSearchButton() {
		searchButton = (ImageButton) findViewById(R.id.search);
		searchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				initialQuery = mSearchEdit.getText().toString();
				startSearch();
			}
		});
	}

	// 搜索框回车键判断
	private View.OnKeyListener enterKeyHandler = new View.OnKeyListener() {
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_ENTER
					|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
				if (event.getAction() == KeyEvent.ACTION_UP) {
					initialQuery = mSearchEdit.getText().toString();
					startSearch();
				}
				return true;
			}
			return false;
		}
	};

	private class TrendsAndSavedSearchesTask extends GenericTask {
		Trend[] trendsList;
		List<SavedSearch> savedSearchsList;

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			try {
				trendsList = getApi().getTrends().getTrends();
				savedSearchsList = getApi().getSavedSearches();
			} catch (WeiboException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			}

			trends.clear();
			savedSearch.clear();

			for (int i = 0; i < trendsList.length; i++) {
				SearchItem item = new SearchItem();
				item.name = trendsList[i].getName();
				item.query = trendsList[i].getQuery();
				trends.add(item);
			}			
			for (int i = 0; i < savedSearchsList.size(); i++) {
				SearchItem item = new SearchItem();
				item.name = savedSearchsList.get(i).getName();
				item.query = savedSearchsList.get(i).getQuery();
				savedSearch.add(item);
			}

			return TaskResult.OK;
		}

	}

}

class SearchItem {
	public String name;
	public String query;
}

class SearchAdapter extends BaseAdapter{
	protected ArrayList<SearchItem> mSearchList;
	private Context mContext;
	protected LayoutInflater mInflater;
	protected StringBuilder mMetaBuilder;

	public SearchAdapter(Context context) {
		mSearchList = new ArrayList<SearchItem>();
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mMetaBuilder = new StringBuilder();
	}
	
	public SearchAdapter(Context context, String prompt) {
		this(context);
		refresh(prompt);
	}
	
	public void refresh(ArrayList<SearchItem> searchList){
		mSearchList = searchList;
		notifyDataSetChanged();
	}
	
	public void refresh(String prompt){
		SearchItem item = new SearchItem();
		item.name = prompt;
		item.query = null;

		mSearchList.clear();
		mSearchList.add(item);
		
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return mSearchList.size();
	}

	@Override
	public Object getItem(int position) {
		return mSearchList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		if (convertView == null) {
			view = mInflater.inflate(R.layout.search_section_view, parent, false);
			TextView text = (TextView)view.findViewById(R.id.search_section_text);
			view.setTag(text);
		} else {
			view = convertView;
		}
		
		TextView text = (TextView)view.getTag();
		
		SearchItem item = mSearchList.get(position);
		text.setText(item.name);
		
		return view;
	}
}
