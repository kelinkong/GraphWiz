# GraphWiz: Algorithms & Mathematical Formulations Guide

This document provides a deep dive into the procedural graph-generation algorithms and the Integer Linear Programming (ILP) mathematical formulations implemented in GraphWiz.

---

## Part 1: Procedural Graph Generation

GraphWiz provides specialized algorithms to construct various graph structures with custom vertex size $n$. This section explains their generation mechanics.

### 1. Common Graph (Erdős-Rényi $G(n, p)$)
- **Topological Description**: A random graph where edges are created independently with a static probability.
- **Algorithm**:
  1. Initialize an empty adjacency matrix for $n$ vertices.
  2. For every distinct pair of vertices $(i, j)$ where $i < j$:
     - Generate a uniform random floating-point value $r \in [0, 1)$.
     - If $r < p$ (default $p = 0.3$), create an undirected edge between $i$ and $j$.

### 2. Bipartite Graph
- **Topological Description**: A graph whose vertices can be divided into two disjoint sets $X$ and $Y$ such that no edge connects vertices within the same set.
- **Algorithm**:
  1. Given total vertices $n$ and desired size of partition $X$ ($|X| = n_x$).
  2. Divide the vertex array into $X = \{0, 1, \dots, n_x - 1\}$ and $Y = \{n_x, \dots, n - 1\}$.
  3. For each $i \in X$ and each $j \in Y$:
     - Draw $r \in [0, 1)$.
     - If $r < 0.3$, add edge $(i, j)$.

### 3. Grid Graph (2D Lattice)
- **Topological Description**: A graph represented as a 2-dimensional grid where each vertex is connected to its immediate neighbors (up, down, left, right).
- **Algorithm**:
  1. Given rows $R$ and columns $C$, set total vertices $n = R \times C$.
  2. Map each vertex $v \in \{0, \dots, n-1\}$ to grid coordinates $(i, j)$ via $v = i \cdot C + j$.
  3. For every cell $(i, j)$:
     - Connect to the cell above $(i-1, j)$ if $i > 0$.
     - Connect to the cell to the left $(i, j-1)$ if $j > 0$.

### 4. Tree Graph (Random Spanning Tree)
- **Topological Description**: A connected acyclic undirected graph with $n$ vertices and $n-1$ edges.
- **Algorithm**:
  1. Initialize vertex 0 as the initial tree.
  2. For $i$ from 1 to $n-1$:
     - Randomly select a parent index $p \in \{0, \dots, i-1\}$.
     - Create an edge $(i, p)$. This guarantees connectivity and prevents cycle formation.

### 5. Interval Graph
- **Topological Description**: An intersection graph of a family of intervals on the real line. Two vertices are connected if their corresponding intervals overlap.
- **Algorithm** (Weighted Random Neighborhood):
  1. Initialize adjacency matrix.
  2. For $i$ from 1 to $n-1$:
     - Select a random boundary index $max\_neighbor \in [i, n-1]$ using a weighted probability function.
     - For $j$ from $max\_neighbor - 1$ down to $i$:
       - Create an edge $(j, max\_neighbor)$.

### 6. Block Graph
- **Topological Description**: A graph in which every biconnected component (block) is a clique (a fully connected subgraph).
- **Algorithm**:
  1. **Disjoint Block Creation**: Partition the $n$ vertices into disjoint blocks of random sizes (using a weighted distribution bounded by remaining vertices). Build a clique (complete graph) for each block.
  2. **Inter-block Bridging**: Link the disjoint blocks sequentially by identifying the last vertex of block $i-1$ and the first vertex of block $i$, merging/connecting them to form cut-vertices.

### 7. Cactus Graph
- **Topological Description**: A connected graph in which any two simple cycles share at most one vertex. Essentially, it is a tree of cycles and edges.
- **Algorithm**:
  1. **Disjoint Cycle Creation**: Partition $n$ vertices into disjoint cycle sets of varying lengths. For each partition of size $k$, connect the nodes sequentially in a ring ($u \to v$ where $v = (u+1) \bmod k$).
  2. **Inter-cycle Bridging**: Join consecutive cycle sets by selecting the last vertex of cycle $i-1$ and the first vertex of cycle $i$ and connecting them via a bridge edge.

