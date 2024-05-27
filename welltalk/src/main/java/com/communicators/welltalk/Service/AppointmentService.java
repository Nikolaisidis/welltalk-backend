package com.communicators.welltalk.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.communicators.welltalk.Repository.AppointmentRepository;
import com.communicators.welltalk.Entity.AppointmentEntity;
import com.communicators.welltalk.Entity.CounselorEntity;
import com.communicators.welltalk.Entity.StudentEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    AppointmentRepository appointmentRepository;

    @Autowired
    StudentService studentService;

    @Autowired
    CounselorService counselorService;

    public AppointmentEntity saveAppointment(int id, AppointmentEntity appointment) {
        if (checkAppointmentIsTaken(appointment.getAppointmentDate(), appointment.getAppointmentStartTime())) {
            throw new IllegalArgumentException("Date and Time is already taken");
        }

        StudentEntity student = studentService.getStudentById(id);

        appointment.setStudent(student);
        appointment.setAppointmentStatus("Pending");

        return appointmentRepository.save(appointment);
    }

    public AppointmentEntity assignCounselor(String counselorEmail, int appointmentId) {
        Optional<AppointmentEntity> appointmentOpt = appointmentRepository
                .findByAppointmentIdAndIsDeletedFalse(appointmentId);
        if (!appointmentOpt.isPresent()) {
            throw new IllegalArgumentException(
                    "Appointment with ID " + appointmentId + " does not exist or is deleted.");
        }

        CounselorEntity counselor = counselorService.getCounselorByEmail(counselorEmail);
        if (counselor == null) {
            throw new IllegalArgumentException("Counselor with email " + counselorEmail + " does not exist.");
        }

        AppointmentEntity appointment = appointmentOpt.get();
        appointment.setCounselor(counselor);
        appointment.setAppointmentStatus("Assigned");
        return appointmentRepository.save(appointment);
    }

    public List<AppointmentEntity> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findByAppointmentDateAndIsDeletedFalse(date);
    }

    public boolean checkAppointmentIsTaken(LocalDate date, String startTime) {
        return appointmentRepository.existsByAppointmentDateAndAppointmentStartTimeAndIsDeletedFalse(date, startTime);
    }

    public List<AppointmentEntity> getAllAppointments() {
        return appointmentRepository.findByIsDeletedFalse();
    }

    public AppointmentEntity getAppointmentByAppointmentId(int id) {
        return appointmentRepository.findByAppointmentIdAndIsDeletedFalse(id).get();
    }

    public List<AppointmentEntity> getAppointmentsByStudent(int studentId) {
        StudentEntity student = studentService.getStudentById(studentId);
        return appointmentRepository.findByStudentAndIsDeletedFalse(student);
    }

    @SuppressWarnings("finally")
    public AppointmentEntity updateAppointment(int id, AppointmentEntity appointment) {
        AppointmentEntity appointmentToUpdate = new AppointmentEntity();
        try {
            appointmentToUpdate = appointmentRepository.findByAppointmentIdAndIsDeletedFalse(id).get();

            appointmentToUpdate.setAppointmentDate(appointment.getAppointmentDate());
            appointmentToUpdate.setAppointmentStartTime(appointment.getAppointmentStartTime());
            appointmentToUpdate.setAppointmentStatus(appointment.getAppointmentStatus());
            appointmentToUpdate.setStudent(appointment.getStudent());
            appointmentToUpdate.setCounselor(appointment.getCounselor());
            appointmentToUpdate.setAppointmentNotes(appointment.getAppointmentNotes());
            appointmentToUpdate.setAppointmentType(appointment.getAppointmentType());
            appointmentToUpdate.setAppointmentPurpose(appointment.getAppointmentPurpose());
        } catch (Exception e) {
            throw new IllegalArgumentException("Appointment " + id + " does not exist.");
        } finally {
            return appointmentRepository.save(appointmentToUpdate);
        }
    }

    public AppointmentEntity updateAppointmentDetails(int id, LocalDate date, String startTime) {
        AppointmentEntity appointmentToUpdate = appointmentRepository.findByAppointmentIdAndIsDeletedFalse(id).get();
        appointmentToUpdate.setAppointmentDate(date);
        appointmentToUpdate.setAppointmentStartTime(startTime);
        return appointmentRepository.save(appointmentToUpdate);
    }

    public AppointmentEntity markAppointmentAsDone(int appointmentId, String notes, String additionalNotes) {
        AppointmentEntity appointment = appointmentRepository.findByAppointmentIdAndIsDeletedFalse(appointmentId).get();
        appointment.setAppointmentStatus("Done");
        appointment.setAppointmentNotes(notes);
        appointment.setAppointmentAdditionalNotes(additionalNotes);
        return appointmentRepository.save(appointment);
    }

    public boolean deleteAppointment(int id) {
        AppointmentEntity appointment = appointmentRepository.findByAppointmentIdAndIsDeletedFalse(id).get();
        if (appointment != null) {
            appointment.setAppointmentStatus("Cancelled");
            appointment.setIsDeleted(true);
            appointmentRepository.save(appointment);
            return true;
        } else {
            System.out.println("Appointment " + id + " does not exist.");
            return false;
        }
    }
}