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

package com.ch_linghu.fanfoudroid.helper;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import com.ch_linghu.fanfoudroid.R;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Manages retrieval and storage of icon images.
 * Use the put method to download and store images.
 * Use the get method to retrieve images from the manager.
 */
public class ImageManager implements ImageCache {
  private static final String TAG = "ImageManager";

  private Context mContext;
  // In memory cache.
  private Map<String, SoftReference<Bitmap>> mCache;
  private HttpClient mClient;
  // MD5 hasher.
  private MessageDigest mDigest;
  
  // We want the requests to timeout quickly.
  // Tweets are processed in a batch and we don't want to stay on one too long.
  private static final int CONNECTION_TIMEOUT_MS = 10 * 1000;
  private static final int SOCKET_TIMEOUT_MS = 10 * 1000;

  public ImageManager(Context context) {
    mContext = context;
    mCache = new HashMap<String, SoftReference<Bitmap>>();
    mClient = new DefaultHttpClient();

    try {
      mDigest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      // This shouldn't happen.
      throw new RuntimeException("No MD5 algorithm.");
    }
  }

  public void setContext(Context context) {
    mContext = context;
  }

  private String getHashString(MessageDigest digest) {
    StringBuilder builder = new StringBuilder();

    for (byte b : digest.digest()) {
      builder.append(Integer.toHexString((b >> 4) & 0xf));
      builder.append(Integer.toHexString(b & 0xf));
    }

    return builder.toString();
  }

  // MD5 hases are used to generate filenames based off a URL.
  private String getMd5(String url) {
    mDigest.update(url.getBytes());

    return getHashString(mDigest);
  }

  // Looks to see if an image is in the file system.
  private Bitmap lookupFile(String url) {
    String hashedUrl = getMd5(url);
    FileInputStream fis = null;

    try {
      fis = mContext.openFileInput(hashedUrl);
      return BitmapFactory.decodeStream(fis);
    } catch (FileNotFoundException e) {
      // Not there.
      return null;
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException e) {
          // Ignore.
        }
      }
    }
  }

 

  public Bitmap fetchImage(String url) throws IOException {
    Log.i(TAG, "Fetching image: " + url);

    HttpGet get = new HttpGet(url);
    HttpConnectionParams.setConnectionTimeout(get.getParams(),
        CONNECTION_TIMEOUT_MS);
    HttpConnectionParams.setSoTimeout(get.getParams(),
        SOCKET_TIMEOUT_MS);

    HttpResponse response;

    try {
      response = mClient.execute(get);
    } catch (ClientProtocolException e) {
      Log.e(TAG, e.getMessage(), e);
      throw new IOException("Invalid client protocol.");
    }

    if (response.getStatusLine().getStatusCode() != 200) {
      throw new IOException("Non OK response: " +
          response.getStatusLine().getStatusCode());
    }

    HttpEntity entity = response.getEntity();
    BufferedInputStream bis = new BufferedInputStream(entity.getContent(),
        8 * 1024);
    Bitmap bitmap = BitmapFactory.decodeStream(bis);
    bis.close();

    return bitmap;
  }

  // Downloads and stores an image.
  public void put(String url) throws IOException {
    if (contains(url)) {
      // Image already exists.
      return;

      // TODO: write to file if not present.
    }

    Bitmap bitmap = fetchImage(url);

    if (bitmap == null) {
      Log.w(TAG, "Retrieved bitmap is null.");
    } else {
      put(url, bitmap);
    }
  }

  @Override
  public void put(String url, Bitmap bitmap) {
    synchronized(this) {
      mCache.put(url, new SoftReference<Bitmap>(bitmap));
    }

    writeFile(url, bitmap);
  }

  private void writeFile(String url, Bitmap bitmap) {
    if (bitmap == null) {
      Log.w(TAG, "Can't write file. Bitmap is null.");
      return;
    }

    String hashedUrl = getMd5(url);

    FileOutputStream fos;

    try {
      fos = mContext.openFileOutput(hashedUrl,
          Context.MODE_PRIVATE);
    } catch (FileNotFoundException e) {
      Log.w(TAG, "Error creating file.");
      return;
    }

    Log.i(TAG, "Writing file: " + hashedUrl);
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);

    try {
      fos.close();
    } catch (IOException e) {
      Log.w(TAG, "Could not close file.");
    }
  }

  public Bitmap get(String url) {
    SoftReference<Bitmap> ref;
    Bitmap bitmap;

    // Look in memory first.
    synchronized(this) {
      ref = mCache.get(url);
    }

    if (ref != null) {
      bitmap = ref.get();

      if (bitmap != null) {
        return bitmap;
      }
    }

    // Now try file.
    bitmap = lookupFile(url);

    if (bitmap != null) {
      synchronized(this) {
        mCache.put(url, new SoftReference<Bitmap>(bitmap));
      }

      return bitmap;
    }

    Log.i(TAG, "Image is missing: " + url);
    //return the default photo
    return mDefaultBitmap;
  }

  public boolean contains(String url) {
    return get(url) != mDefaultBitmap;
  }

  public void clear() {
    String [] files = mContext.fileList();

    for (String file : files) {
      mContext.deleteFile(file);
    }

    synchronized(this) {
      mCache.clear();
    }
  }

  public void cleanup(HashSet<String> keepers) {
    String [] files = mContext.fileList();
    HashSet<String> hashedUrls = new HashSet<String>();

    for (String imageUrl : keepers) {
      hashedUrls.add(getMd5(imageUrl));
    }

    for (String file : files) {
      if (!hashedUrls.contains(file)) {
        Log.i(TAG, "Deleting unused file: " + file);
        mContext.deleteFile(file);
      }
    }
  }

}
