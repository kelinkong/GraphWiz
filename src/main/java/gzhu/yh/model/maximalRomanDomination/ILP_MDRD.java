package gzhu.yh.model.maximalRomanDomination;

import com.gurobi.gurobi.GRB;
import com.gurobi.gurobi.GRBEnv;
import com.gurobi.gurobi.GRBException;
import com.gurobi.gurobi.GRBExpr;
import com.gurobi.gurobi.GRBLinExpr;
import com.gurobi.gurobi.GRBModel;
import com.gurobi.gurobi.GRBVar;
import gzhu.yh.graphsModel.Graph;

import java.util.List;

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
public class ILP_MDRD {
//    public static void ILP_MDRD(Graph graph){
//        // 顶点个数
//        try {
//            int n = graph.getV();
//
//            //MDRD的ILP需要先求各个顶点的度
//            int[] deg = new int[n];
////        int col = graph.getAdjMatrix().get(0).size();
////        for (int i = 0; i < n; i++) {
////            for (int j = 0; j < col; j++) {
////                if (graph.getAdjMatrix().get(i).get(j) == 1) {
////                    deg[i]++;
////                }
////            }
////        }
//            for (int i = 0; i < n; i++) {
//                deg[i]=graph.getAdjList().get(i).size();
//            }
//            //MDRD的ILP需要一个充分大的整数
//            int M = 100000;//TODO 理论上为Integer.MAX_VALUE.鉴于点数没超过1000，M值取100 000
//
//            //创建gurobi环境
//            GRBEnv env = new GRBEnv();
//            env.set("logFile", "src/main/java/gzhu/yh/logger/ILP_MDRD.log"); //设置日志文件
////        env.start();
//
//            // 创建一个新的模型
//            GRBModel model = new GRBModel(env);
//            model.set(GRB.StringAttr.ModelName, "ILP_MDRD");
//
//            // 创建变量
//            GRBVar[][] vars_x = new GRBVar[n][3];//顶点编号从0开始.顶点赋值0；1；2,
//            // TODO 此处vars_x[][0]没用到,可删
//            GRBVar[] vars_z = new GRBVar[n];//顶点编号从0开始，顶点否是与赋值为0的点相连
//            for (int i = 0; i < n; i++) {
//                for (int j = 0; j < 3; j++) { //赋值0，1，2.但是无需顾及0
//                    vars_x[i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x(" + i + "," + j + ")");
//                }
//                vars_z[i] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "z(" + i + ")");
//            }
//
//            // 设置目标函数 最小化 x_{v,1} + 2 * x_{v,2} 的和
//            GRBLinExpr obj = new GRBLinExpr();
//            for (int i = 0; i < n; i++) {
//                obj.addTerm(1.0, vars_x[i][1]);//obj += vars_x[i][1]+ 2 * vars_x[i][2];
//                obj.addTerm(2.0, vars_x[i][2]);
//            }
//            model.setObjective(obj, GRB.MINIMIZE);
//
//            // 添加约束一： x_{v,1} + x_{v,2} +sum_{u \in N(v)} x_{u,2} >= 1
//            //x_{v,1} + x_{v,2} + sum(x_{u,2} for u in N(v)) >= 1
//            for (int i = 0; i < n; i++) {
//                obj = new GRBLinExpr();
//                obj.addTerm(1.0, vars_x[i][1]);
//                obj.addTerm(1.0, vars_x[i][2]);
//    //            for (int j = 0; j < n; j++) {
//    //                if (graph.getAdjMatrix().get(i).get(j) == 1) {//邻域的点
//    //                    obj.addTerm(1.0, vars_x[j][2]);
//    //                }
//    //            }
//                for(int neighbor : graph.getAdjList().get(i)){
//                    obj.addTerm(1.0, vars_x[neighbor][2]);
//                }
//                model.addConstr(obj, GRB.GREATER_EQUAL, 1, "罗马，赋值为0的点邻域内有2"+i);
//            }
//
//            // 添加约束二： (x_{v,1} + x_{v,2}) * deg(v) <= sum_{u \in N(v)} x_{u,1} + sum_{u \in N(v)} x_{u,2} + z_v * M
//            for (int i = 0; i < n; i++) {
//                obj = new GRBLinExpr();
//    //            for (int j = 0; j < n; j++) {
//    //                if (graph.getAdjMatrix().get(i).get(j) == 1) {//邻域的点
//    //                    obj.addTerm(1.0, vars_x[j][1]);
//    //                    obj.addTerm(1.0, vars_x[j][2]);
//    //                }
//    //            }
//                for(int neighbor : graph.getAdjList().get(i)){
//                    obj.addTerm(1.0, vars_x[neighbor][1]);
//                    obj.addTerm(1.0, vars_x[neighbor][2]);
//                }
//                obj.addTerm(M, vars_z[i]);
//
//                obj.addTerm(-1 * deg[i], vars_x[i][1]);
//                obj.addTerm(-1 * deg[i], vars_x[i][2]);
//                model.addConstr(obj, GRB.GREATER_EQUAL, 0, "有不受0控制的点的条件一"+i);
//            }
//
//            // 添加约束三： sum_{v \in V} z_v <= n-1
//            obj = new GRBLinExpr();
//            for (int i = 0; i < n; i++) {
//                obj.addTerm(1.0, vars_z[i]);
//            }
//            model.addConstr(obj, GRB.LESS_EQUAL, n - 1, "有不受0控制的点的条件二");
//
//            // 添加约束四： z_v + x_{v,1} + x_{v,2} >=1
//            for (int i = 0; i < n; i++) {
//                obj = new GRBLinExpr();
//                obj.addTerm(1.0, vars_z[i]);
//                obj.addTerm(1.0, vars_x[i][1]);
//                obj.addTerm(1.0, vars_x[i][2]);
//                model.addConstr(obj, GRB.GREATER_EQUAL, 1, "有不受0控制的点的条件三");
//            }
//
//            // 求解模型
//            model.optimize();
//
//            // 打印结果
//            for (int i = 0; i < n; i++) {
//                System.out.print("v_"+i+ "="+"(");
//                if((int)vars_x[i][1].get(GRB.DoubleAttr.X) == 0 && (int)vars_x[i][2].get(GRB.DoubleAttr.X) == 0){
//                    System.out.print("1");
//                }
//                else {
//                    System.out.print("0");
//                }
//                System.out.println("," + (int)vars_x[i][1].get(GRB.DoubleAttr.X)+ ", "+
//                        (int)vars_x[i][2].get(GRB.DoubleAttr.X) + ")"+ ", z_"+i+"="+
//                        (int)vars_z[i].get(GRB.DoubleAttr.X));
//            }
//
////            System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));
////            System.out.println("Runtime: " + model.get(GRB.DoubleAttr.Runtime));
//        } catch (GRBException e) {
//            e.printStackTrace();
//        }
//    }
}
