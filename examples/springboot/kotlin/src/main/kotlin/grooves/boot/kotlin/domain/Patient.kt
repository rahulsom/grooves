package grooves.boot.kotlin.domain

import com.github.rahulsom.grooves.api.AggregateType

class Patient(var uniqueId: String? = null) : AggregateType<String> {
    override var id: String? = null
    override fun toString() = "Patient(uniqueId=$uniqueId, id=$id)"

}
