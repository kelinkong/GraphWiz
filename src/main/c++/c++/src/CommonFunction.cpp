//
// Created by kelin on 24-4-23.
// 
//

#include "CommonFunction.h"

#include <qdir.h>
#include <QMessageBox>
#include <QDebug>
#include <QRandomGenerator>
#include <QGraphicsView>
#include <QGraphicsScene>
#include <QGraphicsLineItem>
#include <QDesktopWidget>

QVector<QVector<int>> readCSVFile(const QString& fileName) {
    try {
        QFile file(fileName);
        if (!file.open(QIODevice::ReadOnly | QIODevice::Text)) {
            throw std::runtime_error("Failed to open csv file");
        }

        QTextStream in(&file);
        QVector<QVector<int>> data;
        while (!in.atEnd()) {
            QString line = in.readLine();
            QStringList fields = line.split('\t');
            QVector<int> rowData;
            for (const QString& field : fields) {
                int value = field.toInt();
                if(value != 0 && value != 1) {
                    throw std::runtime_error("Invalid value in CSV file");
                }
                rowData.push_back(value);
            }
            data.push_back(rowData);
        }
        if(data.size() != data[0].size()) {
            throw std::runtime_error("Invalid CSV file format");
        }
        file.close();
        return data;
    } catch (const std::exception& e) {
        QMessageBox::warning(nullptr, "Exception caught", e.what());
        return {};
    }
}

void writeAdjMatrixToCSV(const QVector<QVector<int>>& adjMatrix) {
    try {
        QDir dir("../file");
        if (dir.exists()) {
            if (!dir.isReadable()) {
                qDebug() << "The directory exists but is not readable.";
            } else {
                qDebug() << "The directory exists and is readable.";
            }
        } else {
            if (!dir.mkpath(".")) {
                qDebug() << "Failed to create the directory.";
            } else {
                qDebug() << "The directory was created successfully.";
            }
        }
        QString base_name = "graph";
        QString file_path = dir.absoluteFilePath(base_name + ".csv");
        int counter = 1;
        while (QFile::exists(file_path)) {
            file_path = dir.absoluteFilePath(base_name + "_" + QString::number(counter) + ".csv");
            counter++;
        }

        QFile file(file_path);
        if (!file.open(QIODevice::WriteOnly | QIODevice::Text)) {
            throw std::runtime_error("Failed to write adjMatrix to file");
        }

        QTextStream out(&file);
        for (const auto& row : adjMatrix) {
            QStringList rowList;
            for (const auto& cell : row) {
                rowList << QString::number(cell);
            }
            out << rowList.join("\t") << "\n";
        }
        file.close();
    } catch (const std::exception& e) {
        QMessageBox::warning(nullptr, "Exception caught", e.what());
    }
}

void showGraph(const QVector<QVector<int>>& adjMatrix) {
    // Create a QGraphicsView and a QGraphicsScene
    auto *view = new QGraphicsView();
    auto *scene = new QGraphicsScene();

    // Set the scene on the view
    view->setScene(scene);

    // Calculate the radius of the circle on which the vertices will be positioned
    int radius = 200;
    int n = adjMatrix.size();
    QVector<QGraphicsEllipseItem*> vertices;

    // Create the vertices
    for(int i = 0; i < n; i++) {
        double angle = 2 * M_PI * i / n;
        int x = radius * cos(angle);
        int y = radius * sin(angle);
        auto *vertex = new QGraphicsEllipseItem(x, y, 10, 10);
        vertices.append(vertex);
        scene->addItem(vertex);
    }

    // Create the edges
    for(int i = 0; i < n; i++) {
        for(int j = i + 1; j < n; j++) {
            if(adjMatrix[i][j] == 1) {
                auto *edge = new QGraphicsLineItem(vertices[i]->rect().center().x(), vertices[i]->rect().center().y(), vertices[j]->rect().center().x(), vertices[j]->rect().center().y());
                scene->addItem(edge);
            }
        }
    }

    // Resize the view
    view->resize(500, 500); // Set the width to 500 and the height to 500

    // Show the view
    view->show();
}

int weightedRandom(int min, int max) {
    double scaled = static_cast<double>(QRandomGenerator::global()->generate()) / static_cast<double>(QRandomGenerator::max());
    double power = 2.0; // Change this to adjust the weighting
    double weighted = min + (max - min) * std::pow(scaled, power);
    return static_cast<int>(weighted);
}