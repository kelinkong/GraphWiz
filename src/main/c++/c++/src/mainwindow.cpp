//
// Created by kelin on 24-4-16.
//

// You may need to build the project (run Qt uic code generator) to get "ui_MainWindow.h" resolved

#include "mainwindow.h"

#include <memory>
#include "ui_MainWindow.h"


MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent), ui(new Ui::MainWindow) {
    ui->setupUi(this);
    setWindowTitle("GraphWiz");
    this->setFixedSize(this->size());

    // 选择创建的图类、选择需要求解的问题
    connect(ui->selectGraph, QOverload<int>::of(&QComboBox::activated), this, &MainWindow::setCreateGraphType);
    connect(ui->selectProblem, QOverload<int>::of(&QComboBox::activated), this, &MainWindow::setUseModelType);

    // 上传lp文件和邻接矩阵文件
    connect(ui->uploadAdjFile, &QPushButton::clicked, this, &MainWindow::uploadAdjFileClicked);
    connect(ui->uploadLPFile, &QPushButton::clicked, this, &MainWindow::uploadLPFileClicked);

    // 点击创建图，创建模型
    connect(ui->pushButtonEnterGraph, &QPushButton::clicked, this, &MainWindow::createGraph);
    connect(ui->pushButtonEnterModel, &QPushButton::clicked, this, &MainWindow::createModel);

    // 点击帮助按钮
    connect(ui->helpButton, &QPushButton::clicked, this, &MainWindow::helpButtonClicked);

    // 是否保存LP文件、是否保存邻接矩阵、是否展示图、是否开启日志
    connect(ui->saveLPFile, &QCheckBox::stateChanged, this, &MainWindow::setSaveLPFile);
    connect(ui->saveAdjFile, &QCheckBox::stateChanged, this, &MainWindow::setSaveAdjFile);
    connect(ui->openValValue, &QCheckBox::stateChanged, this, &MainWindow::setOpenSolveLog);
    connect(ui->showGraph, &QCheckBox::stateChanged, this, &MainWindow::setShowGraph);

    // 开始计算
    connect(ui->pushButtonStart, &QPushButton::clicked, this, &MainWindow::startCalulation);

    log_stream = std::make_unique<LogStream>(ui->logBrowser);
    std::cout.rdbuf(log_stream.get());
    std::cerr.rdbuf(log_stream.get());
    displayWelcomeMessage();

    gurobi_callback = std::make_unique<GurobiCallback>(ui->logBrowser);
}

MainWindow::~MainWindow() {
    delete ui;
}

void MainWindow::uploadAdjFileClicked() {
    const QString fileName = QFileDialog::getOpenFileName(this, tr("Open File"), "", tr("csv Files (*.csv)"));
    if(fileName.isEmpty()) {
        QMessageBox::warning(this, "Error", "No file selected.");
        return;
    }
    const QVector<QVector<int>> data = readCSVFile(fileName);
    if(data.empty()) {
        return;
    }

    this->create_graph_type = -1; // 上传和选择图优先上传
    const int n = data.size();
    graph = std::make_shared<CommonGraph>(n);

    for(int i = 0;i < n;i++) {
        for(int j = i + 1;j < n;j++) {
            graph->adj_matrix[i][j] = data[i][j];
            graph->adj_matrix[j][i] = data[i][j];
            if(data[i][j] == 1) {
                graph->adj_list[i].push_back(j);
                graph->adj_list[j].push_back(i);
                graph->edges.push_back({i, j});
            }
        }
        graph->max_degree = std::max(graph->max_degree, graph->adj_list[i].size());
        graph->min_degree = std::min(graph->min_degree, graph->adj_list[i].size());
    }
    graph->m = graph->edges.size();
    if(graph) {
        Logger::getInstance() << "Adjacency matrix uploaded successfully\n";
    }
}