### 8. Block-Cactus Graph
- **Topological Description**: A hybrid topological structure containing both cliques (blocks) and simple cycles, joined together such that blocks and cycles share at most single cut-vertices.
- **Algorithm**:
  1. Procedurally generate some independent cycles and some independent cliques.
  2. Randomly select nodes among the sets and wire them using cut-vertices to guarantee global connectivity, while preserving Cycle-Cactus and Clique-Block properties.

---

## Part 2: Integer Linear Programming (ILP) Formulations

GraphWiz models combinatorial problems as exact ILP problems. Let $G = (V, E)$ be the generated graph, where $V = \{0, 1, \dots, n-1\}$ is the vertex set, and $E$ is the edge set. Let $N(i) = \{j \in V \mid (i, j) \in E\}$ denote the open neighborhood of vertex $i$.

---

### 1. Minimum Dominating Set Problem (DP)

The objective is to find a minimum-cardinality subset of vertices $S \subseteq V$ such that every vertex not in $S$ is adjacent to at least one vertex in $S$.

#### Decision Variables
We define two binary decision variables for each vertex $i \in V$:
- $x_{i,0} \in \{0, 1\}$: $x_{i,0} = 1$ if vertex $i \notin S$.
- $x_{i,1} \in \{0, 1\}$: $x_{i,1} = 1$ if vertex $i \in S$.

#### Mathematical Model
$$
\begin{array}{lll}
\min & \displaystyle\sum_{i \in V} x_{i,1} & \\[4ex]
\text{s.t.} & x_{i,0} + x_{i,1} = 1, & \forall i \in V \\[2ex]
            & (1 - x_{i,0}) + \displaystyle\sum_{j \in N(i)} x_{j,1} \ge 1, & \forall i \in V \\[4ex]
            & x_{i,j} \in \{0, 1\}, & \forall i \in V, \, j \in \{0, 1\}
\end{array}
$$

#### Explanation of Constraints
1. **Uniqueness Constraint**: $x_{i,0} + x_{i,1} = 1$ ensures each vertex is assigned exactly one status (either in the set $S$ or out of it).
2. **Dominating Constraint**: If vertex $i$ is not in the set ($x_{i,0} = 1$), then $(1 - x_{i,0}) = 0$, forcing the neighborhood sum $\sum_{j \in N(i)} x_{j,1} \ge 1$. This guarantees that at least one adjacent neighbor is selected in $S$.

---

### 2. Perfect Double Roman Dominating Set Problem (PDRDP)

A Perfect Double Roman Dominating Set (PDRDS) assigns weights $w: V \to \{0, 1, 2, 3\}$ to vertices such that:
- Every vertex with weight 0 is adjacent to exactly two vertices with weight 2, or exactly one vertex with weight 3.
- Every vertex with weight 1 is adjacent to exactly one vertex with weight 2.
- Adjacent vertices cannot have weight 1 and weight 3 simultaneously.

#### Decision Variables
For each vertex $i \in V$, we introduce four binary variables:
- $x_{i,j} \in \{0, 1\}$ for $j \in \{0, 1, 2, 3\}$, where $x_{i,j} = 1$ if vertex $i$ is assigned weight $j$.

#### Mathematical Model
$$
\begin{array}{lll}
\min & \displaystyle\sum_{i \in V} (x_{i,1} + 2x_{i,2} + 3x_{i,3}) & \\[4ex]
\text{s.t.} & \displaystyle\sum_{j=0}^{3} x_{i,j} = 1, & \forall i \in V \\[4ex]
            & 2(1 - x_{i,0}) + x_{i,0} \displaystyle\sum_{k \in N(i)} (x_{k,2} + 2x_{k,3}) = 2, & \forall i \in V \\[4ex]
            & x_{i,1} \left( \displaystyle\sum_{k \in N(i)} x_{k,2} \right) - x_{i,1} = 0, & \forall i \in V \\[4ex]
            & x_{i,1} + x_{j,3} \le 1, & \forall (i, j) \in E \\[2ex]
            & x_{i,j} \in \{0, 1\}, & \forall i \in V, \, j \in \{0, 1, 2, 3\}
