package com.crobot.core.drive;

import com.crobot.core.infra.tool.SQLite;
import com.crobot.core.infra.tool.SQLiteCursor;
import com.crobot.core.infra.tool.SQLiteFactory;
import com.crobot.runtime.engine.Context;
import com.crobot.runtime.engine.ContextException;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.JsonBean;
import com.crobot.runtime.engine.Varargs;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;

public class SQLiteInitiator implements Initiator {
    private SQLiteFactory sqLiteFactory;

    public SQLiteInitiator(SQLiteFactory sqLiteFactory) {
        this.sqLiteFactory = sqLiteFactory;
    }

    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("SQLite", new ObjApt() {
            @Caller("open")
            public SQLiteApt open(String path) {
                try{
                    return new SQLiteApt(sqLiteFactory.open(path));
                }catch (Exception e){
                    throw new ContextException(e.getMessage());
                }
            }
        });
    }

    public static class SQLiteApt extends ObjApt {
        private SQLite sqLite;

        public SQLiteApt(SQLite sqLite) {
            this.sqLite = sqLite;
        }

        @Override
        public int onGc(Context context) {
            this.sqLite.close();
            return super.onGc(context);
        }

        @Caller("getVersion")
        public Number getVersion() {
            return this.sqLite.getVersion();
        }

        @Caller("beginTransaction")
        public void beginTransaction() {
            this.sqLite.beginTransaction();
        }

        @Caller("endTransaction")
        public void endTransaction() {
            this.sqLite.endTransaction();
        }



        private String[] toSelectionArgs(Varargs varargs) {
            Object[] args = varargs.getArgs();
            String[] ret = new String[args.length];
            for (int i = 0; i < args.length; i++) {
                ret[i] = args[i] == null ? null : args[i].toString();
            }
            return ret;
        }

        @Caller("execSQL")
        public void execSQL(String sql, Varargs varargs) {
            this.sqLite.execSQL(sql,toSelectionArgs(varargs));
        }

        @Caller("query")
        public SQLiteCursorApt query(String sql, Varargs varargs) {
            try {
                return new SQLiteCursorApt(this.sqLite.query(sql, toSelectionArgs(varargs)));
            } catch (Exception e) {
                throw new ContextException(e.getMessage());
            }
        }

        @Caller("queryList")
        public JsonBean queryList(String sql, Varargs varargs) {
            try {
                return JsonBean.create(this.sqLite.queryList(sql, toSelectionArgs(varargs)));
            } catch (Exception e) {
                throw new ContextException(e.getMessage());
            }
        }


        @Caller("update")
        public Number update(String sql, Varargs varargs) {
            try {
                return this.sqLite.update(sql, toSelectionArgs(varargs));
            } catch (Exception e) {
                throw new ContextException(e.getMessage());
            }
        }

        @Caller("insert")
        public Number insert(String sql, Varargs varargs) {
            try {
                return this.sqLite.insert(sql, toSelectionArgs(varargs));
            } catch (Exception e) {
                throw new ContextException(e.getMessage());
            }
        }

        @Caller("close")
        public void close() {
            this.sqLite.close();
        }

    }

    public static class SQLiteCursorApt extends ObjApt {
        private SQLiteCursor cursor;

        public SQLiteCursorApt(SQLiteCursor cursor) {
            this.cursor = cursor;
        }

        @Override
        public int onGc(Context context) {
            cursor.close();
            return super.onGc(context);
        }

        @Caller("getCount")
        public Number getCount() {
            checkCursorValid();
            return cursor.getCount();
        }

        @Caller("getPosition")
        public Number getPosition() {
            checkCursorValid();
            return cursor.getPosition();
        }

        @Caller("move")
        public boolean move(Number offset) {
            checkCursorValid();
            return cursor.move(offset.intValue());
        }

        @Caller("moveToPosition")
        public boolean moveToPosition(Number position) {
            checkCursorValid();
            return cursor.moveToPosition(position.intValue());
        }

        @Caller("moveToFirst")
        public boolean moveToFirst() {
            checkCursorValid();
            return cursor.moveToFirst();
        }

        @Caller("moveToLast")
        public boolean moveToLast() {
            checkCursorValid();
            return cursor.moveToLast();
        }

        @Caller("moveToNext")
        public boolean moveToNext() {
            checkCursorValid();
            return cursor.moveToNext();
        }

        @Caller("moveToPrevious")
        public boolean moveToPrevious() {
            checkCursorValid();
            return cursor.moveToPrevious();
        }

        @Caller("isFirst")
        public boolean isFirst() {
            checkCursorValid();
            return cursor.isFirst();
        }

        @Caller("isLast")
        public boolean isLast() {
            checkCursorValid();
            return cursor.isLast();
        }

        @Caller("isBeforeFirst")
        public boolean isBeforeFirst() {
            checkCursorValid();
            return cursor.isBeforeFirst();
        }

        @Caller("isAfterLast")
        public boolean isAfterLast() {
            checkCursorValid();
            return cursor.isAfterLast();
        }

        @Caller("getColumnIndex")
        public Number getColumnIndex(String columnName) {
            checkCursorValid();
            return cursor.getColumnIndex(columnName);
        }

        @Caller("getColumnName")
        public String getColumnName(Number columnIndex) {
            checkCursorValid();
            return cursor.getColumnName(columnIndex.intValue());
        }

        @Caller("getColumnNames")
        public String[] getColumnNames() {
            checkCursorValid();
            return cursor.getColumnNames();
        }

        @Caller("getColumnCount")
        public Number getColumnCount() {
            checkCursorValid();
            return cursor.getColumnCount();
        }

        @Caller("getBlob")
        public byte[] getBlob(Number columnIndex) {
            checkCursorValid();
            return cursor.getBlob(columnIndex.intValue());
        }

        @Caller("getString")
        public String getString(Number columnIndex) {
            checkCursorValid();
            return cursor.getString(columnIndex.intValue());
        }

        @Caller("getShort")
        public Number getShort(Number columnIndex) {
            checkCursorValid();
            return cursor.getShort(columnIndex.intValue());
        }

        @Caller("getInt")
        public Number getInt(Number columnIndex) {
            checkCursorValid();
            return cursor.getInt(columnIndex.intValue());
        }

        @Caller("getLong")
        public Number getLong(Number columnIndex) {
            checkCursorValid();
            return cursor.getLong(columnIndex.intValue());
        }

        @Caller("getFloat")
        public Number getFloat(Number columnIndex) {
            checkCursorValid();
            return cursor.getFloat(columnIndex.intValue());
        }

        @Caller("getDouble")
        public Number getDouble(Number columnIndex) {
            checkCursorValid();
            return cursor.getDouble(columnIndex.intValue());
        }

        @Caller("getType")
        public Number getType(Number columnIndex) {
            checkCursorValid();
            return cursor.getType(columnIndex.intValue());
        }

        @Caller("isNull")
        public boolean isNull(Number columnIndex) {
            checkCursorValid();
            return cursor.isNull(columnIndex.intValue());
        }

        @Caller("close")
        public void close() {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        private void checkCursorValid() {
            if (cursor == null) {
                throw new ContextException("SQLiteCursor has been released or not initialized");
            }
            if (cursor.isClosed()) {
                throw new ContextException("SQLiteCursor is already closed");
            }
        }

    }


}