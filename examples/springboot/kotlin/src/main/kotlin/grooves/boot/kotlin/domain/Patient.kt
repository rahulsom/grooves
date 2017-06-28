package grooves.boot.kotlin.domain

// tag::documented[]
import com.github.rahulsom.grooves.api.AggregateType

class Patient(var uniqueId: String? = null) : AggregateType<String> { // <1>
    override var id: String? = null // <2>
    override fun toString() = "Patient(uniqueId=$uniqueId, id=$id)"
}
// end::documented[]