void MainWindow::uploadLPFileClicked() {
    try {
        const QString fileName = QFileDialog::getOpenFileName(this, tr("Open File"), "", tr("lp Files (*.lp)"));
        if(fileName.isEmpty()) {
            throw std::runtime_error("No file selected.");
        }
        this->model = Model::createModelFromLPFile(fileName);
        if(model == nullptr) {
            throw std::runtime_error("Failed to create model from LP file.");
        }
        // 上传LP文件和选择问题只能有一个起作用
        this->use_model_type = -1;
        model->setCallback(gurobi_callback.get());
        Logger::getInstance() << "LPfile upload successfully\n";
        Logger::getInstance() << "Please click the start button to solve the problem\n";
    } catch (const std::exception& e) {
        QMessageBox::warning(this, "Error", e.what());
    }
}

void MainWindow::createGraph() {
    if(create_graph_type == -1) {
        return;
    }
    switch (create_graph_type) {
        case 0:
            graph = std::make_shared<CommonGraph>()->create();
            break;
        case 1:
            graph = std::make_shared<BipartiteGraph>()->create();
            break;
        case 2:
            graph = std::make_shared<GridGraph>()->create();
            break;
        case 3:
            graph = std::make_shared<TreeGraph>()->create();
            break;
        case 4:
            graph = std::make_shared<IntervalGraph>()->create();
            break;
        case 5:
            graph = std::make_shared<BlockGraph>()->create();
            break;
        case 6:
            graph = std::make_shared<CactusGraph>()->create();
            break;
        case 7:
            graph = std::make_shared<BlockCactusGraph>()->create();
            break;
        default:
            return;
    }
    if(graph == nullptr) {
        return;
    }
    Logger::getInstance()<<"----------------------------------------------------\n";
    Logger::getInstance() << "Graph created successfully\n";
    Logger::getInstance() << "Number of vertices: " << graph->n;
    Logger::getInstance() << "Number of egdes: " << graph->m;
    Logger::getInstance()<<"----------------------------------------------------\n";
    Logger::getInstance() << "Please upload the LP file or select model\n";
    Logger::getInstance()<<"----------------------------------------------------\n";
}

void MainWindow::createModel() {
    try {
        if(this->use_model_type == -1) {
            return;
        }
        if(this->graph == nullptr) {
            throw std::runtime_error("The graph has not been created yet.");
        }
        auto m = Model(this->graph.get());
        this->model = m.createLPModel(this->use_model_type);
        if(this->model == nullptr) {
            throw std::runtime_error("Failed to create model.");
        }
        model->setCallback(gurobi_callback.get());
    } catch (const GRBException& e) {
        QMessageBox::warning(this, "Error", "Failed to create model: " + QString(e.getMessage().c_str()));
    } catch (const std::exception& e) {
        QMessageBox::warning(this, "Error", "An error occurred: " + QString(e.what()));
    } catch (...) {
        QMessageBox::warning(this, "Error", "An unknown error occurred.");
    }
    Logger::getInstance() << "Model created successfully\n";
    Logger::getInstance() << "Model name: " << this->model->get(GRB_StringAttr_ModelName);
    Logger::getInstance()<<"----------------------------------------------------\n";
    Logger::getInstance() << "Please click the start button to solve the problem\n";
}

void MainWindow::setCreateGraphType(const int index) {
    this->create_graph_type = index;
}

void MainWindow::setUseModelType(const int index) {
    this->use_model_type = index;
}

void MainWindow::setSaveLPFile(const bool saveLP) {
    this->save_lp_file = saveLP;
}

void MainWindow::setSaveAdjFile(const bool saveAdj) {
    this->save_adj_file = saveAdj;
}

void MainWindow::setOpenSolveLog(const bool openValValue) {
    this->show_val_value = openValValue;
}

void MainWindow::setShowGraph(const bool showGraph) {
    this->show_graph = showGraph;
}

