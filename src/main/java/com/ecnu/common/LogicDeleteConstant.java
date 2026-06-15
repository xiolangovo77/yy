package com.ecnu.common;

/**
 * Global constant for logic delete flag
 * Used by all tables' is_deleted field
 */
public final class LogicDeleteConstant {

    /**
     * Record is not deleted (normal status)
     */
    public static final int NOT_DELETED = 0;

    /**
     * Record is logically deleted
     */
    public static final int DELETED = 1;

    // Private constructor to prevent instantiation
    private LogicDeleteConstant() {}
}