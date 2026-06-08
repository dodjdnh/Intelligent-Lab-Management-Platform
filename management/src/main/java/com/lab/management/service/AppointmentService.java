package com.lab.management.service;

import com.lab.management.entity.Appointment;

import java.util.List;
import java.util.Map;

public interface AppointmentService {

    List<Appointment> listActiveAppointments();

    String createAppointment(Map<String, String> params);

    String auditAppointment(Map<String, Object> params);
}
