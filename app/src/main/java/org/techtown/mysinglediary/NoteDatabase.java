package org.techtown.mysinglediary;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


//노트데이터베이스
public class NoteDatabase {
	private static final String TAG = "NoteDatabase";


	//데이터 베이스 싱글톤 인스턴스
	private static NoteDatabase database;

	//테이블 이름
	public static String TABLE_NOTE = "NOTE";

    //버전
	public static int DATABASE_VERSION = 1;

    //헬퍼 클래스 인스턴스
    private DatabaseHelper dbHelper;

    //SQLiteDatabase 인스턴스
    private SQLiteDatabase db;

    private Context context;

    //생성자
	private NoteDatabase(Context context) {
		this.context = context;
	}

	//인스턴스 가져오기
	public static NoteDatabase getInstance(Context context) {
		if (database == null) {
			database = new NoteDatabase(context);
		}

		return database;
	}

	//db열기
    public boolean open() {

    	dbHelper = new DatabaseHelper(context);
    	db = dbHelper.getWritableDatabase();

    	return true;
    }

    //db닫기
    public void close() {
    	db.close();

    	database = null;
    }

    //입력 SQL 사용 쿼리실행
	//결과 과져옴
    public Cursor rawQuery(String SQL) {

		Cursor c1 = null;
		try {
			c1 = db.rawQuery(SQL, null);
		} catch(Exception ex) {
    		Log.e(TAG, "Exception in executeQuery", ex);
    	}

		return c1;
	}

    public boolean execSQL(String SQL) {

		try {
			Log.d(TAG, "SQL : " + SQL);
			db.execSQL(SQL);
	    } catch(Exception ex) {
			Log.e(TAG, "Exception in executeQuery", ex);
			return false;
		}

		return true;
	}

	//db helper 이너클래스
    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, Constants.DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {

        	// drop existing table
        	String DROP_SQL = "drop table if exists " + TABLE_NOTE;
        	try {
        		db.execSQL(DROP_SQL);
        	} catch(Exception ex) {
        		Log.e(TAG, "Exception in DROP_SQL", ex);
        	}

        	// create table
        	String CREATE_SQL = "create table " + TABLE_NOTE + "("
		        			+ "  _id INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT, "
							+ "  WEATHER TEXT DEFAULT '', "
							+ "  ADDRESS TEXT DEFAULT '', "
							+ "  LOCATION_X TEXT DEFAULT '', "
							+ "  LOCATION_Y TEXT DEFAULT '', "
		        			+ "  CONTENTS TEXT DEFAULT '', "
		        			+ "  MOOD TEXT, "
		        			+ "  PICTURE TEXT DEFAULT '', "
		        			+ "  CREATE_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
							+ "  MODIFY_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP "
		        			+ ")";
            try {
            	db.execSQL(CREATE_SQL);
            } catch(Exception ex) {
        		Log.e(TAG, "Exception in CREATE_SQL", ex);
        	}

            // create index
        	String CREATE_INDEX_SQL = "create index " + TABLE_NOTE + "_IDX ON " + TABLE_NOTE + "("
		        			+ "CREATE_DATE"
		        			+ ")";
            try {
            	db.execSQL(CREATE_INDEX_SQL);
            } catch(Exception ex) {
        		Log.e(TAG, "Exception in CREATE_INDEX_SQL", ex);
        	}
        }

        public void onOpen(SQLiteDatabase db) {
        	println("opened database [" + Constants.DATABASE_NAME + "].");
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	println("Upgrading database from version " + oldVersion + " to " + newVersion + ".");
        }
    }

    private void println(String msg) {
    	Log.d(TAG, msg);
    }

}