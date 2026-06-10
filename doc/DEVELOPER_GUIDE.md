# GraphWiz: Developer & Second-Development Guide

Welcome to the GraphWiz Developer Guide! This document is designed for engineers, researchers, and students who wish to understand the software architecture of GraphWiz, customize its features, or extend it with new graph generators and mathematical models.

---

## 1. System Architecture & Core Patterns

GraphWiz is built with clear separation of concerns, decoupling the **Graph Topology Layer**, the **Mathematical Optimization Layer**, and the **GUI Presentation Layer**.

```
                  ┌───────────────────────────────┐
                  │          MainWindow           │ (GUI Layer)
                  └──────┬─────────────────┬──────┘
                         │                 │
                         ▼                 ▼
             ┌─────────────────────┐   ┌─────────────────────┐
             │        Graph        │   │        Model        │ (Optimization Bridge)
             │ (Abstract Base Class)│  │ (Gurobi IP Wrapper) │
             └───────────┬─────────┘   └───────────┬─────────┘
                         │                         │
     ┌───────────────────┼───────────────────┐     │
     ▼                   ▼                   ▼     ▼
CommonGraph         BlockGraph          CactusGraph ──▶ Gurobi C++ Engine
```

### 1.1 Polymorphic Graph Factory
The `Graph` class (`src/Graph.h`) represents an abstract graph interface:
- It holds fundamental adjacency models: adjacency matrix (`QVector<QVector<int>> adj_matrix`) and adjacency list (`QVector<QVector<int>> adj_list`).
- Each specialized graph (e.g., `GridGraph`, `CactusGraph`) inherits from `Graph` and overrides the pure virtual function:
  ```cpp
  virtual std::shared_ptr<Graph> create() = 0;
  ```
- Memory is safely managed using modern C++ shared pointers (`std::shared_ptr`) and `std::enable_shared_from_this<Graph>` to allow safe pointer passing.
- In `Graph.h`, `BlockGraph` and `CactusGraph` are declared with virtual inheritance (`class BlockGraph : virtual public Graph`) to prevent the Diamond Multiple Inheritance problem in potential hybrid structures.

### 1.2 Gurobi Environment Singleton & Lifecycle
Creating Gurobi environments (`GRBEnv`) is computationally expensive and prints startup licensing banners. To optimize performance, GraphWiz uses a **static shared-pointer Gurobi environment** inside the `Model` class (`src/Model.h`):
```cpp
class Model {
public:
    static std::shared_ptr<GRBEnv> env;
    ...
};
```
This guarantees that exactly one single Gurobi environment instance is initialized lazily and shared globally across all generated mathematical models, significantly saving memory and startup overhead.

### 1.3 Real-time Log Stream Redirection
A core feature of GraphWiz is displaying the solver's real-time console progress directly inside the GUI. This is achieved via two components:

1. **`LogStream` (`src/LogStream.h`)**:
   Inherits from `std::streambuf` and overrides the `xsputn` and `overflow` methods. It captures standard output streams and forwards them directly to the `QTextBrowser` widget in Qt:
   ```cpp
   class LogStream final : public std::streambuf {
       ...
       std::streamsize xsputn(const char *p, const std::streamsize n) override {
           text_browser->append(QString(p));
           return n;
       }
   };
   ```

2. **`GurobiCallback` (`src/LogStream.h`)**:
   Inherits from Gurobi's `GRBCallback` interface. When Gurobi logs a solution trace (`GRB_CB_MESSAGE`), the callback intercepts the message text via `getStringInfo(GRB_CB_MSG_STRING)` and appends it to the user interface:
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

## 2. Second-Development Tutorial: Extending GraphWiz

### 2.1 How to Add a New Graph Type
Suppose you want to add a **Cycle Graph** ($C_n$). Follow these steps:

#### Step 1: Declare the class in `src/Graph.h`
```cpp
class CycleGraph final : public Graph {
public:
    CycleGraph() {
        this->graph_type = "Cycle Graph";
    }
    std::shared_ptr<Graph> create() override;
};
```

#### Step 2: Implement the generator in `src/Graph.cpp`
```cpp
std::shared_ptr<Graph> CycleGraph::create() {
    bool ok;
    const int n = QInputDialog::getInt(nullptr, "Input", "Enter number of vertices:", 5, 3, 10000, 1, &ok);
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

#### Step 3: Register in the Qt GUI (`src/mainwindow.cpp`)
Add your option to the UI dropdown index mapping:
```cpp
void MainWindow::createGraph() {
    ...
    switch (create_graph_type) {
        ...
        case 8: // Assuming index 8 is registered for CycleGraph
            graph = std::make_shared<CycleGraph>()->create();
            break;
    }
    ...
}
```

---

### 2.2 How to Add a New Mathematical Model
Suppose you want to add the **Minimum Edge Cover Problem**.

#### Step 1: Declare the model builder in `src/Model.h`
```cpp
class Model {
public:
    ...
    // Create Minimum Edge Cover Model
    ModelSharePtr createEdgeCoverModel();
};
```

#### Step 2: Implement the Gurobi Formulation in `src/Model.cpp`
Using the Gurobi C++ API, formulate the ILP:
- Objective: Minimize selected edges $\sum_{e \in E} y_e$.
- Constraint: For each vertex $i \in V$, at least one incident edge must be selected: $\sum_{e \in \delta(i)} y_e \ge 1$.

```cpp
ModelSharePtr Model::createEdgeCoverModel() {
    int n = this->graph->adj_matrix.size();
    int m = this->graph->edges.size();
    
    ModelSharePtr model = std::make_shared<GRBModel>(*env);
    model->set(GRB_StringAttr_ModelName, "Minimum Edge Cover");

    // Decision variables for each edge
    QVector<GRBVar> vars_y(m);
    for (int e = 0; e < m; ++e) {
        std::stringstream varName;
        varName << "y(" << e << ")";
        vars_y[e] = model->addVar(0, 1, 0, GRB_BINARY, varName.str());
    }
    model->update();

    // Constraint: Each vertex must be covered by at least one selected edge
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

    // Objective: Minimize sum of edge variables
    GRBLinExpr obj = 0;
    for (int e = 0; e < m; ++e) {
        obj += vars_y[e];
    }
    model->setObjective(obj, GRB_MINIMIZE);
    model->update();

    return model;
}
```

#### Step 3: Integrate with Gurobi model resolver in `src/Model.cpp`
Update the `createLPModel(index)` dispatcher mapping:
```cpp
ModelSharePtr Model::createLPModel(const int index) {
    switch (index) {
        case 0: return createDPModel();
        case 1: return createPDRDPModel();
        case 2: return createMISModel();
        case 3: return createMVCModel();
        case 4: return createEdgeCoverModel(); // Add Edge Cover here
        default: return nullptr;
    }
}
```

---

## 3. Best Practices for Developers

- **Always configure path in CMake**:
  Gurobi's default paths differ heavily between platforms. When compiling on Windows, Linux, or macOS, specify the correct directory in `CMakeLists.txt` via `GUROBI_HOME`.
- **Use Gurobi Callbacks for Async operations**:
  To prevent Gurobi from blocking the Qt main UI thread during highly complex computations, always hook a customized callback or compile the solver logic as a separate asynchronous background thread if necessary.
- **Maintain Diamond Inheritance Safety**:
  If you inherit multiple subclasses of `Graph` together, always inherit them as `virtual public Graph` to ensure there are no duplicate allocations of the base properties (`adj_matrix`, `adj_list`).
