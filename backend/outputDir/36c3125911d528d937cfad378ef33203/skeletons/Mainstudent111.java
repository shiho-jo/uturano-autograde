package student111;


import java.util.ArrayList;

public class Mainstudent111 {
    // 正确实现的方法
    public static int add(int a, int b) {
        if (a < 0 || b < 0) {
            return 0; // 防止负数相加
        }
        return a + b;
    }

    // 错误 2: 方法签名被修改
    // 原骨架: public static int divide(int a, int b)
    // 学生代码: 返回类型从 int 修改为 double
    public static double divide(int a, int b) {
        if (b == 0) {
            throw new IllegalArgumentException("Division by zero"); // 正确处理分母为 0
        }
        return (double) a / b;
    }

    // 正确实现的方法
    public static boolean isEven(int a) {
        return a % 2 == 0;
    }

    // 正确实现的方法
    public static int multiply(int a, int b) {
        return a * b;
    }

    // 正确实现的方法
    public static int subtract(int a, int b) {
        return a - b;
    }

    // 正确实现的方法
    public static int square(int a) {
        return a * a;
    }
}
