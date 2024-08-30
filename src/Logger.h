//
// Created by kelin on 24-4-23.
//

#ifndef LOGGER_H
#define LOGGER_H

#include <fstream>
#include <iostream>

class Logger {
public:
    static Logger& getInstance() {
        static Logger instance;
        return instance;
    }

    template<typename T>
    Logger& operator<<(const T& msg) {
        try {
            if (!file.is_open()) {
                throw std::runtime_error("Failed to open log file");
            }
            file << msg;
            std::cout << msg;  // Also echo to console
        } catch (const std::exception& e) {
            std::cerr << "Caught exception: " << e.what() << std::endl;
        }
        return *this;
    }

    // 删除复制构造函数和赋值操作符
    Logger(const Logger&) = delete;
    void operator=(const Logger&) = delete;

private:
    std::ofstream file;

    // 私有构造函数和析构函数
    Logger() {
        file.open("../file/log.txt", std::ios::app);
        if (!file.is_open()) {
            std::cerr << "Failed to open log file\n";
        }
    }

    ~Logger() {
        if (file.is_open()) {
            file.close();
        }
    }
};


#endif //LOGGER_H