void MainWindow::startCalulation() {
    try {
        if(use_model_type == -1) {
            solveModel();
            return;
        }
        if (this->model == nullptr) {
            throw std::runtime_error("The model has not been created yet.");
        }
        if (this->graph == nullptr) {
            throw std::runtime_error("The graph has not been created yet.");
        }

        // std::filesystem::path currentPath = std::filesystem::current_path();
        // std::cout << "Current path is " << currentPath << std::endl;

        // 加载设置
        if (show_graph) {
            showGraph(this->graph->adj_matrix);
        }
        if(this->save_adj_file) {
            writeAdjMatrixToCSV(graph->adj_matrix);
        }
        if(this->save_lp_file) {
            saveLpFile();
        }

        solveModel();

    } catch (const GRBException& e) {
        QMessageBox::warning(this, "Error", "Failed to start calculation: " + QString(e.getMessage().c_str()));
    } catch (const std::exception& e) {
        QMessageBox::warning(this, "Error", "An error occurred: " + QString(e.what()));
    } catch (...) {
        QMessageBox::warning(this, "Error", "An unknown error occurred.");
    }
}

void MainWindow::helpButtonClicked() {
    QMessageBox::information(this, "Adjacency Matrix Format Help",
                             "The adjacency matrix should be a symmetric matrix with 0s on the diagonal.\n\n"
                             "Example:\n\n"
                             "0 1 0 1\n"
                             "1 0 1 0\n"
                             "0 1 0 1\n"
                             "1 0 1 0\n\n"
                             "In the above matrix, there is an edge between vertex 1 and vertex 2, and between vertex 1 and vertex 4.");
}

void MainWindow::displayWelcomeMessage() {
    const std::string asciiArt = R"(
Welcome to GraphWiz!



             _____                      _      _    _  _
            |  __ \                    | |    | |  | |(_)
            | |  \/ _ __   __ _  _ __  | |__  | |  | | _  ____
            | | __ | '__| / _` || '_ \ | '_ \ | |/\| || ||_  /
            | |_\ \| |   | (_| || |_) || | | |\  /\  /| | / /
             \____/|_|    \__,_|| .__/ |_| |_| \/  \/ |_|/___|
                                | |
                                |_|
)";
    std::cout<<asciiArt<<std::endl;
}

void MainWindow::solveModel() {

    Logger::getInstance()<<"----------------------------------------------------\n";
    Logger::getInstance()<<"          Start to solve the problem\n";
    Logger::getInstance()<<"----------------------------------------------------\n";

    this->model->optimize();

    Logger::getInstance()<<"----------------------------------------------------\n";
    Logger::getInstance()<<"                 Solve finashing\n";
    Logger::getInstance()<<"----------------------------------------------------\n";

    if(show_val_value) {
        Logger::getInstance() << "Vars value:\n";
        Logger::getInstance() <<"----------------------------------------------------\n";
        const int numVars = model->get(GRB_IntAttr_NumVars);
        for (int i = 0; i < numVars; ++i) {
            GRBVar var = model->getVar(i);
            const double var_value = var.get(GRB_DoubleAttr_X);
            QString var_value_str = QString::number(var_value, 'f', 2);
            Logger::getInstance() << var.get(GRB_StringAttr_VarName) << var_value_str.toStdString();
        }
        Logger::getInstance() <<"----------------------------------------------------\n";
    }
    if(graph) {
        Logger::getInstance() << "Type of graph:" << this->graph->graph_type<<"\n";
        Logger::getInstance() << "Number of vertex of graph:" << this->graph->n;
        Logger::getInstance() << "Number of edge of graph:" << this->graph->m;
        Logger::getInstance() <<"----------------------------------------------------\n";
    }
    if(use_model_type != -1) {
        Logger::getInstance() << "Model name:" << this->model->get(GRB_StringAttr_ModelName) << "\n";
        Logger::getInstance() <<"----------------------------------------------------\n";
    }
    const double optimal_objective = this->model->get(GRB_DoubleAttr_ObjVal);
    QString optimal_objective_str = QString::number(optimal_objective, 'f', 4);
    Logger::getInstance() << "Optimal objective: " << optimal_objective_str.toStdString() << "\n";
}

void MainWindow::saveLpFile() {
    QString model_name = QString::fromStdString(this->model->get(GRB_StringAttr_ModelName));
    model_name = model_name.replace(" ", "_");
    QDir dir("../file");
    if (!dir.exists()) {
        dir.mkpath(".");
    }
    const QString file_path = dir.absoluteFilePath(model_name + ".lp");
    this->model->write(file_path.toStdString());
}
