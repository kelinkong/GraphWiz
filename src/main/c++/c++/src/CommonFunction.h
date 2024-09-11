//
// Created by kelin on 24-4-16.
//

#ifndef COMMONFUNCTION_H
#define COMMONFUNCTION_H

#include <QFile>
#include <QTextStream>
#include <QStringList>
#include <QVector>
#include <exception>
#include <iostream>

QVector<QVector<int>> readCSVFile(const QString& fileName);

void writeAdjMatrixToCSV(const QVector<QVector<int>>& adjMatrix);

void showGraph(const QVector<QVector<int>>& adjMatrix);

int weightedRandom(int min, int max);

#endif //COMMONFUNCTION_H
