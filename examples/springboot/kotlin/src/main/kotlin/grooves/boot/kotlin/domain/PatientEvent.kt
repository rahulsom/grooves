package grooves.boot.kotlin.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.DeprecatedBy
import com.github.rahulsom.grooves.api.events.Deprecates
import com.github.rahulsom.grooves.api.events.RevertEvent
import grooves.boot.kotlin.BeansHolder
import grooves.boot.kotlin.repositories.PatientEventRepository
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import rx.Observable
import rx.Observable.empty
import rx.Observable.just
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

// tag::patientEvent[]
sealed class PatientEvent : BaseEvent<String, Patient, String, PatientEvent> { // <1><2>

    @Id override val id: String? = null

    var aggregateId: String? = null

    @Transient
    override var revertedBy: RevertEvent<String, Patient, String, PatientEvent>? = null // <3>

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    override var timestamp: Date? = null // <4>
    override var createdBy: String? = null
    override var position: Long? = null // <5>

    fun getType() = this.javaClass.simpleName

    @JsonIgnore
    override fun getAggregateObservable(): Observable<Patient> = // <6>
            aggregate?.let { just(it) } ?: empty()

    override var aggregate: Patient?
        @JsonIgnore
        get() = null
        set(value) {
            aggregateId = value!!.id
        }
    // end::patientEvent[]

    fun getTs() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(timestamp)

    // tag::reverted[]
    data class Reverted(override val revertedEventId: String) : // <1>
            PatientEvent(), // <2>
            RevertEvent<String, Patient, String, PatientEvent> { // <3>
        override fun getAudit(): String = "$id - Revert $revertedEventId"
        // end::reverted[]
        override fun toString() = "[${getTs()}] #$position: ${getAudit()}"
        // tag::reverted[]
    }
    // end::reverted[]

    data class PatientDeprecates(val deprecated: Patient) :
            PatientEvent(), Deprecates<String, Patient, String, PatientEvent> {
        var converseId: String? = null

        @JsonIgnore
        override fun getConverseObservable(): Observable<PatientDeprecatedBy> =
                converseId?.let {
                    val patientEventRepository =
                            BeansHolder.context?.getBean("patientEventRepository") as PatientEventRepository?
                    patientEventRepository
                            ?.findById(it)
                            ?.map { it as PatientDeprecatedBy } ?: empty()
                } ?: empty()

        @JsonIgnore
        override fun getDeprecatedObservable(): Observable<Patient> = just(deprecated)

        override fun getAudit(): String = "$id - Deprecates $deprecated"
        override fun toString() = "[${getTs()}] #$position: ${getAudit()}"
    }

    data class PatientDeprecatedBy(
            val deprecator: Patient, val converseId: String) : PatientEvent(),
            DeprecatedBy<String, Patient, String, PatientEvent> {

        @JsonIgnore
        override fun getConverseObservable(): Observable<PatientDeprecates> {
            val patientEventRepository =
                    BeansHolder.context?.getBean("patientEventRepository") as PatientEventRepository?
            return patientEventRepository
                    ?.findById(converseId)
                    ?.map { it as PatientDeprecates } ?: empty()
        }

        @JsonIgnore
        override fun getDeprecatorObservable(): Observable<Patient> = just(deprecator)

        override fun getAudit(): String = "$id - Deprecated by $deprecator"
        override fun toString() = "[${getTs()}] #$position: ${getAudit()}"
    }

    // tag::created[]
    sealed class Applicable : PatientEvent() { // <1>
        data class Created(val name: String) : Applicable() { // <2>
            override fun getAudit(): String = "$id - Created '$name'" // <3>
            // end::created[]
            override fun toString() = "[${getTs()}] #$position: ${getAudit()}"
            // tag::created[]
        }
        // end::created[]

        data class ProcedurePerformed(val code: String, val cost: BigDecimal) : Applicable() {
            override fun getAudit(): String = "$id - Performed '$code' for $cost"
            override fun toString() = "[${getTs()}] #$position: ${getAudit()}"
        }

        data class PaymentMade(val amount: BigDecimal) : Applicable() {
            override fun getAudit(): String = "$id - Paid $amount"
            override fun toString() = "[${getTs()}] #$position: ${getAudit()}"
        }
        // tag::created[]
    }
    // end::created[]
    // tag::patientEvent[]
}
// end::patientEvent[]
