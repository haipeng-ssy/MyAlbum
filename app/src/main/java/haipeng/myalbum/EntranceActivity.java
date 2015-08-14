/**
 * Purport:入口Activity
 * Author:sunyiyan
 * */
package haipeng.myalbum;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import haipeng.myalbum.entity.Album;
import haipeng.myalbum.imageload.ImageCache;
import haipeng.myalbum.imageload.ImageFetcher;

public class EntranceActivity extends AppCompatActivity implements View.OnClickListener{

    private MyGirdView myGirdView;
    private AlbumAdapter mAlbumAdapter;
    private ImageFetcher mImgFetcher = null;

    private List<Album> mAlbums;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance);
        myGirdView = (MyGirdView) findViewById(R.id.id_myGirdView);
        initImgCache();
        mAlbums = new ArrayList<Album>();
        mAlbumAdapter = new AlbumAdapter(this,mAlbums,mImgFetcher);
        myGirdView.setAdapter(mAlbumAdapter);
    }

    public Album createAlbum(String path,String name){
        Album album = new Album();
        album.setName(name);
        album.setPath(path);
        return album;
    }
    public void addImage(List<Album> mAlbums,Album album){
        mAlbums.remove(mAlbums.size()-1);
        mAlbums.add(album);
        if(mImgFetcher==null)
            initImgCache();
        AlbumAdapter albumAdapter = new AlbumAdapter(this,mAlbums,mImgFetcher);
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
        }
    }

    public void OpenOriginCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,CommonData.OpenCamera);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case CommonData.OpenCamera:
                if(resultCode== Activity.RESULT_OK)
                {
                    Bundle bundle = data.getExtras();
                    Bitmap bitmap = (Bitmap) bundle.get("data");

                    ImageFile imageFile = new ImageFile();
                    imageFile.initMyAlbumFolder();
                    String root_path = imageFile.getPath_MyAlbum();
                    String name  = System.currentTimeMillis()+".jpg";
                    String file_path = root_path+File.separator+name;

                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(new File(file_path));
                        bitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);
                        addImage(mAlbums,createAlbum(file_path,name));
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
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
}
