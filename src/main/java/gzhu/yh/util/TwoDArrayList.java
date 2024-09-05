package gzhu.yh.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wendao
 * @since 2024-09-03
 * 创建二维ArrayList，并初始化
 **/
public class TwoDArrayList {
    /**
     * 返回一个row*col的二维ArrayList，初始值为initValue
     * 注意是先构建行后构建列
     * @param row 行数
     * @param col 列数
     * @param initValue 初始值
     * @return java.util.ArrayList<java.util.ArrayList<T>>
     * @author Administrator
     * @date 2024/9/3 0003 17:17
    */
    public static <T> ArrayList<List<T>> createTwoDArrayList(int row, int col, T initValue) {
        //初始化容量
        ArrayList<List<T>> twoDArrayList = new ArrayList<>(row);
        for (int i = 0; i < col; i++) {
            //初始化容量
            ArrayList<T> colArrayList = new ArrayList<>(col);
            twoDArrayList.add(colArrayList);
            for (int j = 0; j < row; j++) {
                colArrayList.add(initValue);
            }
        }
        return twoDArrayList;
    }
    /**
     * 返回一个row*默认列数的二维ArrayList
     * 注意是先构建行后构建列
     * @param col 行元素个数
     * @return java.util.ArrayList<java.util.ArrayList<T>>
     * @author Administrator
     * @date 2024/9/3 0003 17:17
    */
    public static <T> ArrayList<List<T>> createTwoDArrayList(int col) {
        //初始化容量
        ArrayList<List<T>> twoDArrayList = new ArrayList<>(col);
        for (int i = 0; i < col; i++) {
            //列为默认大小
            ArrayList<T> colArrayList = new ArrayList<>();
            twoDArrayList.add(colArrayList);
        }
        return twoDArrayList;
    }
    /**
     * 将一个List<List<T>>转换为一个T[][]数组
     * @param arrayList2D List<List<T>>类型
     * @return T[][]
     * @author Administrator
     * @date 2024/9/5 0005 20:40
    */
    public static <T> T[][] toArray2D(List<ArrayList<T>> arrayList2D) {
        T[][] array2D = (T[][]) new Object[arrayList2D.get(0).size()][arrayList2D.size()];

        for (int i = 0; i < arrayList2D.get(0).size(); i++) {
            for (int j = 0; j < arrayList2D.size(); j++) {
                array2D[i][j] = (T) arrayList2D.get(i).get(j);
            }
        }
        return array2D;
    }


}