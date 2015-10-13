package haipeng.myalbum.ImagesDB;

import java.io.File;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class OutAppDatabaseContext extends ContextWrapper{

	String mDbFolderPath;
	public OutAppDatabaseContext(Context base,String dbFolderPath) {
		super(base);
		mDbFolderPath = dbFolderPath;
	}
	
	
	
	@Override
	public File getDatabasePath(String name) {
		File file = new File(mDbFolderPath+File.separator+name);
		if(!file.getParentFile().exists())
		{
			file.getParentFile().mkdirs();
		}
		return file;
	}



	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode,
			CursorFactory factory) {
		return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory);
	}
	
	@SuppressLint("NewApi")
	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode,
			CursorFactory factory, DatabaseErrorHandler errorHandler) {
		return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name).getAbsolutePath(), factory, errorHandler);
	}
	
	

}
