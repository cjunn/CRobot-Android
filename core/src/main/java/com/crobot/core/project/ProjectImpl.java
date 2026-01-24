package com.crobot.core.project;

import com.crobot.core.infra.tool.Config;
import com.crobot.core.infra.tool.ConfigFactory;
import com.crobot.core.infra.tool.FileOperation;
import com.crobot.core.infra.tool.FileOperationFactory;
import com.crobot.core.util.ZipUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class ProjectImpl implements Project {
    private static final String CORE_NAME = "core";
    private static final String XML_NAME = "uiXml";
    private static final String VERSION_NAME = "version";
    private String uiXml;
    private byte[] coreZip;
    private Integer version;
    private FileOperation project;
    private FileOperation attach;
    private Config ui;

    public ProjectImpl(FileOperationFactory fileOperationFactory, ConfigFactory configFactory) {
        this.project = fileOperationFactory.getModule("project");
        this.attach = this.project.open("attach");
        this.ui = configFactory.getConfig("ui");
        this.uiXml = this.loadUIXml();
        this.coreZip = this.loadCoreZip();
        this.version = this.loadVersion();
    }

    private String loadUIXml() {
        return project.readText(XML_NAME);
    }

    private byte[] loadCoreZip() {
        return project.read(CORE_NAME);
    }

    private Integer loadVersion() {
        String version = project.readText(VERSION_NAME);
        try {
            return Integer.parseInt(version);
        } catch (Exception e) {
            return 0;
        }
    }


    @Override
    public String getUIXml() {
        return this.uiXml;
    }

    @Override
    public byte[] getCoreZip() {
        return this.coreZip;
    }

    @Override
    public Integer getVersion() {
        return this.version;
    }

    @Override
    public void writeUIXml(String uiXml) {
        if (Objects.equals(this.uiXml, uiXml)) {
            return;
        }
        this.uiXml = uiXml;
        project.writeText(XML_NAME, uiXml);
    }

    private void writeCoreZip(byte[] coreZip) {
        if (Arrays.equals(this.coreZip, coreZip)) {
            return;
        }
        this.coreZip = coreZip;
        project.write(CORE_NAME, coreZip);
    }

    private void writeVersion(Integer version) {
        if (Objects.equals(this.version, version)) {
            return;
        }
        this.version = version;
        project.writeText(VERSION_NAME, version + "");
    }

    @Override
    public Map<String, Object> getAllUISetting() {
        return this.ui.getAll();
    }

    @Override
    public void setUISetting(String key, Object value) {
        this.ui.set(key, value);
    }

    @Override
    public void install(String xml, byte[] zip, Integer version) {
        this.writeUIXml(xml);
        this.writeCoreZip(zip);
        this.writeVersion(version);
    }

    @Override
    public void install(String xml, byte[] zip, byte[] attach, Integer version) {
        this.writeUIXml(xml);
        this.writeCoreZip(zip);
        this.writeAttach(attach);
        this.writeVersion(version);
    }

    @Override
    public FileOperation getAttachFile() {
        return this.attach;
    }

    private void writeAttach(byte[] attach) {
        ZipUtil.extract(attach, pair -> ProjectImpl.this.attach.write(pair.first, pair.second));
    }

}
