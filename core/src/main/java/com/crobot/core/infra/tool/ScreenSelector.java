package com.crobot.core.infra.tool;

import android.view.accessibility.AccessibilityNodeInfo;

public interface ScreenSelector {
    AccessibilityNodeInfo[] find(Condition condition);

    class Condition {
        public static final String BFS = "bfs";
        public static final String DFS = "dfs";
        public String algorithm = BFS;
        public boolean findOne = false;
        public String text;
        public String textContains;
        public String textStartsWith;
        public String textEndsWith;
        public String textMatches;
        public String desc;
        public String descContains;
        public String descStartsWith;
        public String descEndsWith;
        public String descMatches;
        public String id;
        public String idContains;
        public String idStartsWith;
        public String idEndsWith;
        public String idMatches;
        public String className;
        public String classNameContains;
        public String classNameStartsWith;
        public String classNameEndsWith;
        public String classNameMatches;
        public String packageName;
        public String packageNameContains;
        public String packageNameStartsWith;
        public String packageNameEndsWith;
        public String packageNameMatches;
        public Integer order;
        public Boolean clickable;
        public Boolean longClickable;
        public Boolean checkable;
        public Boolean selected;
        public Boolean enabled;
        public Boolean scrollable;
        public Boolean editable;
        public Boolean multiLine;
    }

}
