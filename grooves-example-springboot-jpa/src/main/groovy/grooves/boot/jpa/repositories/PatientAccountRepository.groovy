package grooves.boot.jpa.repositories

import grooves.boot.jpa.domain.PatientAccount
import org.springframework.data.jpa.repository.JpaRepository

interface PatientAccountRepository extends JpaRepository<PatientAccount, Long> {
    List<PatientAccount> findAllByAggregateId(Long aggregateId)

    List<PatientAccount> findAllByAggregateIdAndLastEventPositionLessThan(Long aggregateId, Long lastEventPosition)

    List<PatientAccount> findAllByAggregateIdAndLastEventTimestampLessThan(Long aggregateId, Date date)
}
