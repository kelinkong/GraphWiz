package callGurobi;
import com.gurobi.gurobi.GRB;
import com.gurobi.gurobi.GRBEnv;
import com.gurobi.gurobi.GRBException;
import com.gurobi.gurobi.GRBLinExpr;
import com.gurobi.gurobi.GRBModel;
import com.gurobi.gurobi.GRBVar;
import org.junit.Test;

/**
 * @author wendao
 * @since 2024-09-03
 * @desc
 * 首先创建了Gurobi环境和模型。
 * 然后，通过调用model.addVar()方法创建了两个变量x和y，并设置了变量的取值范围和类型。
 * 接下来，使用model.setObjective()方法设置了目标函数，并使用model.addConstr()方法添加了一个约束条件。
 * 最后，调用model.optimize()方法求解模型，
 * 并通过x.get(GRB.DoubleAttr.X)和y.get(GRB.DoubleAttr.X)获取了变量的最优解。
 **/

public class GurobiColumnModelingTest {
    @Test
    public void Test1(){
        try {
            // 创建Gurobi环境
            GRBEnv env = new GRBEnv();
            env.set("logFile", "gurobi.log"); // 设置日志文件

            // 创建模型
            GRBModel model = new GRBModel(env);

            // 创建变量
            GRBVar x = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x");
            GRBVar y = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "y");

            // 设置目标函数
            GRBLinExpr objExpr = new GRBLinExpr();
            objExpr.addTerm(1.0, x);
            objExpr.addTerm(2.0, y);
            model.setObjective(objExpr, GRB.MAXIMIZE);

            // 添加约束条件
            GRBLinExpr constraintExpr = new GRBLinExpr();
            constraintExpr.addTerm(1.0, x);
            constraintExpr.addTerm(2.0, y);
            model.addConstr(constraintExpr, GRB.LESS_EQUAL, 3.0, "constraint");

            // 求解模型
            model.optimize();

            // 输出结果
            System.out.println("x = " + x.get(GRB.DoubleAttr.X));
            System.out.println("y = " + y.get(GRB.DoubleAttr.X));

            // 释放资源
            model.dispose();
            env.dispose();
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }
}