package com.deepblue.rescue.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "medical_records")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "condition_status", length = 50)
    private String conditionStatus;

    @Column(name = "injury_description", columnDefinition = "TEXT")
    private String injuryDescription;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @OneToOne(mappedBy = "medicalRecord")
    private Animal animal;

    public MedicalRecord() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }

    public String getConditionStatus() { return conditionStatus; }
    public void setConditionStatus(String conditionStatus) { this.conditionStatus = conditionStatus; }

    public String getInjuryDescription() { return injuryDescription; }
    public void setInjuryDescription(String injuryDescription) { this.injuryDescription = injuryDescription; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public Animal getAnimal() { return animal; }
    public void setAnimal(Animal animal) { this.animal = animal; }
}