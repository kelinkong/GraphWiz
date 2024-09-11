package gzhu.yh.graphDrawing;


import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;
import gzhu.yh.util.Pair;
import org.graphstream.graph.implementations.SingleGraph;
import gzhu.yh.graphsModel.Graph;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wendao
 * @since 2024-09-10
 * 推荐GraphStream
 * GraphStream 适合处理动态图，能够直接显示图形并支持实时更新。
 * JGraphX 提供了更强大的布局功能，并且适用于需要交互的图形展示。
 * 你可以根据需要选择其中一个工具进行图的可视化。如果你想在较短时间内展示一个静态图，GraphStream 是个不错的选择。如果你需要对图进行更多自定义和布局调整，JGraphX 是更好的选择。
 **/
public class DrawingGraph {
    //绘制图形GraphStream的图
    // 使用 GraphStream 进行可视化，基于自定义 Graph 结构
    public static void visualizeGraphGraphStream(Graph graph) {
        // 创建 GraphStream 的图
        org.graphstream.graph.Graph gsGraph = new SingleGraph("Undirected Graph");
        // 设置布局算法和样式
        gsGraph.addAttribute("ui.stylesheet", "node { fill-color: grey; size: 15px; text-size: 20px; text-color: black; } edge { fill-color: grey; }");

        int v= graph.getV();
        // 添加顶点
        for (int i = 0; i < v; i++) {
            org.graphstream.graph.Node node = gsGraph.addNode(String.valueOf(i));
            node.addAttribute("ui.label", String.valueOf(i)); // 为每个节点添加编号作为标签
        }


        // 添加边
//        for (Pair<Integer, Integer> edge : graph.getEdges()) {
//            Integer source = edge.getFirst();
//            Integer target = edge.getSecond();
//            gsGraph.addEdge(source + "-" + target, source.toString(), target.toString());
//        }
        for (Pair<Integer, Integer> edge : graph.getEdges()) {
            Integer source = edge.getFirst();
            Integer target = edge.getSecond();
            String edgeId = source + "-" + target;

            // 防止重复边
            if (gsGraph.getEdge(edgeId) == null) {
                gsGraph.addEdge(edgeId, source.toString(), target.toString());
            }
        }
        // 设置布局和图的显示属性
        gsGraph.display();
    }
    /**
     * 在@Test环境中也能保持窗口
     * @param graph
     * @return void
     * @author Administrator
     * @date 2024/9/10 0010 17:25
    */
    public static void visualizeGraphGraphStreamForTest(Graph graph) {
        // 创建 GraphStream 的图
        org.graphstream.graph.Graph gsGraph = new SingleGraph("Undirected Graph");
        int v= graph.getV();
        // 添加顶点
        for (int i = 0; i < v; i++) {
            gsGraph.addNode(String.valueOf(i));
        }

        // 添加边
        for (Pair<Integer, Integer> edge : graph.getEdges()) {
            Integer source = edge.getFirst();
            Integer target = edge.getSecond();
            gsGraph.addEdge(source + "-" + target, source.toString(), target.toString());
        }

        // 设置布局和图的显示属性
        gsGraph.display();
        // 延迟 100000 毫秒（100秒）后关闭窗口
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //JGraphX 绘图对象
    public static void visualizeGraphJGraphX(Graph graph) {
        // 创建 JGraphX 绘图对象
        com.mxgraph.view.mxGraph mxGraph = new com.mxgraph.view.mxGraph();
        Object parent = mxGraph.getDefaultParent();
        mxGraph.getModel().beginUpdate();

        try {
            // 创建顶点映射，保存 Graph 顶点和 JGraphX 顶点的关系
            Map<Integer, Object> vertexMap = new HashMap<>();
            int num =graph.getV();
            for (int i = 0; i < num; i++) {
                Object v = mxGraph.insertVertex(parent, null, String.valueOf(i), 0, 0, 30, 30);
                vertexMap.put(i, v);
            }

            // 添加无向边
            for (Pair<Integer, Integer> edge : graph.getEdges()) {
                Integer source = edge.getFirst();
                Integer target = edge.getSecond();
                mxGraph.insertEdge(parent, null, "", vertexMap.get(source), vertexMap.get(target));
            }
        } finally {
            mxGraph.getModel().endUpdate();
        }

        // 设置布局
        mxCircleLayout layout = new mxCircleLayout(mxGraph);
        layout.execute(mxGraph.getDefaultParent());

        // 显示图形
        JFrame frame = new JFrame("Undirected Graph Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mxGraphComponent graphComponent = new mxGraphComponent(mxGraph);
        frame.getContentPane().add(graphComponent);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}
