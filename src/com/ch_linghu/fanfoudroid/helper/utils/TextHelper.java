package com.ch_linghu.fanfoudroid.helper.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class TextHelper {
    private static final String TAG = "TextHelper";

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static String stringifyStream(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }

        return sb.toString();
    }
    
    private static HashMap<String, String> _userLinkMapping = new HashMap<String, String>();

    private static final Pattern NAME_MATCHER = Pattern.compile("@.+?\\s");
    private static final Linkify.MatchFilter NAME_MATCHER_MATCH_FILTER = new Linkify.MatchFilter() {
        @Override
        public final boolean acceptMatch(final CharSequence s, final int start,
                final int end) {

            String name = s.subSequence(start + 1, end).toString().trim();
            boolean result = _userLinkMapping.containsKey(name);
            return result;
        }
    };

    private static final Linkify.TransformFilter NAME_MATCHER_TRANSFORM_FILTER = new Linkify.TransformFilter() {

        @Override
        public String transformUrl(Matcher match, String url) {
            // TODO Auto-generated method stub
            String name = url.subSequence(1, url.length()).toString().trim();
            return _userLinkMapping.get(name);
        }
    };

    private static final String TWITTA_USER_URL = "twitta://users/";

    public static void linkifyUsers(TextView view) {
        Linkify.addLinks(view, NAME_MATCHER, TWITTA_USER_URL,
                NAME_MATCHER_MATCH_FILTER, NAME_MATCHER_TRANSFORM_FILTER);
    }

    private static final Pattern TAG_MATCHER = Pattern.compile("#\\w+#");

    private static final Linkify.TransformFilter TAG_MATCHER_TRANSFORM_FILTER = new Linkify.TransformFilter() {
        @Override
        public final String transformUrl(Matcher match, String url) {
            String result = url.substring(1, url.length() - 1);
            return "%23" + result + "%23";
        }
    };

    private static final String TWITTA_SEARCH_URL = "twitta://search/";

    public static void linkifyTags(TextView view) {
        Linkify.addLinks(view, TAG_MATCHER, TWITTA_SEARCH_URL, null,
                TAG_MATCHER_TRANSFORM_FILTER);
    }


    private static Pattern USER_LINK = Pattern
            .compile("@<a href=\"http:\\/\\/fanfou\\.com\\/(.*?)\" class=\"former\">(.*?)<\\/a>");

    public static String preprocessText(String text) {
        // 处理HTML格式返回的用户链接
        Matcher m = USER_LINK.matcher(text);
        while (m.find()) {
            _userLinkMapping.put(m.group(2), m.group(1));
            Log.d(TAG,
                    String.format("Found mapping! %s=%s", m.group(2),
                            m.group(1)));
        }

        // 将User Link的连接去掉
        StringBuffer sb = new StringBuffer();
        m = USER_LINK.matcher(text);
        while (m.find()) {
            m.appendReplacement(sb, "@$2");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static String getSimpleTweetText(String text) {
        return text.replaceAll("<.*?>", "").replace("&lt;", "<")
                .replace("&gt;", ">").replace("&nbsp;", " ")
                .replace("&amp;", "&").replace("&quot;", "\"");
    }

    public static void setSimpleTweetText(TextView textView, String text) {
        String processedText = getSimpleTweetText(text);
        textView.setText(processedText);
    }

    public static void setTweetText(TextView textView, String text) {
        String processedText = preprocessText(text);
        textView.setText(Html.fromHtml(processedText), BufferType.SPANNABLE);
        Linkify.addLinks(textView, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
        linkifyUsers(textView);
        linkifyTags(textView);
        _userLinkMapping.clear();
    }
}
