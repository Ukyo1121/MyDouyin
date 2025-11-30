package com.bytedance.mydouyin.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RemarkDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "douyin_lite.db";
    private static final int DB_VERSION = 1;

    // 表名和列名
    private static final String TABLE_NAME = "user_remarks";
    private static final String COL_NICKNAME = "nickname";
    private static final String COL_REMARK = "remark";

    public RemarkDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // 第一次创建数据库时调用建表
    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL语句：CREATE TABLE user_remarks (nickname TEXT PRIMARY KEY, remark TEXT)
        String createTableSql = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_NICKNAME + " TEXT PRIMARY KEY, " +
                COL_REMARK + " TEXT)";
        db.execSQL(createTableSql);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    //  保存备注
    public void saveRemark(String nickname, String remark) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NICKNAME, nickname);
        values.put(COL_REMARK, remark);

        // insertWithOnConflict 如果主键(nickname)冲突了会自动覆盖旧数据
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        db.close();
    }

    // 查询备注
    public String getRemark(String nickname) {
        SQLiteDatabase db = getReadableDatabase();
        String result = null;

        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COL_REMARK}, // 只查 remark 这一列
                COL_NICKNAME + "=?",
                new String[]{nickname},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(COL_REMARK);
            if (index >= 0) {
                result = cursor.getString(index);
            }
            cursor.close();
        }

        db.close();
        return result;
    }
}
