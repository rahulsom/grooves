package grooves.boot.jpa.repositories

import grooves.boot.jpa.domain.PatientHealth
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Jpa backed repository for the PatientHealth Snapshot
 *
 * @author Rahul Somasunderam
 */
interface PatientHealthRepository extends JpaRepository<PatientHealth, Long> {
    List<PatientHealth> findAllByAggregateId(Long aggregateId)

    List<PatientHealth> findAllByAggregateIdAndLastEventPositionLessThan(
    Long aggregateId, Long lastEventPosition)

    List<PatientHealth> findAllByAggregateIdAndLastEventTimestampLessThan(
    Long aggregateId, Date date)
}
