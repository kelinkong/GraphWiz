// //
// // Created by kelin on 24-4-24.
// //
//
// #include <QInputDialog>
// #include <gtest/gtest.h>
// #include <gmock/gmock.h>
//
// class MockQInputDialog : public QInputDialog {
// public:
//     MOCK_METHOD(int, getInt, (QWidget *parent, const QString &title, const QString &label, int value = 0, int minValue = -2147483647, int maxValue = 2147483647, int step = 1, bool *ok = nullptr, Qt::WindowFlags flags = Qt::WindowFlags()), (override));
// };
//
// TEST(BipartiteGraphTest, CreateGeneratesValidBipartiteGraph) {
//     MockQInputDialog mockInputDialog;
//     EXPECT_CALL(mockInputDialog, getInt(_, _, _, _, _, _, _, _)).WillOnce(Return(10)).WillOnce(Return(5));
//
//     auto graph = std::make_shared<BipartiteGraph>();
//     auto createdGraph = graph->create();
//     ASSERT_NE(createdGraph, nullptr);
//
//     // Check that the graph is a valid bipartite graph
//     for (int i = 0; i < graph->partions_X.size(); ++i) {
//         for (int j = i + 1; j < graph->partions_X.size(); ++j) {
//             EXPECT_EQ(graph->adj_matrix[graph->partions_X[i]][graph->partions_X[j]], 0);
//         }
//     }
//     for (int i = 0; i < graph->partions_Y.size(); ++i) {
//         for (int j = i + 1; j < graph->partions_Y.size(); ++j) {
//             EXPECT_EQ(graph->adj_matrix[graph->partions_Y[i]][graph->partions_Y[j]], 0);
//         }
//     }
// }