package com.crobot.core.drive;

import android.os.Build;
import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;

import com.crobot.core.infra.tool.ScreenSelector;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.apt.FuncApt;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;
import com.crobot.runtime.engine.apt.anno.Execute;

public class SelectorInitiator implements Initiator {
    private ScreenSelector screenSelector;

    public SelectorInitiator(ScreenSelector screenSelector) {
        this.screenSelector = screenSelector;
    }

    @Override
    public void execute(ContextProxy context) {
        context.setFuncApt("createSelector", new FuncApt() {
            @Execute
            public SelectorBuildApt execute() {
                return new SelectorBuildApt();
            }
        });
    }

    public class SelectorBuildApt extends ObjApt {
        private ScreenSelector.Condition condition = new ScreenSelector.Condition();

        @Caller("findOne")
        public NodeApt findOne() {
            condition.findOne = true;
            AccessibilityNodeInfo[] nodes = screenSelector.find(condition);
            if (nodes.length == 0) {
                return null;
            }
            return new NodeApt(nodes[0]);
        }

        @Caller("find")
        public NodeApt[] find() {
            condition.findOne = false;
            AccessibilityNodeInfo[] nodes = screenSelector.find(condition);
            NodeApt[] ret = new NodeApt[nodes.length];
            for (int i = 0; i < nodes.length; i++) {
                ret[i] = new NodeApt(nodes[i]);
            }
            return ret;
        }

        @Caller("text")
        public SelectorBuildApt text(String text) {
            condition.text = text;
            return this;
        }

        @Caller("algorithm")
        public SelectorBuildApt algorithm(String algorithm) {
            condition.algorithm = algorithm;
            return this;
        }

        @Caller("textContains")
        public SelectorBuildApt textContains(String textContains) {
            condition.textContains = textContains;
            return this;
        }

        @Caller("textStartsWith")
        public SelectorBuildApt textStartsWith(String textStartsWith) {
            condition.textStartsWith = textStartsWith;
            return this;
        }

        @Caller("textEndsWith")
        public SelectorBuildApt textEndsWith(String textEndsWith) {
            condition.textEndsWith = textEndsWith;
            return this;
        }

        @Caller("textMatches")
        public SelectorBuildApt textMatches(String textMatches) {
            condition.textMatches = textMatches;
            return this;
        }

        @Caller("desc")
        public SelectorBuildApt desc(String desc) {
            condition.desc = desc;
            return this;
        }

        @Caller("descContains")
        public SelectorBuildApt descContains(String descContains) {
            condition.descContains = descContains;
            return this;
        }

        @Caller("descStartsWith")
        public SelectorBuildApt descStartsWith(String descStartsWith) {
            condition.descStartsWith = descStartsWith;
            return this;
        }

        @Caller("descEndsWith")
        public SelectorBuildApt descEndsWith(String descEndsWith) {
            condition.descEndsWith = descEndsWith;
            return this;
        }

        @Caller("descMatches")
        public SelectorBuildApt descMatches(String descMatches) {
            condition.descMatches = descMatches;
            return this;
        }


        @Caller("id")
        public SelectorBuildApt id(String id) {
            condition.id = id;
            return this;
        }

        @Caller("idContains")
        public SelectorBuildApt idContains(String idContains) {
            condition.idContains = idContains;
            return this;
        }

        @Caller("idStartsWith")
        public SelectorBuildApt idStartsWith(String idStartsWith) {
            condition.idStartsWith = idStartsWith;
            return this;
        }

        @Caller("idEndsWith")
        public SelectorBuildApt idEndsWith(String idEndsWith) {
            condition.idEndsWith = idEndsWith;
            return this;
        }

        @Caller("idMatches")
        public SelectorBuildApt idMatches(String idMatches) {
            condition.idMatches = idMatches;
            return this;
        }

        @Caller("className")
        public SelectorBuildApt className(String className) {
            condition.className = className;
            return this;
        }

        @Caller("classNameContains")
        public SelectorBuildApt classNameContains(String classNameContains) {
            condition.classNameContains = classNameContains;
            return this;
        }

