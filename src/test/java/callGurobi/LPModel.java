package callGurobi;

import com.gurobi.gurobi.GRB;
import com.gurobi.gurobi.GRBEnv;
import com.gurobi.gurobi.GRBException;
import com.gurobi.gurobi.GRBModel;

/**
 * @author wendao
 * @since 2024-09-02
 * @Desc
 * This example reads an LP model from a file and solves it.
 *    If the model is infeasible or unbounded, the example turns off
 *    presolve and solves the model again. If the model is infeasible,
 *    the example computes an Irreducible Inconsistent Subsystem (IIS),
 *    and writes it to a file
 **/
public class LPModel {
    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: java Lp filename");
            System.exit(1);
        }

        try {
            GRBEnv env = new GRBEnv();
            GRBModel model = new GRBModel(env, args[0]);

            model.optimize();

            int optimstatus = model.get(GRB.IntAttr.Status);

            if (optimstatus == GRB.Status.INF_OR_UNBD) {
                model.set(GRB.IntParam.Presolve, 0);
                model.optimize();
                optimstatus = model.get(GRB.IntAttr.Status);
            }

            if (optimstatus == GRB.Status.OPTIMAL) {
                double objval = model.get(GRB.DoubleAttr.ObjVal);
                System.out.println("Optimal objective: " + objval);
            } else if (optimstatus == GRB.Status.INFEASIBLE) {
                System.out.println("Model is infeasible");

                // Compute and write out IIS
                model.computeIIS();
                model.write("model.ilp");
            } else if (optimstatus == GRB.Status.UNBOUNDED) {
                System.out.println("Model is unbounded");
            } else {
                System.out.println("Optimization was stopped with status = "
                        + optimstatus);
            }

            // Dispose of model and environment
            model.dispose();
            env.dispose();

        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " +
                    e.getMessage());
        }
    }

}
