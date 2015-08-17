/*
 * Copyright (C) 2012 The Android Open Source Project
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

package haipeng.myalbum.imageload;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.WeakReference;




import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import haipeng.myalbum.BuildConfig;
import haipeng.myalbum.Utils.Loger;


/**
 * This class wraps up completing some arbitrary long running work when loading
 * a bitmap to an ImageView. It handles things like using a memory and disk
 * cache, running the work in a background thread and setting a placeholder
 * image.
 */
/**
 * @author zhaohaitao
 *
 */
public abstract class ImageWorker {
	private static final String TAG = "ImageWorker";
	private static final int FADE_IN_TIME = 200;

	private ImageCache mImageCache;
	private ImageCache.ImageCacheParams mImageCacheParams;
	private Bitmap mLoadingBitmap;
	private boolean mFadeInBitmap = true;
	private boolean mExitTasksEarly = false;
	protected boolean mPauseWork = false;
	private final Object mPauseWorkLock = new Object();

	protected Resources mResources;

	private static final int MESSAGE_CLEAR = 0;
	private static final int MESSAGE_INIT_DISK_CACHE = 1;
	private static final int MESSAGE_FLUSH = 2;
	private static final int MESSAGE_CLOSE = 3;

	public interface ImgLoadCompleteListener {
		void onLoadComplete(ImageView imgView, Bitmap bitmap, String url);
	}
	private ImgLoadCompleteListener mLoadCompleteListener;
	
	protected ImageWorker(Context context) {
		mResources = context.getResources();
	}

	public void setImgLoadCompleteListener(ImgLoadCompleteListener listener){
		this.mLoadCompleteListener = listener;
	}
	
	private void notifyLoadComplete(String url, Bitmap bm, ImageView imgView){
		if(mLoadCompleteListener != null){
			mLoadCompleteListener.onLoadComplete(imgView, bm, url);
		}
	}
	
	/**
	 * 从缓存中取bitmap， 没有返回null， 加载图片需调用loadImage
	 */
	public Bitmap getBitmapFromMemoryCache(Object data){
		Bitmap bitmap = null;
		if (mImageCache != null) {
			bitmap = mImageCache.getBitmapFromMemCache(String.valueOf(data));
		}
		return bitmap;
	}
	
	/**
	 * 从磁盘中取bitmap， 没有返回null， 加载图片需调用loadImage
	 */
	public Bitmap getBitmapFromDiskCache(Object data){
		Bitmap bitmap = null;
		if (mImageCache != null) {
			bitmap = mImageCache.getBitmapFromDiskCache(String.valueOf(data));
		}
		if (bitmap != null && mImageCache != null) {
			mImageCache.addBitmapToCache(String.valueOf(data), bitmap);
		}
		return bitmap;
	}
	
	public void addBitmap2Cache(Object data, Bitmap bitmap){
		if(data == null || bitmap == null){
			return;
		}
		if(mImageCache != null){
			mImageCache.addBitmapToCache(String.valueOf(data), bitmap);
		}
	}
	
