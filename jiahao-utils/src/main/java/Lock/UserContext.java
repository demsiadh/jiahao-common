package Lock;

/**
 * <big>用户内容(存入线程内)</big>
 *
 * @author 13684
 * @date 2024/4/8
 */
public class UserContext {

    // 在当前线程中存储用户上下文的ThreadLocal变量
    public static final ThreadLocal<Long> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 获取当前线程的用户上下文。
     *
     * @return 当前线程存储的用户ID，如果未设置则返回null。
     */
    public static Long getUserContext() {
        return THREAD_LOCAL.get();
    }

    /**
     * 设置当前线程的用户上下文。
     *
     * @param userId 要设置的用户ID。
     */
    public static void setUserContext(Long userId) {
        THREAD_LOCAL.set(userId);
    }

    /**
     * 移除当前线程的用户上下文。(本处是为了模拟场景，实际场景中在过滤器或拦截器一定要移除内容防止内存泄漏)
     */
    public static void removeUserContext() {
        THREAD_LOCAL.remove();
    }

}
