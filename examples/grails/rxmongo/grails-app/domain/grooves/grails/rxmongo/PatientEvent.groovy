package grooves.grails.rxmongo


import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.DeprecatedBy
import com.github.rahulsom.grooves.api.events.Deprecates
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.groovy.transformations.Event
import grails.gorm.rx.mongodb.RxMongoEntity
import groovy.json.JsonBuilder
import groovy.transform.EqualsAndHashCode

/**
 * Represents Patient Events
 *
 * @author Rahul Somasunderam
 */
@EqualsAndHashCode
@SuppressWarnings(['AbstractClassWithoutAbstractMethod', 'GrailsDomainReservedSqlKeywordName'])
abstract class PatientEvent implements RxMongoEntity, BaseEvent<Patient, String, PatientEvent> {

    String id
    RevertEvent<Patient, String, PatientEvent> revertedBy
    String createdBy
    Date timestamp
    Long position
    Patient aggregate

    static transients = ['revertedBy']

    static constraints = {
    }
    @Override String toString() { "PatientEvent $id" }
}

@Event(Patient)
@EqualsAndHashCode
class PatientCreated extends PatientEvent {
    String name

    @Override
    String getAudit() { new JsonBuilder([name: name]).toString() }
    @Override String toString() { "<$id> created" }
}

@Event(Patient)
@EqualsAndHashCode
class ProcedurePerformed extends PatientEvent {
    String code
    BigDecimal cost

    @Override
    String getAudit() { new JsonBuilder([code: code, cost: cost]).toString() }
    @Override String toString() { "<$id> performed $code for $cost" }
}

@Event(Patient)
@EqualsAndHashCode
class PaymentMade extends PatientEvent {
    BigDecimal amount

    @Override
    String getAudit() { new JsonBuilder([amount: amount]).toString() }
    @Override String toString() { "<$id> paid $amount" }
}

@EqualsAndHashCode
class PatientEventReverted extends PatientEvent
        implements RevertEvent<Patient, String, PatientEvent> {
    String revertedEventId

    @Override
    String getAudit() { new JsonBuilder([revertedEvent: revertedEventId]).toString() }
    @Override String toString() { "<$id> reverted $revertedEventId" }
}

@EqualsAndHashCode
class PatientDeprecatedBy extends PatientEvent
        implements DeprecatedBy<Patient, String, PatientEvent> {
    PatientDeprecates converse
    Patient deprecator

    @Override String getAudit() { new JsonBuilder([deprecatedBy: deprecator.id]).toString() }
    @Override String toString() { "<$id> deprecated by #${deprecator.id}" }
}

@EqualsAndHashCode
class PatientDeprecates extends PatientEvent implements Deprecates<Patient, String, PatientEvent> {
    PatientDeprecatedBy converse
    Patient deprecated

    @Override String getAudit() { new JsonBuilder([deprecates: deprecated.id]).toString() }
    @Override String toString() { "<$id> deprecates #${deprecated.id}" }
}
