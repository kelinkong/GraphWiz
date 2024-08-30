//
// Created by kelin on 24-4-16.
//

#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>
#include <QFileDialog>
#include <QMessageBox>
#include <memory>
#include <QDebug>
#include "Graph.h"
#include "CommonFunction.h"
#include "Model.h"
#include "LogStream.h"
#include "Logger.h"

using GraphSharePtr = std::shared_ptr<Graph>;

QT_BEGIN_NAMESPACE
namespace Ui { class MainWindow; }
QT_END_NAMESPACE

class MainWindow final : public QMainWindow {
Q_OBJECT

public:
    explicit MainWindow(QWidget *parent = nullptr);
    ~MainWindow() override;


public slots:
    // 上传lp文件和邻接矩阵文件
    void uploadAdjFileClicked();
    void uploadLPFileClicked();

    // 选择创建的图类、选择需要求解的问题
    void setCreateGraphType(int index);
    void setUseModelType(int index);

    // 点击创建图、创建模型
    void createGraph();
    void createModel();

    // 点击开始计算
    void startCalulation();

    // 点击帮助按钮
    void helpButtonClicked();

    // 是否保存LP文件、是否保存邻接矩阵、是否展示图、是否开启日志
    void setSaveLPFile(bool saveLP);
    void setSaveAdjFile(bool saveAdj);
    void setOpenSolveLog(bool openLog);
    void setShowGraph(bool showGraph);

    static void displayWelcomeMessage();

private:
    Ui::MainWindow *ui;
    int create_graph_type = 0;
    int use_model_type = 0;
    bool show_val_value = true;
    bool save_lp_file = true;
    bool save_adj_file = true;
    bool show_graph = false;
    GraphSharePtr graph = nullptr;
    ModelSharePtr model = nullptr;
    std::unique_ptr<LogStream> log_stream;
    std::unique_ptr<GurobiCallback> gurobi_callback;
    void solveModel();
    void saveLpFile();
};


#endif //MAINWINDOW_H
