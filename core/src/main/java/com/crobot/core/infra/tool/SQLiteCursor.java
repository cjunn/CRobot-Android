package com.crobot.core.infra.tool;

public interface SQLiteCursor {
    int getCount();
    int getPosition();
    boolean move(int offset);
    boolean moveToPosition(int position);
    boolean moveToFirst();
    boolean moveToLast();
    boolean moveToNext();
    boolean moveToPrevious();
    boolean isFirst();
    boolean isLast();
    boolean isBeforeFirst();
    boolean isAfterLast();
    int getColumnIndex(String columnName);
    String getColumnName(int columnIndex);
    String[] getColumnNames();
    int getColumnCount();
    byte[] getBlob(int columnIndex);
    String getString(int columnIndex);
    short getShort(int columnIndex);
    int getInt(int columnIndex);
    long getLong(int columnIndex);
    float getFloat(int columnIndex);
    double getDouble(int columnIndex);
    int getType(int columnIndex);
    boolean isNull(int columnIndex);
    void close();
    boolean isClosed();
}
