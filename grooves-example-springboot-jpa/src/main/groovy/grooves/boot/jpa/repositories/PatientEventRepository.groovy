package grooves.boot.jpa.repositories

import grooves.boot.jpa.domain.Patient
import grooves.boot.jpa.domain.PatientEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * Jpa backed repository for the PatientEvent event hierarechy
 *
 * @author Rahul Somasunderam
 */
interface PatientEventRepository extends JpaRepository<PatientEvent, Long> {
    long countByAggregateId(Long aggregateId)

    @Query('from PatientEvent e where e.aggregate = ?1 and e.position > ?2 and e.position <= ?3')
    List<PatientEvent> getUncomputedEventsByVersion(
            Patient patient, Long startPosition, Long endPosition)

    List<PatientEvent> findAllByAggregateIn(List<Patient> patients)

    @Query('from PatientEvent e where e.aggregate = ?1 and e.timestamp <= ?2')
    List<PatientEvent> getUncomputedEventsUntilDate(Patient patient, Date date)

    @Query('from PatientEvent e where e.aggregate = ?1 and e.timestamp > ?2 and e.timestamp <= ?3')
    List<PatientEvent> getUncomputedEventsByDateRange(Patient patient, Date fromDate, Date toDate)
}
