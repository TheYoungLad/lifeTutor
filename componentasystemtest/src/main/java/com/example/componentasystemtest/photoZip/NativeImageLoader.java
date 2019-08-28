package com.example.componentasystemtest.photoZip;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;


import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.collection.LruCache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 本地图片加载器,采用的是异步解析本地图片，单例模式利用getInstance()获取NativeImageLoader实例
 * 调用loadNativeImage()方法加载本地图片，此类可作为一个加载本地图片的工具类
 */
public class NativeImageLoader {
    private LruCache<String, Bitmap> mMemoryCache;
    private static NativeImageLoader mInstance = new NativeImageLoader();
    private ExecutorService mImageThreadPool = Executors.newFixedThreadPool(1);


    private NativeImageLoader() {
        //获取应用程序的最大内存
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        //用最大内存的1/4来存储图片
        final int cacheSize = maxMemory / 4;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

            //获取每张图片的大小
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
    }

    /**
     * 通过此方法来获取NativeImageLoader的实例
     *
     * @return
     */
    public static NativeImageLoader getInstance() {
        return mInstance;
    }


    /**
     * 加载本地图片，对图片不进行裁剪
     *
     * @param path
     * @param mCallBack
     * @return
     */
    public Bitmap loadNativeImage(final String path, final NativeImageCallBack mCallBack) {
        return this.loadNativeImage(path, null, mCallBack);
    }

