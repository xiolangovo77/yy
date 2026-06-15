package com.ecnu.common;

/**
 * Constant for user account status
 * Used by users.status field
 */
public final class StatusConstant {

    /**
     * Account is enabled and can login
     */
    public static final int ENABLED = 1;

    /**
     * Account is disabled and cannot login
     */
    public static final int DISABLED = 0;

    // Private constructor to prevent instantiation
    private StatusConstant() {}
}