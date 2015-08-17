package haipeng.myalbum.ImagesDB;

import android.database.SQLException;

public class DBException extends Exception {
    public DBException(SQLException e){
        e.printStackTrace();    	
    }
}
