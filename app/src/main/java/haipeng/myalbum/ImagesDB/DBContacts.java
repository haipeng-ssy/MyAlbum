package haipeng.myalbum.ImagesDB;

import java.util.ArrayList;
import java.util.List;

import haipeng.myalbum.Utils.Version;

public class DBContacts {

	public final static String DBNAME = "my_images.db";
	public final static int DBVERSION= Version.getVersionCode();
	public static List<String> createTableSqls = null;
	public final static String MYALBUMTABLENAME = "imageTable";
	public final static String dropMyAlbumTableSql = "DROP TABLE IF EXISTS "+MYALBUMTABLENAME;
	public final static String createMyALbumTableSql = "CREATE TABLE IF NOT EXISTS "+MYALBUMTABLENAME+" ( "+
	"_id INTEGER PRIMARY KEY autoincrement, "+
    "image_name TEXT NOT NULL,"+
	"isParent TEXT NOT NULL,"+
    "isChild TEXT NOT NULL,"+
	"folderPath TEXT NOT NULL );";

	static{
		createTableSqls = new ArrayList<String>();

		createTableSqls.add(dropMyAlbumTableSql);
		createTableSqls.add(createMyALbumTableSql);
	}
	
}