	/**
	 * Load an image specified by the data parameter into an ImageView (override
	 * {@link ImageWorker(Object)} to define the processing
	 * logic). A memory and disk cache will be used if an {@link ImageCache} has
	 * been set using {@link ImageWorker#setImageCache(ImageCache)}. If the
	 * image is found in the memory cache, it is set immediately, otherwise an
	 * {@link AsyncTask} will be created to asynchronously load the bitmap.
	 * 
	 * @param data
	 *            The URL of the image to download.
	 * @param imageView
	 *            The ImageView to bind the downloaded image to.
	 * @param loadImgViaMobile 
	 * 				移动网络连接下，是否加载图片，true表示加载
	 */
	public void loadImage(Object data, ImageView imageView, boolean loadImgViaMobile) {

		if(data == null || TextUtils.isEmpty(data.toString().trim())){
			if(mLoadingBitmap != null && imageView !=null)
				imageView.setImageBitmap(mLoadingBitmap);
			return;
		}
		
		Bitmap bitmap = null;

		if (mImageCache != null) {
			bitmap = mImageCache.getBitmapFromMemCache(String.valueOf(data));
		}

		if (bitmap != null) {
			// Bitmap found in memory cache
			notifyLoadComplete(String.valueOf(data), bitmap, imageView);
			imageView.setImageBitmap(bitmap);
		} else if (cancelPotentialWork(data, imageView)) {
			final BitmapWorkerTask task = new BitmapWorkerTask(imageView, loadImgViaMobile);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(mResources,
					mLoadingBitmap, task);
			imageView.setImageDrawable(asyncDrawable);

			// NOTE: This uses a custom version of AsyncTask that has been
			// pulled from the
			// framework and slightly modified. Refer to the docs at the top of
			// the class
			// for more info on what was changed.
			task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, data);
		}
	}

	/**
	 * Load an image specified by the data parameter into an ImageView (override
	 * {@link ImageWorker(Object)} to define the processing
	 * logic). A memory and disk cache will be used if an {@link ImageCache} has
	 * been set using {@link ImageWorker#setImageCache(ImageCache)}. If the
	 * image is found in the memory cache, it is set immediately, otherwise an
	 * {@link AsyncTask} will be created to asynchronously load the bitmap.
	 * 
	 * @param data
	 *            The URL of the image to download.
	 * @param imageView
	 *            The ImageView to bind the downloaded image to.
	 * @param loadImgViaMobile 
	 * 				移动网络连接下，是否加载图片，true表示加载
	 */
	public void loadImage(Object data, ImageView imageView, LoadingIndicatorView indicatorView,boolean loadImgViaMobile) {

		if(data == null || TextUtils.isEmpty(data.toString().trim())){
			if(mLoadingBitmap != null && imageView !=null)
				imageView.setImageBitmap(mLoadingBitmap);
			return;
		}
		
		Bitmap bitmap = null;

		if (mImageCache != null) {
			bitmap = mImageCache.getBitmapFromMemCache(String.valueOf(data));
		}
		
		if (bitmap != null) {
			// Bitmap found in memory cache
			notifyLoadComplete(String.valueOf(data), bitmap, imageView);
			imageView.setImageBitmap(bitmap);
			indicatorView.onProgressUpdate(100);
			indicatorView.setAsyncIndicator(null);
		} else if (cancelPotentialWork(data, imageView)) {
			final BitmapWorkerTask task = new BitmapWorkerTask(imageView, indicatorView,loadImgViaMobile);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(mResources,
					mLoadingBitmap, task);
			imageView.setImageDrawable(asyncDrawable);
			
			final AsyncIndicator asyncIndicator = new AsyncIndicator(task);
			indicatorView.setAsyncIndicator(asyncIndicator);
			
			// NOTE: This uses a custom version of AsyncTask that has been
			// pulled from the
			// framework and slightly modified. Refer to the docs at the top of
			// the class
			// for more info on what was changed.
			task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, data);
		}
	}
	
	/**
	 * Set placeholder bitmap that shows when the the background thread is
	 * running.
	 * 
	 * @param bitmap
	 */
	public void setLoadingImage(Bitmap bitmap) {
		mLoadingBitmap = bitmap;
	}

	/**
	 * Set placeholder bitmap that shows when the the background thread is
	 * running.
	 * 
	 * @param resId
	 */
	public void setLoadingImage(int resId) {
		mLoadingBitmap = BitmapFactory.decodeResource(mResources, resId);
	}

	/**
	 * Adds an {@link ImageCache} to this worker in the background (to prevent
	 * disk access on UI thread).
	 * 
	 * @param fragmentManager
	 * @param cacheParams
	 */
	public void addImageCache(FragmentManager fragmentManager,
			ImageCache.ImageCacheParams cacheParams) {
		mImageCacheParams = cacheParams;
		setImageCache(ImageCache.findOrCreateCache(fragmentManager,
				mImageCacheParams));
		new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
	}

	public void addImageCache(ImageCache.ImageCacheParams cacheParams) {
		mImageCacheParams = cacheParams;
		setImageCache(new ImageCache(cacheParams));
		new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
	}

	/**
	 * Sets the {@link ImageCache} object to use with this ImageWorker. Usually
	 * you will not need to call this directly, instead use
	 * {@link ImageWorker#addImageCache} which will create and add the
	 * {@link ImageCache} object in a background thread (to ensure no disk
	 * access on the main/UI thread).
	 * 
	 * @param imageCache
	 */
	public void setImageCache(ImageCache imageCache) {
		mImageCache = imageCache;
	}

	/**
	 * If set to true, the image will fade-in once it has been loaded by the
	 * background thread.
	 */
	public void setImageFadeIn(boolean fadeIn) {
		mFadeInBitmap = fadeIn;
	}

	public void setExitTasksEarly(boolean exitTasksEarly) {
		mExitTasksEarly = exitTasksEarly;
	}

	/**
	 * Subclasses should override this to define any processing or work that
	 * must happen to produce the final bitmap. This will be executed in a
	 * background thread and be long running. For example, you could resize a
	 * large bitmap here, or pull down an image from the network.
	 * 
	 * @param data
	 *            The data to identify which image to process, as provided by
	 *            {@link ImageWorker(Object, ImageView)}
	 * @return The processed bitmap
	 */
	protected abstract Bitmap processBitmap(Object data, BitmapWorkerTask bitmapworkerTask);

	/**
	 * Cancels any pending work attached to the provided ImageView.
	 * 
	 * @param imageView
	 */
	public static void cancelWork(ImageView imageView) {
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
		if (bitmapWorkerTask != null) {
			bitmapWorkerTask.cancel(true);
			if (BuildConfig.DEBUG) {
				final Object bitmapData = bitmapWorkerTask.data;
				Log.d(TAG, "cancelWork - cancelled work for " + bitmapData);
			}
		}
	}

	/**
	 * Returns true if the current work has been canceled or if there was no
	 * work in progress on this image view. Returns false if the work in
	 * progress deals with the same data. The work is not stopped in that case.
	 */
	public static boolean cancelPotentialWork(Object data, ImageView imageView) {
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {
			final Object bitmapData = bitmapWorkerTask.data;
			if (bitmapData == null || !bitmapData.equals(data)) {
				bitmapWorkerTask.cancel(true);
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "cancelPotentialWork - cancelled work for "
							+ data);
				}
			} else {
				// The same work is already in progress.
				return false;
			}
		}
		return true;
	}

	/**
	 * @param imageView
	 *            Any imageView
	 * @return Retrieve the currently active work task (if any) associated with
	 *         this imageView. null if there is no such task.
	 */
	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}

	/**
	 * The actual AsyncTask that will asynchronously process the image.
	 */
	protected class BitmapWorkerTask extends AsyncTask<Object, Integer, Bitmap> {
		private Object data;
		/* 下载进度跨度达到该值时通知 */
		private int publishInterval; 
		private boolean needNotify;
		private final WeakReference<ImageView> imageViewReference;
		private final WeakReference<LoadingIndicatorView> loadingIndicatorViewRef;
		/** 是否在移动网络下加载图片*/
		private boolean shouldLoadInMobile;
		
		public BitmapWorkerTask(ImageView imageView, boolean shouldLoadInMobile) {
			imageViewReference = new WeakReference<ImageView>(imageView);
			loadingIndicatorViewRef = null;
			needNotify = false;
			this.shouldLoadInMobile = shouldLoadInMobile;
			this.publishInterval = 1000;
		}

		public BitmapWorkerTask(ImageView imageView, LoadingIndicatorView indicatorView, boolean shouldLoadInMobile) {
			imageViewReference = new WeakReference<ImageView>(imageView);
			loadingIndicatorViewRef = new WeakReference<LoadingIndicatorView>(indicatorView);
			this.needNotify = true;
			this.shouldLoadInMobile = shouldLoadInMobile;
			this.publishInterval = 5;
		}
		
		public int getPublishInterval() {
			return publishInterval;
		}

		public boolean isNeedNotify() {
			return needNotify;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			int progress = values[0];
			if(progress != -1){
				final LoadingIndicatorView indicatorView = getAttachedIndicatorView();
				if(indicatorView != null){
					indicatorView.onProgressUpdate(progress);
				}
			}
		}
		
		/**
		 * Background processing.
		 */
		@Override
		protected Bitmap doInBackground(Object... params) {
			if (BuildConfig.DEBUG) {
//				Log.d(TAG, "doInBackground - starting work");
			}

			data = params[0];
			final String dataString = String.valueOf(data);
			Bitmap bitmap = null;

			// Wait here if work is paused and the task is not cancelled
			synchronized (mPauseWorkLock) {
				while (mPauseWork && !isCancelled()) {
					try {
						mPauseWorkLock.wait();
					} catch (InterruptedException e) {
					}
				}
			}

			// If the image cache is available and this task has not been
			// cancelled by another
			// thread and the ImageView that was originally bound to this task
			// is still bound back
			// to this task and our "exit early" flag is not set then try and
			// fetch the bitmap from
			// the cache
			if (mImageCache != null && !isCancelled()
					&& getAttachedImageView() != null && !mExitTasksEarly) {
//				bitmap = mImageCache.getBitmapFromDiskCache(dataString);
				Loger.i("not found in memcache, decode from file:" + dataString);
//				bitmap = ImageUtil.getBitmapByBytes(dataString);
				try {
					InputStream inputStream = new FileInputStream(dataString);
					if (inputStream != null) {
						bitmap = BitmapFactory.decodeStream(inputStream);
                    }
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}

			// If the bitmap was not found in the cache and this task has not
			// been cancelled by
			// another thread and the ImageView that was originally bound to
			// this task is still
			// bound back to this task and our "exit early" flag is not set,
			// then call the main
			// process method (as implemented by a subclass)
//			if (bitmap == null && !isCancelled()
//					&& getAttachedImageView() != null && !mExitTasksEarly) {
//				if(!shouldLoadInMobile){
//					if(!PhoneStateUtil.isUsingMobileConnect(BaseApplication.getContext())){
//						bitmap = processBitmap(params[0], this);
//					}else{
//						Log.i(TAG, "mobile状态下不加载图片");
//					}
//				}else{
//					bitmap = processBitmap(params[0], this);
//				}
//			}

			// If the bitmap was processed and the image cache is available,
			// then add the processed
			// bitmap to the cache for future use. Note we don't check if the
			// task was cancelled
			// here, if it was, and the thread is still running, we may as well
			// add the processed
			// bitmap to our cache as it might be used again in the future
			if (bitmap != null && mImageCache != null) {
				mImageCache.addBitmapToCache(dataString, bitmap);
			}

			if (BuildConfig.DEBUG) {
//				Log.d(TAG, "doInBackground - finished work");
			}

			return bitmap;
		}

		/**
		 * Once the image is processed, associates it to the imageView
		 */
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			// if cancel was called on this task or the "exit early" flag is set
			// then we're done
			if (isCancelled() || mExitTasksEarly) {
				bitmap = null;
			}

			final ImageView imageView = getAttachedImageView();
			if (bitmap != null && imageView != null) {
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "onPostExecute - setting bitmap");
				}
				notifyLoadComplete(String.valueOf(data), bitmap, imageView);
				setImageBitmap(imageView, bitmap);
			}
			
			final LoadingIndicatorView indicatorView = getAttachedIndicatorView();
			if(indicatorView != null){
				if(bitmap != null){
					indicatorView.onProgressUpdate(100);
				}else{
					indicatorView.onProgressUpdate(-1);
				}
				indicatorView.setAsyncIndicator(null);
			}
		}

		@Override
		protected void onCancelled(Bitmap bitmap) {
			super.onCancelled(bitmap);
			synchronized (mPauseWorkLock) {
				mPauseWorkLock.notifyAll();
			}
		}

		/**
		 * Returns the ImageView associated with this task as long as the
		 * ImageView's task still points to this task as well. Returns null
		 * otherwise.
		 */
		private ImageView getAttachedImageView() {
			final ImageView imageView = imageViewReference.get();
			final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

			if (this == bitmapWorkerTask) {
				return imageView;
			}

			return null;
		}
		
		private LoadingIndicatorView getAttachedIndicatorView(){
			if(loadingIndicatorViewRef != null){
				final LoadingIndicatorView indicatorView = loadingIndicatorViewRef.get();
				if(indicatorView != null){
					final AsyncIndicator indicator = indicatorView.getAsyncIndicator();
					if(indicator != null){
						final BitmapWorkerTask bitmapWorkerTask = indicator.getBitmapWorkerTask();
						if (this == bitmapWorkerTask) {
							return indicatorView;
						}
					}
				}
			}
			return null;
		}
	}

	/**
	 * A custom Drawable that will be attached to the imageView while the work
	 * is in progress. Contains a reference to the actual worker task, so that
	 * it can be stopped if a new binding is required, and makes sure that only
	 * the last started worker process can bind its result, independently of the
	 * finish order.
	 */
	private static class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

		public AsyncDrawable(Resources res, Bitmap bitmap,
				BitmapWorkerTask bitmapWorkerTask) {
			super(res, bitmap);
			bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(
					bitmapWorkerTask);
		}

		public BitmapWorkerTask getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

	static class AsyncIndicator {
		private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

		public AsyncIndicator(BitmapWorkerTask bitmapWorkerTask) {
			bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(
					bitmapWorkerTask);
		}
		
		public BitmapWorkerTask getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

	/**
	 * Called when the processing is complete and the final bitmap should be set
	 * on the ImageView.
	 * 
	 * @param imageView
	 * @param bitmap
	 */
	@SuppressWarnings("deprecation")
	private void setImageBitmap(ImageView imageView, Bitmap bitmap) {
		if (mFadeInBitmap) {
			// Transition drawable with a transparent drwabale and the final
			// bitmap
			final TransitionDrawable td = new TransitionDrawable(
					new Drawable[] {
							new ColorDrawable(Color.TRANSPARENT),
							new BitmapDrawable(mResources, bitmap) });
			// Set background to loading bitmap
			imageView.setBackgroundDrawable(new BitmapDrawable(mResources,
					mLoadingBitmap));
			
			imageView.setImageDrawable(td);
			td.startTransition(FADE_IN_TIME);
		} else {
			imageView.setImageBitmap(bitmap);
		}
	}

	public void setPauseWork(boolean pauseWork) {
		synchronized (mPauseWorkLock) {
			mPauseWork = pauseWork;
			if (!mPauseWork) {
				mPauseWorkLock.notifyAll();
			}
		}
	}

	protected class CacheAsyncTask extends AsyncTask<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... params) {
			switch ((Integer) params[0]) {
			case MESSAGE_CLEAR:
				clearCacheInternal();
				break;
			case MESSAGE_INIT_DISK_CACHE:
				initDiskCacheInternal();
				break;
			case MESSAGE_FLUSH:
				flushCacheInternal();
				break;
			case MESSAGE_CLOSE:
				closeCacheInternal();
				break;
			}
			return null;
		}
	}

	protected void initDiskCacheInternal() {
		if (mImageCache != null) {
			mImageCache.initDiskCache();
		}
	}

	protected void clearCacheInternal() {
		if (mImageCache != null) {
			mImageCache.clearCache();
		}
	}

	protected void flushCacheInternal() {
		if (mImageCache != null) {
			mImageCache.flush();
		}
	}

	protected void closeCacheInternal() {
		if (mImageCache != null) {
			mImageCache.close();
			mImageCache = null;
		}
	}

	public void clearCache() {
		new CacheAsyncTask().execute(MESSAGE_CLEAR);
	}

	public void flushCache() {
		new CacheAsyncTask().execute(MESSAGE_FLUSH);
	}

	public void closeCache() {
		new CacheAsyncTask().execute(MESSAGE_CLOSE);
	}
}
