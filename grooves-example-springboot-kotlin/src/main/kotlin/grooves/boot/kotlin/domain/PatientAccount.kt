package grooves.boot.kotlin.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.rahulsom.grooves.api.snapshots.Snapshot
import grooves.boot.kotlin.repositories.PatientRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Configurable
import reactor.core.publisher.Flux.empty
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.just
import java.math.BigDecimal
import java.util.Date

// tag::documented[]
@Configurable
class PatientAccount : Snapshot<Patient, String, String, PatientEvent> { // <1>

    override var id: String? = null
    override var lastEventPosition: Long = 0 // <2>

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    override var lastEventTimestamp: Date? = null // <3>
    val deprecatesIds: MutableList<String> = mutableListOf()
    private var deprecator: Patient? = null
    var aggregateId: String? = null

    var name: String? = null
    var balance: BigDecimal = BigDecimal.ZERO
    var moneyMade: BigDecimal = BigDecimal.ZERO

    fun getDeprecatedBy() = deprecator

    @JsonIgnore
    override fun getAggregateObservable() =
        // <4>
        aggregateId?.let { patientRepository!!.findAllById(just(it)) } ?: empty()

    override fun setAggregate(aggregate: Patient) {
        this.aggregateId = aggregate.id
    }

    @JsonIgnore
    @Autowired
    var patientRepository: PatientRepository? = null

    @JsonIgnore
    override fun getDeprecatedByObservable() =
        // <5>
        deprecator?.let { just(it) } ?: Mono.empty()

    override fun setDeprecatedBy(deprecatingAggregate: Patient) {
        deprecator = deprecatingAggregate
    }

    @JsonIgnore
    override fun getDeprecatesObservable() =
        // <6>
        if (deprecatesIds.isEmpty()) {
            empty()
        } else {
            patientRepository!!.findAllById(deprecatesIds)
        }

    // end::documented[]
    override fun toString(): String =
        "PatientAccount(id=$id, aggregate=$aggregateId, " +
            "lastEventPosition=$lastEventPosition, lastEventTimestamp=$lastEventTimestamp)"
    // tag::documented[]
}
// end::documented[]