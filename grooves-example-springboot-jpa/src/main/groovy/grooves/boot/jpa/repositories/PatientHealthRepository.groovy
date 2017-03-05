package grooves.boot.jpa.repositories

import grooves.boot.jpa.domain.PatientHealth
import org.springframework.data.jpa.repository.JpaRepository

interface PatientHealthRepository extends JpaRepository<PatientHealth, Long> {
    List<PatientHealth> findAllByAggregateId(Long aggregateId)

    List<PatientHealth> findAllByAggregateIdAndLastEventLessThan(Long aggregateId, Long lastEvent)
}