package com.example.firesystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.firesystem.model.Alert;
import com.example.firesystem.enums.StatusType;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByStatus(StatusType status);

    List<Alert> findBySensorId(Long sensorId);

}