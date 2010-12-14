package com.ch_linghu.android.fanfoudroid;

import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.TextView;

public class TweetEdit {
  private EditText mEditText;
  private TextView mCharsRemainText;
  
  TweetEdit(EditText editText, TextView charsRemainText) {
    mEditText = editText;
    mCharsRemainText = charsRemainText;
    
    mEditText.addTextChangedListener(mTextWatcher);
    mEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
        MAX_TWEET_INPUT_LENGTH) });        
  }  

  private static final int MAX_TWEET_LENGTH = 140;
  private static final int MAX_TWEET_INPUT_LENGTH = 400;
  
  public void setTextAndFocus(String text, boolean start) {
    setText(text);
    Editable editable = mEditText.getText();
    if (!start){
    	Selection.setSelection(editable, editable.length());
    }else{
    	Selection.setSelection(editable, 0);    	
    }
    mEditText.requestFocus();    
  }

  public void setText(String text) {
    mEditText.setText(text);
    updateCharsRemain();
  }
  
  private TextWatcher mTextWatcher = new TextWatcher() {
    @Override
    public void afterTextChanged(Editable e) {
      updateCharsRemain();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
        int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
  };

  public void updateCharsRemain() {
    int remaining = MAX_TWEET_LENGTH - mEditText.length();
    mCharsRemainText.setText(remaining + "");
  }

  public String getText() {
    return mEditText.getText().toString();
  }

  public void setEnabled(boolean b) {
    mEditText.setEnabled(b);
  }

  public void setOnKeyListener(OnKeyListener listener) {
    mEditText.setOnKeyListener(listener);    
  }
  
  public void addTextChangedListener(TextWatcher watcher){
	  mEditText.addTextChangedListener(watcher);
  }

  public void requestFocus() {
    mEditText.requestFocus();
  }
    
}
