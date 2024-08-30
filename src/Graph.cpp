//
// Created by kelin on 24-4-16.
//

#include "Graph.h"

#include "CommonFunction.h"
#include "Logger.h"
#include <QStack>


void Graph::init(const int n) {
    adj_matrix = QVector<QVector<int>>(n, QVector<int>(n, 0));
    this->adj_list.resize(n);
    this->m = 0;
    this->max_degree = 0;
    this->min_degree = INT_MAX;
    vertices = QVector<int>(n);
    std::iota(vertices.begin(),vertices.end(),0);
}

CommonGraph::CommonGraph(const int n){
    this->n = n;
    this->init(n);
    this->graph_type = "Custom Graph";
}

std::shared_ptr<Graph> CommonGraph::create() {
    bool ok;
    const int n = QInputDialog::getInt(nullptr, "Input", "请输入顶点个数：", 10, 1, 10000, 1, &ok);
    if(ok) {
        this->n = n;
        this->init(this->n);
        for(int i = 0;i < n;i++) {
            for(int j = i + 1;j < n;j++) {
                if (i != j && QRandomGenerator::global()->generateDouble() < 0.3) {
                    adj_matrix[i][j] = 1;
                    adj_matrix[j][i] = 1;
                    adj_list[i].append(j);
                    adj_list[j].append(i);
                    edges.append({i, j});
                }
            }
        }
        m = edges.size();
        return shared_from_this();
    }
    return nullptr;
}

std::shared_ptr<Graph> BipartiteGraph::create() {
    bool ok1,ok2;
    const int n = QInputDialog::getInt(nullptr, "Input", "请输入顶点个数：", 10, 1, 10000, 1, &ok1);
    const int x_size = QInputDialog::getInt(nullptr, "Input", "请输入X集合的顶点数：", n/2, 1, n, 1, &ok2);
    if(ok1 && ok2) {
        this->n = n;
        this->init(this->n);
        const int y_size = n - x_size;
        for(int i = 0; i < x_size; i++) {
            for(int j = x_size; j < n; j++) {
                if (QRandomGenerator::global()->generateDouble() < 0.3) {
                    adj_matrix[i][j] = 1;
                    adj_matrix[j][i] = 1;
                    adj_list[i].append(j);
                    adj_list[j].append(i);
                    edges.append({i, j});
                }
            }
        }
        m = edges.size();
        partions_X = vertices.mid(0, x_size);
        partions_Y = vertices.mid(x_size, y_size);
        return shared_from_this();
    }
    return nullptr;
}

std::shared_ptr<Graph> GridGraph::create() {
    bool ok1, ok2;
    int rows = QInputDialog::getInt(nullptr, "Input", "请输入行数：", 10, 1, 10000, 1, &ok1);
    int cols = QInputDialog::getInt(nullptr, "Input", "请输入列数：", 10, 1, 10000, 1, &ok2);
    if(ok1 && ok2) {
        this->n = rows * cols;
        this->rows = rows;
        this->cols = cols;
        this->init(this->n);
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++) {
                int current = i * cols + j;
                if(i > 0) {
                    int up = (i - 1) * cols + j;
                    adj_matrix[current][up] = 1;
                    adj_matrix[up][current] = 1;
                    adj_list[current].append(up);
                    adj_list[up].append(current);
                    edges.append({current, up});
                }
                if(j > 0) {
                    int left = i * cols + (j - 1);
                    adj_matrix[current][left] = 1;
                    adj_matrix[left][current] = 1;
                    adj_list[current].append(left);
                    adj_list[left].append(current);
                    edges.append({current, left});
                }
            }
        }
        m = edges.size();
        return shared_from_this();
    }
    return nullptr;
}

std::shared_ptr<Graph> TreeGraph::create() {
    bool ok;
    const int n = QInputDialog::getInt(nullptr, "Input", "请输入顶点个数：", 10, 1, 10000, 1, &ok);
    if(ok) {
        this->n = n;
        this->init(this->n);
        for(int i = 1; i < n; i++) {
            int parent = QRandomGenerator::global()->bounded(i);
            adj_matrix[i][parent] = 1;
            adj_matrix[parent][i] = 1;
            adj_list[i].append(parent);
            adj_list[parent].append(i);
            edges.append({i, parent});
        }
        m = edges.size();
        return shared_from_this();
    }
    return nullptr;
}

/*
 * 生成区间图的方法：
 * - 从0号顶点开始，往后随机找到一个顶点j
 * - 假设顶点i和顶点j有边
 * - 添加啊顶点j和顶点i之间的所有边
 */

std::shared_ptr<Graph> IntervalGraph::create() {
    bool ok;
    const int n = QInputDialog::getInt(nullptr, "Input", "请输入顶点个数：", 10, 1, 10000, 1, &ok);
    if(ok) {
        this->n = n;
        this->init(this->n);
        for(int i = 1; i < n; i++) {
            // 在i和 n-1之间生成一个随机数
            int max_neighbor = weightedRandom(i, n);
            for(int j = max_neighbor - 1; j >= i; --j) {
                adj_matrix[max_neighbor][j] = 1;
                adj_matrix[j][max_neighbor] = 1;
                adj_list[max_neighbor].append(j);
                adj_list[j].append(max_neighbor);
                edges.append({j, max_neighbor});
            }
        }
        m = edges.size();
        return shared_from_this();
    }
    return nullptr;
}

