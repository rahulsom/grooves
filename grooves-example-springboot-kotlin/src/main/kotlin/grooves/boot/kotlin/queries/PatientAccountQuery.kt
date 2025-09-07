package grooves.boot.kotlin.queries

import com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE
import com.github.rahulsom.grooves.queries.QuerySupport
import com.github.rahulsom.grooves.queries.internal.SimpleExecutor
import com.github.rahulsom.grooves.queries.internal.SimpleQuery
import grooves.boot.kotlin.domain.Patient
import grooves.boot.kotlin.domain.PatientAccount
import grooves.boot.kotlin.domain.PatientEvent
import grooves.boot.kotlin.repositories.PatientAccountRepository
import grooves.boot.kotlin.repositories.PatientEventRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono.just
import java.lang.Exception
import java.util.Date

// tag::documented[]
@Component
class PatientAccountQuery constructor(
    @Autowired val patientEventRepository: PatientEventRepository,
    @Autowired val patientAccountRepository: PatientAccountRepository,
) : QuerySupport<Patient, String, PatientEvent, String, PatientAccount>,
    // <1>
    SimpleQuery<
        Patient,
        String,
        PatientEvent,
        PatientEvent.Applicable,
        String,
        PatientAccount,
    > { // <2>

    override fun getExecutor() =
        SimpleExecutor<
            Patient,
            String,
            PatientEvent,
            PatientEvent.Applicable,
            String,
            PatientAccount,
            PatientAccountQuery,
        >() // <3>

    override fun createEmptySnapshot() = PatientAccount() // <4>

    override fun getSnapshot(
        maxPosition: Long,
        aggregate: Patient,
    ) = // <5>
        patientAccountRepository.findByAggregateIdAndLastEventPositionLessThan(
            aggregate.id!!,
            maxPosition,
        )

    override fun getSnapshot(
        maxTimestamp: Date?,
        aggregate: Patient,
    ) = // <6>
        patientAccountRepository.findByAggregateIdAndLastEventTimestampLessThan(
            aggregate.id!!,
            maxTimestamp!!,
        )

    override fun shouldEventsBeApplied(snapshot: PatientAccount) = true // <7>

    override fun addToDeprecates(
        snapshot: PatientAccount,
        deprecatedAggregate: Patient,
    ) {
        snapshot.deprecatesIds.add(deprecatedAggregate.id!!)
    }

    override fun onException(
        e: Exception,
        snapshot: PatientAccount,
        event: PatientEvent,
    ) = // <8>
        just(CONTINUE)

    override fun getUncomputedEvents(
        aggregate: Patient,
        lastSnapshot: PatientAccount?,
        version: Long,
    ) = // <9>
        patientEventRepository.findAllByPositionRange(
            aggregate.id!!,
            lastSnapshot?.lastEventPosition ?: 0,
            version,
        )

    override fun getUncomputedEvents(
        aggregate: Patient,
        lastSnapshot: PatientAccount?,
        snapshotTime: Date,
    ) = // <10>
        lastSnapshot?.lastEventTimestamp?.let {
            patientEventRepository.findAllByTimestampRange(
                aggregate.id!!,
                it,
                snapshotTime,
            )
        } ?: patientEventRepository.findAllByAggregateIdAndTimestampLessThan(
            aggregate.id!!,
            snapshotTime,
        )

    override fun applyEvent(
        event: PatientEvent.Applicable,
        snapshot: PatientAccount,
    ) = when (event) { // <11>
        is PatientEvent.Applicable.Created -> {
            if (event.aggregateId == snapshot.aggregateId) {
                snapshot.name = event.name
            }
            just(CONTINUE)
        }
        is PatientEvent.Applicable.ProcedurePerformed -> {
            snapshot.balance = snapshot.balance.add(event.cost)
            just(CONTINUE)
        }
        is PatientEvent.Applicable.PaymentMade -> {
            snapshot.balance = snapshot.balance.subtract(event.amount)
            snapshot.moneyMade = snapshot.moneyMade.add(event.amount)
            just(CONTINUE)
        }
    }
}
// end::documented[]