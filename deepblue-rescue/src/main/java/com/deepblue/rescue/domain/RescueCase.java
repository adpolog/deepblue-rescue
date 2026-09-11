package com.deepblue.rescue.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "rescue_cases")
public class RescueCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_code", unique = true, nullable = false)
    private String caseCode;

    @Column(name = "rescue_date")
    private LocalDate rescueDate;

    @Column(name = "rescue_location")
    private String rescueLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RescueStatus status;

    // Paso 20: Relación N:1 hacia RescueCenter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rescue_center_id", nullable = false)
    private RescueCenter rescueCenter;

    // Paso 21: Relación 1:1 con Animal
    @OneToOne(
            mappedBy = "rescueCase",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Animal animal;

    public void assignAnimal(Animal animal) {
        this.animal = animal;
        animal.setRescueCase(this);
    }

    // Getters y Setters manuales
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCaseCode() { return caseCode; }
    public void setCaseCode(String caseCode) { this.caseCode = caseCode; }
    public LocalDate getRescueDate() { return rescueDate; }
    public void setRescueDate(LocalDate rescueDate) { this.rescueDate = rescueDate; }
    public String getRescueLocation() { return rescueLocation; }
    public void setRescueLocation(String rescueLocation) { this.rescueLocation = rescueLocation; }
    public RescueStatus getStatus() { return status; }
    public void setStatus(RescueStatus status) { this.status = status; }
    public RescueCenter getRescueCenter() { return rescueCenter; }
    public void setRescueCenter(RescueCenter rescueCenter) { this.rescueCenter = rescueCenter; }
    public Animal getAnimal() { return animal; }
    public void setAnimal(Animal animal) { this.animal = animal; }
}