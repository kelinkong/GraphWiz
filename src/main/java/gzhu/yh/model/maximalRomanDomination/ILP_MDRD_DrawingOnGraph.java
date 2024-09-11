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

/**
 * @author wendao
 * @since 2024-09-04
 * //
 * <p>
 * maxiaml roman domiation的ILP方程
 *      varialble:
 *              x_{v,1}:顶点是否赋值为1。是则取值1，否取值0
 *              x_{v,2}:顶点是否赋值为2。是则取值1，否取值0
 *              z_v:顶点是否不与赋值为0的点相连。是则取值0，否取值1
 *      minumum: x_{v,1} + 2 * x_{v,2}
 *      subject to:
 *              x_{v,1} + x_{v,2} +sum_{u \in N(v)} x_{u,2} >= 1
 *              (x_{v,1} + x_{v,2}) * deg(v) <= sum_{u \in N(v)} x_{u,1} + sum_{u \in N(v)} x_{u,2} + z_v * M
 *              sum_{v \in V} z_v <= n-1
 *              x_{v,1},  x_{v,2}, z_v binary , M is a sufficiently large const integer.
 *              z_v + x_{v,1} + x_{v,2} >=1
 **/
public class ILP_MDRD_DrawingOnGraph {
    public static void ILP_MDRD_DrawingOnGraph(Graph graph){
        // 顶点个数
        try {
            int n = graph.getV();

            //MDRD的ILP需要先求各个顶点的度
            int[] deg = new int[n];
//        int col = graph.getAdjMatrix().get(0).size();
//        for (int i = 0; i < n; i++) {
//            for (int j = 0; j < col; j++) {
//                if (graph.getAdjMatrix().get(i).get(j) == 1) {
//                    deg[i]++;
//                }
//            }
//        }
            for (int i = 0; i < n; i++) {
                deg[i]=graph.getAdjList().get(i).size();
            }
            //MDRD的ILP需要一个充分大的整数
            int M = 100000;//TODO 理论上为Integer.MAX_VALUE.鉴于点数没超过1000，M值取100 000

            //创建gurobi环境
            GRBEnv env = new GRBEnv();
            env.set("logFile", "src/main/java/gzhu/yh/logger/ILP_MDRD.log"); //设置日志文件
//        env.start();

            // 创建一个新的模型
            GRBModel model = new GRBModel(env);
            model.set(GRB.StringAttr.ModelName, "ILP_MDRD");

            // 创建变量
            GRBVar[][] vars_x = new GRBVar[n][3];//顶点编号从0开始.顶点赋值0；1；2,
            // TODO 此处vars_x[][0]没用到,可删
            GRBVar[] vars_z = new GRBVar[n];//顶点编号从0开始，顶点否是与赋值为0的点相连
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < 3; j++) { //赋值0，1，2.但是无需顾及0
                    vars_x[i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x(" + i + "," + j + ")");
                }
                vars_z[i] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "z(" + i + ")");
            }

            // 设置目标函数 最小化 x_{v,1} + 2 * x_{v,2} 的和
            GRBLinExpr obj = new GRBLinExpr();
            for (int i = 0; i < n; i++) {
                obj.addTerm(1.0, vars_x[i][1]);//obj += vars_x[i][1]+ 2 * vars_x[i][2];
                obj.addTerm(2.0, vars_x[i][2]);
            }
            model.setObjective(obj, GRB.MINIMIZE);

            // 添加约束一： x_{v,1} + x_{v,2} +sum_{u \in N(v)} x_{u,2} >= 1
            //x_{v,1} + x_{v,2} + sum(x_{u,2} for u in N(v)) >= 1
            for (int i = 0; i < n; i++) {
                obj = new GRBLinExpr();
                obj.addTerm(1.0, vars_x[i][1]);
                obj.addTerm(1.0, vars_x[i][2]);
    //            for (int j = 0; j < n; j++) {
    //                if (graph.getAdjMatrix().get(i).get(j) == 1) {//邻域的点
    //                    obj.addTerm(1.0, vars_x[j][2]);
    //                }
    //            }
                for(int neighbor : graph.getAdjList().get(i)){
                    obj.addTerm(1.0, vars_x[neighbor][2]);
                }
                model.addConstr(obj, GRB.GREATER_EQUAL, 1, "罗马，赋值为0的点邻域内有2"+i);
            }

            // 添加约束二： (x_{v,1} + x_{v,2}) * deg(v) <= sum_{u \in N(v)} x_{u,1} + sum_{u \in N(v)} x_{u,2} + z_v * M
            for (int i = 0; i < n; i++) {
                obj = new GRBLinExpr();
    //            for (int j = 0; j < n; j++) {
    //                if (graph.getAdjMatrix().get(i).get(j) == 1) {//邻域的点
    //                    obj.addTerm(1.0, vars_x[j][1]);
    //                    obj.addTerm(1.0, vars_x[j][2]);
    //                }
    //            }
                for(int neighbor : graph.getAdjList().get(i)){
                    obj.addTerm(1.0, vars_x[neighbor][1]);
                    obj.addTerm(1.0, vars_x[neighbor][2]);
                }
                obj.addTerm(M, vars_z[i]);

                obj.addTerm(-1 * deg[i], vars_x[i][1]);
                obj.addTerm(-1 * deg[i], vars_x[i][2]);
                model.addConstr(obj, GRB.GREATER_EQUAL, 0, "有不受0控制的点的条件一"+i);
            }

            // 添加约束三： sum_{v \in V} z_v <= n-1
            obj = new GRBLinExpr();
            for (int i = 0; i < n; i++) {
                obj.addTerm(1.0, vars_z[i]);
            }
            model.addConstr(obj, GRB.LESS_EQUAL, n - 1, "有不受0控制的点的条件二");

            // 添加约束四： z_v + x_{v,1} + x_{v,2} >=1
            for (int i = 0; i < n; i++) {
                obj = new GRBLinExpr();
                obj.addTerm(1.0, vars_z[i]);
                obj.addTerm(1.0, vars_x[i][1]);
                obj.addTerm(1.0, vars_x[i][2]);
                model.addConstr(obj, GRB.GREATER_EQUAL, 1, "有不受0控制的点的条件三");
            }

            // 求解模型
            model.optimize();

            // 打印结果
            for (int i = 0; i < n; i++) {
                System.out.print("v_"+i+ "="+"(");
                if((int)vars_x[i][1].get(GRB.DoubleAttr.X) == 0 && (int)vars_x[i][2].get(GRB.DoubleAttr.X) == 0){
                    System.out.print("1");
                }
                else {
                    System.out.print("0");
                }
                System.out.println("," + (int)vars_x[i][1].get(GRB.DoubleAttr.X)+ ", "+
                        (int)vars_x[i][2].get(GRB.DoubleAttr.X) + ")"+ ", z_"+i+"="+
                        (int)vars_z[i].get(GRB.DoubleAttr.X));
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
                if((int)vars_z[i].get(GRB.DoubleAttr.X)==0){label = "T";}
                else{label = "F";}
                // 为每个节点添加编号作为标签，加上gurobi计算结果
                if ((int)vars_x[i][1].get(GRB.DoubleAttr.X) == 1){
                    node.addAttribute("ui.label", "("+String.valueOf(i)+")"+ " 1, " +label);
                } else if ((int)vars_x[i][2].get(GRB.DoubleAttr.X) == 1) {
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
