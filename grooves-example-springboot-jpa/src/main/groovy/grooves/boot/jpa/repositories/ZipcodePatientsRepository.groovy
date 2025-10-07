package grooves.boot.jpa.repositories

import grooves.boot.jpa.domain.ZipcodePatients
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Jpa backed repository for the PatientHealth Snapshot
 *
 * @author Rahul Somasunderam
 */
interface ZipcodePatientsRepository extends JpaRepository<ZipcodePatients, Long> {
    List<ZipcodePatients> findAllByAggregateId(Long aggregateId)

    List<ZipcodePatients> findAllByAggregateIdAndLastEventPositionLessThan(
    Long aggregateId, Long lastEventPosition)

    List<ZipcodePatients> findAllByAggregateIdAndLastEventTimestampLessThan(
    Long aggregateId, Date date)
}
