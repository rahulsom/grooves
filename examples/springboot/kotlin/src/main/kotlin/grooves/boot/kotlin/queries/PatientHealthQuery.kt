package grooves.boot.kotlin.queries

import com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE
import com.github.rahulsom.grooves.queries.QuerySupport
import com.github.rahulsom.grooves.queries.internal.SimpleExecutor
import com.github.rahulsom.grooves.queries.internal.SimpleQuery
import grooves.boot.kotlin.domain.Patient
import grooves.boot.kotlin.domain.PatientHealth
import grooves.boot.kotlin.domain.PatientEvent
import grooves.boot.kotlin.domain.Procedure
import grooves.boot.kotlin.repositories.PatientHealthRepository
import grooves.boot.kotlin.repositories.PatientEventRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import rx.Observable
import rx.Observable.just
import java.lang.Exception
import java.util.*

@Component
class PatientHealthQuery :
        QuerySupport<String, Patient, String, PatientEvent, String, PatientHealth,
                PatientHealthQuery>,
        SimpleQuery<String, Patient, String, PatientEvent, PatientEvent.Applicable, String,
                PatientHealth, PatientHealthQuery> {

    override fun getExecutor() = SimpleExecutor<String, Patient, String, PatientEvent,
            PatientEvent.Applicable, String, PatientHealth, PatientHealthQuery>()

    @Autowired lateinit var patientEventRepository: PatientEventRepository
    @Autowired lateinit var PatientHealthRepository: PatientHealthRepository

    override fun createEmptySnapshot() = PatientHealth()

    override fun getSnapshot(maxPosition: Long, aggregate: Patient): Observable<PatientHealth> =
            PatientHealthRepository.findByAggregateIdAndLastEventPositionLessThan(
                    aggregate.id!!, maxPosition)

    override fun getSnapshot(maxTimestamp: Date, aggregate: Patient): Observable<PatientHealth> =
            PatientHealthRepository.findByAggregateIdAndLastEventTimestampLessThan(
                    aggregate.id!!, maxTimestamp)

    override fun shouldEventsBeApplied(snapshot: PatientHealth?) = true

    override fun addToDeprecates(snapshot: PatientHealth, deprecatedAggregate: Patient) {
        snapshot.deprecatesIds.add(deprecatedAggregate.id!!)
    }

    override fun onException(e: Exception, snapshot: PatientHealth, event: PatientEvent) =
            just(CONTINUE)

    override fun getUncomputedEvents(
            aggregate: Patient, lastSnapshot: PatientHealth?, version: Long) =
            patientEventRepository.
                    findAllByPositionRange(
                            aggregate.id!!, lastSnapshot?.lastEventPosition ?: 0, version)

    override fun getUncomputedEvents(
            aggregate: Patient, lastSnapshot: PatientHealth?, snapshotTime: Date) =
            lastSnapshot?.lastEventTimestamp?.
                    let {
                        patientEventRepository.
                                findAllByTimestampRange(
                                        aggregate.id!!, it, snapshotTime)
                    } ?:
                    patientEventRepository.
                            findAllByAggregateIdAndTimestampLessThan(aggregate.id!!, snapshotTime)

    override fun applyEvent(event: PatientEvent.Applicable, snapshot: PatientHealth) =
            when (event) {
                is PatientEvent.Applicable.Created -> {
                    if (event.aggregateId == snapshot.aggregateId) {
                        snapshot.name = event.name
                    }
                    just(CONTINUE)
                }
                is PatientEvent.Applicable.ProcedurePerformed -> {
                    snapshot.procedures.add(Procedure(event.code, event.timestamp!!))
                    just(CONTINUE)
                }
                is PatientEvent.Applicable.PaymentMade -> {
                    just(CONTINUE)
                }
            }
}