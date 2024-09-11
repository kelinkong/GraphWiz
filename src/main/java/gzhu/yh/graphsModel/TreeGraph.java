package gzhu.yh.graphsModel;

import gzhu.yh.util.IsNumProper;
import gzhu.yh.util.Pair;
import gzhu.yh.util.TwoDArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author wendao
 * @since 2024-09-03
 * 树图
 **/
public class TreeGraph extends Graph{

    @Override
    public void setGraphType() {
        super.setGraphType("TREE_GRAPH");
    }

    public TreeGraph(Integer v, Integer e, List<List<Integer>> adjMatrix, List<Pair<Integer, Integer>> edges) {
        super(v, e, adjMatrix, edges);
        setGraphType("TREE_GRAPH");
    }
    public TreeGraph(Integer v, Integer e, List<List<Integer>> adjMatrix, List<List<Integer>> adjList,List<Pair<Integer, Integer>> edges) {
        super(v, e, adjMatrix, edges);
        super.setAdjList(adjList);
        setGraphType("TREE_GRAPH");
    }

    /**
     * 静态方法：根据输入顶点数量，随机生成一个树
     * @param vertexNum
     * @return gzhu.yh.graphs.TreeGraph
     * @author Administrator
     * @date 2024/9/3 0003 11:28
    */
    public static TreeGraph randomGenTreeGraphByVertexNum(int vertexNum){
        //输入顶点数，需要判断其合理性，由于gurobi性能限制,点的数量不能太大
//        assert IsNumProper.isNumProper(vertexNum): "顶点数必须在1至1000之间";
        if (IsNumProper.isNumProper(vertexNum)) {
            throw new RuntimeException("点数输入不合理，顶点数必须在1至1000之间");
        }
        Random gen = new Random();
        List<List<Integer>> adjMatrix = TwoDArrayList.createTwoDArrayList(vertexNum, vertexNum,0);
        List<List<Integer>> adjList = TwoDArrayList.createTwoDArrayList(vertexNum);
        List<Pair<Integer,Integer>> edges = new ArrayList<>();
        //从第二个点开始随机取parent
        for (int i = 1; i < vertexNum; i++) {
            //随机取parent
            int parent = gen.nextInt(i);

            //邻接矩阵,set()替换
            adjMatrix.get(i).set(parent, 1);
            adjMatrix.get(parent).set(i, 1);
            //邻接表，add()追加
            adjList.get(i).add(parent);
            adjList.get(parent).add(i);
            //往边的列表记录
            edges.add(new Pair<Integer,Integer>(i, parent));
        }
        return new TreeGraph(vertexNum, vertexNum-1, adjMatrix, adjList, edges);
    }
}
