//
// Created by kelin on 24-4-19.
//
#include "Model.h"
#include <qdebug.h>

std::shared_ptr<GRBEnv> Model::env = std::make_shared<GRBEnv>();

ModelSharePtr Model::createModelFromLPFile(const QString &file_path) {
    auto model = std::make_shared<GRBModel>(env.get(), file_path.toStdString());
    return model;
}

ModelSharePtr Model::createLPModel(const int index) {
    switch (index) {
        case 0:
            return createDPModel();
        case 1:
            return createPDRDPModel();
        case 2:
            return createMISModel();
        case 3:
            return createMVCModel();
        default:
            return nullptr;
    }
}

ModelSharePtr Model::createDPModel() {
    int n = this->graph->adj_matrix.size();
    ModelSharePtr model = std::make_shared<GRBModel>(*env);
    model->set(GRB_StringAttr_ModelName, "Domination Problem");

    QVector<QVector<GRBVar>> vars_x(n, QVector<GRBVar>(4));

    for (int i = 0; i < n; ++i) {
        for (int j = 0; j < 2; ++j) {
            std::stringstream varName;
            varName << "x(" << i << "," << j << ")";
            vars_x[i][j] = model->addVar(0, 1, 0,GRB_BINARY, varName.str());
        }
    }

    model->update();

    for (int i = 0;i < n;++i) {
        // 每个顶点只能被分配一个值
        GRBLinExpr sum1 = 0;
        for (int j = 0; j < 2; ++j) {
            sum1 += vars_x[i][j];
        }
        model->addConstr(sum1 == 1);

        // 赋值为0的顶点至少连接一个1 (1 - xi0) + sum(xj1) >= 1
        GRBLinExpr sum2 = 0;
        for (int j = 0; j < n; ++j) {
            if (this->graph->adj_matrix[i][j] == 1) {
                sum2 += vars_x[j][1];
            }
        }
        model->addConstr((1 - vars_x[i][0]) + sum2 >= 1);
    }

    GRBLinExpr obj = 0;
    for (int i = 0; i < n; ++i) {
        obj += vars_x[i][1];
    }
    model->setObjective(obj, GRB_MINIMIZE);

    model->update();

    return model;
}

ModelSharePtr Model::createPDRDPModel() {
    int n = this->graph->adj_matrix.size();
    ModelSharePtr model = std::make_shared<GRBModel>(*env);
    model->set(GRB_StringAttr_ModelName, "Perfect Double Roman Domination Problem");

    QVector<QVector<GRBVar>> vars_x(n, QVector<GRBVar>(4));

    for (int i = 0; i < n; ++i) {
        for (int j = 0; j < 4; ++j) {
            std::stringstream varName;
            varName << "x(" << i << "," << j << ")";
            vars_x[i][j] = model->addVar(0, 1, 0,GRB_BINARY, varName.str());
        }
    }
    model->update();
    for (int i = 0; i < n; ++i) {

        // 每个顶点只能被分配一个值
        GRBLinExpr sum1 = 0;
        for (int j = 0; j < 4; ++j) {
            sum1 += vars_x[i][j];
        }
        model->addConstr(sum1 == 1);

        // 赋值为0的顶点恰好有两个2或者一个3
        GRBLinExpr sum2 = 0;
        for (int j = 0; j < n; ++j) {
            if (this->graph->adj_matrix[i][j] == 1) {
                sum2 += vars_x[j][2] + 2 * vars_x[j][3];
            }
        }
        model->addQConstr(2 * (1 - vars_x[i][0]) + vars_x[i][0] * sum2 == 2);

        // 赋值为1的顶点恰好有一个2
        GRBLinExpr sum3 = 0;
        for (int j = 0; j < n; ++j) {
            if (this->graph->adj_matrix[i][j] == 1) {
                sum3 += vars_x[j][2];
            }
        }
        model->addQConstr(vars_x[i][1] * sum3 - vars_x[i][1] == 0);

        // 对于每一条边，1和3都不相连
        for (int j = 0; j < n; ++j) {
            if (this->graph->adj_matrix[i][j] == 1) {
                model->addConstr(vars_x[i][1] + vars_x[j][3] <= 1);
            }
        }
    }

    GRBLinExpr obj = 0;
    for (int i = 0; i < n; ++i) {
        obj += vars_x[i][1] + 2 * vars_x[i][2] + 3 * vars_x[i][3];
    }
    model->setObjective(obj, GRB_MINIMIZE);

    model->update();

    return model;
}

ModelSharePtr Model::createMISModel() {
    int n = this->graph->adj_matrix.size();
    ModelSharePtr model = std::make_shared<GRBModel>(*env);
    model->set(GRB_StringAttr_ModelName, "Maximum Independent Set Problem");

    QVector<GRBVar> vars_x(n);

    for (int i = 0; i < n; ++i) {
        std::stringstream varName;
        varName << "x(" << i << ")";
        vars_x[i] = model->addVar(0, 1, 0, GRB_BINARY, varName.str());
    }

    model->update();

    // Add constraints: for each edge (i, j), x[i] + x[j] <= 1
    for (auto [x, y]: this->graph->edges) {
        model->addConstr(vars_x[x] + vars_x[y] <= 1);
    }

    // Set objective: maximize sum of x[i]
    GRBLinExpr obj = 0;
    for (int i = 0; i < n; ++i) {
        obj += vars_x[i];
    }
    model->setObjective(obj, GRB_MAXIMIZE);

    model->update();

    return model;
}

ModelSharePtr Model::createMVCModel() {
    int n = this->graph->adj_matrix.size();
    ModelSharePtr model = std::make_shared<GRBModel>(*env);
    model->set(GRB_StringAttr_ModelName, "vertex Cover Problem");

    QVector<QVector<GRBVar>> vars_x(n, QVector<GRBVar>(4));

    for (int i = 0; i < n; ++i) {
        for (int j = 0; j < 2; ++j) {
            std::stringstream varName;
            varName << "x(" << i << "," << j << ")";
            vars_x[i][j] = model->addVar(0, 1, 0,GRB_BINARY, varName.str());
        }
    }

    model->update();

    // 每个顶点只能被分配一个值
    for (int i = 0;i < n;++i) {
        GRBLinExpr sum1 = 0;
        for (int j = 0; j < 2; ++j) {
            sum1 += vars_x[i][j];
        }
        model->addConstr(sum1 == 1);
    }

    // 每条边至少有一个顶点为1
    for (auto [x,y]: this->graph->edges) {
        model->addConstr(vars_x[x][1] + vars_x[y][1] >= 1);
    }

    GRBLinExpr obj = 0;
    for (int i = 0; i < n; ++i) {
        obj += vars_x[i][1];
    }
    model->setObjective(obj, GRB_MINIMIZE);

    model->update();

    return model;
}


