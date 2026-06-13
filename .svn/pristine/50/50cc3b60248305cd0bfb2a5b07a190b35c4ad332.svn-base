package com.micsig.tbook.scope.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "micsig_scope.db";

    public static final String TABLE_NAME = "PROBE_DA";

    public DBHelper(@Nullable Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists " + TABLE_NAME + " (Id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "CH INTERGER NOT NULL, SN VARCHAR (64) NOT NULL, " +
                "DA INTEGER NOT NULL DEFAULT (0) )";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);
    }

    private void insert(int chIdx,String sn,int daVal){
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        ContentValues contentValues = new ContentValues();
        contentValues.put("CH", chIdx);
        contentValues.put("SN", sn);
        contentValues.put("DA", daVal);
        db.insertOrThrow(TABLE_NAME, null, contentValues);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }


    private void update(int chIdx,String sn,int daVal){
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        values.put("DA",daVal);
        String whereClause = "CH=? AND SN=?";
        String[] whereArgs={String.valueOf(chIdx),sn};
        db.update(TABLE_NAME,values,whereClause,whereArgs);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public int getChProbeDa(int chIdx,String sn){
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME,null,"CH=? AND SN=?",new String[]{String.valueOf(chIdx),sn},null,null,null);
        int da = -1;
        if(cursor != null){
            if(cursor.getCount() > 0){
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    da = cursor.getInt(cursor.getColumnIndex("DA"));
                    break;
                }
            }
            cursor.close();
        }
        db.close();
        return da;
    }

    public void setChProbeDa(int chIdx,String sn,int daVal){
        if(getChProbeDa(chIdx,sn) < 0){
            insert(chIdx,sn,daVal);
        }else{
            update(chIdx,sn,daVal);
        }
    }
}
