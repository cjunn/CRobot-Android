package com.crobot.core.project;

import com.crobot.core.infra.tool.FileOperation;

import java.util.Map;

public interface Project {
    String MAIN = "main";

    String getUIXml();

    byte[] getCoreZip();

    Integer getVersion();

    FileOperation getAttachFile();

    void writeUIXml(String xml);

    Map<String, Object> getAllUISetting();

    void setUISetting(String key, Object value);

    void install(String xml, byte[] zip, Integer version);

    void install(String xml, byte[] zip, byte[] attach, Integer version);
}
