package com.ch_linghu.fanfoudroid;

import java.util.ArrayList;
import java.util.List;

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
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
	private ArrayAdapter<String> trendsAdapter;
	private ArrayAdapter<String> savedSearchesAdapter;

	private ArrayList<String> trends;
	private ArrayList<String> savedSearch;
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
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "onResume()...");
		super.onResume();

		refreshSearchSectionList(SearchActivity.LOADING);

		trendsAndSavedSearchesTask = new TrendsAndSavedSearchesTask();
		trendsAndSavedSearchesTask
				.setListener(trendsAndSavedSearchesTaskListener);
		trendsAndSavedSearchesTask.execute();
	}

	private void initSearchSectionList() {
		trends = new ArrayList<String>();
		savedSearch = new ArrayList<String>();
		savedSearch.add(getResources().getString(R.string.search_loading));
		trends.add(getResources().getString(R.string.search_loading));

		trendsAdapter = new ArrayAdapter<String>(this,
				R.layout.search_section_view, trends);
		savedSearchesAdapter = new ArrayAdapter<String>(this,
				R.layout.search_section_view, savedSearch);

		mSearchSectionAdapter.addView(savedSearchTitle);
		mSearchSectionAdapter.addAdapter(savedSearchesAdapter);
		mSearchSectionAdapter.addView(trendsTitle);
		mSearchSectionAdapter.addAdapter(trendsAdapter);
		mSearchSectionList.setAdapter(mSearchSectionAdapter);
	}

	/**
	 * flag: loading;network error;success
	 */
	private void refreshSearchSectionList(int flag) {
		AdapterView.OnItemClickListener searchSectionListListener = new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				initialQuery = ((TextView) view).getText().toString();
				startSearch();
			}
		};
		if (flag == SearchActivity.LOADING) {
			mSearchSectionList.setOnItemClickListener(null);
			savedSearch.clear();
			trends.clear();
			savedSearch.add(getResources().getString(R.string.search_loading));
			trends.add(getResources().getString(R.string.search_loading));
		} else if (flag == SearchActivity.NETWORKERROR) {
			mSearchSectionList.setOnItemClickListener(null);
			savedSearch.clear();
			trends.clear();
			savedSearch.add(getResources().getString(
					R.string.login_status_network_or_connection_error));
			trends.add(getResources().getString(
					R.string.login_status_network_or_connection_error));
		}

		trendsAdapter = new ArrayAdapter<String>(this,
				R.layout.search_section_view, trends);
		savedSearchesAdapter = new ArrayAdapter<String>(this,
				R.layout.search_section_view, savedSearch);
		mSearchSectionAdapter.notifyDataSetChanged();

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
				trends.add(trendsList[i].getName());
			}
			for (int i = 0; i < savedSearchsList.size(); i++) {
				savedSearch.add(savedSearchsList.get(i).getName());
			}

			return TaskResult.OK;
		}

	}

}
