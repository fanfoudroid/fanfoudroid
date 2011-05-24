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

package com.ch_linghu.fanfoudroid.app;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.http.Response;

/**
 * Manages retrieval and storage of icon images. Use the put method to download
 * and store images. Use the get method to retrieve images from the manager.
 */
public class ImageManager implements ImageCache {
    private static final String TAG = "ImageManager";
    
    // 饭否目前最大宽度支持496px, 超过则同比缩小
    // 最大宽度为992px, 超过从中截取
    public static final int DEFAULT_COMPRESS_QUALITY = 90;
    public static final int MAX_WIDTH  = 596;
    public static final int MAX_HEIGHT = 1192;

    private Context mContext;
    // In memory cache.
    private Map<String, SoftReference<Bitmap>> mCache;
    // MD5 hasher.
    private MessageDigest mDigest;

	public static Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable
				.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	public ImageManager(Context context) {
        mContext = context;
        mCache = new HashMap<String, SoftReference<Bitmap>>();

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
     * @throws HttpException 
     */
    public Bitmap downloadImage(String url) throws HttpException {
        Log.d(TAG, "Fetching image: " + url);
        Response res = TwitterApplication.mApi.getHttpClient().get(url);
        return BitmapFactory.decodeStream(new BufferedInputStream(res.asStream()));
    }
    
    public Bitmap downloadImage2(String url) throws HttpException {
        Log.d(TAG, "[NEW]Fetching image: " + url);
        final Response res = TwitterApplication.mApi.getHttpClient().get(url);
        String file = writeToFile(res.asStream(), getMd5(url));
        return BitmapFactory.decodeFile(file);
    }
    
    /**
     * 下载远程图片 -> 转换为Bitmap -> 写入缓存器.
     * @param url
     * @param quality image quality 1～100
     * @throws HttpException 
     */
    public void put(String url, int quality, boolean forceOverride) throws HttpException {
        if (!forceOverride && contains(url)) {
            // Image already exists.
            return;

            // TODO: write to file if not present.
        }

        Bitmap bitmap = downloadImage(url);
        if (bitmap != null) {
            put(url, bitmap, quality); // file cache
        } else {
            Log.w(TAG, "Retrieved bitmap is null.");
        }
    }
    
    /**
     * 重载 put(String url, int quality)
     * @param url
     * @throws HttpException 
     */
    public void put(String url) throws HttpException {
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

        BufferedOutputStream bos = null;
        try {
            String hashedUrl = getMd5(file);
            bos = new BufferedOutputStream(
                    mContext.openFileOutput(hashedUrl, Context.MODE_PRIVATE));
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
            Log.d(TAG, "Writing file: " + file);
        } catch (IOException ioe) {
            Log.e(TAG, ioe.getMessage());
        } finally {
            try {
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
                //bitmap.recycle();
            } catch (IOException e) {
                Log.e(TAG, "Could not close file.");
            }
        }
    }
    
    private String writeToFile(InputStream is, String filename) {
        Log.d("LDS", "new write to file");
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            in  = new BufferedInputStream(is);
            out = new BufferedOutputStream(
                    mContext.openFileOutput(filename, Context.MODE_PRIVATE));
            byte[] buffer = new byte[1024];
            int l;
            while ((l = in.read(buffer)) != -1) {
                out.write(buffer, 0, l);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (in  != null) in.close();
                if (out != null) {
                    Log.d("LDS", "new write to file to -> " + filename);
                    out.flush();
                    out.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return mContext.getFilesDir() + "/" + filename;
    }
    
    public Bitmap get(File file) {
        return get(file.getPath());
    }
    
    /**
     * 判断缓存着中是否存在该文件对应的bitmap
     */
    public boolean isContains(String file) {
    	return mCache.containsKey(file);
    }
    
    /**
     * 获得指定file/URL对应的Bitmap，首先找本地文件，如果有直接使用，否则去网上获取
     * @param file file URL/file PATH
     * @param bitmap
     * @param quality
     * @throws HttpException 
     */
    public Bitmap safeGet(String file) throws HttpException {
        Bitmap bitmap = lookupFile(file); // first try file.
        
        if (bitmap != null) {
            synchronized (this) { // memory cache
                mCache.put(file, new SoftReference<Bitmap>(bitmap));
            }
            return bitmap;
        } else { //get from web
        	String url = file;
            bitmap = downloadImage2(url);
            
            // 注释掉以测试新的写入文件方法
            //put(file, bitmap); // file Cache
            return bitmap;
        }
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
        
        // 若图片过长, 则从中部截取
        if (height > maxHeight) {
            height = maxHeight;
            
        	int half_diff = (int)((originHeight - maxHeight)  / 2.0);
            bitmap = Bitmap.createBitmap(bitmap, 0, half_diff, width, height);
        }
        
//        Log.d(TAG, width + " width");
//        Log.d(TAG, height + " height");
        
        return bitmap;
    }

}
