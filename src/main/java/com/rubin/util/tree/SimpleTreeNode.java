package com.rubin.util.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SimpleTreeNode<T> implements TreeNode<T> {

    private T data;
    private TreeNode<T> parent;
    private List<TreeNode<T>> children;

    public SimpleTreeNode(T data) {
        this(data, null);
    }

    public SimpleTreeNode(TreeNode<T> tree) {
        final SimpleTreeNode<T> copy = copy(tree);
        this.data = copy.data;
        this.children = copy.children;
        this.children.forEach(child -> child.setParent(this));
    }

    public SimpleTreeNode(T data, TreeNode<T> parent) {
        this.data = data;
        this.parent = parent;
        this.children = new ArrayList<>();
    }

    @Override
    public TreeNode<T> getParent() {
        return parent;
    }

    @Override
    public List<TreeNode<T>> getChildren() {
        return children;
    }

    @Override
    public T getData() {
        return data;
    }

    @Override
    public void setParent(TreeNode<T> treeNode) {
        this.parent = treeNode;
    }

    @Override
    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return data.toString();
    }

    @SuppressWarnings("unchecked")
    private <V> SimpleTreeNode<V> copy(TreeNode<V> tree) {
        final List<SimpleTreeNode<V>> childrenCopy =
                tree.getChildren().stream().map((Function<TreeNode<V>, SimpleTreeNode<V>>) this::copy).collect(Collectors.toList());
        final SimpleTreeNode rootCopy = new SimpleTreeNode(tree.getData(), null);
        childrenCopy.forEach(rootCopy::addChild);
        return rootCopy;
    }
}
