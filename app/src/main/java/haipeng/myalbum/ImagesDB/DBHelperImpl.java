package haipeng.myalbum.ImagesDB;


import android.database.sqlite.SQLiteDatabase;

import haipeng.myalbum.Utils.Version;

public class DBHelperImpl extends DBHelper {

	public DBHelperImpl() {
		super(DBContacts.DBNAME, DBContacts.DBVERSION, DBContacts.createTableSqls);
	}

	@Override
	protected void onUpgradeDB(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion< Version.getVersionCode())
		{
			
		}
	}

	@Override
	protected void InitDB(SQLiteDatabase db) {
	}

}
