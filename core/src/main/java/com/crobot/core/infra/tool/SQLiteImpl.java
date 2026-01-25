package com.crobot.core.infra.tool;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.CancellationSignal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLiteImpl implements SQLite {
    private SQLiteDatabase database;

    public SQLiteImpl(SQLiteDatabase database) {
        this.database = database;
    }

    @Override
    public int getVersion() {
        return database.getVersion();
    }

    @Override
    public void beginTransaction() {
        database.beginTransaction();
    }

    @Override
    public void endTransaction() {
        database.endTransaction();
    }

    @Override
    public void execSQL(String sql, Object[] bindArgs) throws SQLException {
        database.execSQL(sql,bindArgs);
    }

    @Override
    public SQLiteCursor query(String sql, String[] selectionArgs) {
        String path = database.getPath();
        CancellationSignal signal = new CancellationSignal();
        Cursor cursor = database.rawQuery(sql, selectionArgs, signal);
        return new SQLiteCursorImpl(cursor, signal);
    }

    @Override
    public List<Map<String, Object>> queryList(String sql, String[] selectionArgs) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Cursor cursor = database.rawQuery(sql, selectionArgs);
        try {
            String[] columnNames = cursor.getColumnNames();
            int columnCount = columnNames.length;
            if (cursor.moveToFirst()) { // 移动到第一行，判断是否有数据
                do {
                    Map<String, Object> rowMap = new HashMap<>();
                    for (int i = 0; i < columnCount; i++) {
                        String columnName = columnNames[i];
                        switch (cursor.getType(i)) {
                            case Cursor.FIELD_TYPE_NULL:
                                rowMap.put(columnName, null); // 空值
                                break;
                            case Cursor.FIELD_TYPE_INTEGER:
                                rowMap.put(columnName, cursor.getLong(i)); // 整数（统一用long兼容int）
                                break;
                            case Cursor.FIELD_TYPE_FLOAT:
                                rowMap.put(columnName, cursor.getDouble(i)); // 浮点数（统一用double兼容float）
                                break;
                            case Cursor.FIELD_TYPE_STRING:
                                rowMap.put(columnName, cursor.getString(i)); // 字符串
                                break;
                            case Cursor.FIELD_TYPE_BLOB:
                                rowMap.put(columnName, cursor.getBlob(i)); // 二进制数据（BLOB）
                                break;
                            default:
                                rowMap.put(columnName, cursor.getString(i));
                                break;
                        }
                    }
                    resultList.add(rowMap);
                } while (cursor.moveToNext()); // 移动到下一行，直到遍历完所有数据
            }
        } catch (Exception e) {
            throw e;
        } finally {
            // 6. 必须关闭Cursor，释放资源
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return resultList;
    }

    @Override
    public int update(String sql, String[] selectionArgs) {
        SQLiteStatement sqLiteStatement = database.compileStatement(sql);
        sqLiteStatement.bindAllArgsAsStrings(selectionArgs);
        return sqLiteStatement.executeUpdateDelete();
    }

    @Override
    public long insert(String sql, String[] selectionArgs) {
        SQLiteStatement sqLiteStatement = database.compileStatement(sql);
        sqLiteStatement.bindAllArgsAsStrings(selectionArgs);
        return sqLiteStatement.executeInsert();
    }

    @Override
    public void close() {
        database.close();
    }

}
