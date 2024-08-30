//
// Created by kelin on 24-4-16.
//

#ifndef GRAPH_H
#define GRAPH_H
#include <QVector>
#include <QRandomGenerator>
#include <QInputDialog>


class Graph: public std::enable_shared_from_this<Graph>{
public:
    Graph() = default;
    virtual ~Graph() = default;
    virtual std::shared_ptr<Graph> create() = 0;
    void init(int n);

    int n{};
    int m{};
    QVector<QVector<int>> adj_matrix{}; // 存放形式为[[0,1,1],[1,0,0],[1,0,0]]
    QVector<QVector<int>> adj_list{}; // 存放形式为[[1,2],[1],[1]]
    QVector<int> vertices{};
    int max_degree{};
    int min_degree{};
    QList<QPair<int, int>> edges{};
    std::string graph_type;
};

class CommonGraph final: public Graph {
public:
    CommonGraph() {
        this->graph_type = "Common Graph";
    }
    explicit CommonGraph(int n);
    std::shared_ptr<Graph> create() override;
};

class BipartiteGraph final : public Graph {
public:
    BipartiteGraph() {
        this->graph_type = "Bipartite Graph";
    }
    QVector<int> partions_X{};
    QVector<int> partions_Y{};
    std::shared_ptr<Graph> create() override;
};

class GridGraph final : public Graph {
public:
    GridGraph() {
        this->graph_type = "Grid Graph";
    }
    int rows{};
    int cols{};
    std::shared_ptr<Graph> create() override;
};

class TreeGraph final : public Graph {
public:
    TreeGraph() {
        this->graph_type = "Tree Graph";
    }
    std::shared_ptr<Graph> create() override;
};

class IntervalGraph final : public Graph {
public:
    IntervalGraph() {
        this->graph_type = "Interval Graph";
    }
    std::shared_ptr<Graph> create() override;
};

// 块-仙人掌图由块图和仙人掌图继承而来，造成了菱形继承，所以这里使用虚继承
class BlockGraph : virtual public Graph {
public:
    BlockGraph() {
        this->graph_type = "Block Graph";
    }
    QVector<QVector<int>> block_set{};
    QVector<int> cut_vertices_set{};
    std::shared_ptr<Graph> create() override;
private:
    void findBlocksAndCutVertices(int u, QVector<bool>& visited, QVector<int>& disc, QVector<int>& low, QVector<int>& parent, QVector<bool>& is_cut_vertex, QStack<int>& vertex_stack);
};

class CactusGraph : virtual public Graph {
public:
    CactusGraph() {
        this->graph_type = "Cactus Graph";
    }
    QVector<QVector<int>> cactus_set{}; // 存放所有的圈
    QVector<int> cut_vertices_set{}; // 存放所有的割点
    std::shared_ptr<Graph> create() override;
private:
    void findCacti(int u, QVector<bool>& visited, QVector<int>& disc, QVector<int>& low, QVector<int>& parent, QVector<bool>& is_cactus, QStack<int>& vertex_stack);
};

class BlockCactusGraph final : public Graph{
public:
    BlockCactusGraph() {
        this->graph_type = "Block Cactus Graph";
    }
    QVector<int> cut_vertices_set{};
    QVector<QVector<int>> cactus_set{}; // 存放所有的圈
    QVector<QVector<int>> block_set{};
    std::shared_ptr<Graph> create() override;
private:
    void findBlocksAndCacti(int u, QVector<bool>& visited, QVector<int>& disc, QVector<int>& low, QVector<int>& parent, QVector<bool>& is_cactus, QStack<int>& vertex_stack);
};

#endif //GRAPH_H
