package gzhu.yh.graphsModel;

import gzhu.yh.util.IsNumProper;
import gzhu.yh.util.Pair;
import gzhu.yh.util.TwoDArrayList;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * @author wendao
 * @since 2024-09-02
 * common graph 一般图
 **/
@Getter
public class CommonGraph extends Graph{
    @Override
    public void setGraphType() {
        // graphType 须为 common graph
        super.setGraphType("COMMON_GRAPH");
    }
    public CommonGraph(Integer v, Integer e, List<List<Integer>> adjMatrix, List<Pair<Integer, Integer>> edges) {
        super(v, e, adjMatrix, edges);
        setGraphType();
    }
    public CommonGraph(Integer v, Integer e, List<List<Integer>> adjMatrix, List<List<Integer>> adjList,List<Pair<Integer, Integer>> edges) {
        super(v, e, adjMatrix, edges);
        super.setAdjList(adjList);
        setGraphType();
    }
    /**
     * 静态方法：根据顶点数量要求,快速随机生成一个一般图
     * @param vertexNum 顶点数
     * @return gzhu.yh.graphs.CommonGraph 一个一般图
     * @author Administrator
     * @date 2024/9/3 0003 10:11
    */
    public static CommonGraph randomGenCommonGraphByVertexNum(int vertexNum){
        //输入顶点数，需要判断其合理性，由于gurobi性能限制,点的数量不能太大
        if (IsNumProper.isNumProper(vertexNum)) {
            throw new RuntimeException("点数输入不合理，顶点数必须在1至1000之间");
        }
        Random gen = new Random();
        List<List<Integer>> adjMatrix = TwoDArrayList.createTwoDArrayList(vertexNum, vertexNum,0);
        List<List<Integer>> adjList = TwoDArrayList.createTwoDArrayList(vertexNum);
        List<Pair<Integer,Integer>> edges = new ArrayList<>();
        for (int i = 0; i < vertexNum; i++) {
            //无向图，所以是j=i+1
            for (int j = i+1; j < vertexNum; j++) {
                //在每个顶点对之间以30%的概率生成一条边。
                // 生成一个0-1之间的随机数，但是math.Random()的范围是[0,1)，娶不到1
//                if ((Math.random()) < 0.3) {
//                    //生成边
//                    adjMatrix.get(i).set(j, 1);
//                    adjMatrix.get(j).set(i, 1);
//                    //生成边的列表
//                    edges.add(new Pair<>(i, j));
//                }
                if (gen.nextDouble() < 0.3) {
                    //邻接矩阵,set()替换
                    adjMatrix.get(i).set(j, 1);
                    adjMatrix.get(j).set(i, 1);
                    //邻接表，add()追加
                    adjList.get(i).add(j);
                    adjList.get(j).add(i);

                    //往边的列表记录
                    edges.add(new Pair<Integer,Integer>(i, j));
                }
            }
        }
        return new CommonGraph(vertexNum, edges.size(), adjMatrix, adjList, edges);
    }
}