/*
 * 生成块图的方法：
 * - 生成一些块
 * - 在这些块中添加边
 */

std::shared_ptr<Graph> BlockGraph::create() {
    bool ok;
    const int n = QInputDialog::getInt(nullptr, "Input", "请输入顶点个数：", 10, 1, 10000, 1, &ok);
    if(ok) {
        this->n = n;
        this->init(this->n);

        // First, create some disjoint blocks
        int remaining_vertices = n;
        while(remaining_vertices > 0) {
            int block_size = weightedRandom(1, remaining_vertices + 1);
            // int block_size = QRandomGenerator::global()->bounded(1, remaining_vertices + 1);
            for(int i = 0; i < block_size; i++) {
                for(int j = i + 1; j < block_size; j++) {
                    int u = n - remaining_vertices + i;
                    int v = n - remaining_vertices + j;
                    if(u == v) {
                        continue;
                    }
                    adj_matrix[u][v] = 1;
                    adj_matrix[v][u] = 1;
                    adj_list[u].append(v);
                    adj_list[v].append(u);
                    edges.append({u, v});
                }
            }
            block_set.append(vertices.mid(n - remaining_vertices, block_size));
            remaining_vertices -= block_size;
        }

        // Then, randomly select some cut vertices to connect the blocks
        for(int i = 1; i < block_set.size(); i++) {
            int u = block_set[i - 1].last();
            int v = block_set[i].first();
            if(u == v) {
                continue;
            }
            adj_matrix[u][v] = 1;
            adj_matrix[v][u] = 1;
            adj_list[u].append(v);
            adj_list[v].append(u);
            edges.append({u, v});
            cut_vertices_set.append(u);
        }

        m = edges.size();

        // Identify blocks and cut vertices
        // QVector<int> low(n), disc(n), parent(n, -1);
        // QVector<bool> visited(n, false), is_cut_vertex(n, false);
        // QStack<int> vertex_stack;
        //
        // for(int i = 0; i < n; i++) {
        //     if(!visited[i]) {
        //         findBlocksAndCutVertices(i, visited, disc, low, parent, is_cut_vertex, vertex_stack);
        //     }
        // }
        //
        // for(int i = 0; i < n; i++) {
        //     if(is_cut_vertex[i]) {
        //         cut_vertices_set.append(i);
        //     }
        // }

        Logger::getInstance() << "Number of cut vertices: " << cut_vertices_set.size();
        Logger::getInstance() << "Number of blocks: " << block_set.size();

        // 打印割点
        for(int i : cut_vertices_set) {
            Logger::getInstance()<<("Cut vertex: " + QString::number(i)).toStdString();
        }

        // 打印块
        for(int i = 0; i < block_set.size(); i++) {
            Logger::getInstance()<<("Block " + QString::number(i) + ": ").toStdString();
            for(int j : block_set[i]) {
                Logger::getInstance()<<QString::number(j).toStdString();
            }
        }

        return shared_from_this();
    }
    return nullptr;
}

void BlockGraph::findBlocksAndCutVertices(int u, QVector<bool>& visited, QVector<int>& disc, QVector<int>& low, QVector<int>& parent, QVector<bool>& is_cut_vertex, QStack<int>& vertex_stack) {
    static int time = 0;
    int children = 0;
    visited[u] = true;
    disc[u] = low[u] = ++time;
    vertex_stack.push(u);

    for(auto v : adj_list[u]) {
        if(!visited[v]) {
            children++;
            parent[v] = u;
            findBlocksAndCutVertices(v, visited, disc, low, parent, is_cut_vertex, vertex_stack);
            low[u] = std::min(low[u], low[v]);
            if(parent[u] == -1 && children > 1) {
                is_cut_vertex[u] = true;
            }
            if(parent[u] != -1 && low[v] >= disc[u]) {
                is_cut_vertex[u] = true;

                // Found a block
                QVector<int> block;
                while(vertex_stack.top() != u) {
                    block.append(vertex_stack.top());
                    vertex_stack.pop();
                }
                block.append(u);
                block_set.append(block);
            }
        } else if(v != parent[u]) {
            low[u] = std::min(low[u], disc[v]);
        }
    }
}

/*
 * 生成仙人掌图的方法：
 * - 生成一些圈
 * - 在这些圈中添加边
 */
