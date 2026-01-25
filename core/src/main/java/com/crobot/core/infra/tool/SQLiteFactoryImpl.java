package com.crobot.core.infra.tool;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

public class SQLiteFactoryImpl implements SQLiteFactory {
    private Context context;

    public SQLiteFactoryImpl(Context context) {
        this.context = context;
    }

    @Override
    public SQLite open(String path) {
        File file = new File(path);
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(new SQLiteContext(context, file), file.getName());
        return new SQLiteImpl(helper.getWritableDatabase());
    }

    public class SQLiteContext extends ContextWrapper {
        private File file;

        public SQLiteContext(Context context, File file) {
            super(context);
            this.file = file;
        }

        @Override
        public File getDatabasePath(String name) {
            return file;
        }
    }

    public class MySQLiteOpenHelper extends SQLiteOpenHelper {

        public MySQLiteOpenHelper(Context context, String name) {
            super(context, name, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }


}
