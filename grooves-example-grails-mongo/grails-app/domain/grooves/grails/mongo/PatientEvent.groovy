package grooves.grails.mongo

import com.github.rahulsom.grooves.api.events.*
import com.github.rahulsom.grooves.transformations.Event
import groovy.json.JsonBuilder

abstract class PatientEvent implements BaseEvent<Patient, Long, PatientEvent> {

    RevertEvent<Patient, Long, PatientEvent> revertedBy
    String createdBy
    Date timestamp
    Long position
    Patient aggregate

    static transients = ['revertedBy']

    static constraints = {
    }
}

@Event(Patient)
class PatientCreated extends PatientEvent {
    String name

    @Override String getAudit() { new JsonBuilder([name: name]).toString() }
    @Override String toString() { "<$id> created as ${name}" }
}

class PatientAddedToZipcode extends PatientEvent implements JoinEvent<Patient, Long, PatientEvent, Zipcode>{
    Zipcode zipcode
    @Override Zipcode getJoinAggregate() { zipcode }
    @Override void setJoinAggregate(Zipcode rollupAggregate) { zipcode = rollupAggregate}

    @Override String getAudit() { new JsonBuilder([zipcodeId: joinAggregate?.id]).toString() }
    @Override String toString() { "<$id> sent to zipcode ${zipcode.uniqueId}" }

    static transients = ['joinAggregate']
}

class PatientRemovedFromZipcode extends PatientEvent implements DisjoinEvent<Patient, Long, PatientEvent, Zipcode> {
    Zipcode zipcode
    @Override Zipcode getJoinAggregate() { zipcode }
    @Override void setJoinAggregate(Zipcode rollupAggregate) { zipcode = rollupAggregate}

    @Override String getAudit() { new JsonBuilder([zipcodeId: joinAggregate?.id]).toString() }
    @Override String toString() { "<$id> removed from zipcode ${zipcode.uniqueId}" }

    static transients = ['joinAggregate']
}

@Event(Patient)
class ProcedurePerformed extends PatientEvent {
    String code
    BigDecimal cost

    @Override String getAudit() { new JsonBuilder([code: code, cost: cost]).toString() }
    @Override String toString() { "<$id> performed $code for \$ $cost" }
}

@Event(Patient)
class PaymentMade extends PatientEvent {
    BigDecimal amount

    @Override String getAudit() { new JsonBuilder([amount: amount]).toString() }
    @Override String toString() { "<$id> paid \$ $amount" }
}

class PatientEventReverted extends PatientEvent implements RevertEvent<Patient, Long, PatientEvent> {
    Long revertedEventId

    @Override String getAudit() { new JsonBuilder([revertedEvent: revertedEventId]).toString() }
    @Override String toString() { "<$id> reverted #$revertedEventId"}
}

class PatientDeprecatedBy extends PatientEvent implements DeprecatedBy<Patient, Long, PatientEvent> {
    static hasOne = [
            converse: PatientDeprecates
    ]
    Patient deprecator

    @Override String getAudit() { new JsonBuilder([deprecatedBy: deprecator.id]).toString() }
    @Override String toString() { "<$id> deprecated by #${deprecator.id}"}
}

class PatientDeprecates extends PatientEvent implements Deprecates<Patient, Long, PatientEvent> {
    static belongsTo = [
            converse: PatientDeprecatedBy
    ]
    Patient deprecated

    @Override String getAudit() { new JsonBuilder([deprecates: deprecated.id]).toString() }
    @Override String toString() { "<$id> deprecates #${deprecated.id}"}
}
