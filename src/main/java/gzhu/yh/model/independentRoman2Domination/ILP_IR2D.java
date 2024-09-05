package gzhu.yh.model.independentRoman2Domination;

import com.gurobi.gurobi.GRB;
import com.gurobi.gurobi.GRBEnv;
import com.gurobi.gurobi.GRBException;
import com.gurobi.gurobi.GRBLinExpr;
import com.gurobi.gurobi.GRBModel;
import com.gurobi.gurobi.GRBVar;
import gzhu.yh.graphsModel.Graph;
import gzhu.yh.util.TwoDArrayList;


/**
 * @author wendao
 * @since 2024-09-05
 * independent roman-2 domiation的ILP方程
 *      varialble:
 *              x_v:顶点是否赋值为0。是则取值1，否取值0
 *              y_v:顶点是否赋值为1。是则取值1，否取值0
 *              z_v:顶点是否赋值为2。是则取值1，否取值0
 *      minumum: y_v + 2 * z_v
 *      subject to:
 *              x_v + y_v + z_v = 1
 *              y_v + z_v + y_u + z_u <= 1, if uv相连
 *              2 * x_v - sum_{u \in N(v)} (y_u + 2 * z_u ) <= 0
 *              x_v,  y_v, z_v binary.
 *
 **/
public class ILP_IR2D {
    public static void ILP_MDRD(Graph graph) throws GRBException {
        // 顶点个数
        int n = graph.getV();

        //MDRD的ILP需要先求各个顶点的度
        int[] deg = new int[n];


        //创建gurobi环境
        GRBEnv env = new GRBEnv(true);
        env.set("logFile", "src/main/java/gzhu/yh/logger/ILP_IR2D.log"); //设置日志文件
        env.start();

        // 创建一个新的模型
        GRBModel model = new GRBModel(env);
        model.set(GRB.StringAttr.ModelName, "ILP_IR2D");

        // 创建变量
        GRBVar[][] vars_x = new GRBVar[n][3];//顶点编号从0开始.顶点赋值0；1；2
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 3; j++) { //赋值0，1，2.但是无需顾及0
                vars_x[i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x(" + i + "," + j + ")");
            }
        }

        // 设置目标函数
        GRBLinExpr obj = new GRBLinExpr();
        for (int i = 0; i < n; i++) {
            obj.addTerm(1.0, vars_x[i][1]);//obj= y_v + 2 * z_v;
            obj.addTerm(2.0, vars_x[i][2]);
        }
        model.setObjective(obj, GRB.MINIMIZE);

        // 添加约束一： x_v + y_v + z_v = 1
        obj = new GRBLinExpr();
        for (int i = 0; i < n; i++) {
                obj.addTerm(1.0, vars_x[i][0]);
                obj.addTerm(1.0, vars_x[i][1]);
                obj.addTerm(1.0, vars_x[i][2]);
        }
        model.addConstr(obj, GRB.EQUAL, 1, "每个顶点只能被分配一个值");

        // 添加约束二： y_v + z_v + y_u + z_u <= 1, if uv相连
        obj = new GRBLinExpr();
        for (int i = 0; i < n; i++) {
            obj.addTerm(1.0, vars_x[i][1]);
            obj.addTerm(1.0, vars_x[i][2]);
            for (int j = 0; j < n; j++) {
                if (graph.getAdjMatrix().get(i).get(j) == 1) {//邻域的点
                    obj.addTerm(1.0, vars_x[j][1]);
                    obj.addTerm(1.0, vars_x[j][2]);
                }
            }
        }
        model.addConstr(obj, GRB.LESS_EQUAL, 1, "独立的性质");



        // 添加约束三： 2 * x_v - sum_{u \in N(v)} (y_u + 2 * z_u ) <= 0
        obj = new GRBLinExpr();
        for (int i = 0; i < n; i++) {
            obj.addTerm(2.0, vars_x[i][0]);
            for (int j = 0; j < n; j++) {
                if (graph.getAdjMatrix().get(i).get(j) == 1) {//邻域的点
                    obj.addTerm(-1.0, vars_x[j][1]);
                    obj.addTerm(-2.0, vars_x[j][2]);
                }
            }
        }
        model.addConstr(obj, GRB.LESS_EQUAL, 0, "0受两个1或一个2控制");

        // 求解模型
        model.optimize();

        // 打印结果
        for (int i = 1; i <= n; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.println(vars_x[i][j].get(GRB.StringAttr.VarName) + " " + vars_x[i][j].get(GRB.DoubleAttr.X));
            }
        }
        System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));
        System.out.println("Runtime: " + model.get(GRB.DoubleAttr.Runtime));
    }
}
