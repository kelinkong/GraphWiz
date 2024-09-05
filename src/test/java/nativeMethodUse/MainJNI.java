package nativeMethodUse;

/**
 * @author wendao
 * @since 2024-09-02
 * 使用jni java native interface调用c++， 间接调用qt
 **/
public class MainJNI {
    static {
        System.loadLibrary("MainJNI");
    }

    public native void sayHello();

    public static void main(String[] args) {
        new MainJNI().sayHello();
    }



}
