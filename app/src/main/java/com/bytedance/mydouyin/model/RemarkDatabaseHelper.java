package com.bytedance.mydouyin.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RemarkDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "douyin_lite.db";
    private static final int DB_VERSION = 3;

    // 表名和列名
    private static final String TABLE_NAME = "user_remarks";
    private static final String COL_NICKNAME = "nickname";
    private static final String COL_REMARK = "remark";
    private static final String COL_IS_PINNED = "is_pinned";
    private static final String COL_PHONE = "phone";

    public RemarkDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // 第一次创建数据库时调用建表
    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL语句：CREATE TABLE user_remarks (nickname TEXT PRIMARY KEY, remark TEXT)
        String createTableSql = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_NICKNAME + " TEXT PRIMARY KEY, " +
                COL_REMARK + " TEXT," +
                COL_PHONE + " TEXT," +
                COL_IS_PINNED + " INTEGER DEFAULT 0)";
        db.execSQL(createTableSql);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String upgradeSql = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_IS_PINNED + " INTEGER DEFAULT 0";
            db.execSQL(upgradeSql);
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_PHONE + " TEXT");
        }

    }

    //  保存备注和电话
    public void saveRemarkInfo(String nickname, String remark, String phone) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NICKNAME, nickname);
        values.put(COL_REMARK, remark);
        values.put(COL_PHONE, phone); // 保存电话


        int rows = db.update(TABLE_NAME, values, COL_NICKNAME + "=?", new String[]{nickname});
        if (rows == 0) {
            // 如果是新数据，插入
            db.insert(TABLE_NAME, null, values);
        }
        db.close();
    }
    // 定义一个简单的内部类来返回多个数据
    public static class LocalInfo {
        public String remark;
        public String phone;
        public boolean isPinned;
    }
    // 获取所有本地信息（备注、电话、置顶）
    public LocalInfo getLocalInfo(String nickname) {
        SQLiteDatabase db = getReadableDatabase();
        LocalInfo info = new LocalInfo();
        // 默认空值
        info.remark = "";
        info.phone = "";
        info.isPinned = false;

        Cursor cursor = db.query(TABLE_NAME, null, COL_NICKNAME + "=?", new String[]{nickname}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            // 读取备注
            int idxRemark = cursor.getColumnIndex(COL_REMARK);
            if (idxRemark >= 0) info.remark = cursor.getString(idxRemark);

            // 读取电话
            int idxPhone = cursor.getColumnIndex(COL_PHONE);
            if (idxPhone >= 0) info.phone = cursor.getString(idxPhone);

            // 读取置顶
            int idxPinned = cursor.getColumnIndex(COL_IS_PINNED);
            if (idxPinned >= 0) info.isPinned = cursor.getInt(idxPinned) == 1;

            cursor.close();
        }
        db.close();
        return info;
    }

    // 兼容旧代码的方法 (只取备注)
    public String getRemark(String nickname) {
        return getLocalInfo(nickname).remark;
    }

    // 兼容旧代码的方法 (只取置顶)
    public boolean isPinned(String nickname) {
        return getLocalInfo(nickname).isPinned;
    }

    // 兼容旧代码的方法 (更新置顶)
    public void updatePinStatus(String nickname, boolean isPinned) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_IS_PINNED, isPinned ? 1 : 0);
        int rows = db.update(TABLE_NAME, values, COL_NICKNAME + "=?", new String[]{nickname});
        if (rows == 0) {
            values.put(COL_NICKNAME, nickname);
            db.insert(TABLE_NAME, null, values);
        }
        db.close();
    }
}
