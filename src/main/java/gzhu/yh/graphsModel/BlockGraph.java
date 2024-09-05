package gzhu.yh.graphsModel;

import gzhu.yh.util.IsNumProper;
import gzhu.yh.util.Pair;
import gzhu.yh.util.TwoDArrayList;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

/**
 * @author wendao
 * @since 2024-09-03
 **/
@Getter
public class BlockGraph extends Graph {

    //block graph的特性
    //block集合
    List<List<Integer>> blocks;
    //cut verytex 集合
    List<Integer> cutVertices;

    // 设置块的集合
    public void setBlocks(List<List<Integer>> blocks) {
        this.blocks = blocks;
    }

    // 设置割点的集合
    public void setCutVertices(List<Integer> cutVertices) {
        this.cutVertices = cutVertices;
    }

    @Override
    public void setGraphType() {
        super.setGraphType("BLOCK_GRAPH");
    }

    public BlockGraph(Integer v, Integer e, List<List<Integer>> adjMatrix, List<Pair<Integer, Integer>> edges) {
        super(v, e, adjMatrix, edges);
        setGraphType();
    }

    /**
     * 生成一个包含vertexNum个顶点的block graph
     *
     * @param vertexNum
     * @return gzhu.yh.graphsModel.BlockGraph
     * @author Administrator
     * @date 2024/9/5 0005 22:25
     */
    public static BlockGraph randomGenBlockGraphByVertexNum(Integer vertexNum) {
        if (IsNumProper.isNumProper(vertexNum)) {
            throw new RuntimeException("点数输入不合理，顶点数必须在1至1000之间");
        }

        List<List<Integer>> adjMatrix = TwoDArrayList.createTwoDArrayList(vertexNum, vertexNum, 0);
        List<List<Integer>> adjList = TwoDArrayList.createTwoDArrayList(vertexNum);
        List<Pair<Integer, Integer>> edges = new ArrayList<>();
        List<Integer> cutVertices = new ArrayList<>();  //割点集合
        List<List<Integer>> blocks = new ArrayList<>(); //各个block的集合。即每个block包含的点的集合的集合

        // First, create some disjoint blocks
        int remainingVertices = vertexNum;
        while (remainingVertices > 0) {
//            int blockSize = weightedRandom(1, remainingVertices + 1);
            int blockSize = (int) (Math.random() * (remainingVertices + 1));
            for (int i = 0; i < blockSize; i++) {
                for (int j = i + 1; j < blockSize; j++) {
                    int u = vertexNum - remainingVertices + i;
                    int v = vertexNum - remainingVertices + j;
                    if (u != v) {
                        adjMatrix.get(u).set(v, 1);
                        adjMatrix.get(v).set(u, 1);
                        adjList.get(u).add(v);
                        adjList.get(v).add(u);
                        edges.add(new Pair<Integer, Integer>(u, v));
                    }
                }
            }
            List<Integer> block = new ArrayList<>();
            for (int i = vertexNum - remainingVertices; i < vertexNum - remainingVertices + blockSize; i++) {
                block.add(i);
            }
            blocks.add(block); //TODO 此方法无法保证不会出现只有一个点，并与其他割点构成block的情况，所以此处的来的集合blocks是全部的block
            remainingVertices -= blockSize;
        }
        // Second, randomly select some cut vertices to connect the blocks

        for (int i = 1; i < blocks.size(); i++) {
            int u = blocks.get(i - 1).get(blocks.get(i - 1).size() - 1);
            int v = blocks.get(i).get(0);
            if (u != v) {
                adjMatrix.get(u).set(v, 1);
                adjMatrix.get(v).set(u, 1);
                adjList.get(u).add(v);
                adjList.get(v).add(u);
                edges.add(new Pair<Integer, Integer>(u, v));
                cutVertices.add(u);
            }
        }
        BlockGraph blockGraph = new BlockGraph(vertexNum, edges.size(), adjMatrix, edges);
        blockGraph.setBlocks(blocks);
        blockGraph.setCutVertices(cutVertices);
        return blockGraph;
    }


