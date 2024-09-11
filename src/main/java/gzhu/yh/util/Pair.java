package gzhu.yh.util;

/**
 * @author wendao
 * @since 2024-09-02
 * 用于储存一对值的类。
 * 用于储存边的顶点序号。Map类的key唯一无法实现该功能。
 **/
public class Pair <T, U>{
    private final T first;
    private final U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }
}