\end{array}
$$

#### Explanation of Constraints
1. **Partition Constraint**: $\sum_{j=0}^{3} x_{i,j} = 1$ ensures that every vertex $i$ is assigned exactly one weight from $\{0, 1, 2, 3\}$.
2. **Weight-0 Neighbor Constraint**: If a vertex has weight 0 ($x_{i,0}=1$), then the quadratic term becomes $\sum_{k \in N(i)} (x_{k,2} + 2x_{k,3}) = 2$. This requires that the sum of neighboring weights equals exactly 2 (either two vertices of weight 2, or one vertex of weight 3). If $x_{i,0}=0$, the constraint reduces to $2 = 2$ and is trivially satisfied.
3. **Weight-1 Neighbor Constraint**: If a vertex has weight 1 ($x_{i,1}=1$), it forces $\sum_{k \in N(i)} x_{k,2} = 1$. This means it must have exactly one neighbor of weight 2.
4. **Adjacency Restriction**: $x_{i,1} + x_{j,3} \le 1$ forbids any edge from connecting a vertex of weight 1 directly to a vertex of weight 3.

---

### 3. Maximum Independent Set Problem (MIS)

An independent set is a subset of vertices $I \subseteq V$ such that no two vertices in $I$ are adjacent. We seek to maximize the size of $I$.

#### Decision Variables
We define a binary variable for each vertex $i \in V$:
- $x_i \in \{0, 1\}$: $x_i = 1$ if vertex $i \in I$, and $0$ otherwise.

#### Mathematical Model
$$
\begin{array}{lll}
\max & \displaystyle\sum_{i \in V} x_i & \\[4ex]
\text{s.t.} & x_i + x_j \le 1, & \forall (i, j) \in E \\[2ex]
            & x_i \in \{0, 1\}, & \forall i \in V
\end{array}
$$

#### Explanation of Constraints
1. **Conflict Constraint**: $x_i + x_j \le 1$ ensures that if edge $(i, j)$ exists, we cannot select both endpoint vertices $i$ and $j$ into the independent set $I$.

---

### 4. Minimum Vertex Cover Problem (MVC)

A vertex cover is a subset of vertices $C \subseteq V$ such that each edge $(i, j) \in E$ has at least one of its endpoints in $C$. We seek to minimize the size of $C$.

#### Decision Variables
We define binary status variables for each vertex $i \in V$:
- $x_{i,0} \in \{0, 1\}$: $x_{i,0} = 1$ if vertex $i \notin C$.
- $x_{i,1} \in \{0, 1\}$: $x_{i,1} = 1$ if vertex $i \in C$.

#### Mathematical Model
$$
\begin{array}{lll}
\min & \displaystyle\sum_{i \in V} x_{i,1} & \\[4ex]
\text{s.t.} & x_{i,0} + x_{i,1} = 1, & \forall i \in V \\[2ex]
            & x_{i,1} + x_{j,1} \ge 1, & \forall (i, j) \in E \\[2ex]
            & x_{i,j} \in \{0, 1\}, & \forall i \in V, \, j \in \{0, 1\}
\end{array}
$$

#### Explanation of Constraints
1. **Uniqueness Constraint**: $x_{i,0} + x_{i,1} = 1$ ensures each vertex is strictly assigned "in cover" or "out of cover".
2. **Covering Constraint**: $x_{i,1} + x_{j,1} \ge 1$ ensures that for any edge $(i, j)$, at least one of the vertices $i$ or $j$ is chosen in the vertex cover $C$.
