package grooves.boot.kotlin.repositories

import grooves.boot.kotlin.domain.Patient
import grooves.boot.kotlin.domain.PatientAccount
import grooves.boot.kotlin.domain.PatientEvent
import grooves.boot.kotlin.domain.PatientHealth
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.Date

interface PatientBlockingRepository : CrudRepository<Patient, String>
interface PatientEventBlockingRepository : CrudRepository<PatientEvent, String> {
    fun findAllByAggregateIdIn(aggregateIds: List<String>): List<PatientEvent>
}

interface PatientRepository : ReactiveCrudRepository<Patient, String> {
    fun findAllByOrderByUniqueIdAsc(): Flux<Patient>
}

interface PatientEventRepository : ReactiveCrudRepository<PatientEvent, String> {

    fun findAllByAggregateIdIn(aggregateIds: List<String>): Flux<PatientEvent>

    @Query("{'aggregateId':?0, 'position':{'\$gt':?1, '\$lte':?2}}")
    fun findAllByPositionRange(
        aggregateId: String,
        lowerBoundExclusive: Long,
        upperBoundInclusive: Long
    ): Flux<PatientEvent>

    @Query("{'aggregateId':?0, 'timestamp':{'\$gt':?1, '\$lte':?2}}")
    fun findAllByTimestampRange(
        aggregateId: String,
        lowerBoundExclusive: Date,
        upperBoundInclusive: Date
    ): Flux<PatientEvent>

    fun findAllByAggregateIdAndTimestampLessThan(
        aggregateId: String, snapshotTime: Date
    ): Flux<PatientEvent>

}

interface PatientAccountRepository : ReactiveCrudRepository<PatientAccount, String> {

    fun findByAggregateIdAndLastEventPositionLessThan(
        aggregateId: String, maxPosition: Long
    ): Mono<PatientAccount>

    fun findByAggregateIdAndLastEventTimestampLessThan(
        aggregateId: String, maxTimestamp: Date
    ): Mono<PatientAccount>

}

interface PatientHealthRepository : ReactiveCrudRepository<PatientHealth, String> {

    fun findByAggregateIdAndLastEventPositionLessThan(
        aggregateId: String, maxPosition: Long
    ): Mono<PatientHealth>

    fun findByAggregateIdAndLastEventTimestampLessThan(
        aggregateId: String, maxTimestamp: Date
    ): Mono<PatientHealth>

}
