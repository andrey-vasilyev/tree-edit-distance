package com.rubin.util.tree;

import java.util.List;

public interface TreeNode<T> {
    TreeNode<T> getParent();

    List<TreeNode<T>> getChildren();

    T getData();

    void setData(T data);

    void setParent(TreeNode<T> treeNode);

    default void addChild(TreeNode<T> child) {
        getChildren().add(child);
        child.setParent(this);
    }

    default boolean isRoot() {
        return getParent() == null;
    }

    default boolean isLeaf() {
        return getChildren().size() == 0;
    }
}
