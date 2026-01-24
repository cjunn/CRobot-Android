package com.crobot.core.ui.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlNode {
    private final String element;

    private final List<XmlNode> children = new ArrayList<>();

    private Map<String, String> attr = new HashMap<>();

    public XmlNode(String element) {
        this.element = element;
    }

    public void addAttr(String key, String value) {
        this.attr.put(key, value);
    }

    public void addChild(XmlNode node) {
        this.children.add(node);
    }

    public Map<String, String> getAttr() {
        return attr;
    }

    public List<XmlNode> getChildren() {
        return children;
    }

    public String getElement() {
        return element;
    }
}
