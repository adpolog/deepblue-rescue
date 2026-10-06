package com.deepblue.rescue.repository;

import com.deepblue.rescue.domain.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, Long> {


    List<Treatment> findByAnimalIdOrderByPerformedAtAsc(Long animalId);


    @Query("""
        SELECT t
        FROM Treatment t
        WHERE t.performedAt BETWEEN :start AND :end
        ORDER BY t.performedAt ASC
        """)
    List<Treatment> findTreatmentsBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    @Query("""
        SELECT t
        FROM Treatment t
        JOIN t.animal a
        JOIN a.rescueCase rc
        JOIN rc.rescueCenter rcent
        WHERE rcent.code = :centerCode
        """)
    List<Treatment> findByRescueCenterCode(@Param("centerCode") String centerCode);


    @Query("""
        SELECT DISTINCT t
        FROM Treatment t
        JOIN t.specialist s
        JOIN s.expertiseAreas e
        WHERE LOWER(e.name) = LOWER(:expertiseName)
        """)
    List<Treatment> findBySpecialistExpertiseName(@Param("expertiseName") String expertiseName);

    List<Treatment>
    findByAnimalAnimalCodeOrderByPerformedAtAsc(
            String animalCode);

}