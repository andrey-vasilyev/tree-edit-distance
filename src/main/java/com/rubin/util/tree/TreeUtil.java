package com.rubin.util.tree;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.TreeTraverser;

import java.util.*;

public class TreeUtil {

    public static <T> String treeToString(TreeNode<T> node) {
        if (node.getChildren().isEmpty()) {
                return node.getData().toString();
        } else {
            String result = node.getData().toString() + "(";
            for (TreeNode<T> child : node.getChildren()) {
                    result += treeToString(child) + " ";
                }
            return result.substring(0, result.length() - 1) + ")";
        }
    }

    public static void treeFromString(TreeNode<String> node, String text) {
        final int idx = text.indexOf("(");
        if (idx == -1) {
            node.setData(text);
        } else {
            node.setData(text.substring(0, idx));
            for(String subString : split(text.substring(idx + 1, text.lastIndexOf(")")))) {
                final TreeNode<String> n = new SimpleTreeNode<>("");
                node.addChild(n);
                treeFromString(n, subString);
            }
        }
    }

    public static <T> BiMap<TreeNode<T>, Integer> postOrder(TreeNode<T> root) {
        BiMap<TreeNode<T>, Integer> result = HashBiMap.create();
        final Iterable<TreeNode<T>> treeNodes = new SimpleTreeTraverser<T>().postOrderTraversal(root);
        int i = 1;
        for (TreeNode<T> node : treeNodes) {
            result.put(node, i++);
        }
        return result;
    }

    public static <T> int treeEditDistance(TreeNode<T> root1, TreeNode<T> root2, List<TreeOperation> ops,
                                       TriFunction<TreeNode<T>, TreeNode<T>, TreeOperationType, Integer> costFunction) {
        Objects.requireNonNull(root1, "parameter root1 must not be null");
        Objects.requireNonNull(root2, "parameter root2 must not be null");
        Objects.requireNonNull(ops, "parameter ops must not be null");
        final BiMap<TreeNode<T>, Integer> order1 = postOrder(root1);
        final BiMap<TreeNode<T>, Integer> order2 = postOrder(root2);

        int[] l1 = leftMostDescendants(order1);
        int[] l2 = leftMostDescendants(order2);

        int[] kr1 = keyRoots(root1, order1);
        int[] kr2 = keyRoots(root2, order2);

        CostAndOps[][] td = new CostAndOps[order1.size() + 1][order2.size() + 1];

        for (int aKr1 : kr1) {
            for (int aKr2 : kr2) {
                forestDistance(aKr1, aKr2, l1, l2, td, order1, order2, costFunction);
            }
        }

        ops.addAll(td[order1.size()][order2.size()].getOperations());
        return td[order1.size()][order2.size()].getCost();
    }

    private static <T> int[] leftMostDescendants(BiMap<TreeNode<T>, Integer> order) {
        int[] result = new int[order.size() + 1];
        for (int i = 1; i < result.length; i++) {
            TreeNode<T> localRoot = order.inverse().get(i);
            if (localRoot.isLeaf()) {
                result[i] = i;
            } else {
                final TreeNode<T> node = new SimpleTreeTraverser<T>().postOrderTraversal(localRoot).first().orNull();
                result[i] = order.get(node);
            }
        }
        return result;
    }

    private static <T> int[] keyRoots(TreeNode<T> root, BiMap<TreeNode<T>, Integer> order) {
        TreeSet<Integer> result = new TreeSet<>();
        new SimpleTreeTraverser<T>().breadthFirstTraversal(root).forEach(node -> {
            if (node.getParent() == null) {
                result.add(order.get(node));
            } else {
                final TreeNode parent = node.getParent();
                if (parent.getChildren().indexOf(node) > 0) {
                    result.add(order.get(node));
                }
            }
        });
        int i = 0;
        int[] res = new int[result.size()];
        for (Integer num : result) {
            res[i++] = num;
        }
        return res;
    }

