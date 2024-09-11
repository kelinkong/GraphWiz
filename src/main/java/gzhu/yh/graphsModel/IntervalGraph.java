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
 *
 * 去见图
 **/
public class IntervalGraph extends Graph{

    @Override
    public void setGraphType() {
        super.setGraphType("INTERVAL_GRAPH");
    }

    public IntervalGraph(Integer v, Integer e, List<List<Integer>> adjMatrix, List<List<Integer>> adjList,List<Pair<Integer, Integer>> edges) {
        super(v, e, adjMatrix, edges);
        super.setAdjList(adjList);
        setGraphType();
    }

    public static IntervalGraph randomGenIntervalGraphByVertexNum(Integer vertexNum){
//        assert IsNumProper.isNumProper(vertexNum): "顶点数必须在1至1000之间";
        if (IsNumProper.isNumProper(vertexNum)) {
            throw new RuntimeException("点数输入不合理，顶点数必须在1至1000之间");
        }
        Random gen = new Random();
        List<List<Integer>> adjMatrix = TwoDArrayList.createTwoDArrayList(vertexNum, vertexNum,0);
        List<List<Integer>> adjList = TwoDArrayList.createTwoDArrayList(vertexNum);
        List<Pair<Integer,Integer>> edges = new ArrayList<>();
        Integer maxIndex=0;
        for (Integer i = 0; i < vertexNum; i++) {
            // 在i和 n-1之间生成一个随机数
            //maxIndex = Math.random()*(vertexNum-1 + 1 -i)+i;
            maxIndex = (int) (Math.random()*(vertexNum-i)+i);
            for (int j = maxIndex-1; j >= i ; j--) {
                // 邻接矩阵,set()替换
                adjMatrix.get(maxIndex).set(j,1);
                adjMatrix.get(j).set(maxIndex,1);
                //邻接表，add()追加
                adjList.get(maxIndex).add(j);
                adjList.get(j).add(maxIndex);
                //边表
                edges.add(new Pair<Integer,Integer>(maxIndex,j));
            }
        }
        return new IntervalGraph(vertexNum,edges.size(),adjMatrix,adjList,edges);
    }
}