    /**
     * 此方法来加载本地图片，这里的mPoint是用来封装ImageView的宽和高，我们会根据ImageView控件的大小来裁剪Bitmap
     * 如果你不想裁剪图片，调用loadNativeImage(final String path, final NativeImageCallBack mCallBack)来加载
     *
     * @param path
     * @param mPoint
     * @param mCallBack
     * @return
     */
    public Bitmap loadNativeImage(final String path, final Point mPoint, final NativeImageCallBack mCallBack) {
        //先获取内存中的Bitmap
        Bitmap bitmap = getBitmapFromMemCache(path);

        /**
         * 设置
         */
        final Handler mHander = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mCallBack.onImageLoader((Bitmap) msg.obj, path);
            }

        };

        //若该Bitmap不在内存缓存中，则启用线程去加载网络的图片，并将Bitmap加入到mMemoryCache中
        if (bitmap == null) {
            mImageThreadPool.execute(new Runnable() {

                @Override
                public void run() {
                    //先获取图片的缩略图
                    Bitmap mBitmap = decodeThumbBitmapForNet(path,
                            mPoint == null ? 0 : mPoint.x, mPoint == null ? 0 : mPoint.y);
                    Message msg = mHander.obtainMessage();
                    msg.obj = mBitmap;
                    mHander.sendMessage(msg);

                    //将图片加入到内存缓存
                    addBitmapToMemoryCache(path, mBitmap);
                }
            });
        }
        return bitmap;

    }

    /**
     * 同样用来对文件进行压缩的实现,但是只能压缩图片占用的磁盘大小,不能改变占用的内存大小
     *
     * @param path
     * @param width
     * @param height
     * @return
     */
    public Bitmap decodeThumbBitmapForNet2(String path,
                                           int width, int height) {

        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url(path)
                .build();
        Response response = null;
        Bitmap bitmap = null;
        try {
            response = okHttpClient.newCall(request).execute();
            InputStream inputStream = response.body().byteStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
            bitmap = compressBitmap(bitmap, width, height);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;

    }

    private Bitmap compressBitmap(Bitmap bitmap, int width, int height) {
        float bitHeight = bitmap.getHeight();
        float bitWidth = bitmap.getWidth();

        float curentRate = 0f;

        if (bitHeight > height) {//图片较大 我们取较大的比例
            float rate = bitHeight / height;
            float rate1 = bitWidth / width;
            curentRate = Math.max(rate, rate1);
            bitHeight /= curentRate;
            bitWidth /= curentRate;

        } else if (bitHeight < height) { //图片较小,我们取较小的比例

            float rate = height / bitHeight;
            float rate1 = width / bitWidth;
            curentRate = Math.min(rate, rate1);
            bitHeight *= curentRate;
            bitWidth *= curentRate;
        }
        //下面的缩放算法只能实现磁盘占用的减小,但是不能是实现内存占用的减小
        return Bitmap.createScaledBitmap(bitmap,
                (int) bitWidth, (int) bitHeight, false);
    }


    /**
     * 往内存缓存中添加Bitmap
     *
     * @param key
     * @param bitmap
     */
    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null && bitmap != null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    /**
     * 根据key来获取内存中的图片
     *
     * @param key
     * @return
     */
    private Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }


    /**
     * 根据View(主要是ImageView)的宽和高来获取图片的缩略图
     * 这个从文件里面去解析
     *
     * @param path
     * @param viewWidth
     * @param viewHeight
     * @return
     */
    public Bitmap decodeThumbBitmapForFile(String path, int viewWidth, int viewHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //设置为true,表示解析Bitmap对象，该对象不占内存
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        //设置缩放比例
        options.inSampleSize = computeScale(options, viewWidth, viewHeight);

        //设置为false,解析Bitmap对象加入到内存中
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);

    }

    /**
     * 解析精简的bitmap从网络中
     * <p>
     * 下面的代码是解析网络数据的代码,尝试过很多次,本来按照google的写法
     * 使用BitmapFactory.decodeStream()但是一直null,无奈实现不了,最终按照下面的实现
     *
     * @param url
     * @param viewWidth
     * @param viewHeight
     * @return
     */
    public Bitmap decodeThumbBitmapForNet(String url, int viewWidth, int viewHeight) {

//        URL url1 = null;
        Bitmap bitmap = null;
        InputStream in = null;
        byte[] bytes;
        try {
//            url1 = new URL(url);
//            HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
//            conn.setDoInput(true);
//            conn.setRequestMethod("GET");
//            conn.connect();

            Request request = new Request.Builder().url(url).build();
            OkHttpClient okHttpClient = new OkHttpClient();
            Response response = okHttpClient.newCall(request).execute();
            in = new ByteArrayInputStream(bytes = response.body().bytes());

            BitmapFactory.Options options = new BitmapFactory.Options();
            //设置为true,表示解析Bitmap对象，该对象不占内存
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in,
                    null, options);
            //设置缩放比例
            options.inSampleSize = computeScale(options, viewWidth, viewHeight);
            //设置为false,解析Bitmap对象加入到内存中
            options.inJustDecodeBounds = false;
//            bitmap = BitmapFactory.decodeStream(in,
//                    null, options);

            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }


    //定义一个根据图片url获取InputStream的方法
    public static byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024]; // 用数据装
        int len = -1;
        while ((len = is.read(buffer)) != -1) {
            outstream.write(buffer, 0, len);
        }
        outstream.close();
        // 关闭流一定要记得。
        return outstream.toByteArray();
    }


    /**
     * 根据View(主要是ImageView)的宽和高来计算Bitmap缩放比例。默认不缩放
     *
     * @param options
     */
    private int computeScale(BitmapFactory.Options options, int viewWidth, int viewHeight) {
        int inSampleSize = 1;
        if (viewWidth == 0 || viewWidth == 0) {
            return inSampleSize;
        }
        int bitmapWidth = options.outWidth;
        int bitmapHeight = options.outHeight;

        //假如Bitmap的宽度或高度大于我们设定图片的View的宽高，则计算缩放比例
        if (bitmapWidth > viewWidth || bitmapHeight > viewWidth) {
            int widthScale = Math.round((float) bitmapWidth / (float) viewWidth);
            int heightScale = Math.round((float) bitmapHeight / (float) viewWidth);

            //为了保证图片不缩放变形，我们取宽高比例最小的那个
            inSampleSize = widthScale < heightScale ? widthScale : heightScale;
        }
        return inSampleSize;
    }


    /**
     * 加载本地图片的回调接口
     *
     * @author xiaanming
     */
    public interface NativeImageCallBack {
        /**
         * 当子线程加载完了本地的图片，将Bitmap和图片路径回调在此方法中
         *
         * @param bitmap
         * @param path
         */
        public void onImageLoader(Bitmap bitmap, String path);
    }
}