    private static <T> void forestDistance(int kr1, int kr2, int[] l1, int[] l2, CostAndOps[][] td,
                                       BiMap<TreeNode<T>, Integer> order1, BiMap<TreeNode<T>, Integer> order2,
                                       TriFunction<TreeNode<T>, TreeNode<T>, TreeOperationType, Integer> costFunction) {
        CostAndOps[][] fd = new CostAndOps[td.length + 1][td[0].length + 1];
        fd[l1[kr1] - 1][l2[kr2] - 1] = CostAndOps.ZERO;

        for (int di = l1[kr1]; di <= kr1; di++) {
            final Integer cost = costFunction.apply(order1.inverse().get(di),
                    order2.inverse().get(l2[kr2] - 1), TreeOperationType.DELETE);
            fd[di][l2[kr2] - 1] = fd[di - 1][l2[kr2] - 1].addOperation(SimpleTreeOperation.delete(di, cost));
        }

        for (int dj = l2[kr2]; dj <= kr2; dj++) {
            final Integer cost = costFunction.apply(order1.inverse().get(l1[kr1] - 1),
                    order2.inverse().get(dj), TreeOperationType.INSERT);
            fd[l1[kr1] - 1][dj] = fd[l1[kr1] - 1][dj - 1].addOperation(SimpleTreeOperation.insert(dj, cost));
        }

        for (int di = l1[kr1]; di <= kr1; di++) {
            for (int dj = l2[kr2]; dj <= kr2; dj++) {
                CostAndOps commonMin;
                final Integer costDelete = costFunction.apply(order1.inverse().get(di - 1), order2.inverse().get(dj),
                        TreeOperationType.DELETE);
                final Integer costInsert = costFunction.apply(order1.inverse().get(di), order2.inverse().get(dj - 1),
                        TreeOperationType.INSERT);
                if (fd[di - 1][dj].getCost() + costDelete < fd[di][dj - 1].getCost() + costInsert) {
                    commonMin = fd[di - 1][dj].addOperation(SimpleTreeOperation.delete(di, costDelete));
                } else {
                    commonMin = fd[di][dj - 1].addOperation(SimpleTreeOperation.insert(dj, costInsert));
                }
                if (l1[di] == l1[kr1] && l2[dj] == l2[kr2]) {
                    TreeOperation op;
                    if (order1.inverse().get(di).getData().equals(order2.inverse().get(dj).getData())) {
                        final int cost = costFunction.apply(order1.inverse().get(di), order2.inverse().get(dj),
                                TreeOperationType.KEEP);
                        op = SimpleTreeOperation.keep(di, dj, cost);
                    } else {
                        final int cost = costFunction.apply(order1.inverse().get(di), order2.inverse().get(dj),
                                TreeOperationType.DELETE);
                        op = SimpleTreeOperation.replace(di, dj, cost);
                    }
                    if (commonMin.getCost() < fd[di - 1][dj - 1].addOperation(op).getCost()) {
                        fd[di][dj] = commonMin;
                    } else {
                        fd[di][dj] = fd[di - 1][dj - 1].addOperation(op);
                    }
                    td[di][dj] = fd[di][dj];
                } else {
                    if (commonMin.getCost() < fd[l1[di] - 1][l2[dj] - 1].getCost() + td[di][dj].getCost()) {
                        fd[di][dj] = commonMin;
                    } else {
                        fd[di][dj] = fd[l1[di] - 1][l2[dj] - 1].addOperations(td[di][dj].getOperations());
                    }
                }
            }
        }
    }

    private static int indexOfClosingBracket(String text, int openingBracket) {
        int result = -1;
        ArrayDeque<String> stack = new ArrayDeque<>();
        for (int i = openingBracket; i < text.length(); i++) {
            if (text.charAt(i) == '(') {
                stack.push("(");
            } else if (text.charAt(i) == ')') {
                if (stack.isEmpty()) {
                    break;
                }
                stack.pop();
                if (stack.isEmpty()) {
                    result = i;
                    break;
                }
            }
        }
        return result;
    }

    private static List<String> split(String text) {
        final ArrayList<String> result = new ArrayList<>();
        String newText = "";
        Scanner scan = new Scanner(text).useDelimiter(" ");
        while (scan.hasNext()) {
            newText += scan.next();
            final int openingIdx = newText.indexOf("(");
            if (openingIdx == -1) {
                result.add(newText);
                newText = "";
            } else {
                final int closingIdx = indexOfClosingBracket(newText, openingIdx);
                if (closingIdx == -1) {
                    newText += " ";
                } else {
                    result.add(newText);
                    newText = "";
                }
            }
        }
        return result;
    }

    private static class CostAndOps {
        private final int cost;
        private final List<TreeOperation> operations;

        @SuppressWarnings("unchecked")
        public final static CostAndOps ZERO = new CostAndOps(0, Collections.EMPTY_LIST);

        public CostAndOps(int cost, List<TreeOperation> operations) {
            this.cost = cost;
            this.operations = new ArrayList<>(operations);
        }

        public int getCost() {
            return cost;
        }

        public List<TreeOperation> getOperations() {
            return operations;
        }

        public CostAndOps addOperation(TreeOperation operation) {
            final ArrayList<TreeOperation> newOperations = new ArrayList<>(this.operations);
            newOperations.add(operation);
            return new CostAndOps(this.cost + operation.getCost(), newOperations);
        }

        public CostAndOps addOperations(List<TreeOperation> operations) {
            final ArrayList<TreeOperation> newOperations = new ArrayList<>(this.operations);
            newOperations.addAll(operations);
            return new CostAndOps(this.cost + operations.stream().mapToInt(TreeOperation::getCost).sum(), newOperations);
        }

        @Override
        public String toString() {
            return String.valueOf(cost);
        }
    }

    private static class SimpleTreeTraverser<T> extends TreeTraverser<TreeNode<T>> {

        @Override
        public Iterable<TreeNode<T>> children(TreeNode<T> tTreeNode) {
            return tTreeNode.getChildren();
        }
    }
}
