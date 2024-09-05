package gzhu.yh.graphsModel;
// 方程

// 日志
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * @author wendao
 * @since 2024-09-02
 * 图的生成类, 用于生成图
 * 目前可以生成一般图、二部图、树、网格图、区间图、仙人掌图、块图等，
 * 生成的图可以用于项目内部计算，也可以保存邻接矩阵文件。
 *
 **/


@Slf4j
@Getter
@Setter
public class GenerateGraph {
    // TODO 用父类调用子类对象 Graph 调用其他的 xxxGraph
    /**
     * 快速生成图。目前支持的 graphType 有: BIPARTITE_GRAPH; BLOCK_GRAPH; COMMON_GRAPH; GRID_GRAPH; INTERVAL_GRAPH; TREE_GRAPH;
     * @param graphType
     * @return gzhu.yh.graphs.Graph
     * @author Administrator
     * @date 2024/9/3 0003 22:36
    */
    public static Graph generateGraph(String graphType) {
        Graph graph = null;
        Random gen = new Random();
        int vertexNum = gen.nextInt(1000); // TODO 请设置顶点数量
        switch (graphType) {
            case "BIPARTITE_GRAPH":
                // TODO 请设置XY集合数
                int xSetNum = gen.nextInt(vertexNum -1);
                graph = BipartiteGraph.randomGenBipartiteGraphByVertexNum(vertexNum, xSetNum);
                break;
            case "BLOCK_GRAPH":
                graph = BlockGraph.randomGenBlockGraphByVertexNum(vertexNum);
                break;

            case "COMMON_GRAPH":
                graph = CommonGraph.randomGenCommonGraphByVertexNum(vertexNum);
                break;
            case "GRID_GRAPH":
                int col = gen.nextInt(vertexNum);
                graph = GridGraph.genGridGraphBy(vertexNum, col);
                break;

            case "INTERVAL_GRAPH":
                graph = IntervalGraph.randomGenIntervalGraphByVertexNum(vertexNum);
                break;

            case "TREE_GRAPH":
                graph = TreeGraph.randomGenTreeGraphByVertexNum(vertexNum);
                break;

            default:
                System.out.println("输入的图类型有误");
                System.out.println(
                        "目前支持的 graphType 有:"+ " BIPARTITE_GRAPH; BLOCK_GRAPH; COMMON_GRAPH; GRID_GRAPH; INTERVAL_GRAPH; TREE_GRAPH;"
                );
                throw new RuntimeException("输入的图类型有误" + graphType);
        }
        return graph;
    }
}