        @Caller("classNameStartsWith")
        public SelectorBuildApt classNameStartsWith(String classNameStartsWith) {
            condition.classNameStartsWith = classNameStartsWith;
            return this;
        }

        @Caller("classNameEndsWith")
        public SelectorBuildApt classNameEndsWith(String classNameEndsWith) {
            condition.classNameEndsWith = classNameEndsWith;
            return this;
        }


        @Caller("classNameMatches")
        public SelectorBuildApt classNameMatches(String classNameMatches) {
            condition.classNameMatches = classNameMatches;
            return this;
        }


        @Caller("packageName")
        public SelectorBuildApt packageName(String packageName) {
            condition.packageName = packageName;
            return this;
        }

        @Caller("packageNameContains")
        public SelectorBuildApt packageNameContains(String packageNameContains) {
            condition.packageNameContains = packageNameContains;
            return this;
        }

        @Caller("packageNameStartsWith")
        public SelectorBuildApt packageNameStartsWith(String packageNameStartsWith) {
            condition.packageNameStartsWith = packageNameStartsWith;
            return this;
        }

        @Caller("packageNameEndsWith")
        public SelectorBuildApt packageNameEndsWith(String packageNameEndsWith) {
            condition.packageNameEndsWith = packageNameEndsWith;
            return this;
        }

        @Caller("packageNameMatches")
        public SelectorBuildApt packageNameMatches(String packageNameMatches) {
            condition.packageNameMatches = packageNameMatches;
            return this;
        }

        @Caller("drawingOrder")
        public SelectorBuildApt drawingOrder(Number order) {
            condition.order = order.intValue();
            return this;
        }

        @Caller("clickable")
        public SelectorBuildApt clickable(Boolean b) {
            condition.clickable = b;
            return this;
        }

        @Caller("longClickable")
        public SelectorBuildApt longClickable(Boolean b) {
            condition.longClickable = b;
            return this;
        }

        @Caller("checkable")
        public SelectorBuildApt checkable(Boolean b) {
            condition.checkable = b;
            return this;
        }

        @Caller("selected")
        public SelectorBuildApt selected(Boolean b) {
            condition.selected = b;
            return this;
        }

        @Caller("enabled")
        public SelectorBuildApt enabled(Boolean b) {
            condition.enabled = b;
            return this;
        }

        @Caller("scrollable")
        public SelectorBuildApt scrollable(Boolean b) {
            condition.scrollable = b;
            return this;
        }

        @Caller("editable")
        public SelectorBuildApt editable(Boolean b) {
            condition.editable = b;
            return this;
        }

        @Caller("multiLine")
        public SelectorBuildApt multiLine(Boolean b) {
            condition.multiLine = b;
            return this;
        }

    }

    public class NodeApt extends ObjApt {
        private AccessibilityNodeInfo node;

        public NodeApt(AccessibilityNodeInfo node) {
            this.node = node;
        }

        @Caller("text")
        public String text() {
            CharSequence text = node.getText();
            if (text == null) {
                return null;
            }
            return text.toString();
        }

        @Caller("desc")
        public String desc() {
            CharSequence desc = node.getContentDescription();
            if (desc == null) {
                return null;
            }
            return desc.toString();
        }

