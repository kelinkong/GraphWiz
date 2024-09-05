# model 文件夹的说明
用于存放调用gurobi的ILP模型以及对应求解问题的自编代码
> 以perfectDoubleRomanDomination为例，文件夹内包含
> 1. ILP文件求解任意图上的控制数。
> 2. 设计的某个DP程序求解树上的控制数。



# gurobi Java API 概述 
[原文出处](https://www.gurobi.com/documentation/9.5/refman/java_api_overview.html)

本节介绍 Gurobi Java 接口。本手册首先简要概述接口中公开的类以及这些类上最重要的方法。然后 全面介绍所有可用的类和方法。

如果您更喜欢 Javadoc 格式，Gurobi Java 接口的文档也可在文件gurobi-javadoc.jar中找到。Javadoc 格式在 Eclipse® 等集成开发环境中使用时特别有用。有关如何导入 Javadoc 文件的信息，请参阅 IDE 的文档。

如果您是 Gurobi Optimizer 的新手，我们建议您从 快速入门指南 或 示例导览开始。这些文档提供了如何使用此处描述的类和方法的具体示例。

## 环境

使用 Gurobi Java 接口的第一步是创建环境对象。环境使用 GRBEnv类表示。环境充当与一组优化运行相关的所有数据的容器。您的程序中通常只需要一个环境对象。

对于更高级的用例，您可以使用空环境创建未初始化的环境，然后以编程方式设置满足特定要求的所有必需选项。有关更多详细信息，请参阅环境部分。

## 模型

您可以在环境中创建一个或多个优化模型。每个模型都表示为 GRBModel类的对象。模型由一组决策变量（ GRBVar类的对象）、这些变量的线性或二次目标函数（使用 GRBModel.setObjective指定）以及这些变量的一组约束（ GRBConstr、 GRBQConstr、 GRBSOS或GRBGenConstr类的对象）组成。每个变量都有关联的下限、上限和类型（连续、二进制等）。每个线性或二次约束都有关联的含义（小于或等于、大于或等于或等于）和右侧值。 有关变量、约束和目标的更多信息，请参阅参考手册中的 此部分。

线性约束通过构建线性表达式（GRBLinExpr类的对象）然后指定这些表达式之间的关系（例如，要求一个表达式等于另一个表达式）来指定。二次约束以类似的方式构建，但使用二次表达式（ GRBQuadExpr类的对象）。

优化模型可以一次性指定，方法是从文件加载模型（使用适当的 GRBModel构造函数），也可以逐步构建，方法是先构造 GRBModel类的空对象，然后调用GRBModel.addVar 或GRBModel.addVars来添加其他变量，然后调用 GRBModel.addConstr、 GRBModel.addQConstr、 GRBModel.addSOS或任何 GRBModel.addGenConstrXxx方法来添加其他约束。模型是动态实体；您随时可以添加或删除变量或约束。

我们经常提到优化模型的类别。具有线性目标函数、线性约束和连续变量的模型称为线性规划 (LP)。如果目标是二次的，则该模型为二次规划 (QP)。如果任何约束是二次的，则该模型为 二次约束规划 (QCP)。我们有时会提到 Q​​CP 的一些特例：具有凸约束的 QCP、具有非凸约束的 QCP、 双线性规划和二阶锥规划 (SOCP)。如果模型包含任何整数变量、半连续变量、半整数变量、特殊有序集 (SOS) 约束或一般约束，则该模型为 混合整数规划 (MIP)。我们有时还会讨论 MIP 的特殊情况，包括混合整数线性规划 (MILP)、混合整数二次规划 (MIQP)、 混合整数二次约束规划 (MIQCP)和 混合整数二阶锥规划 (MISOCP)。Gurobi 优化器处理所有这些模型类。

## 解决模型

构建模型后，您可以调用 GRBModel.optimize来计算解决方案。默认情况下， optimize 将使用 并发优化器 来解决 LP 模型，使用障碍算法来解决具有凸目标的 QP 模型和具有凸约束的 QCP 模型，否则使用分支定界算法。解决方案存储在模型的一组 属性中。可以使用GRBModel、 GRBVar、 GRBConstr、 GRBQConstr、 GRBSOS和 GRBGenConstr以及类 上的一组属性查询方法来查询这些属性 。

Gurobi 算法会仔细跟踪模型的状态，因此，只有当相关数据自上次优化模型以来发生变化时，调用 GRBModel.optimize 才会执行进一步优化。如果您想丢弃之前计算的解决方案信息并从头开始重新优化而不更改模型，您可以调用 GRBModel.reset。

在 MIP 模型求解后，您可以调用 GRBModel.fixedModel 来计算相关的固定模型。此模型与原始模型相同，只是整数变量在 MIP 解决方案中固定为它们的值。如果您的模型包含 SOS 约束，则这些约束中出现的一些连续变量也可能是固定的。在某些应用中，计算此固定模型的信息（例如，对偶变量、敏感度信息等）可能很有用，但您应该小心解释这些信息。

## 多种解决方案、目标和场景

默认情况下，Gurobi 优化器假设您的目标是找到一个经过验证的、针对单个目标函数的单个模型的最佳解决方案。Gurobi 提供以下功能，让您可以放宽这些假设：

- 解决方案池：允许您找到更多解决方案。
- 多种场景：允许您找到多个相关模型的解决方案。
- 多重目标：允许您指定多个目标函数并控制它们之间的权衡。
## 不可行的模型

如果发现模型不可行，您有几种选择。您可以尝试诊断不可行性的原因，尝试修复不可行性，或两者兼而有之。要获取可用于诊断不可行性原因的信息，请调用 GRBModel.computeIIS 来计算不可约不一致子系统 (IIS)。此方法可用于连续和 MIP 模型，但您应该知道 MIP 版本可能非常昂贵。此方法填充一组 IIS 属性。

要尝试修复不可行性，请调用 GRBModel.feasRelax 来计算模型的可行性松弛。此松弛可让您找到最小化约束违规程度的解决方案。

## 查询和修改属性

与 Gurobi 模型相关的大部分信息都存储在一组属性中。一些属性与模型的变量相关，一些与模型的约束相关，一些与模型本身相关。举一个简单的例子，求解优化模型会导致变量属性X被填充。X由 Gurobi 优化器计算的属性不能由用户直接修改，而其他属性，例如变量下限（属性LB），则可以。

使用GRBVar.get、 GRBConstr.get、 GRBQConstr.get、 GRBSOS.get、 GRBGenConstr.get或 GRBModel.get 查询属性 ，并使用 GRBVar.set、 GRBConstr.set、 GRBQConstr.set、 GRBGenConstr.set或 GRBModel.set修改属性。属性按类型分组为一组枚举（GRB.CharAttr、 GRB.DoubleAttr、 GRB.IntAttr、 GRB.StringAttr）。get()和set()方法是重载的，因此属性的类型决定了返回值的类型。因此，constr.get(GRB.DoubleAttr.RHS)返回一个 double，而constr.get(GRB.CharAttr.Sense)返回一个 char。

如果您希望检索一组变量或约束的属性值，通常使用关联GRBModel对象上的数组方法会更有效。方法 GRBModel.get 包含签名，允许您查询或修改一维、二维和三维变量或约束数组的属性值。

完整的属性列表可以在 属性部分找到。

## 附加模型修改信息

对现有模型的大多数修改都是通过属性接口进行的（例如，对变量边界、约束右侧等的更改）。主要的例外是对约束矩阵和目标函数的修改。

约束矩阵可以通过几种方式进行修改。第一种是调用GRBModel对象上的chgCoeff方法来更改单个矩阵系数。此方法可用于修改现有非零值、将现有非零值设置为零或创建新的非零值。当您从模型中删除变量或约束时（通过 GRBModel.remove 方法 ） ，约束矩阵也会被修改。与已删除的约束或变量相关的非零值将与约束或变量本身一起被删除。

模型目标函数也可以通过几种方式进行修改。最简单的方法是构建一个捕获目标函数的表达式（GRBLinExpr或 GRBQuadExpr对象），然后将该表达式传递给方法 GRBModel.setObjective。如果您希望修改目标，只需 使用新的或 对象setObjective再次调用即可。 GRBLinExprGRBQuadExpr

对于线性目标函数，另一种方法setObjective 是使用Obj变量属性来修改各个线性目标系数。

如果您的变量具有分段线性目标，则可以使用GRBModel.setPWLObj 方法指定它们。为每个相关变量调用一次此方法。Gurobi 单纯形求解器包括对凸分段线性目标函数的算法支持，因此对于连续模型，您应该看到使用此功能可带来显着的性能优势。要清除先前指定的分段线性目标函数，只需将Obj相应变量的属性设置为 0。

## 延迟更新

关于 Gurobi 优化器中的模型修改，需要注意的一点是，它以惰性 方式执行，这意味着修改不会立即影响模型。相反，它们会排队并稍后应用。如果您的程序只是创建一个模型并对其进行求解，您可能永远不会注意到这种行为。但是，如果您在应用修改之前询问有关模型的信息，那么惰性更新方法的细节可能与您有关。

正如我们刚才提到的，模型修改（边界变化、右侧变化、目标变化等）被放在队列中。这些排队的修改可以通过三种不同的方式应用于模型。第一种是通过显式调用 GRBModel.update 。第二种是通过调用GRBModel.optimize。第三种是通过调用GRBModel.write来写出模型。第一种情况让你可以对何时应用修改进行细粒度的控制。第二种和第三种情况假设你希望在优化模型或将其写入磁盘之前应用所有待处理的修改。

Gurobi 接口为什么以这种方式运行？有几个原因。首先，这种方法使对模型进行多次修改变得更加容易，因为模型在修改之间保持不变。第二，处理模型修改可能很昂贵，特别是在 Compute Server 环境中，修改需要机器之间的通信。因此，了解这些修改的具体应用时间很有用。一般来说，如果您的程序需要对模型进行多次修改，您应该分阶段进行，先进行一组修改，然后更新，然后进行更多修改，然后再次更新等。每次修改后进行更新的成本可能非常高。

如果您忘记调用更新，您的程序不会崩溃。您的查询将仅返回从上次更新点开始的请求数据的值。如果您尝试查询的对象当时不存在，您将得到一个NOT_IN_MODEL异常。

自早期 Gurobi 版本以来，延迟更新的语义已发生变化。虽然绝大多数程序不受此更改的影响，但 如果遇到问题， 您可以使用UpdateMode参数恢复到以前的行为。

## 管理参数

Gurobi 优化器提供了一组参数，可让您控制优化过程的许多细节。在开始优化之前，可以通过修改 Gurobi 参数来控制可行性和最优性容差、算法选择、探索 MIP 搜索树的策略等因素。参数可以是int、double或string类型。

设置参数最简单的方法是通过 模型对象上的 GRBModel.set方法。同样，可以使用GRBModel.get查询参数值。

还可以使用GRBEnv.set 在 Gurobi 环境对象上设置参数 。请注意，每个模型在创建时都会获得自己的环境副本，因此对原始环境的参数更改不会对现有模型产生影响。

您可以使用GRBEnv.readParams 从文件中读取一组参数设置 ，或者使用GRBEnv.writeParams写入一组更改的参数 。

我们还提供了一个自动参数调整工具，它可以探索许多不同的参数变化集，以找到一组可以提高性能的参数变化集。您可以调用 GRBModel.tune 来在模型上调用调整工具。有关 更多信息，请参阅参数调整工具 部分。

Gurobi 参数的完整列表可以在 参数部分找到。

## 内存管理

用户通常不需要关心 Java 中的内存管理，因为它由垃圾收集器自动处理。Gurobi Java 接口使用与其他 Java 程序相同的垃圾收集机制，但用户应该注意我们的内存管理的一些细节。

一般来说，Gurobi 对象与其他 Java 对象位于同一个 Java 堆中。当它们不再被引用时，它们将成为垃圾收集的候选对象，并在下次调用垃圾收集器时返回到可用空间池。两个重要的例外是 GRBEnv 和GRBModel 对象。GRBModel对象在 Java 堆中具有少量与其关联的内存，但与模型关联的大部分空间位于 Gurobi 本机代码库（Windows 中的 Gurobi DLL 或 Linux 或 Mac 中的 Gurobi 共享库）的堆中。Java 堆管理器不知道本机代码库中与模型关联的内存，因此在决定是否调用垃圾收集器时不会考虑此内存使用情况。当垃圾收集器最终收集 Java 对象时GRBModel ，Gurobi 本机代码库中与模型关联的内存将被释放，但此收集可能比您想要的晚。类似的考虑也适用于对象GRBEnv 。

如果您正在编写使用多个 Gurobi 模型或环境的 Java 程序，我们建议您 在使用完关联对象后调用GRBModel.disposeGRBModel，并 在使用关联 对象后调用GRBEnv.disposeGRBEnv，并在 使用该对象创建的所有模型上调用GRBModel.disposeGRBEnv之后调用 GRBEnv.dispose 。

## 本机代码

如前所述，Gurobi Java 接口是一个薄层，位于我们的本机代码库（Windows 上的 Gurobi DLL 和 Linux 或 Mac 上的 Gurobi 共享库）之上。因此，使用 Gurobi Java 库的应用程序将在运行时加载 Gurobi 本机代码库。为了实现这一点，您需要确保两件事属实。首先，您需要确保本机代码库在目标机器（PATH 在 Windows、LD_LIBRARY_PATHLinux 或DYLD_LIBRARY_PATHMac 上）的搜索路径中可用。这些路径是作为 Gurobi Optimizer 安装的一部分设置的，但在未安装完整 Gurobi Optimizer 的机器上可能无法正确配置。其次，您需要确保 Java JVM 和 Gurobi 本机库使用相同的对象格式。特别是，您需要使用 64 位 Java JVM 才能使用 64 位 Gurobi 本机库。

## 监控进度 - 日志和回调

可以通过 Gurobi 日志记录来监控优化进度。默认情况下，Gurobi 会将输出发送到屏幕。可以使用一些简单的控件来修改默认日志记录行为。如果您希望将输出直接发送到文件和屏幕，请在GRBEnv构造函数中指定日志文件名。如果您希望在创建环境对象后将日志重定向到其他文件，则可以修改 LogFile参数。可以使用DisplayInterval 参数控制日志输出的频率 ，并且可以使用OutputFlag参数 完全关闭日志 记录。有关 Gurobi 日志文件的详细描述，请参阅日志记录部分。

可以通过 GRBCallback类进行更详细的进度监控。GRBModel.setCallback 方法允许您从 Gurobi 优化器接收定期回调。您可以通过对GRBCallback抽象类进行子类化并在该类上编写自己的方法来执行此操作。您可以在回调中调用 GRBCallback.getDoubleInfo、 GRBCallback.getIntInfo、 GRBCallback.getStringInfo或 GRBCallback.getSolution 来获取有关优化状态的其他信息。 Callback()

此外，您可以向环境对象 ( GRBEnv.setLogCallback ) 或模型对象 ( GRBModelEnv.setLogCallback ) 添加日志回调函数。这样您就可以捕获环境对象或模型对象发布的输出。

## 修改求解器行为 - 回调

回调还可用于修改 Gurobi 优化器的行为。最简单的控制回调是 GRBCallback.abort，它要求优化器在最早的方便点终止。方法 GRBCallback.setSolution 允许您在 MIP 模型的解决过程中注入可行解决方案（或部分解决方案）。方法 GRBCallback.addCut 和 GRBCallback.addLazy 分别允许您在 MIP 优化期间 添加切割平面和惰性约束。方法GRBCallback.stopOneMultiObj允许您中断多目标 MIP 问题中某个优化步骤的优化过程，而无需停止分层优化过程。

## 批次优化

Gurobi Compute Server 使程序能够将优化计算卸载到专用服务器上。Gurobi Cluster Manager 在此基础上增加了许多附加功能。其中一项重要功能是 批量优化，它允许您使用客户端程序构建优化模型，将其提交给 Compute Server 集群（通过 Cluster Manager），然后检查模型的状态并检索其解决方案。您可以使用 Batch 对象更轻松地处理批次。有关批次的详细信息，请参阅 批量优化 部分。

## 错误处理

Gurobi Java 库中的所有方法都可以抛出GRBException类型的异常。发生异常时，可以通过检索错误代码（使用方法 GRBException.getErrorCode）或检索异常消息（使用 GRBException.getMessage父类中的方法）来获取有关错误的其他信息。可能的错误返回代码列表可在错误代码部分中找到。

