package gzhu.yh.util;

/**
 * @author wendao
 * @since 2024-09-03
 * 用于判断一个数是否符合1至1000 因为这个区间的内的点数，gurobi运算的比较块
 **/
public class IsNumProper {
    public static Boolean isNumProper(int num){
        Boolean isProper = true;
        if (num >= 1 && num <= 1000){
            return false;
        }
        return isProper;
    }
}
