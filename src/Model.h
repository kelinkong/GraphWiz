//
// Created by kelin on 24-4-19.
//

#ifndef MODEL_H
#define MODEL_H
#include <sstream>
#include "Graph.h"
#include "gurobi_c++.h"


using ModelSharePtr = std::shared_ptr<GRBModel>;

class Model {
public:
    Model() = default;
    explicit Model(Graph* graph): graph(graph) {};

    static ModelSharePtr createModelFromLPFile(const QString& file_path);

    ModelSharePtr createLPModel(int index);

    // 创建完美双罗马控制集问题模型
    ModelSharePtr createPDRDPModel();

    // 创建标准控制集问题模型
    ModelSharePtr createDPModel();

    // 创建顶点覆盖问题模型
    ModelSharePtr createMVCModel();

    // 创建最大团问题模型
    ModelSharePtr createMISModel();

private:
    Graph* graph;

    // 所有model共享一个env
    static std::shared_ptr<GRBEnv> env;
};



#endif //MODEL_H
