package com.example.firesystem.model;

import com.example.firesystem.enums.EventType;
import com.example.firesystem.enums.StatusType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "alerts")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Sensor sensor;

    @Enumerated(EnumType.STRING)
    private EventType type; // "accident", "hard_braking", "button"

    private LocalDateTime timestamp;

    private String description;

    @Enumerated(EnumType.STRING)
    private StatusType status; // "new_status, "in_progress", "resolved"

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> photoUrls; // Список URL-адресов фото

    @ManyToOne
    private User assignedTo;
}