    // 深度优先搜索寻找块和割点
   /**
    * findBlocksAndCutVertices 方法使用深度优先搜索 (DFS) 来找到图中的割点（cut vertices）和块（blocks）。割点是指如果移除该节点，图就会被分成多个连通部分。该算法类似于 Tarjan 的割点和块查找算法，具体使用了 DFS 的递归调用。
    * // TODO 不太懂这个方法
    * @param u
    * @param visited
    * @param disc
    * @param low
    * @param parent
    * @param isCutVertex
    * @param vertexStack
    * @return void
    * @author Administrator
    * @date 2024/9/5 0005 23:03
    * 方法参数：
    * int u: 当前访问的顶点。
    * boolean[] visited: 记录顶点是否被访问过，visited[i] = true 表示第 i 个顶点已经访问过。
    * int[] disc: 记录顶点的发现时间，即顶点在 DFS 中被第一次访问的时间。
    * int[] low: 记录顶点能够访问到的最早的祖先节点的时间戳，主要用于判断回边和是否存在割点。
    * int[] parent: 记录 DFS 树中顶点的父节点，parent[i] 表示顶点 i 的父节点编号。
    * boolean[] isCutVertex: 记录顶点是否是割点，isCutVertex[i] = true 表示顶点 i 是割点。
    * Stack<Integer> vertexStack: 用于存储正在遍历的顶点，用于查找块。
    * 方法逻辑：
    * 初始设置：
    *
    * 将顶点 u 标记为已访问，设置其发现时间 disc[u] 和 low[u]（初始时 disc[u] = low[u]）。
    * 将顶点 u 压入栈 vertexStack，以便之后用来找到块。
    * 递归遍历邻接节点：
    *
    * 对于每一个与 u 相连的节点 v，执行以下操作：
    * 如果 v 没有被访问过（即 !visited[v]），则将 u 作为 v 的父节点，并递归调用 findBlocksAndCutVertices 继续遍历。
    * 在递归返回时，更新 low[u]，即 u 能够到达的最早的祖先节点。low[u] = min(low[u], low[v])。
    * 割点判断：
    *
    * 根节点：如果 u 是 DFS 树的根节点（即 parent[u] == -1），并且有多个子节点（children > 1），则 u 是割点。
    * 非根节点：如果 u 不是根节点，并且满足 low[v] >= disc[u]（即从子树无法回到 u 的祖先节点），则 u 是割点。
    * 查找块：
    *
    * 如果 u 是割点，或者在遍历过程中发现 low[v] >= disc[u]，则弹出栈顶的顶点，直到 u 为止，这些顶点组成一个块。
    * 处理回边：
    *
    * 如果 v 是 u 的祖先节点（v != parent[u]），则更新 low[u]，即 low[u] = min(low[u], disc[v])。
    * 关键概念：
    * 发现时间 (disc[u])：顶点 u 在 DFS 过程中被访问的时间。
    * 最小可到达时间 (low[u])：顶点 u 能够通过后代回到的最早的祖先节点的时间戳。
    * 割点 (Cut Vertex)：如果删除该节点，会导致图分裂为多个连通部分。
    * 块 (Block)：由不含割点的顶点组成的连通子图。
   */
    public void findBlocksAndCutVertices(int u, boolean[] visited, int[] disc, int[] low, int[] parent, boolean[] isCutVertex, Stack<Integer> vertexStack) {
        int children = 0;  // 子节点计数
        visited[u] = true;  // 标记 u 为已访问
        disc[u] = low[u] = ++time;  // 记录 u 的发现时间和 low 值
        vertexStack.push(u);  // 将 u 压入栈中

        for (int v : this.getAdjList().get(u)) {  // 遍历 u 的所有邻接节点 v
            if (!visited[v]) {  // 如果 v 未被访问
                children++;  // u 的子节点数量加1
                parent[v] = u;  // 设置 v 的父节点为 u
                findBlocksAndCutVertices(v, visited, disc, low, parent, isCutVertex, vertexStack);  // 递归访问 v
                low[u] = Math.min(low[u], low[v]);  // 更新 u 的 low 值

                // 如果 u 是根节点且有多个子节点，或者 u 不是根节点且满足条件，则 u 是割点
                if (parent[u] == -1 && children > 1) {
                    isCutVertex[u] = true;
                }
                if (parent[u] != -1 && low[v] >= disc[u]) {
                    isCutVertex[u] = true;

                    // 找到一个块，将栈中节点组成的块弹出
                    List<Integer> block = new ArrayList<>();
                    while (vertexStack.peek() != u) {
                        block.add(vertexStack.pop());
                    }
                    block.add(u);
                    blocks.add(block);  // 将块添加到 blockSet 中
                }
            } else if (v != parent[u]) {  // 如果 v 是 u 的祖先
                low[u] = Math.min(low[u], disc[v]);  // 更新 low 值
            }
        }
    }

    // 计时器变量，用于追踪 DFS 时间戳
    private static int time = 0;


}