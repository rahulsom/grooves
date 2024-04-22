package grooves.boot.kotlin.domain

// tag::documented[]

class Patient(var uniqueId: String? = null) { // <1>
    var id: String? = null

    override fun toString() = "Patient(uniqueId=$uniqueId, id=$id)"
}
// end::documented[]