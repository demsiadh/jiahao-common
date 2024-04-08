package Lock;

/**
 * <big></big>
 *
 * @author 13684
 * @date 2024/4/8
 */
public class LockApplication {
    public static void main(String[] args) {
        test();
    }

    @MyLock(name = "lock:coupon:#{T(UserContext).getUserContext())}")
    private static void test() {
        System.out.println("测试");
    }
}
