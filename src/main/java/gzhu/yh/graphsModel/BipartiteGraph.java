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
 * bipartite graph 二部图 继承了 Graph
 **/
public class BipartiteGraph extends Graph{
    /* TODO 创建属于子类BipartiteGraph的示例变量顶点集合X 与集合Y*/
    @Override
    public void setGraphType() {
        super.setGraphType("BIPARTITE_GRAPH");
    }

    public BipartiteGraph(Integer v, Integer e, List<List<Integer>> adjMatrix, List<Pair<Integer, Integer>> edges) {
        super(v, e, adjMatrix, edges);
        setGraphType();
    }
    /**
     * 静态方法：根据顶点数量的要求快速随机生成一个二部图
     * @param vertexNum 顶点数量要求
     * @param xNum 集合x的顶点数。集合 y= vertexNum - xNum
     * @return gzhu.yh.graphs.BipartiteGraph 一个二分图
     * @author Administrator
     * @date 2024/9/3 0003 10:09
    */
    public static BipartiteGraph randomGenBipartiteGraphByVertexNum(Integer vertexNum,Integer xNum) {
        //输入顶点数，需要判断其合理性，由于gurobi性能限制,点的数量不能太大
        if (IsNumProper.isNumProper(vertexNum)) {
            throw new RuntimeException("点数输入不合理，顶点数需要在0-1000之间");
        }
        if (xNum >= 1 && vertexNum < vertexNum) {
            throw new RuntimeException("x集合的点数输入不合理，二部图的单个集合大小需要大于1且小于顶点数");
        }


        Random gen = new Random();
        List<List<Integer>> adjMatrix = TwoDArrayList.createTwoDArrayList(vertexNum, vertexNum,0);
        List<Pair<Integer,Integer>> edges = new ArrayList<>();
        for (int i = 0; i < vertexNum; i++) {
            //无向图，所以是j=i+1
            for (int j = xNum; j < vertexNum; j++) {
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
                    //生成边
                    adjMatrix.get(i).set(j, 1);
                    adjMatrix.get(j).set(i, 1);
                    //往边的列表记录
                    edges.add(new Pair<Integer,Integer>(i, j));
                }
            }
        }
        /* TODO 划分X Y 集合*/
        return new BipartiteGraph(vertexNum, edges.size(), adjMatrix, edges);
    }
}