std::shared_ptr<Graph> CactusGraph::create() {
    bool ok;
    const int n = QInputDialog::getInt(nullptr, "Input", "请输入顶点个数：", 10, 1, 10000, 1, &ok);
    if(ok) {
        this->n = n;
        this->init(this->n);

        // First, create some disjoint cycles
        int remaining_vertices = n;
        while(remaining_vertices > 0) {
            int cycle_size = weightedRandom(1, remaining_vertices + 1);
            // int cycle_size = QRandomGenerator::global()->bounded(1, remaining_vertices + 1);
            for(int i = 0; i < cycle_size; i++) {
                int u = n - remaining_vertices + i;
                int v = n - remaining_vertices + ((i + 1) % cycle_size);
                if(u == v) {
                    continue;
                }
                adj_matrix[u][v] = 1;
                adj_matrix[v][u] = 1;
                adj_list[u].append(v);
                adj_list[v].append(u);
                edges.append({u, v});
            }
            cactus_set.append(vertices.mid(n - remaining_vertices, cycle_size));
            remaining_vertices -= cycle_size;
        }

        // Then, randomly select some vertices to connect the cycles
        for(int i = 1; i < cactus_set.size(); i++) {
            int u = cactus_set[i - 1].last();
            int v = cactus_set[i].first();
            if(u == v) {
                continue;
            }
            adj_matrix[u][v] = 1;
            adj_matrix[v][u] = 1;
            adj_list[u].append(v);
            adj_list[v].append(u);
            edges.append({u, v});
            cut_vertices_set.append(u);
        }

        m = edges.size();

        Logger::getInstance() << "Number of cut vertices: " << cut_vertices_set.size();
        Logger::getInstance() << "Number of cycle: " << cactus_set.size();

        // 打印割点
        for(int i : cut_vertices_set) {
            Logger::getInstance()<<("Cut vertex: " + QString::number(i)).toStdString();
        }

        // 打印块
        for(int i = 0; i < cactus_set.size(); i++) {
            Logger::getInstance()<<("Cactus " + QString::number(i) + ": ").toStdString();
            for(int j : cactus_set[i]) {
                Logger::getInstance()<<QString::number(j).toStdString();
            }
        }

        return shared_from_this();
    }
    return nullptr;
}

/*
 * 生成块-仙人掌图的方法：
 * - 生成一些块和圈
 * - 在这些块和圈中添加边
 */
std::shared_ptr<Graph> BlockCactusGraph::create() {
    bool ok;
    const int n = QInputDialog::getInt(nullptr, "Input", "请输入顶点个数：", 10, 1, 10000, 1, &ok);
    if(ok) {
        this->n = n;
        this->init(this->n);

        // First, create some disjoint cycles
        int cycle_count = QRandomGenerator::global()->bounded(3, n + 1);
        int remaining_vertices = cycle_count;
        while(remaining_vertices > 0) {
            int cycle_size = weightedRandom(1, remaining_vertices + 1);
            // int cycle_size = QRandomGenerator::global()->bounded(1, remaining_vertices + 1);
            for(int i = 0; i < cycle_size; i++) {
                int u = n - remaining_vertices + i;
                int v = n - remaining_vertices + ((i + 1) % cycle_size);
                if(u == v) {
                    continue;
                }
                adj_matrix[u][v] = 1;
                adj_matrix[v][u] = 1;
                adj_list[u].append(v);
                adj_list[v].append(u);
                edges.append({u, v});
            }
            cactus_set.append(vertices.mid(n - remaining_vertices, cycle_size));
            remaining_vertices -= cycle_size;
        }

        // Then, create some disjoint blocks
        remaining_vertices = n - cycle_count;
        while(remaining_vertices > 0) {
            int block_size = weightedRandom(1, remaining_vertices + 1);
            // int block_size = QRandomGenerator::global()->bounded(1, remaining_vertices + 1);
            for(int i = 0; i < block_size; i++) {
                for(int j = i + 1; j < block_size; j++) {
                    int u = n - remaining_vertices + i;
                    int v = n - remaining_vertices + j;
                    if(u == v) {
                        continue;
                    }
                    adj_matrix[u][v] = 1;
                    adj_matrix[v][u] = 1;
                    adj_list[u].append(v);
                    adj_list[v].append(u);
                    edges.append({u, v});
                }
            }
            block_set.append(vertices.mid(n - remaining_vertices, block_size));
            remaining_vertices -= block_size;
        }

        // Finally, randomly select some vertices to connect the cycles and blocks
        for(int i = 1; i < cactus_set.size(); i++) {
            int u = cactus_set[i - 1].last();
            int v = cactus_set[i].first();
            if(u == v) {
                continue;
            }
            adj_matrix[u][v] = 1;
            adj_matrix[v][u] = 1;
            adj_list[u].append(v);
            adj_list[v].append(u);
            edges.append({u, v});
            cut_vertices_set.append(u);
        }

        for(int i = 1; i < block_set.size(); i++) {
            int u = block_set[i - 1].last();
            int v = block_set[i].first();
            if(u == v) {
                continue;
            }
            adj_matrix[u][v] = 1;
            adj_matrix[v][u] = 1;
            adj_list[u].append(v);
            adj_list[v].append(u);
            edges.append({u, v});
            cut_vertices_set.append(u);
        }

        m = edges.size();
        return shared_from_this();
    }
    return nullptr;
}

void BlockCactusGraph::findBlocksAndCacti(int u, QVector<bool> &visited, QVector<int> &disc, QVector<int> &low,
    QVector<int> &parent, QVector<bool> &is_cactus, QStack<int> &vertex_stack) {
}




