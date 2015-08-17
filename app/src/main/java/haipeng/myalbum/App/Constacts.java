package haipeng.myalbum.App;

import android.os.Environment;

import java.io.File;

/**
 * Created by Administrator on 2015/8/17.
 */
public class Constacts {
    public static String AppFolderPath;
    public static String MyAlbumDBPath;
    public static String MyAlbumImagePath;
    static{
        AppFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"MyAlbum";
        MyAlbumDBPath = AppFolderPath+File.separator+"db";
        MyAlbumImagePath=AppFolderPath+File.separator+"Images";

        File file = new File(AppFolderPath);
        if(!file.exists())
        {
            file.mkdirs();
        }

        File db_file = new File(MyAlbumDBPath);
        if(!db_file.exists())
        {
            db_file.mkdirs();
        }

        File images_file = new File(MyAlbumImagePath);
        if(!images_file.exists())
        {
            images_file.mkdirs();
        }

    }

}
