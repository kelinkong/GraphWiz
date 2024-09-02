cmake_minimum_required(VERSION 3.28)
project(GraphWiz)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTORCC ON)
set(CMAKE_AUTOUIC ON)

set(CMAKE_PREFIX_PATH "/opt/homebrew/Cellar/qt@5/5.15.13") # Qt5 安装路径
set(GUROBI_HOME "/Library/gurobi1101/macos_universal2") # Gurobi 安装路径

find_package(Qt5 COMPONENTS
        Core
        Gui
        Widgets
        REQUIRED)

qt5_add_resources(QRC_RESOURCES images/images.qrc)

find_library(GUROBI_CXX_LIBRARY
        NAMES gurobi_c++
        HINTS ${GUROBI_HOME}/lib)

find_library(GUROBI_LIBRARY
        NAMES gurobi110
        HINTS ${GUROBI_HOME}/lib)

add_executable(GraphWiz src/main.cpp
        src/mainwindow.cpp
        src/mainwindow.h
        src/mainwindow.ui
        src/Graph.cpp
        src/Graph.h
        src/CommonFunction.h
        ${QRC_RESOURCES}
        src/Model.cpp
        src/Model.h
        src/LogStream.h
        src/Logger.h
        src/CommonFunction.cpp
        test/GraphTest.cpp
)

include_directories(${GUROBI_HOME}/include)

target_link_libraries(GraphWiz
        Qt5::Core
        Qt5::Gui
        Qt5::Widgets
        ${GUROBI_CXX_LIBRARY}
        ${GUROBI_LIBRARY}
)