        @Caller("id")
        public String id() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return node.getUniqueId();
            }
            return "";
        }

        @Caller("className")
        public String className() {
            CharSequence className = node.getClassName();
            if (className == null) {
                return null;
            }
            return className.toString();
        }

        @Caller("packageName")
        public String packageName() {
            CharSequence packageName = node.getPackageName();
            if (packageName == null) {
                return null;
            }
            return packageName.toString();
        }


        @Caller("click")
        public Boolean click() {
            if (node != null && node.isClickable()) {
                return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
            return false;
        }

        @Caller("longClick")
        public Boolean longClick() {
            if (node != null && node.isClickable()) {
                return node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
            }
            return false;
        }

        @Caller("setText")
        public Boolean setText(String text) {
            if (node != null && node.isEditable()) {
                Bundle arguments = new Bundle();
                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
                return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
            }
            return false;
        }

        @Caller("copy")
        public Boolean copy() {
            if (node != null) {
                return node.performAction(AccessibilityNodeInfo.ACTION_COPY);
            }
            return false;
        }

        @Caller("cut")
        public Boolean cut() {
            if (node != null) {
                return node.performAction(AccessibilityNodeInfo.ACTION_CUT);
            }
            return false;
        }

        @Caller("paste")
        public Boolean paste() {
            if (node != null) {
                return node.performAction(AccessibilityNodeInfo.ACTION_PASTE);
            }
            return false;
        }

        @Caller("setSelection")
        public Boolean setSelection(Number start, Number end) {
            if (node != null && node.isEditable()) {
                Bundle arguments = new Bundle();
                arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, start.intValue());
                arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, end.intValue());
                return node.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, arguments);
            }
            return false;
        }

        @Caller("scrollForward")
        public Boolean scrollForward() {
            if (node != null && node.isScrollable()) {
                return node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            }
            return false;
        }

        @Caller("scrollBackward")
        public Boolean scrollBackward() {
            if (node != null && node.isScrollable()) {
                return node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
            }
            return false;
        }

        @Caller("select")
        public Boolean select() {
            if (node != null) {
                return node.performAction(AccessibilityNodeInfo.ACTION_SELECT);
            }
            return false;
        }

        @Caller("collapse")
        public Boolean collapse() {
            if (node != null) {
                return node.performAction(AccessibilityNodeInfo.ACTION_COLLAPSE);
            }
            return false;
        }

        @Caller("expand")
        public Boolean expand() {
            if (node != null) {
                return node.performAction(AccessibilityNodeInfo.ACTION_EXPAND);
            }
            return false;
        }


        @Caller("show")
        public Boolean show() {
            if (node != null) {
                // 使用 ACTION_ACCESSIBILITY_FOCUS 来聚焦节点，使其显示在屏幕上
                return node.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
            }
            return false;
        }


        @Caller("scrollUp")
        public Boolean scrollUp() {
            if (node != null && node.isScrollable()) {
                // 向上滚动使用向后滚动
                return node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
            }
            return false;
        }

        @Caller("scrollDown")
        public Boolean scrollDown() {
            if (node != null && node.isScrollable()) {
                // 向下滚动使用向前滚动
                return node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            }
            return false;
        }

        @Caller("scrollLeft")
        public Boolean scrollLeft() {
            if (node != null && node.isScrollable()) {
                // 向左滚动使用向后滚动
                return node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
            }
            return false;
        }

        @Caller("scrollRight")
        public Boolean scrollRight() {
            if (node != null && node.isScrollable()) {
                // 向右滚动使用向前滚动
                return node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            }
            return false;
        }

        @Caller("children")
        public NodeApt[] children() {
            if (node != null) {
                int childCount = node.getChildCount();
                if (childCount > 0) {
                    NodeApt[] children = new NodeApt[childCount];
                    for (int i = 0; i < childCount; i++) {
                        AccessibilityNodeInfo child = node.getChild(i);
                        if (child != null) {
                            children[i] = new NodeApt(child);
                        }
                    }
                    return children;
                }
            }
            return new NodeApt[0];
        }

        @Caller("childCount")
        public Integer childCount() {
            if (node != null) {
                return node.getChildCount();
            }
            return 0;
        }

        @Caller("child")
        public NodeApt child(Number i) {
            int idx = i.intValue();
            if (node != null && idx >= 0 && idx < node.getChildCount()) {
                AccessibilityNodeInfo child = node.getChild(idx);
                if (child != null) {
                    return new NodeApt(child);
                }
            }
            return null;
        }

        @Caller("parent")
        public NodeApt parent() {
            if (node != null) {
                AccessibilityNodeInfo parent = node.getParent();
                if (parent != null) {
                    return new NodeApt(parent);
                }
            }
            return null;
        }

    }


}
