package grooves.boot.jpa.repositories

import grooves.boot.jpa.domain.PatientAccount
import org.springframework.data.jpa.repository.JpaRepository

interface PatientAccountRepository extends JpaRepository<PatientAccount, Long> {
    List<PatientAccount> findAllByAggregateId(Long aggregateId)

    List<PatientAccount> findAllByAggregateIdAndLastEventLessThan(Long aggregateId, Long lastEvent)
}
