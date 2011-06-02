package com.ch_linghu.fanfoudroid.photo;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class PhotoActivity extends Activity {
    private static final String TAG = "PhotoActivity";

    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.photo);
        setContentView(new MyView(this));

        /*
         * mImageView = (ImageView) findViewById(R.id.photo_test);
         * 
         * File sdcard = Environment.getExternalStorageDirectory(); File image =
         * new File(sdcard.getAbsolutePath() + "/test.jpg"); if
         * (!image.exists()) { Log.e(TAG, "photo is missing"); }
         * 
         * Bitmap bm = BitmapFactory.decodeFile(image.getAbsolutePath());
         * 
         * mImageView.setImageBitmap(bm);
         */

        // Canvas c = new Canvas(bm);

    }

    class MyView extends View {
        private Paint mPaint;
        private MaskFilter mBlur;

        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Paint mBitmapPaint;

        public MyView(Context c) {
            super(c);
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setColor(0xFFFF0000);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(12);
            mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
            mPaint.setMaskFilter(mBlur);
            
            Bitmap bitmap = getPic();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());   
            
            mBitmap = Bitmap.createBitmap(320, 480, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            
            mCanvas.drawBitmap(bitmap, rect, rect,  mPaint);
            
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        }

        private Bitmap getPic() {
            File sdcard = Environment.getExternalStorageDirectory();
            File image = new File(sdcard.getAbsolutePath() + "/test.jpg");
            if (!image.exists()) {
                Log.e(TAG, "photo is missing");
            }

            return BitmapFactory.decodeFile(image.getAbsolutePath());
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFAAAAAA);

            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

            canvas.drawPath(mPath, mPaint);
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }
        }

        private void touch_up() {
            mPath.lineTo(mX, mY);
            // commit the path to our offscreen
            mCanvas.drawPath(mPath, mPaint);
            // kill this so we don't double draw
            mPath.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
            }
            return true;
        }
    }

}
