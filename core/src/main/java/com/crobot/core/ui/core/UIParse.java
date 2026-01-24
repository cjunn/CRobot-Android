package com.crobot.core.ui.core;

import android.content.Context;

import com.crobot.core.ui.xml.XmlNode;
import com.crobot.core.ui.xml.XmlParse;

import java.util.ArrayList;
import java.util.List;

public class UIParse {
    private XmlNode xmlNode;
    private Context context;


    public UIParse(String xml, Context context) {
        this.xmlNode = new XmlParse(xml).build();
        this.context = context;
    }

    public Result build() {
        List<UISupport> flat = new ArrayList<>();
        return new Result(build(this.xmlNode, flat), flat);
    }

    private UISupport build(XmlNode node, List<UISupport> flatList) {
        UISupport support = UIRegister.newInstance(node.getElement(), this.context, node.getAttr());
        flatList.add(support);
        for (XmlNode item : node.getChildren()) {
            UISupport child = build(item, flatList);
            support.addChild(child);
        }
        return support;
    }

    public static class Result {
        protected final UISupport root;
        protected final List<UISupport> flat;

        public Result(UISupport root, List<UISupport> flat) {
            this.root = root;
            this.flat = flat;
        }
    }
}
