package haipeng.myalbum.ImagesDB;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;

import java.util.ArrayList;
import java.util.List;

import haipeng.myalbum.entity.Album;


/**
 * Created by Administrator on 2015/8/19.
 */
public class DBExecuteController {
    DBHelperImpl dbHelperImpl=null;
    public DBExecuteController(){
        if(dbHelperImpl==null)
        dbHelperImpl = new DBHelperImpl();//会重新检查db是否获取了
    }
    public void insertImage(ContentValues cv){
          dbHelperImpl.beginTransaction();
          try {
              dbHelperImpl.insert(DBContacts.MYALBUMTABLENAME, cv);
              dbHelperImpl.setTransactionSuccessful();
          }catch (Exception e)
          {
              new DBException(new SQLException("insert error"));
          }finally {
              dbHelperImpl.endTransaction();
          }
    }
    public List<Album> getListAlbum(){
        List<Album> list = new ArrayList<Album>();

        Cursor cursor = dbHelperImpl.query("select * from "+DBContacts.MYALBUMTABLENAME+" where isParent = '1';");
        //+"where isParent = '1';"
        while (cursor.moveToNext())
        {
            String image_name = cursor.getString(cursor.getColumnIndex("image_name"));
            String isParent = cursor.getString(cursor.getColumnIndex("isParent"));
            String isChild = cursor.getString(cursor.getColumnIndex("isChild"));
            String folderPath = cursor.getString(cursor.getColumnIndex("folderPath"));

            Album album = new Album();
            album.setImage_name(image_name);
            album.setIsParent(isParent);
            album.setIsChild(isChild);
            album.setFolderPath(folderPath);

            list.add(album);

        }
        return list;
    }
}
