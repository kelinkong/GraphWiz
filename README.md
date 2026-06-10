# GraphWiz

An interactive Qt/C++ desktop application that bridges **Procedural Graph Generation** and **Exact Integer Linear Programming (ILP) Solvers**.

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](./LICENSE)
[![C++](https://img.shields.io/badge/C%2B%2B-17-blue.svg?style=flat&logo=c%2B%2B)](https://en.cppreference.com/w/cpp/17)
[![Qt5](https://img.shields.io/badge/Qt-5.15-green.svg?style=flat&logo=qt)](https://www.qt.io/)
[![Gurobi](https://img.shields.io/badge/Gurobi-Optimizer-darkgreen.svg?style=flat)](https://www.gurobi.com/)

[**简体中文版说明 (Chinese Version)**](./README_zh.md) | [**Algorithms & Models Guide**](./doc/ALGORITHMS.md) | [**Developer Guide**](./doc/DEVELOPER_GUIDE.md)

---

## Why GraphWiz?

In graph theory and operations research, researchers and students often face a disjointed workflow: generating specific graph topologies in one script, exporting them, writing integer programming formulations in another language, and then feeding them into a solver.

**GraphWiz** unifies this entire pipeline into a single, intuitive GUI application:
1. **Generate**: Instantly generate complex, procedurally constructed graphs (such as Block, Cactus, Interval, and Grid graphs) of arbitrary sizes.
2. **Formulate**: Automatically map classic and advanced combinatorial optimization problems onto the generated graph using mathematically verified ILP formulations.
3. **Solve & Analyze**: Solve the model in real-time using the industry-leading **Gurobi Optimizer** and examine the detailed status of variables, optimal objective values, and solver performance right inside the app.

---

## Core Features

### 1. Procedural Graph Generation
GraphWiz supports generating 8 different topological graph types with custom vertex and edge distributions:
- **Common Graph**: Standard random graph (Erdős-Rényi-like).
- **Bipartite Graph**: Generates independent partition sets $X$ and $Y$ with no intra-set edges.
- **Grid Graph**: 2D grid structure defined by rows and columns.
- **Tree Graph**: Connected acyclic graph generated using random parent links.
- **Interval Graph**: Constructed from randomly weighted overlapping segments.
- **Block Graph**: Clusters of fully-connected blocks joined via cut-vertices.
- **Cactus Graph**: A connected graph in which any two simple cycles share at most one vertex.
- **Block-Cactus Graph**: A hybrid configuration generalizing both structures.

### 2. Built-in Integer Programming (ILP) Models
Solve classical and cutting-edge graph optimization problems directly on the active graph:
- **Minimum Dominating Set (DP)**: Finding the smallest set of vertices such that every vertex not in the set is adjacent to at least one member.
- **Minimum Vertex Cover (MVC)**: Selecting a minimum set of vertices such that each edge of the graph is incident to at least one selected vertex.
- **Maximum Independent Set (MIS)**: Finding the largest set of vertices no two of which are adjacent.
- **Perfect Double Roman Dominating Set (PDRDP)**: Solving an advanced, resource-constrained Roman domination configuration with exact formulation.

### 3. General LP File Solver
Already have your own optimization problems? Upload any external standard `.lp` or `.mps` files to run them directly through the GraphWiz solver engine with real-time log callbacks.

---

## Architectural Overview

GraphWiz is engineered with clean OOP principles and decoupled C++ patterns:

- **Graph Factory & Hierarchy**: An abstract base `Graph` class with specialized polymorphic implementations using standard library features. It uses C++ virtual inheritance to resolve diamond inheritance for hybrid structures (such as `BlockCactusGraph` inheriting from `BlockGraph` and `CactusGraph`).
- **Optimization Bridge**: The `Model` class decouples the graph topology from Gurobi's solver API, acting as a factory for ILP model generation.
- **GUI Engine**: A responsive Qt GUI coupled with standard standard streams redirected to a custom GUI log via `LogStream`.

```
                    ┌────────────────────────┐
                    │      MainWindow        │ (Qt GUI View)
                    └───────────┬────────────┘
                                │
               ┌────────────────┴────────────────┐
               ▼                                 ▼
   ┌───────────────────────┐         ┌───────────────────────┐
   │       Graph           │         │         Model         │ (ILP Generator)
   │  (Abstract Base Class)│         │ (Decoupled Solver API)│
   └───────────┬───────────┘         └───────────┬───────────┘
               │                                 │
     ┌─────────┼─────────┐                       │ (Invokes Gurobi)
     ▼         ▼         ▼                       ▼
Common...   Block     Cactus       ┌───────────────────────────┐
                      ▲   ▲        │     Gurobi Optimizer      │ (C++ API)
                      └───┴────────┤ (GRBEnv, GRBModel, Var/Con)│
                    Virtual        └───────────────────────────┘
                  Inheritance
```

---

## Getting Started

### Prerequisites

To compile and run GraphWiz locally, your system must have:
- **C++ Compiler**: Supporting C++17 or higher (e.g., GCC 9+, Clang 10+, or MSVC 2019+).
- **CMake**: Minimum version `3.28`.
- **Qt5 SDK**: `Core`, `Gui`, and `Widgets` modules installed.
- **Gurobi Optimizer**: An active installation (academic/student or full license recommended, as the free trial has model size limits).

### Installation & Build

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/GraphWiz.git
   cd GraphWiz
   ```

2. **Configure Paths**:
   Open `CMakeLists.txt` and update the search paths for Qt5 and Gurobi depending on your platform:
   ```cmake
   # For macOS (Example with Homebrew and Gurobi 11.0.1)
   set(CMAKE_PREFIX_PATH "/opt/homebrew/Cellar/qt@5/5.15.13") 
   set(GUROBI_HOME "/Library/gurobi1101/macos_universal2")
   ```

3. **Build the Project**:
   ```bash
   mkdir build && cd build
   cmake ..
   make
   ```

4. **Run**:
   ```bash
   ./GraphWiz
   ```

---

## Secondary Documentation Guides

To help you get the most out of GraphWiz, we have prepared comprehensive guides:

- **[Algorithms & Mathematical Formulations Guide](./doc/ALGORITHMS.md)**: Includes procedural graph-generation pseudocode and strict LaTeX mathematical formulations of all the ILP models (DP, PDRDP, MVC, MIS).
- **[Developer & Second-Development Guide](./doc/DEVELOPER_GUIDE.md)**: Explains the internal class layouts, design patterns (virtual inheritance, factory), memory management, and step-by-step instructions on how to add custom graph topologies or new solver models.

---

## License

This project is licensed under the **GNU General Public License v3.0 (GPLv3)**. See the [LICENSE](./LICENSE) file for the full text.

---

*Developed with ❤️ for Graph Theory and Optimization Researchers.*
