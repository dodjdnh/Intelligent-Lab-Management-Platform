package com.lab.management.common;

public final class RealtimeEventType {

    public static final String INVENTORY_CHANGED = "inventory.changed";
    public static final String APPOINTMENT_AUDITED = "appointment.audited";
    public static final String ALERT_CREATED = "alert.created";
    public static final String DEVICE_STATUS_CHANGED = "device.status.changed";

    private RealtimeEventType() {
    }
}
