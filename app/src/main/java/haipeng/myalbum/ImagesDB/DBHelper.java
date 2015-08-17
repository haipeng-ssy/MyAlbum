package haipeng.myalbum.ImagesDB;

import java.io.File;
import java.util.List;


import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class DBHelper extends SQLiteOpenHelper {

	SQLiteDatabase db;
	static String dbFolderPath;
	List<String> mTableCreateSqls;

	public DBHelper(String dbName, int dbVersion, List<String> tablecreateSqls) {
		super(new OutAppDatabaseContext(
				new Application().getApplicationContext(), dbFolderPath),
				dbName, null, dbVersion);
		mTableCreateSqls = tablecreateSqls;
		if (db == null) {
			db = getWritableDatabase();
		}
		// 如果数据库被删除了，重新建立数据库
		if (!(new File(dbFolderPath + dbName)).exists()) {
			onCreate(db);
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// 空判断
		if (mTableCreateSqls != null || db != null) {
			db.beginTransaction();

			try {
				for (String sql : mTableCreateSqls) {
					db.execSQL(sql);
				}
				db.setTransactionSuccessful();
			  
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
                db.endTransaction();
			}
			  InitDB(db);
		}else{
			
			throw new SQLException("db Create Failure");
		}

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgradeDB(db, oldVersion, newVersion);
	}
	protected abstract void onUpgradeDB(SQLiteDatabase db,int oldVersion,int newVersion);

	protected abstract void InitDB(SQLiteDatabase db);
	
	//----------- 工具方法 ---------------------------------------

		/**
		 * 数据库是否打开
		 * 
		 * @return 若数据库处于打开状态,返回true.
		 */
		public boolean isOpen() {
			if (null == db) {
				return false;
			}
			return db.isOpen();
		}

		/**
		 * 开始事务
		 */
		public void beginTransaction() {
			db.beginTransaction();
		}

		/**
		 * 提交事务
		 */
		public void setTransactionSuccessful() {
			db.setTransactionSuccessful();
		}

		/**
		 * 结束事务
		 */
		public void endTransaction() {
			db.endTransaction();
		}

		// ----通用（可多表）操作----

		/**
		 * 用原始SQL查询（可以多表,预编译方式）
		 * 
		 * @param sql
		 *            the SQL query. The SQL string must not be terminated.
		 * @param selectionArgs
		 *            You may include ?s in where clause in the query, which will be
		 *            replaced by the values from selectionArgs. The values will be
		 *            bound as Strings.
		 * @return object, which is positioned before the first entry. Note that
		 *         Cursors are not synchronized, see the documentation for more
		 *         details.
		 * @see {@link SQLiteDatabase#rawQuery(String, String[])}
		 */
		public Cursor query(String sql, String[] selectionArgs) {
			Cursor cursor = db.rawQuery(sql, selectionArgs);
			return cursor;
		}

		/**
		 * 用原始SQL查询（可以多表,直接SQL方式）
		 * 
		 * @param sql
		 * @param selectionArgs
		 * @return object, which is positioned before the first entry. Note that
		 *         Cursors are not synchronized, see the documentation for more
		 *         details.
		 */
		public Cursor query(String sql) {
			Cursor cursor = db.rawQuery(sql, null);
			return cursor;
		}

		// ----单表操作----

		/**
		 * 查询实体（单表）
		 * 
		 * @param distinct
		 * @param table
		 * @param columns
		 * @param selection
		 * @param selectionArgs
		 * @param groupBy
		 * @param having
		 * @param orderBy
		 * @param limit
		 * @return
		 * @see Cursor
		 * @see {@link SQLiteDatabase#query(boolean, String, String[], String, String[], String, String, String, String)}
		 */
		public Cursor query(boolean distinct, String table, String[] columns,
				String selection, String[] selectionArgs, String groupBy,
				String having, String orderBy, String limit) {
			Cursor cursor = db.query(distinct, table, columns, selection,
					selectionArgs, groupBy, having, orderBy, limit);
			return cursor;
		}

		// ----单表操作----

		/**
		 * 查询实体（单表）
		 * 
		 * @param table
		 * @param columns
		 * @param selection
		 * @param selectionArgs
		 * @param orderBy
		 * @return
		 * @see Cursor
		 * @see SQLiteDatabase#query(String, String[], String, String[], String,
		 *      String, String, String)
		 */
		public Cursor query(String table, String[] columns, String selection,
				String[] selectionArgs, String groupBy) {
			Cursor cursor = db.query(table, columns, selection, selectionArgs,
					groupBy, null, null, null);
			return cursor;
		}

		/**
		 * 除查询外的SQL操作(直接SQL方式)
		 * 
		 * @param sql
		 * @throws SQLException
		 * @see SQLiteDatabase#execSQL(String)
		 */
		public void execute(String sql) throws DBException{
			try{
				db.execSQL(sql);
			} catch (SQLException e) {
				throw new DBException(e);
			}
			
		}
		
		public void execute(String sql, Object[] params) throws DBException{
			try{
				db.execSQL(sql, params);
			} catch (SQLException e) {
				throw new DBException(e);
			}
		}

		// ----单表操作----

		/**
		 * 新增
		 * 
		 * @param table
		 * @param values
		 * @return the row ID of the newly inserted row, or -1 if an error occurred.
		 * @see SQLiteDatabase#insert(String, String, ContentValues)
		 */
		public long insert(String table, ContentValues contentValues) {
			long result = db.insert(table, null, contentValues);
			return result;
		}

		/**
		 * 更新
		 * 
		 * @param tableName
		 * @param contentValues
		 * @param whereClause
		 * @param whereArgs
		 * @return <code>int</code>受影响的行数
		 * @see SQLiteDatabase#update(String, ContentValues, String, String[])
		 */
		public int update(String table, ContentValues contentValues,
				String whereClause, String[] whereArgs) {
			int result = db.update(table, contentValues, whereClause, whereArgs);
			return result;
		}

		/**
		 * 删除
		 * 
		 * @param tableName
		 * @param whereClause
		 * @param whereArgs
		 * @return the number of rows affected if a whereClause is passed in, 0
		 *         otherwise. To remove all rows and get a count pass "1" as the
		 *         whereClause.
		 * @see SQLiteDatabase#delete(String, String, String[])
		 */
		public int delete(String table, String whereClause, String[] whereArgs) {
			int result = db.delete(table, whereClause, whereArgs);
			return result;
		}
}
