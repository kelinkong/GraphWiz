# GraphWiz: 开发者与二次开发指南

欢迎来到 GraphWiz 开发者指南！本指南旨在帮助需要进行二次开发、扩展图算法或添加新数学优化模型的工程师、研究人员和学生，深入理解本项目的代码架构设计与核心扩展方法。

---

## 1. 系统架构与核心设计模式

GraphWiz 采用了清晰的分层设计，完全解耦了**图拓扑结构层**、**数学优化层**和 **GUI 表现层**。

```
                  ┌───────────────────────────────┐
                  │          MainWindow           │ (GUI 表现层)
                  └──────┬─────────────────┬──────┘
                         │                 │
                         ▼                 ▼
             ┌─────────────────────┐   ┌─────────────────────┐
             │        Graph        │   │        Model        │ (优化桥接器)
             │      (抽象基类)     │   │ (Gurobi 整数规划封装)│
             └───────────┬─────────┘   └───────────┬─────────┘
                         │                         │
     ┌───────────────────┼───────────────────┐     │
     ▼                   ▼                   ▼     ▼
CommonGraph         BlockGraph          CactusGraph ──▶ Gurobi C++ 引擎
```

### 1.1 多态图工厂 (Polymorphic Graph Factory)
`Graph` 类 (`src/Graph.h`) 定义了抽象图的通用接口：
- 拥有底层通用的图表示法：邻接矩阵 (`QVector<QVector<int>> adj_matrix`) 和邻接表 (`QVector<QVector<int>> adj_list`)。
- 每种具体的图结构（例如 `GridGraph`、`CactusGraph`）都继承自 `Graph`，并重写纯虚函数：
  ```cpp
  virtual std::shared_ptr<Graph> create() = 0;
  ```
- 内存管理上，全面采用现代 C++ 智能指针 (`std::shared_ptr`) 并继承自 `std::enable_shared_from_this<Graph>`，以保证图对象指针在跨层级传递时的生命周期安全。
- 为了应对像混合“块-仙人掌图”这种可能引发菱形继承（Diamond Inheritance）的复杂结构，`BlockGraph` 和 `CactusGraph` 的继承均声明为虚继承（`class BlockGraph : virtual public Graph`）。

### 1.2 Gurobi 环境单例化与生命周期管理
在 Gurobi 优化器中，创建一个环境变量 (`GRBEnv`) 是一项非常昂贵的系统操作，并且会在终端打印商业授权 Banner。为了提升应用启动和计算效率，GraphWiz 在 `Model` 类中对 `GRBEnv` 采用了**静态智能指针单例化管理** (`src/Model.h`)：
```cpp
class Model {
public:
    static std::shared_ptr<GRBEnv> env;
    ...
};
```
该设计确保了无论界面上生成、重构了多少次模型，全局都仅保持一个延迟初始化的 Gurobi 环境实例，显著减少了内存占用和多次环境初始化的开销。

### 1.3 实时日志流重定向机制
GraphWiz 的一大亮点在于能够将底层的 Gurobi 引擎求解日志实时反馈到 Qt 的 GUI 文本面板中。这一逻辑是由以下两部分优雅实现的：

1. **`LogStream` (`src/LogStream.h`)**：
   继承自标准库的 `std::streambuf`，并重写了 `xsputn` 和 `overflow` 方法。它的作用是接管 C++ 的标准输出流，将其自动重定向并追加到 Qt 界面上的 `QTextBrowser` 组件中：
   ```cpp
   class LogStream final : public std::streambuf {
       ...
       std::streamsize xsputn(const char *p, const std::streamsize n) override {
           text_browser->append(QString(p));
           return n;
       }
   };
   ```

2. **`GurobiCallback` (`src/LogStream.h`)**：
   继承自 Gurobi 提供的 `GRBCallback` 类。在 Gurobi 求解过程中，每当触发日志打印事件 (`GRB_CB_MESSAGE`)，该回调类会使用 `getStringInfo(GRB_CB_MSG_STRING)` 截获当前行文本，然后动态追加到界面上：
   ```cpp
   class GurobiCallback final : public GRBCallback {
   protected:
       void callback() override {
           if (where == GRB_CB_MESSAGE) {
               std::string message = getStringInfo(GRB_CB_MSG_STRING);
               text_browser->append(QString::fromStdString(message));
           }
       }
   };
   ```

---

## 2. 二次开发教程：如何扩展 GraphWiz

### 2.1 如何增加一种新的图拓扑结构
假设我们想在项目中添加一个**圈图 (Cycle Graph $C_n$)**。

#### 第一步：在 `src/Graph.h` 中声明类
```cpp
class CycleGraph final : public Graph {
public:
    CycleGraph() {
        this->graph_type = "Cycle Graph";
    }
    std::shared_ptr<Graph> create() override;
};
```

