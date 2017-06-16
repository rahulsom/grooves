package grooves.boot.kotlin.repositories

import grooves.boot.kotlin.domain.Patient
import grooves.boot.kotlin.domain.PatientAccount
import grooves.boot.kotlin.domain.PatientEvent
import grooves.boot.kotlin.domain.PatientHealth
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.reactive.RxJava1CrudRepository
import rx.Observable
import java.util.*

interface PatientBlockingRepository : CrudRepository<Patient, String>
interface PatientEventBlockingRepository : CrudRepository<PatientEvent, String> {
    fun findAllByAggregateIdIn(aggregateIds: List<String>): List<PatientEvent>
}

interface PatientRepository : RxJava1CrudRepository<Patient, String> {
    fun findAllByOrderByUniqueIdAsc(): Observable<Patient>
}

interface PatientEventRepository : RxJava1CrudRepository<PatientEvent, String> {

    fun findAllByAggregateIdIn(aggregateIds: List<String>): Observable<PatientEvent>

    @Query("{'aggregateId':?0, 'position':{'\$gt':?1, '\$lte':?2}}")
    fun findAllByPositionRange(
            aggregateId: String,
            lowerBoundExclusive: Long,
            upperBoundInclusive: Long): Observable<PatientEvent>

    @Query("{'aggregateId':?0, 'timestamp':{'\$gt':?1, '\$lte':?2}}")
    fun findAllByTimestampRange(
            aggregateId: String,
            lowerBoundExclusive: Date,
            upperBoundInclusive: Date): Observable<PatientEvent>

    fun findAllByAggregateIdAndTimestampLessThan(
            aggregateId: String, snapshotTime: Date): Observable<PatientEvent>

}

interface PatientAccountRepository : RxJava1CrudRepository<PatientAccount, String> {

    fun findByAggregateIdAndLastEventPositionLessThan(
            aggregateId: String, maxPosition: Long): Observable<PatientAccount>

    fun findByAggregateIdAndLastEventTimestampLessThan(
            aggregateId: String, maxTimestamp: Date): Observable<PatientAccount>

}

interface PatientHealthRepository : RxJava1CrudRepository<PatientHealth, String> {

    fun findByAggregateIdAndLastEventPositionLessThan(
            aggregateId: String, maxPosition: Long): Observable<PatientHealth>

    fun findByAggregateIdAndLastEventTimestampLessThan(
            aggregateId: String, maxTimestamp: Date): Observable<PatientHealth>

}
