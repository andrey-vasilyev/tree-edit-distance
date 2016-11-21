package com.rubin.util.tree;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TreeUtilTest {

    @Test
    public void treeEditDistance1() {
        TreeNode<String> T1 = new SimpleTreeNode<>("");
        TreeUtil.treeFromString(T1, "c(b(a))");

        TreeNode<String> T2 = new SimpleTreeNode<>("");
        TreeUtil.treeFromString(T2, "c(b(a d))");

        List<TreeOperation> ops = new ArrayList<>();
        assertEquals(1, TreeUtil.treeEditDistance(T1, T2, ops, new SimpleCostFunction<>()));
    }

    @Test
    public void treeEditDistance2() {
        TreeNode<String> T1 = new SimpleTreeNode<>("");
        TreeUtil.treeFromString(T1, "f(d(a c(b)) e)");

        TreeNode<String> T2 = new SimpleTreeNode<>("");
        TreeUtil.treeFromString(T2, "f(c(d(a b)) e)");

        List<TreeOperation> ops = new ArrayList<>();
        assertEquals(2, TreeUtil.treeEditDistance(T1, T2, ops, new SimpleCostFunction<>()));
    }

    @Test
    public void treeEditDistance3() {
        TreeNode<String> T1 = new SimpleTreeNode<>("");
        TreeUtil.treeFromString(T1, "a(b(d(g) e(h i j k)) c(f(l(m n))))");

        TreeNode<String> T2 = new SimpleTreeNode<>("");
        TreeUtil.treeFromString(T2, "a(b(d(g)) c(f(l)) hello(world))");

        List<TreeOperation> ops = new ArrayList<>();
        assertEquals(9, TreeUtil.treeEditDistance(T1, T2, ops, new SimpleCostFunction<>()));
    }
}
