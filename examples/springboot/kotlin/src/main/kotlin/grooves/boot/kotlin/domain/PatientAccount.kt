package grooves.boot.kotlin.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.rahulsom.grooves.api.snapshots.Snapshot
import grooves.boot.kotlin.repositories.PatientRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Configurable
import rx.Observable.empty
import rx.Observable.just
import java.math.BigDecimal
import java.util.*

@Configurable
class PatientAccount : Snapshot<String, Patient, String, String, PatientEvent> {
    override var id: String? = null
    override var lastEventPosition: Long? = null

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    override var lastEventTimestamp: Date? = null
    val deprecatesIds: MutableList<String> = mutableListOf()
    private var deprecator: Patient? = null
    var aggregateId: String? = null

    var name: String? = null
    var balance: BigDecimal = BigDecimal.ZERO
    var moneyMade: BigDecimal = BigDecimal.ZERO

    fun getDeprecatedBy() = deprecator

    @JsonIgnore
    override fun getAggregateObservable() =
            aggregateId?.let { patientRepository!!.findAllById(just(it)) } ?: empty()

    override fun setAggregate(aggregate: Patient) {
        this.aggregateId = aggregate.id
    }

    @JsonIgnore
    @Autowired
    var patientRepository: PatientRepository? = null

    @JsonIgnore
    override fun getDeprecatedByObservable() =
            deprecator?.let { just(it) } ?: empty()

    override fun setDeprecatedBy(deprecatingAggregate: Patient) {
        deprecator = deprecatingAggregate
    }

    @JsonIgnore
    override fun getDeprecatesObservable() =
            if (deprecatesIds.size > 0)
                patientRepository!!.findAllById(deprecatesIds)
            else
                empty()

    override fun toString(): String {
        return "PatientAccount(id=$id, aggregate=$aggregateId, " +
                "lastEventPosition=$lastEventPosition, lastEventTimestamp=$lastEventTimestamp)"
    }

}