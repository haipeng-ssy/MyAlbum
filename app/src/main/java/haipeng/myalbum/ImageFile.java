package haipeng.myalbum;

import android.os.Environment;

import java.io.File;

import haipeng.myalbum.App.Constacts;
import haipeng.myalbum.ImagesDB.DBContacts;

/**
 * Created by Administrator on 2015/8/14.
 */
public class ImageFile {

    public ImageFile(){

    }
    public void initMyAlbumFolder(){
        File file = new File(Constacts.AppFolderPath);
        if(!file.exists())
        {
            file.mkdirs();
        }

        File myImage_file = new File(Constacts.MyAlbumImagePath);
        if(!myImage_file.exists())
        {
            myImage_file.mkdirs();
        }
    }
    public String getPath_MyAlbum(){
        return Constacts.MyAlbumImagePath;
    }

}
