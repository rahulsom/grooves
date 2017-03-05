package grooves.boot.jpa.repositories

import grooves.boot.jpa.domain.Patient
import grooves.boot.jpa.domain.PatientEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PatientEventRepository extends JpaRepository<PatientEvent, Long> {
    long countByAggregateId(Long aggregateId)

    @Query("from PatientEvent e where e.aggregate = ?1 and e.position > ?2 and e.position <= ?3")
    List<PatientEvent> getUncomputedEvents(Patient patient, Long startPosition, Long endPosition)

    List<PatientEvent> findAllByAggregateIn(List<Patient> patients)
}
