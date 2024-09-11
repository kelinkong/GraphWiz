package gzhu.yh.graphsModel;


import gzhu.yh.util.IsNumProper;
import gzhu.yh.util.Pair;
import gzhu.yh.util.TwoDArrayList;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wendao
 * @since 2024-09-02
 * grid graph 继承了 graph
 **/
@Getter
public class GridGraph extends Graph {
    private Integer lineNum;
    private Integer rowNum;

    public void setLineNum(Integer lineNum) {
        this.lineNum = lineNum;
    }

    public void setRowNum(Integer rowNum) {
        this.rowNum = rowNum;
    }

    @Override
    public void setGraphType() {
        // graphType 须为 grid graph
        super.setGraphType("GRID_GRAPH");
    }
    //只创建邻接矩阵
    public GridGraph(Integer v, Integer e, List<List<Integer>> adjMatrix, List<Pair<Integer, Integer>> edges) {
        super(v, e, adjMatrix, edges);
        setGraphType();
    }
    //邻接表也创建
    public GridGraph(Integer v, Integer e, List<List<Integer>> adjMatrix, List<List<Integer>> adjList,List<Pair<Integer, Integer>> edges) {
        super(v, e, adjMatrix, edges);
        super.setAdjList(adjList);
        setGraphType();
    }
    /**
     * 静态方法：根据行数与列数生成网格图 grid graph
     * @param row
     * @param col
     * @return gzhu.yh.graphs.GridGraph
     * @author Administrator
     * @date 2024/9/3 0003 10:25
    */
    public static GridGraph genGridGraphBy(int row, int col){
        //输入顶点数，需要判断其合理性，由于gurobi性能限制,点的数量不能太大
        if (IsNumProper.isNumProper(row)) {
            throw new RuntimeException("行数输入不合理，必须在1至1000之间");
        }
        if (IsNumProper.isNumProper(col)) {
            throw new RuntimeException("列数输入不合理，必须在1至1000之间");
        }

        List<List<Integer>> adjMatrix = TwoDArrayList.createTwoDArrayList(row * col, row * col,0);
        List<List<Integer>> adjList = TwoDArrayList.createTwoDArrayList(row * col);
        List<Pair<Integer,Integer>> edges = new ArrayList<>();
        for (int i = 0; i < col; i++) {
            for (int j = 0; j < col; j++) {
                int currenVertex = i * row + j;//当前顶点
                //从第二行开始为每个顶点添加向上的边
                if(i>0){
                    int upVertex = (i-1) * row + j;
                    //邻接矩阵
                    adjMatrix.get(currenVertex).set(upVertex, 1);
                    adjMatrix.get(upVertex).set(currenVertex, 1);
                    //邻接表
                    adjList.get(currenVertex).add(upVertex);
                    adjList.get(upVertex).add(currenVertex);
                    //边表
                    edges.add(new Pair<Integer,Integer>(currenVertex, upVertex));
                }
                //从第二行开始为每个顶点添加想左的边
                if(j>0){
                    int leftVertex = i * row + (j-1);
                    //邻接矩阵
                    adjMatrix.get(currenVertex).set(leftVertex, 1);
                    adjMatrix.get(leftVertex).set(currenVertex, 1);
                    //邻接表
                    adjList.get(currenVertex).add(leftVertex);
                    adjList.get(leftVertex).add(currenVertex);
                    //边表
                    edges.add(new Pair<Integer,Integer>(currenVertex, leftVertex));
                }
            }
        }

//        Integer edgeNum = (col-1)*row+ (col-1)*row;
        Integer edgeNum = edges.size();
        GridGraph gridGraph= new GridGraph(col * row, edgeNum, adjMatrix, adjList, edges);
        gridGraph.setLineNum(row);
        gridGraph.setRowNum(col);
        return gridGraph;
    }
}
