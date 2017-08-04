package grooves.example.rxrest

import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.DeprecatedBy
import com.github.rahulsom.grooves.api.events.Deprecates
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.groovy.transformations.Event
import grails.gorm.rx.rest.RxRestEntity
import groovy.json.JsonBuilder
import groovy.transform.EqualsAndHashCode
import rx.Observable

import static rx.Observable.just

/**
 * Represents Patient Events
 *
 * @author Rahul Somasunderam
 */
@EqualsAndHashCode
@SuppressWarnings(['AbstractClassWithoutAbstractMethod', 'GrailsDomainReservedSqlKeywordName'])
class PatientEvent implements
        RxRestEntity<PatientEvent>, BaseEvent<Long, Patient, Long, PatientEvent> {

    RevertEvent<Long, Patient, Long, PatientEvent> revertedBy
    String createdBy
    Date timestamp
    Long position
    Long aggregateId

    @Override
    void setAggregate(Patient patient) { aggregateId = patient.id }

    @Override
    Patient getAggregate() { aggregateObservable.toBlocking().first() }

    Observable<Patient> getAggregateObservable() {
        log.error "getAggregateObservable $aggregateId"
        Patient.get(aggregateId)
    }

    static transients = ['revertedBy']

    static constraints = {
    }
    @Override String toString() { "<$id, ${aggregateId}, ${position}>" }
    @Override String getAudit() { "${id} Unknown Event" }
}

@Event(Patient)
@EqualsAndHashCode
class PatientCreated extends PatientEvent {
    String name

    @Override
    String getAudit() { new JsonBuilder([name: name]).toString() }
    @Override String toString() { "${super.toString()} created as $name" }
}

@Event(Patient)
@EqualsAndHashCode
class ProcedurePerformed extends PatientEvent {
    String code
    Double cost

    @Override
    String getAudit() { new JsonBuilder([code: code, cost: cost]).toString() }
    @Override String toString() { "${super.toString()} performed $code for $cost" }
}

@Event(Patient)
@EqualsAndHashCode
class PaymentMade extends PatientEvent {
    Double amount

    @Override
    String getAudit() { new JsonBuilder([amount: amount]).toString() }
    @Override String toString() { "${super.toString()} paid $amount" }
}

@EqualsAndHashCode
class PatientEventReverted extends PatientEvent
        implements RevertEvent<Long, Patient, Long, PatientEvent> {
    Long revertedEventId

    @Override
    String getAudit() { new JsonBuilder([revertedEvent: revertedEventId]).toString() }
    @Override String toString() { "${super.toString()} reverted $revertedEventId" }
}

@EqualsAndHashCode
class PatientDeprecatedBy extends PatientEvent
        implements DeprecatedBy<Long, Patient, Long, PatientEvent> {
    PatientDeprecates converse
    Patient deprecator

    @Override String getAudit() { new JsonBuilder([deprecatedBy: deprecator?.id]).toString() }
    @Override String toString() { "${super.toString()} deprecated by #${deprecator?.id}" }

    @Override
    Observable<Deprecates<Long, Patient, Long, PatientEvent>> getConverseObservable() {
        just converse
    }

    @Override
    Observable<Patient> getDeprecatorObservable() {
        just deprecator
    }
}

@EqualsAndHashCode
class PatientDeprecates extends PatientEvent
        implements Deprecates<Long, Patient, Long, PatientEvent> {
    PatientDeprecatedBy converse
    Patient deprecated

    @Override String getAudit() { new JsonBuilder([deprecates: deprecated?.id]).toString() }
    @Override String toString() { "${super.toString()} deprecates #${deprecated?.id}" }

    @Override
    Observable<DeprecatedBy<Long, Patient, Long, PatientEvent>> getConverseObservable() {
        just converse
    }

    @Override
    Observable<Patient> getDeprecatedObservable() {
        just deprecated
    }
}
