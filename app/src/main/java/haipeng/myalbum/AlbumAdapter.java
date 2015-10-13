package haipeng.myalbum;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.Pools;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import haipeng.myalbum.entity.Album;
import haipeng.myalbum.imageload.ImageFetcher;

/**
 * Created by Administrator on 2015/8/14.
 */
public class AlbumAdapter extends BaseAdapter implements AbsListView.OnScrollListener{


    Context mContext;
    List<Album> mList;
    ImageFetcher mImageFetcher;
//    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);
    ExecutorService executorService ;
    int firstVisibleItem;
    int visibleItemCount;
    int totalItemCount;
    boolean isFirst = true;
    MyGirdView myGirdView;
    Map<Integer,String> pathMap = new HashMap<Integer,String>();
    private LruCache<String,Bitmap> mMemoryCache;
    public AlbumAdapter(Context context, List<Album> list,ImageFetcher imageFetcher,MyGirdView girdView,LruCache<String,Bitmap> memoryCache){
        mList = list;
        mList.add(getButtonAlbum());
        mContext = context;
        mImageFetcher = imageFetcher;
        girdView.setOnScrollListener(this);
        myGirdView = girdView;
        getExecutorService();
        mMemoryCache = memoryCache;
        Log.i("tag","AlbumAdapter Construct Fanction");
    }
    public Album getButtonAlbum(){
        Album album = new Album();
        album.setImage_name("");
        album.setIsParent("");
        album.setIsChild("");
        album.setFolderPath("");
        return album;
    }
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.item_album,null);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.item_album_iv);
        TextView  textView  = (TextView) convertView.findViewById(R.id.item_album_tv);
        Button    btn       = (Button) convertView.findViewById(R.id.item_album_btn);
        String image_name = mList.get(position).getImage_name();
        String isParent   = mList.get(position).getIsParent();
        String isChild    = mList.get(position).getIsChild();
        String image_folder_path = mList.get(position).getFolderPath();
        String imagePath = image_folder_path+File.separator+image_name;

        imageView.setOnClickListener((EntranceActivity) mContext);
        imageView.setTag(imagePath);
//        Log.i("tag", "position=" + position);
//        Log.i("tag","imagePath="+imagePath);
        if(!"".equals(image_folder_path)&&!"".equals(image_name)) {
//            mImageFetcher.loadImage(image_folder_path+ File.separator+image_name, imageView, true);
            if(!pathMap.containsKey(position))
            pathMap.put(position,imagePath);
//            Log.i("tag","position="+position);
//            Log.i("tag","imagePath="+imagePath);
            if(executorService!=null) {
                getBitmap(imagePath, imageView, position);
            }
            else {
                imageView.setImageBitmap(null);
            }
            textView.setText(image_name);
        }
        if(position == mList.size()-1)
        {
            btn.setVisibility(View.VISIBLE);
            btn.setOnClickListener((EntranceActivity)mContext);
        }

        return convertView;
    }

    public ExecutorService getExecutorService(){
        if(executorService == null)
        {
            synchronized (ExecutorService.class)
            {
                if(executorService == null)
                {
                    executorService = Executors.newFixedThreadPool(3);
                }
            }
        }
        return executorService;
    }
    public void getBitmap(String imagePath, final ImageView imageView, final int position){
        final String path = imagePath;

        getExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                System.out.println("executor ...");
                BitmapFactory.Options options = new BitmapFactory.Options();

                options.inSampleSize = 16;

                Bitmap bitmap = null;

                try {
                     bitmap = mMemoryCache.get(path);
                     if(bitmap!=null)
                     {
                         options.inBitmap = bitmap;
                     }
                }catch (Exception e){
                    e.printStackTrace();
                }

                if(bitmap == null) {
                    bitmap = BitmapFactory.decodeFile(path, options);
                    mMemoryCache.put(path, bitmap);
                }
                imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(mMemoryCache.get(path));
                    }
                });

            }
        });
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
       //SCROLL_STATE_IDLE静止时
        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
        {


                 for (int i=firstVisibleItem;i<firstVisibleItem + visibleItemCount;i++) {
                     Log.i("tag", "i" + i);
                     String path = pathMap.get(i);
                     Log.i("tag", "path" + path);
                     final ImageView imageView = (ImageView) myGirdView.findViewWithTag(path);
                     getBitmap(path, imageView, i);

             }

        }else{

                if(executorService!=null) {
                    executorService.shutdownNow();
                    executorService = null;
                }

        }
    }

    //listview滚动的时候调用此方法，刚开始显示view也调用此方法
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.firstVisibleItem = firstVisibleItem;
        this.visibleItemCount = visibleItemCount;
        this.totalItemCount = totalItemCount;
//        Log.i("tag","firstVisibleItem"+firstVisibleItem);
//        Log.i("tag","visibleItemCount"+visibleItemCount);
//        Log.i("tag","totalItemCount"+totalItemCount);
//        Log.i("tag","onScroll");
        if(isFirst&&visibleItemCount>0)
        {
            isFirst = false;
        }
    }
}
