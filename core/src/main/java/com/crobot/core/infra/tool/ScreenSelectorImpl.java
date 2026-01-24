package com.crobot.core.infra.tool;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ScreenSelectorImpl implements ScreenSelector {
    private static final String BFS = "bfs";
    private static final String DFS = "dfs";
    private AccessibilityService service;

    public ScreenSelectorImpl(AccessibilityService accessibility) {
        this.service = accessibility;
    }

    private AccessibilityNodeInfo getRoot() {
        return service.getRootInActiveWindow();
    }

    //广度搜索
    private void bfs(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> result, Function<AccessibilityNodeInfo, Boolean> match, boolean findOne) {
        Deque<AccessibilityNodeInfo> queue = new LinkedList<>();
        queue.addLast(node);
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int j = 0; j < size; j++) {
                AccessibilityNodeInfo item = queue.removeFirst();
                if (match.apply(item)) {
                    result.add(item);
                    if (findOne) {
                        return;
                    }
                }
                int childCount = item.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    queue.addLast(item.getChild(i));
                }
            }
        }
    }

    //深度搜索
    private void dfs(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> result, Function<AccessibilityNodeInfo, Boolean> match, boolean findOne) {
        if (node == null) {
            return;
        }
        if (match.apply(node)) {
            result.add(node);
            if (findOne) {
                return;
            }
        }
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            dfs(node.getChild(i), result, match, findOne);
        }
    }


    private List<AccessibilityNodeInfo> find2(Condition condition) {
        List<AccessibilityNodeInfo> result = new ArrayList<>();
        AccessibilityNodeInfo root = getRoot();
        boolean findOne = condition.findOne;
        String algorithm = condition.algorithm;
        Function<AccessibilityNodeInfo, Boolean> match = node -> {
            boolean ret = true;
            if (ret && condition.text != null) {
                ret = node.getText() == null ? false : Objects.equals(condition.text, node.getText().toString());
            }
            if (ret && condition.textContains != null) {
                ret = node.getText() == null ? false : node.getText().toString().contains(condition.textContains);
            }
            if (ret && condition.textStartsWith != null) {
                ret = node.getText() == null ? false : node.getText().toString().startsWith(condition.textStartsWith);
            }
            if (ret && condition.textEndsWith != null) {
                ret = node.getText() == null ? false : node.getText().toString().endsWith(condition.textEndsWith);
            }
            if (ret && condition.textMatches != null) {
                ret = node.getText() == null ? false : node.getText().toString().matches(condition.textMatches);
            }

            if (ret && condition.desc != null) {
                ret = node.getContentDescription() == null ? false : Objects.equals(condition.desc, node.getContentDescription().toString());
            }
            if (ret && condition.descContains != null) {
                ret = node.getContentDescription() == null ? false : node.getContentDescription().toString().contains(condition.descContains);
            }
            if (ret && condition.descStartsWith != null) {
                ret = node.getContentDescription() == null ? false : node.getContentDescription().toString().startsWith(condition.descStartsWith);
            }
            if (ret && condition.descEndsWith != null) {
                ret = node.getContentDescription() == null ? false : node.getContentDescription().toString().endsWith(condition.descEndsWith);
            }
            if (ret && condition.descMatches != null) {
                ret = node.getContentDescription() == null ? false : node.getContentDescription().toString().matches(condition.descMatches);
            }


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ret && condition.id != null) {
                    ret = node.getUniqueId() == null ? false : Objects.equals(condition.id, node.getUniqueId());
                }
                if (ret && condition.idContains != null) {
                    ret = node.getUniqueId() == null ? false : node.getUniqueId().contains(condition.idContains);
                }
                if (ret && condition.idStartsWith != null) {
                    ret = node.getUniqueId() == null ? false : node.getUniqueId().startsWith(condition.idStartsWith);
                }
                if (ret && condition.idEndsWith != null) {
                    ret = node.getUniqueId() == null ? false : node.getUniqueId().endsWith(condition.idEndsWith);
                }
                if (ret && condition.idMatches != null) {
                    ret = node.getUniqueId() == null ? false : node.getUniqueId().matches(condition.idMatches);
                }
            }
            if (ret && condition.className != null) {
                ret = node.getClassName() == null ? false : Objects.equals(condition.className, node.getClassName().toString());
            }
            if (ret && condition.classNameContains != null) {
                ret = node.getClassName() == null ? false : node.getClassName().toString().contains(condition.classNameContains);
            }
            if (ret && condition.classNameStartsWith != null) {
                ret = node.getClassName() == null ? false : node.getClassName().toString().startsWith(condition.classNameStartsWith);
            }
            if (ret && condition.classNameEndsWith != null) {
                ret = node.getClassName() == null ? false : node.getClassName().toString().endsWith(condition.classNameEndsWith);
            }
            if (ret && condition.classNameMatches != null) {
                ret = node.getClassName() == null ? false : node.getClassName().toString().matches(condition.classNameMatches);
            }

            if (ret && condition.packageName != null) {
                ret = node.getPackageName() == null ? false : Objects.equals(condition.packageName, node.getPackageName().toString());
            }
            if (ret && condition.packageNameContains != null) {
                ret = node.getPackageName() == null ? false : node.getPackageName().toString().contains(condition.packageNameContains);
            }
            if (ret && condition.packageNameStartsWith != null) {
                ret = node.getPackageName() == null ? false : node.getPackageName().toString().startsWith(condition.packageNameStartsWith);
            }
            if (ret && condition.packageNameEndsWith != null) {
                ret = node.getPackageName() == null ? false : node.getPackageName().toString().endsWith(condition.packageNameEndsWith);
            }
            if (ret && condition.packageNameMatches != null) {
                ret = node.getPackageName() == null ? false : node.getPackageName().toString().matches(condition.packageNameMatches);
            }
            if (ret && condition.order != null) {
                ret = Objects.equals(condition.order, node.getDrawingOrder());
            }
            if (ret && condition.clickable != null) {
                ret = Objects.equals(condition.clickable, node.isClickable());
            }

            if (ret && condition.longClickable != null) {
                ret = Objects.equals(condition.longClickable, node.isLongClickable());
            }
            if (ret && condition.checkable != null) {
                ret = Objects.equals(condition.checkable, node.isCheckable());
            }
            if (ret && condition.selected != null) {
                ret = Objects.equals(condition.selected, node.isSelected());
            }
            if (ret && condition.enabled != null) {
                ret = Objects.equals(condition.enabled, node.isEnabled());
            }
            if (ret && condition.scrollable != null) {
                ret = Objects.equals(condition.scrollable, node.isScrollable());
            }
            if (ret && condition.editable != null) {
                ret = Objects.equals(condition.editable, node.isEditable());
            }
            if (ret && condition.multiLine != null) {
                ret = Objects.equals(condition.multiLine, node.isMultiLine());
            }
            return ret;
        };
        if (BFS.equals(algorithm)) {
            bfs(root, result, match, findOne);
        } else {
            dfs(root, result, match, findOne);
        }
        return result;
    }

    @Override
    public AccessibilityNodeInfo[] find(Condition condition) {
        List<AccessibilityNodeInfo> nodes = this.find2(condition);
        return nodes.toArray(new AccessibilityNodeInfo[nodes.size()]);
    }
}
