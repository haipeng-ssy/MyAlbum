/**
 * Purport:入口Activity
 * Author:sunyiyan
 * */
package haipeng.myalbum;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import haipeng.myalbum.App.Constacts;
import haipeng.myalbum.ImagesDB.DBExecuteController;
import haipeng.myalbum.entity.Album;
import haipeng.myalbum.imageload.ImageCache;
import haipeng.myalbum.imageload.ImageFetcher;

public class EntranceActivity extends AppCompatActivity implements View.OnClickListener{

    private MyGirdView myGirdView;
    private AlbumAdapter mAlbumAdapter;
    private ImageFetcher mImgFetcher = null;

    private List<Album> mAlbums;
    private DBExecuteController mDBExecuteController;
    private String LOCALTEMPFILE ="localtemp";
    private String LOGCALTEMPFILENAME ="tempName.jpg";
    private LruCache mLruCache;
    Thread thread1 ;
    Thread thread2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance);
        Log.i("tag", "EntranceActivity onCreate()");
        myGirdView = (MyGirdView) findViewById(R.id.id_myGirdView);
        initImgCache();
        int maxMemory = (int) (Runtime.getRuntime().maxMemory()/1024);//KB
        int cacheSize = maxMemory/8;
        Log.i("tag","cacheSize MB ="+cacheSize/1024);
        mLruCache = new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount()/1024;
            }
        };
        mDBExecuteController = new DBExecuteController();
        mAlbums = mDBExecuteController.getListAlbum();
        mAlbumAdapter = new AlbumAdapter(this,mAlbums,mImgFetcher,myGirdView,mLruCache);
        myGirdView.setAdapter(mAlbumAdapter);

    }


    public Album createAlbum(String image_name,String isParent,String isChild,String folderPath){
        Album album = new Album();
        album.setImage_name(image_name);
        album.setFolderPath(folderPath);
        album.setIsParent(isParent);
        album.setIsChild(isChild);
        return album;
    }
    public void addImage(List<Album> mAlbums,Album album){
        mAlbums.remove(mAlbums.size()-1);
        mAlbums.add(album);
        if(mImgFetcher==null)
            initImgCache();
        AlbumAdapter albumAdapter = new AlbumAdapter(EntranceActivity.this,mAlbums,mImgFetcher,myGirdView,mLruCache);
        myGirdView.setAdapter(albumAdapter);
        albumAdapter.notifyDataSetChanged();
    }
    private void initImgCache() {
        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(
                EntranceActivity.this, "haipeng");
        cacheParams.setMemCacheSizePercent(this, 0.4f);
        mImgFetcher = new ImageFetcher(this, 500);
        mImgFetcher.addImageCache(cacheParams);
        mImgFetcher.setImageFadeIn(false);
        mImgFetcher.setLoadingImage(R.drawable.icon_default);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.item_album_btn:
                OpenOriginCamera();
                break;
            case R.id.item_album_iv:
                Intent intent = new Intent(this,ShowImageActivity.class);
                intent.putExtra("imagePath",v.getTag().toString());
                startActivity(intent);
                break;
        }
    }

    public void OpenOriginCamera(){
        //保存的是缩率图
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(intent,CommonData.OpenCamera);
        //保存的是原图

        thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                String status = Environment.getExternalStorageState();
                if(status.equals(Environment.MEDIA_MOUNTED))
                {
                    try{
                        File dir = new File(Environment.getExternalStorageDirectory()+File.separator+LOCALTEMPFILE);
                        if(!dir.exists()) dir.mkdirs();

                        File f = new File(dir,LOGCALTEMPFILENAME);
                        Uri u  = Uri.fromFile(f);
                        intent.putExtra(MediaStore.Images.Media.ORIENTATION,0);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,u);
//                startActivityForResult(intent,CommonData.OpenCamera);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(EntranceActivity.this,"没有外部存储设备",Toast.LENGTH_LONG).show();
                }
                startActivityForResult(intent,CommonData.OpenCamera);
            }
        });
        thread1.start();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=RESULT_OK)
            return;
        switch (requestCode)
        {
            case CommonData.OpenCamera:
                if(resultCode== Activity.RESULT_OK)
                {
                    //获取的是缩率图
//                    Bundle bundle = data.getExtras();
//                    Bitmap bitmap = (Bitmap) bundle.get("data");
                    //获取的是原图

                    thread2 = new Thread(new Runnable() {
                        @Override
                        public void run() {


                    File file = new File(Environment.getExternalStorageDirectory()+File.separator+LOCALTEMPFILE+File.separator+LOGCALTEMPFILENAME);
                    try {
                        Uri u = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), null, null));
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                    ImageFile imageFile = new ImageFile();
                    imageFile.initMyAlbumFolder();
                    String root_path = imageFile.getPath_MyAlbum();
                    final String name  = System.currentTimeMillis()+".jpg";
                    final String file_path = root_path+File.separator+name;

                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(new File(file_path));
                        byte[] bytes = new byte[2048];
                        BufferedInputStream bi = new BufferedInputStream(new FileInputStream(file));
                        int read;
                        while((read=bi.read(bytes))!=-1)
                        {
                            fileOutputStream.write(bytes,0,read);
                        }
                        file.delete();
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                         String isParent = "0";
                         String isChild  = "1";
                        if(root_path.equals(Constacts.MyAlbumImagePath))
                        {
                            isParent = "1";
                            isChild  = "0";
                        }
                        final String isParentf = isParent;
                        final String isChildf  = isChild;
                        final String root_pathf = root_path;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addImage(mAlbums, createAlbum(name, isParentf, isChildf, root_pathf));
                            }
                        });

                        DBExecuteController dbExecute = new DBExecuteController();
                        ContentValues cv = new ContentValues();
                        cv.put("image_name",name);
                        cv.put("isParent", isParent);
                        cv.put("isChild",isChild);
                        cv.put("folderPath",root_path);
                        dbExecute.insertImage(cv);

                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    }
                });
                    thread2.start();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_entrance, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {


        mImgFetcher.flushCache();
        super.onDestroy();
    }

    //拍照遇到的其他问题
//    别高兴太早了，还有关键问题：
//    上面的代码只要你会用google，一般都能找来拼凑一番。但是，仅仅是这样，你还会得到未知错误……Samsung的系统相机，版式是横板的，如果你的activity恰巧是竖版的，那么获取这个回调uri的时候，很可能为空！
//    原因在于，如果你没有设置版式改变的时候，activity不要调用onCreate方法！这就是要命的地方！
//    设置方法其实很简单：
//            1、在Manfest.xml中，给activity添加一个属性：android:configChanges="orientation|keyboardHidden"
//            2、在activity中添加：
//    @Override
//    public void onConfigurationChanged(Configuration config) {
//        super.onConfigurationChanged(config);
//    }
//    另外还有一个蛋疼的错误容易犯：千万别给这个activity添加单例模式，即：android:launchMode="singleInstance"
//
//    好了，完成这些，你就终于可以不“折疼”了！ good luck!
//
//    照相得到的图片，最好自己指定路径，这样返回数据时，就不用从Intent中获取了，我们知道是什么路径，可以直接去拿。
//

}
