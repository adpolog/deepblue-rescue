package com.deepblue.rescue.repository;
import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.RescueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

    Optional<Animal> findByAnimalCode(String animalCode);

    List<Animal> findByRescueCaseRescueCenterCode(String centerCode);

    @Query("SELECT a FROM Animal a WHERE a.rescueCase.status = :status")
    List<Animal> findByRescueStatus(@Param("status") RescueStatus status);
}