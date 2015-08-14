package haipeng.myalbum;

import android.os.Environment;

import java.io.File;

/**
 * Created by Administrator on 2015/8/14.
 */
public class ImageFile {

    private String Path_MyAlbum=Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"MyAlbum";
    public ImageFile(){

    }
    public void initMyAlbumFolder(){
        File file = new File(Path_MyAlbum);
        if(!file.exists())
        {
            file.mkdirs();
        }
    }
    public String getPath_MyAlbum(){
        return Path_MyAlbum;
    }

}
