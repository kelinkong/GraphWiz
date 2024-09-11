package gzhu.yh.model.maximalRomanDomination;

import com.gurobi.gurobi.GRB;
import com.gurobi.gurobi.GRBEnv;
import com.gurobi.gurobi.GRBException;
import com.gurobi.gurobi.GRBLinExpr;
import com.gurobi.gurobi.GRBModel;
import com.gurobi.gurobi.GRBVar;
import gzhu.yh.graphsModel.Graph;
import gzhu.yh.util.Pair;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import java.util.List;

/**
 * @author wendao
 * @since 2024-09-04
 * //
 * <p>
 * maxiaml roman domiation的ILP方程
 *      varialble:
 *          x0_v, x1_v, x2_v 是三种赋值状态的二元变量。v赋值分别为0，1，2时等于1表示.否则为0
 *          y[i] 是辅助变量，用于检测是否满足特定条件。表示顶点v是否被满足了条件 "其闭邻域内没有赋值为 0"
 *      minumum: x1_v + 2 * x2_v
 *      subject to:for v in V
 *      x0_v + x1_v + x2_v=0
 *      x0_v <= sum(x2_u) for u in N(v)
 *      y_v >= x1_v + x2_v - sum(x0_u) for u in N(v)
 *      y_v <= 1 - x0_v
 *      y_v <= 1 - max(x0_u) for u in N(v)  //当邻域有0， max(x0_u)=1, 否则为0
 *      1 <= sum((x1_v + x1_v) * y_v) for u in N(v)
 *
 **/
public class ILP_MDRD_Modify_DrawingOnGraph {
    public static void ILP_MDRD_DrawingOnGraph(Graph graph){
        try {
            // 创建环境
            GRBEnv env = new GRBEnv(true);
            env.set("logFile", "src/main/java/gzhu/yh/logger/ILP_MDRD.log"); //设置日志文件
            env.start();

            // 创建模型
            GRBModel model = new GRBModel(env);
            model.set(GRB.StringAttr.ModelName, "ILP_MDRD");
            // 获取图的属性
            int numVertices = graph.getV(); // 顶点数
            List<List<Integer>> adjMatrix = graph.getAdjMatrix(); // 邻接矩阵

            // 定义变量
            GRBVar[][] x = new GRBVar[numVertices][3]; // 0: x_v^0, 1: x_v^1, 2: x_v^2
            GRBVar[] y = new GRBVar[numVertices]; // 辅助变量 y_w,但前点是否被V0控制

            for (int v = 0; v < numVertices; v++) {
                x[v][0] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x_" + v + "_0");
                x[v][1] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x_" + v + "_1");
                x[v][2] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x_" + v + "_2");
                y[v] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "y_" + v);
            }

            // 约束1：每个顶点只能被赋值为0、1或2其中的一个
            // x0_v + x1_v + x2_v=0
            for (int v = 0; v < numVertices; v++) {
                GRBLinExpr constraint1 = new GRBLinExpr();
                constraint1.addTerm(1.0, x[v][0]);
                constraint1.addTerm(1.0, x[v][1]);
                constraint1.addTerm(1.0, x[v][2]);
                model.addConstr(constraint1, GRB.EQUAL, 1.0,"约束1：每个顶点只能被赋值为0、1或2其中的一个");
            }

            // 约束2：赋值为0的顶点至少有一个邻接点赋值为2
            // x0_v <= sum(x2_u) for u in N(v)
            for (int v = 0; v < numVertices; v++) {
                GRBLinExpr constraint2 = new GRBLinExpr();
                for (int u = 0; u < numVertices; u++) {
                    if (adjMatrix.get(v).get(u) == 1) {
                        constraint2.addTerm(1.0, x[u][2]);
                    }
                }
                model.addConstr(x[v][0], GRB.LESS_EQUAL, constraint2,"约束2：赋值为0的顶点至少有一个邻接点赋值为2");
            }

            // 约束3：辅助变量 y 的约束，确保不受0控制的，顶点 v 的邻域中没有顶点被赋值为0。
            // y_v >= x1_v + x2_v - sum(x0_u) for u in N(v)
            for (int i = 0; i < numVertices; i++) {
                GRBLinExpr constraint3 = new GRBLinExpr();
                constraint3.addTerm(1.0,x[i][1]);
                constraint3.addTerm(1.0,x[i][2]);
                for (int j = 0; j < numVertices; j++) {
                    if(adjMatrix.get(i).get(j) ==1){
                        constraint3.addTerm(-1.0,x[j][0]);
                    }
                }
                model.addConstr(y[i], GRB.GREATER_EQUAL, constraint3,"辅助变量 y 的约束，确保不受0控制的，顶点 v 的邻域中没有顶点被赋值为0");
            }

            // 约束4：当顶点 v 被赋值为0时，y_v必须为0
            // y_v <= 1 - x0_v
            for (int i = 0; i < numVertices; i++) {
                GRBLinExpr constraint4 = new GRBLinExpr();
                constraint4.addTerm(1.0,y[i]);
                constraint4.addTerm(1.0,x[i][0]);

                model.addConstr(constraint4, GRB.LESS_EQUAL, 1,"当顶点 v 被赋值为0时，y_v必须为0");
            }
            // 约束5: 如果顶点 v 的邻域中有顶点被赋值为0，则 y_v 必须为0
            // y_v <= 1 - max(x0_u) for u in N(v)
            /*for (int i = 0; i < numVertices; i++) {
                GRBLinExpr maxConstraint = new GRBLinExpr();
                for (int j = 0; j < numVertices; j++) {
                    if(adjMatrix.get(i).get(j) ==1){
                        maxConstraint.addTerm(1.0,x[j][0]);
                    }
                }
                model.addConstr(y[i], GRB.GREATER_EQUAL, 1-maxConstraint,"辅助变量 y 的约束，确保不受0控制的，顶点 v 的邻域中没有顶点被赋值为0");
            }*/
            //模拟max()函数
            for (int i = 0; i < numVertices; i++) {
                GRBVar maxVal = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "maxVal");
                for (int j = 0; j < numVertices; j++) {
                    if(adjMatrix.get(i).get(j) ==1){
                        model.addConstr(maxVal, GRB.GREATER_EQUAL,x[j][0],"取邻点u的x[u][0]的最大值");
                    }
                }
                GRBLinExpr constraint5 = new GRBLinExpr();
                constraint5.addTerm(1.0,maxVal);
                constraint5.addTerm(1.0,y[i]);
                model.addConstr(constraint5, GRB.LESS_EQUAL, 1,"辅助变量 y 的约束，确保不受0控制的，顶点 v 的邻域中没有顶点被赋值为0");
            }
            // 约束6: 至少有一个顶点被赋值为1或2，且其邻域内没有赋值为0的顶点
            // 1 <= sum((x1_v + x1_v) * y_v) for u in N(v)
            // 1 <= sum(y_v)

