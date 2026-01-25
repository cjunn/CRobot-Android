package com.crobot.core.infra.tool;

import android.database.SQLException;

import java.util.List;
import java.util.Map;

public interface SQLite {
    int getVersion();
    void beginTransaction();
    void endTransaction();
    void execSQL(String sql, Object[] bindArgs) throws SQLException;
    SQLiteCursor query(String sql, String[] selectionArgs);
    List<Map<String,Object>> queryList(String sql, String[] selectionArgs);
    int update(String sql, String[] selectionArgs);
    long insert(String sql, String[] selectionArgs);
    void close();
}
