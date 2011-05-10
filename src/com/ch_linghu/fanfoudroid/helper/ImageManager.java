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
import java.io.File;
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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Manages retrieval and storage of icon images. Use the put method to download
 * and store images. Use the get method to retrieve images from the manager.
 */
public class ImageManager implements ImageCache {
    private static final String TAG = "ImageManager";
    
    // 饭否目前最大宽度支持496px, 超过则同比缩小
    // 最大宽度为992px, 超过从中截取
    public static final int DEFAULT_COMPRESS_QUALITY = 90;
    public static final int MAX_WIDTH  = 496;
    public static final int MAX_HEIGHT = 992;

    private Context mContext;
    // In memory cache.
    private Map<String, SoftReference<Bitmap>> mCache;
    private HttpClient mClient;
    // MD5 hasher.
    private MessageDigest mDigest;

    // We want the requests to timeout quickly.
    // Tweets are processed in a batch and we don't want to stay on one too
    // long.
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
    
    /**
     * Downloads a file
     * @param url
     * @return
     * @throws IOException
     */
    public Bitmap fetchImage(String url) throws IOException {
        Log.d(TAG, "Fetching image: " + url);

        HttpGet get = new HttpGet(url);
        HttpConnectionParams.setConnectionTimeout(get.getParams(),
                CONNECTION_TIMEOUT_MS);
        HttpConnectionParams.setSoTimeout(get.getParams(), SOCKET_TIMEOUT_MS);

        HttpResponse response;

        try {
            response = mClient.execute(get);
        } catch (ClientProtocolException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new IOException("Invalid client protocol.");
        }

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Non OK response: "
                    + response.getStatusLine().getStatusCode());
        }

        HttpEntity entity = response.getEntity();
        BufferedInputStream bis = new BufferedInputStream(entity.getContent(),
                8 * 1024);
        Bitmap bitmap = BitmapFactory.decodeStream(bis);
        bis.close();

        return bitmap;
    }

    /**
     * 下载远程图片 -> 转换为Bitmap -> 写入缓存器.
     * @param url
     * @param quality image quality 1～100
     * @throws IOException
     */
    public void put(String url, int quality, boolean forceOverride) throws IOException {
        if (!forceOverride && contains(url)) {
            // Image already exists.
            return;

            // TODO: write to file if not present.
        }

        Bitmap bitmap = fetchImage(url);

        if (bitmap == null) {
            Log.w(TAG, "Retrieved bitmap is null.");
        } else {
            put(url, bitmap, quality);
        }
    }
    
    /**
     * 重载 put(String url, int quality)
     * @param url
     * @throws IOException
     */
    public void put(String url) throws IOException {
        put(url, DEFAULT_COMPRESS_QUALITY, false);
    }
    
    /**
     * 将本地File -> 转换为Bitmap -> 写入缓存器.
     * 如果图片大小超过MAX_WIDTH/MAX_HEIGHT, 则将会对图片缩放.
     * 
     * @param file
     * @param quality 图片质量(0~100)
     * @param forceOverride 
     * @throws IOException
     */
    public void put(File file, int quality, boolean forceOverride) throws IOException {
        if (!file.exists()) {
            Log.w(TAG, file.getName() + " is not exists.");
            return;
        }
        if (!forceOverride && contains(file.getPath())) {
            // Image already exists.
            Log.d(TAG, file.getName() + " is exists");
            return;
            // TODO: write to file if not present.
        }

        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
        bitmap = resizeBitmap(bitmap, MAX_WIDTH, MAX_HEIGHT);

        if (bitmap == null) {
            Log.w(TAG, "Retrieved bitmap is null.");
        } else {
            put(file.getPath(), bitmap, quality);
        }
    }
    
    /**
     * 将Bitmap写入缓存器.
     * @param filePath file path
     * @param bitmap
     * @param quality 1~100
     */
    public void put(String file, Bitmap bitmap, int quality) {
        synchronized (this) {
            mCache.put(file, new SoftReference<Bitmap>(bitmap));
        }

        writeFile(file, bitmap, quality);
    }
    
    /**
     * 重载 put(String file, Bitmap bitmap, int quality)
     * @param filePath file path
     * @param bitmap
     * @param quality 1~100
     */
    @Override
    public void put(String file, Bitmap bitmap) {
        put(file, bitmap, DEFAULT_COMPRESS_QUALITY);
    }

    /**
     * 将Bitmap写入本地缓存文件.
     * @param file URL/PATH
     * @param bitmap
     * @param quality
     */
    private void writeFile(String file, Bitmap bitmap, int quality) {
        if (bitmap == null) {
            Log.w(TAG, "Can't write file. Bitmap is null.");
            return;
        }

        String hashedUrl = getMd5(file);

        FileOutputStream fos;

        try {
            fos = mContext.openFileOutput(hashedUrl, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Error creating file.");
            return;
        }
        
        // image is too small
        if (bitmap.getWidth() < 100 && bitmap.getHeight() < 100) {
            quality = 100;
        }
        
        Log.d(TAG, "Writing file: " + hashedUrl);
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);

        try {
            fos.close();
        } catch (IOException e) {
            Log.w(TAG, "Could not close file.");
        }
    }
    
    public Bitmap get(File file) {
        return get(file.getPath());
    }
    
    /**
     * 从缓存器中读取文件
     * @param file file URL/file PATH
     * @param bitmap
     * @param quality
     */
    public Bitmap get(String file) {
        SoftReference<Bitmap> ref;
        Bitmap bitmap;

        // Look in memory first.
        synchronized (this) {
            ref = mCache.get(file);
        }

        if (ref != null) {
            bitmap = ref.get();

            if (bitmap != null) {
                return bitmap;
            }
        }

        // Now try file.
        bitmap = lookupFile(file);

        if (bitmap != null) {
            synchronized (this) {
                mCache.put(file, new SoftReference<Bitmap>(bitmap));
            }

            return bitmap;
        }

        //TODO: why?  
        //upload: see profileImageCacheManager line 96
        Log.w(TAG, "Image is missing: " + file);
        // return the default photo
        return mDefaultBitmap;
    }

    public boolean contains(String url) {
        return get(url) != mDefaultBitmap;
    }

    public void clear() {
        String[] files = mContext.fileList();

        for (String file : files) {
            mContext.deleteFile(file);
        }

        synchronized (this) {
            mCache.clear();
        }
    }

    public void cleanup(HashSet<String> keepers) {
        String[] files = mContext.fileList();
        HashSet<String> hashedUrls = new HashSet<String>();

        for (String imageUrl : keepers) {
            hashedUrls.add(getMd5(imageUrl));
        }

        for (String file : files) {
            if (!hashedUrls.contains(file)) {
                Log.d(TAG, "Deleting unused file: " + file);
                mContext.deleteFile(file);
            }
        }
    }
    
    /**
     * Compress and resize the Image
     * @param targetFile
     * @param quality
     * @return
     * @throws IOException
     */
    public File compressImage(File targetFile, int quality) throws IOException {
        
        put(targetFile, quality, true); // compress, resize, store 
        
        String filePath = getMd5(targetFile.getPath());
        File compressedImage = mContext.getFileStreamPath(filePath);
        
        return compressedImage;
    }
    
    /**
     * 保持长宽比缩小Bitmap
     * 
     * @param bitmap
     * @param maxWidth
     * @param maxHeight
     * @param quality 1~100
     * @return
     */
    public Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        
        int originWidth  = bitmap.getWidth();
        int originHeight = bitmap.getHeight();
        
        // no need to resize
        if (originWidth < maxWidth && originHeight < maxHeight) { 
            return bitmap;
        }
        
        int width  = originWidth;
        int height = originHeight;
        
        // 若图片过宽, 则保持长宽比缩放图片
        if (originWidth > maxWidth) {
            width = maxWidth;
            
            double i = originWidth * 1.0 / maxWidth;
            height = (int) Math.floor(originHeight / i);
        
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        }
        
        // 若图片过长, 则从上端截取
        if (height > maxHeight) {
            height = maxHeight;
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        }
        
//        Log.d(TAG, width + " width");
//        Log.d(TAG, height + " height");
        
        return bitmap;
    }

}