            GRBLinExpr constraint6 = new GRBLinExpr();
            for (int i = 0; i < numVertices; i++) {
//                constraint6.addTerm(x[i][1].get(GRB.DoubleAttr.X), y[i]);
//                constraint6.addTerm(x[i][2].get(GRB.DoubleAttr.X), y[i]);
                constraint6.addTerm(1.0, y[i]);
            }
            model.addConstr(constraint6, GRB.GREATER_EQUAL, 1,"至少有一个不被0控制的点");

            // 目标函数：最小化赋值总和
            GRBLinExpr objective = new GRBLinExpr();
            for (int v = 0; v < numVertices; v++) {
                objective.addTerm(0.0, x[v][0]);
                objective.addTerm(1.0, x[v][1]);
                objective.addTerm(2.0, x[v][2]);
            }
            model.setObjective(objective, GRB.MINIMIZE);

            // 优化模型
            model.optimize();

            // 输出结果
            for (int v = 0; v < numVertices; v++) {
                System.out.print("Vertex " + v + ": x_" + v + "_0 = " + x[v][0].get(GRB.DoubleAttr.X));
                System.out.print(", x_" + v + "_1 = " + x[v][1].get(GRB.DoubleAttr.X));
                System.out.print(", x_" + v + "_2 = " + x[v][2].get(GRB.DoubleAttr.X)+")");
                System.out.println(", y_" + v + y[v].get(GRB.DoubleAttr.X));
            }

            //System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));
            //System.out.println("Runtime: " + model.get(GRB.DoubleAttr.Runtime));


            //画图
            // 创建 GraphStream 的图
            org.graphstream.graph.Graph gsGraph = new SingleGraph("Undirected Graph");
            // 设置布局算法和样式
            gsGraph.addAttribute("ui.stylesheet", "node { fill-color: grey; size: 15px; text-size: 20px; text-color: black; } edge { fill-color: grey; }");

            // 启用高质量显示
            gsGraph.addAttribute("ui.quality");
            gsGraph.addAttribute("ui.antialias");


            int v= graph.getV();
            // 添加顶点
            for (int i = 0; i < v; i++) {
                org.graphstream.graph.Node node = gsGraph.addNode(String.valueOf(i));
                String label;
                if((int)y[i].get(GRB.DoubleAttr.X)==1){label = "T";}
                else{label = "F";}
                // 为每个节点添加编号作为标签，加上gurobi计算结果
                if ((int)x[i][1].get(GRB.DoubleAttr.X) == 1){
                    node.addAttribute("ui.label", "("+String.valueOf(i)+")"+ " 1, " +label);
                } else if ((int)x[i][2].get(GRB.DoubleAttr.X) == 1) {
                    node.addAttribute("ui.label", "("+String.valueOf(i)+")"+ " 2, " +label);
                }else{
                    node.addAttribute("ui.label", "("+String.valueOf(i)+")"+ " 0, " +label);
                }
            }
            // 添加边
            for (Pair<Integer, Integer> edge : graph.getEdges()) {
                Integer source = edge.getFirst();
                Integer target = edge.getSecond();
                String edgeId = source + "-" + target;

                // 防止重复边
                if (gsGraph.getEdge(edgeId) == null) {
                    gsGraph.addEdge(edgeId, source.toString(), target.toString());
                }
            }
            // 显示图形并设置窗口标题
//           gsGraph.display();
            Viewer viewer = gsGraph.display();
            viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.EXIT); // 设置窗口关闭策略

            // 添加注释（例如可以添加作为图的一部分显示）
            org.graphstream.graph.Node commentNode = gsGraph.addNode("comment");
            commentNode.addAttribute("ui.label", "(i),1,T 分别为顶点编号，赋值，是否为maximal");
            commentNode.addAttribute("ui.style", "text-alignment: at-right; text-color: black; fill-color: rgba(255, 255, 255, 0);");
            commentNode.setAttribute("xyz", 0, v / 2.0, 0);  // 将注释节点放置在合适的地方


            // 清理
            model.dispose();
            env.dispose();
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

}
