package com.ehrplatform.audit.dto;

/**
 * Common event types for the audit system.
 * Use these constants when producing audit events.
 */
public final class AuditEventTypes {

    private AuditEventTypes() {}

    // Authentication events
    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILURE = "LOGIN_FAILURE";
    public static final String LOGOUT = "LOGOUT";
    public static final String TOKEN_REFRESH = "TOKEN_REFRESH";
    public static final String PASSWORD_CHANGED = "PASSWORD_CHANGED";
    public static final String PASSWORD_RESET_REQUESTED = "PASSWORD_RESET_REQUESTED";
    public static final String MFA_ENABLED = "MFA_ENABLED";
    public static final String MFA_DISABLED = "MFA_DISABLED";

    // Record access events
    public static final String RECORD_VIEWED = "RECORD_VIEWED";
    public static final String RECORD_CREATED = "RECORD_CREATED";
    public static final String RECORD_UPDATED = "RECORD_UPDATED";
    public static final String RECORD_DELETED = "RECORD_DELETED";
    public static final String PATIENT_ACCESSED = "PATIENT_ACCESSED";
    public static final String EXPORT_REQUESTED = "EXPORT_REQUESTED";

    // Admin events
    public static final String ROLE_CHANGED = "ROLE_CHANGED";
    public static final String PERMISSION_CHANGED = "PERMISSION_CHANGED";
    public static final String USER_CREATED = "USER_CREATED";
    public static final String USER_DELETED = "USER_DELETED";
    public static final String ADMIN_CREATED = "ADMIN_CREATED";
    public static final String ADMIN_DELETED = "ADMIN_DELETED";
    public static final String RETENTION_CHANGED = "RETENTION_CHANGED";

    // Access control events
    public static final String ACCESS_DENIED = "ACCESS_DENIED";
    public static final String ACCESS_GRANTED = "ACCESS_GRANTED";
}
