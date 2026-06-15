package com.ecnu.context;

public class RoleContext {

    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentRoleId(Long roleId) {
        threadLocal.set(roleId);
    }

    public static Long getCurrentRoleId() {
        return threadLocal.get();
    }

    public static void removeCurrentRoleId() {
        threadLocal.remove();
    }

}
