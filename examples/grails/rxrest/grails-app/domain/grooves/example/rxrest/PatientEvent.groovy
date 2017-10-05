package grooves.example.rxrest

import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.DeprecatedBy
import com.github.rahulsom.grooves.api.events.Deprecates
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.groovy.transformations.Event
import grails.gorm.rx.rest.RxRestEntity
import groovy.transform.EqualsAndHashCode

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
}

@Event(Patient)
@EqualsAndHashCode
class PatientCreated extends PatientEvent {
    String name

    @Override
    @Override String toString() { "${super.toString()} created as $name" }
}

@Event(Patient)
@EqualsAndHashCode
class ProcedurePerformed extends PatientEvent {
    String code
    Double cost

    @Override
    @Override String toString() { "${super.toString()} performed $code for $cost" }
}

@Event(Patient)
@EqualsAndHashCode
class PaymentMade extends PatientEvent {
    Double amount

    @Override
    @Override String toString() { "${super.toString()} paid $amount" }
}

@EqualsAndHashCode
class PatientEventReverted extends PatientEvent
        implements RevertEvent<Long, Patient, Long, PatientEvent> {
    Long revertedEventId

    @Override
    @Override String toString() { "${super.toString()} reverted $revertedEventId" }
}

@EqualsAndHashCode
class PatientDeprecatedBy extends PatientEvent
        implements DeprecatedBy<Long, Patient, Long, PatientEvent> {
    PatientDeprecates converse
    Patient deprecator

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
