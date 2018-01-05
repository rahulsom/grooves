package rxmongo

import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.DeprecatedBy
import com.github.rahulsom.grooves.api.events.Deprecates
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.groovy.transformations.Event
import grails.gorm.rx.mongodb.RxMongoEntity
import groovy.transform.EqualsAndHashCode
import org.reactivestreams.Publisher

import static rx.Observable.empty
import static rx.Observable.just
import static rx.RxReactiveStreams.toPublisher

/**
 * Represents Patient Events
 *
 * @author Rahul Somasunderam
 */
@EqualsAndHashCode
@SuppressWarnings(['AbstractClassWithoutAbstractMethod', 'GrailsDomainReservedSqlKeywordName'])
class PatientEvent implements RxMongoEntity<PatientEvent>,
        BaseEvent<Patient, String, PatientEvent> {

    String id
    RevertEvent<Patient, String, PatientEvent> revertedBy
    Date timestamp
    long position
    Patient aggregate
    Publisher<Patient> getAggregateObservable() {
        toPublisher(aggregate ? just(aggregate) : empty())
    }

    static transients = ['revertedBy']

    static constraints = {
    }
    @Override String toString() { "PatientEvent $id" }

}

@Event(Patient)
@EqualsAndHashCode
class PatientCreated extends PatientEvent {
    String name

    @Override String toString() { "<$id> created" }
}

@Event(Patient)
@EqualsAndHashCode
class ProcedurePerformed extends PatientEvent {
    String code
    BigDecimal cost

    @Override String toString() { "<$id> performed $code for $cost" }
}

@Event(Patient)
@EqualsAndHashCode
class PaymentMade extends PatientEvent {
    BigDecimal amount

    @Override String toString() { "<$id> paid $amount" }
}

@EqualsAndHashCode
class PatientEventReverted extends PatientEvent
        implements RevertEvent<Patient, String, PatientEvent> {
    String revertedEventId

    @Override String toString() { "<$id> reverted $revertedEventId" }
}

@EqualsAndHashCode
class PatientDeprecatedBy extends PatientEvent
        implements DeprecatedBy<Patient, String, PatientEvent> {
    PatientDeprecates converse
    Patient deprecator

    Publisher<PatientDeprecates> getConverseObservable() { toPublisher(just(converse)) }
    Publisher<Patient> getDeprecatorObservable() { toPublisher(just(deprecator)) }

    @Override String toString() { "<$id> deprecated by #${deprecator.id}" }
}

@EqualsAndHashCode
class PatientDeprecates extends PatientEvent
        implements Deprecates<Patient, String, PatientEvent> {
    PatientDeprecatedBy converse
    Patient deprecated

    Publisher<PatientDeprecatedBy> getConverseObservable() { toPublisher(just(converse)) }
    Publisher<Patient> getDeprecatedObservable() { toPublisher(just(deprecated)) }

    @Override String toString() { "<$id> deprecates #${deprecated.id}" }
}
