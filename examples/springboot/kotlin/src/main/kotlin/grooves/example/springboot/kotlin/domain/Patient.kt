package grooves.example.springboot.kotlin.domain

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.api.snapshots.Snapshot
import rx.Observable
import rx.Observable.*
import java.math.BigDecimal
import java.util.*

class Patient(val uniqueId: String) : AggregateType<String> {
    override var id: String? = null
}

sealed class PatientEvent : BaseEvent<String, Patient, String, PatientEvent> {

    override var aggregate: Patient? = null
    override var timestamp: Date? = null
    override var createdBy: String? = null
    override var position: Long? = null
    override var id: String? = null

    override fun getAggregateObservable(): Observable<Patient> =
            if (aggregate != null) just(aggregate!!) else empty()

    override var revertedBy: RevertEvent<String, Patient, String, PatientEvent>? = null

    sealed class Applicable {
        data class PatientCreated(val name: String) : PatientEvent() {
            override fun getAudit(): String = "name: $name"
        }

        data class ProcedurePerformed(
                val code: String, val cost: BigDecimal) : PatientEvent() {
            override fun getAudit(): String = "code: $code, cost: $cost"
        }

        data class PaymentMade(val amount: BigDecimal) : PatientEvent() {
            override fun getAudit(): String = "amount: $amount"
        }
    }

    data class PatientEventReverted(override val revertedEventId: String) :
            PatientEvent(), RevertEvent<String, Patient, String, PatientEvent> {
        override fun getAudit(): String = "revertedEvent: $revertedEventId"
    }

    data class PatientDeprecatedBy(val deprecator: Patient, val converse: PatientDeprecates) :
            PatientEvent() {

        override fun getConverseObservable() = Observable.just(converse)
        override fun getDeprecatorObservable() = Observable.just(deprecator)

        override fun getAudit(): String = "deprecatedBy: $deprecator"
    }

    data class PatientDeprecates(val deprecated: Patient) :
            PatientEvent() {

        var converse: PatientDeprecatedBy? = null

        override fun getConverseObservable() =
                if (converse != null) Observable.just(converse!!) else empty()

        override fun getDeprecatedObservable() = Observable.just(deprecated)

        override fun getAudit(): String = "deprecates: $deprecated"
    }

}

class PatientHealth : Snapshot<String, Patient, String, String, PatientEvent> {
    override var id: String? = null
    override var lastEventPosition: Long? = null
    override var lastEventTimestamp: Date? = null
    var aggregateId: Patient? = null
    var deprecatedById: Patient? = null
    var deprecates: List<Patient> = listOf()

    override fun setAggregate(aggregate: Patient) {
        aggregateId = aggregate
    }

    override fun setDeprecatedBy(deprecatingAggregate: Patient) {
        deprecatedById = deprecatingAggregate
    }

    override fun getAggregateObservable() =
            if (aggregateId == null) empty() else just(aggregateId!!)

    override fun getDeprecatedByObservable() =
            if (deprecatedById == null) empty() else just(deprecatedById!!)

    override fun getDeprecatesObservable() = from(deprecates)
}