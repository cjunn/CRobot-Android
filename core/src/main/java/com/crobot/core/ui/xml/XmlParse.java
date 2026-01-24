package com.crobot.core.ui.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class XmlParse {
    private String xml;

    public XmlParse(String xml) {
        this.xml = xml;
    }

    private void setAttrValues(XmlNode node, Attributes attributes) {
        int len = attributes.getLength();
        for (int i = 0; i < len; i++) {
            String attr = attributes.getQName(i);
            String value = attributes.getValue(i);
            node.addAttr(attr, value);
        }
    }


    public XmlNode build() {
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            ByteArrayInputStream stream = new ByteArrayInputStream(this.xml.getBytes(StandardCharsets.UTF_8));
            XmlNode root = new XmlNode("Setting");
            Deque<XmlNode> stack = new ArrayDeque();
            stack.addLast(root);
            saxParser.parse(stream, new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
                    XmlNode element = new XmlNode(name);
                    setAttrValues(element, attributes);
                    XmlNode last = stack.getLast();
                    last.addChild(element);
                    stack.addLast(element);
                }

                @Override
                public void endElement(String uri, String localName, String name) {
                    stack.removeLast();
                }
            });
            return root;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
