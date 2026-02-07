package com.ehrplatform.audit.dto;

/**
 * Severity levels for audit events.
 */
public final class AuditSeverity {

    private AuditSeverity() {}

    public static final String INFO = "INFO";
    public static final String WARN = "WARN";
    public static final String HIGH = "HIGH";
    public static final String CRITICAL = "CRITICAL";
}
