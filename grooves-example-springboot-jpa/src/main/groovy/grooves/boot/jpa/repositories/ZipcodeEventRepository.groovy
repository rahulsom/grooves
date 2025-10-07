package grooves.boot.jpa.repositories

import grooves.boot.jpa.domain.Zipcode
import grooves.boot.jpa.domain.ZipcodeEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * Jpa backed repository for the ZipcodeEvent event hierarchy
 *
 * @author Rahul Somasunderam
 */
interface ZipcodeEventRepository extends JpaRepository<ZipcodeEvent, Long> {
    long countByAggregateId(Long aggregateId)

    @Query('from ZipcodeEvent e where e.aggregate = ?1 and e.position > ?2 and e.position <= ?3')
    List<ZipcodeEvent> getUncomputedEventsByVersion(
    Zipcode zipcode, Long startPosition, Long endPosition)

    List<ZipcodeEvent> findAllByAggregateIn(List<Zipcode> patients)

    @Query('from ZipcodeEvent e where e.aggregate = ?1 and e.timestamp <= ?2')
    List<ZipcodeEvent> getUncomputedEventsUntilDate(Zipcode zipcode, Date date)

    @Query('from ZipcodeEvent e where e.aggregate = ?1 and e.timestamp > ?2 and e.timestamp <= ?3')
    List<ZipcodeEvent> getUncomputedEventsByDateRange(Zipcode zipcode, Date fromDate, Date toDate)
}
