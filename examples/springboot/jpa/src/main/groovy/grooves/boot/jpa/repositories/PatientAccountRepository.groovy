package grooves.boot.jpa.repositories

import grooves.boot.jpa.domain.PatientAccount
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Jpa backed repository for the PatientAccount Snapshot
 *
 * @author Rahul Somasunderam
 */
interface PatientAccountRepository extends JpaRepository<PatientAccount, Long> {
    List<PatientAccount> findAllByAggregateId(Long aggregateId)

    List<PatientAccount> findAllByAggregateIdAndLastEventPositionLessThan(
            Long aggregateId, Long lastEventPosition)

    List<PatientAccount> findAllByAggregateIdAndLastEventTimestampLessThan(
            Long aggregateId, Date date)
}
