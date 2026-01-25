package com.crobot.core.infra.tool;

public interface SQLiteFactory {
    SQLite open(String path);
}
