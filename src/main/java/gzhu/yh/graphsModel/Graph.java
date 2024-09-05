package gzhu.yh.graphsModel;

import gzhu.yh.util.Pair;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wendao
 * @since 2024-09-02
 * 通用的图描述类，即图的基类
 **/
@Getter
@Setter
@NoArgsConstructor
abstract public class Graph {
    //顶点数
    private Integer v;
    //边数
    private Integer e;
    //邻接矩阵（无向图）  存放形式为[[0,1,1],[1,0,0],[1,0,0]]
//    private List<ArrayList<Integer>> adjMatrix ;
    private List<List<Integer>> adjMatrix ;
    //邻接表（无向图） 存放形式为[[1,2],[1],[1]]
//    private List<ArrayList<Integer>> adjList;
    private List<List<Integer>> adjList;
    //图中的顶点列表
    private List<Integer> vertices;
    //图的类型 例如 "Common Graph"、"Bipartite Graph" 等。
    private String graphType; /* TODO 可以考虑改成枚举*/
    //边的列表
    private List<Pair<Integer, Integer>> edges;
    //图的最大度数
    private Integer maxDegree;
    //图的最小度数
    private Integer minDegree;

    //抽象方法，强制其子类重写图的类型，提醒作用
    public abstract void setGraphType();

    /*
    * 伪全构造函数（没包括最大度、最小度）
    * graphType 由子类写
    * */
    public Graph(Integer v, Integer e, List<List<Integer>> adjMatrix,  List<Pair<Integer, Integer>> edges) {
        //输入顶点数，需要判断其合理性，由于gurobi性能限制,点的数量不能太大
        if (v.intValue() >= 1 && v.intValue() <= 1000){throw new RuntimeException("点数输入不合理，需要大于1且小于1000");}
        this.v = v;
        this.e = e;
        this.adjMatrix = adjMatrix;
        this.edges = edges;
    }

    {
        //匿名代码块，每次生成对象时均会执行，优先于构造函数执行，用于在构造前初始化对象
        // 包装类默认值为null。所以注意Integer与int的区别
        v = 0;
        e = 0;
    }
}