#### 第二步：在 `src/Graph.cpp` 中实现图生成逻辑
```cpp
std::shared_ptr<Graph> CycleGraph::create() {
    bool ok;
    const int n = QInputDialog::getInt(nullptr, "Input", "请输入顶点个数：", 5, 3, 10000, 1, &ok);
    if (ok) {
        this->n = n;
        this->init(this->n);
        for (int i = 0; i < n; i++) {
            int u = i;
            int v = (i + 1) % n;
            adj_matrix[u][v] = 1;
            adj_matrix[v][u] = 1;
            adj_list[u].append(v);
            adj_list[v].append(u);
            edges.append({u, v});
        }
        m = edges.size();
        return shared_from_this();
    }
    return nullptr;
}
```

#### 第三步：在 Qt 界面中注册该图类型 (`src/mainwindow.cpp`)
在下拉菜单响应事件中，添加对新图类型的实例化映射：
```cpp
void MainWindow::createGraph() {
    ...
    switch (create_graph_type) {
        ...
        case 8: // 对应 UI 下拉菜单中为您新添加的索引项
            graph = std::make_shared<CycleGraph>()->create();
            break;
    }
    ...
}
```

---

### 2.2 如何添加一个自定义数学优化模型
假设您想为图添加一个**最小边覆盖问题 (Minimum Edge Cover)** 的模型。

#### 第一步：在 `src/Model.h` 中声明模型构建函数
```cpp
class Model {
public:
    ...
    // 创建最小边覆盖模型
    ModelSharePtr createEdgeCoverModel();
};
```

#### 第二步：在 `src/Model.cpp` 中实现 Gurobi 建模逻辑
利用 Gurobi C++ API 构建 ILP 形式：
- 目标函数：最小化被选中的边数 $\sum_{e \in E} y_e$。
- 约束条件：对任意顶点 $i \in V$，至少要有一条与之关联的边被选中：$\sum_{e \in \delta(i)} y_e \ge 1$。

```cpp
ModelSharePtr Model::createEdgeCoverModel() {
    int n = this->graph->adj_matrix.size();
    int m = this->graph->edges.size();
    
    ModelSharePtr model = std::make_shared<GRBModel>(*env);
    model->set(GRB_StringAttr_ModelName, "Minimum Edge Cover");

    // 为每条边引入一个二进制决策变量
    QVector<GRBVar> vars_y(m);
    for (int e = 0; e < m; ++e) {
        std::stringstream varName;
        varName << "y(" << e << ")";
        vars_y[e] = model->addVar(0, 1, 0, GRB_BINARY, varName.str());
    }
    model->update();

    // 约束：每个顶点必须被至少一条选中的边覆盖
    for (int i = 0; i < n; ++i) {
        GRBLinExpr sum_edges = 0;
        for (int e = 0; e < m; ++e) {
            auto [u, v] = this->graph->edges[e];
            if (u == i || v == i) {
                sum_edges += vars_y[e];
            }
        }
        model->addConstr(sum_edges >= 1);
    }

    // 目标：最小化边变量的总和
    GRBLinExpr obj = 0;
    for (int e = 0; e < m; ++e) {
        obj += vars_y[e];
    }
    model->setObjective(obj, GRB_MINIMIZE);
    model->update();

    return model;
}
```

#### 第三步：在模型路由中注册该函数 (`src/Model.cpp`)
更新模型工厂派发器：
```cpp
ModelSharePtr Model::createLPModel(const int index) {
    switch (index) {
        case 0: return createDPModel();
        case 1: return createPDRDPModel();
        case 2: return createMISModel();
        case 3: return createMVCModel();
        case 4: return createEdgeCoverModel(); // 在这里添加您的新模型路由
        default: return nullptr;
    }
}
```

---

## 3. 开发者最佳实践

1. **跨平台兼容与路径配置**：
   Gurobi 的默认安装路径在 Windows、Linux 和 macOS 上具有较大差异。若在不同的操作系统或不同的编译器中运行，请优先修改 `CMakeLists.txt` 中的 `GUROBI_HOME` 指向本机的 Gurobi 安装目录。
2. **异步求解优化**：
   如果需要计算变量数万、极度耗时的复杂大规模图优化模型，为防止 Gurobi 的求解过程阻塞 Qt 主界面的 GUI 线程，建议将模型求解逻辑包装进入独立的 `QThread` 线程中运行。
3. **虚继承防菱形问题**：
   任何未来可能被用于进一步组合、混合的多态图生成类，请在继承 `Graph` 时声明为 `virtual public Graph`。这能够有效防止由于 C++ 深度继承产生的重复基类空间开销与指针二义性错误。
