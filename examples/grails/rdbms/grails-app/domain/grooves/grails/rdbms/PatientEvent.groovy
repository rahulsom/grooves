package grooves.grails.rdbms

import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.DeprecatedBy
import com.github.rahulsom.grooves.api.events.Deprecates
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.groovy.transformations.Event
import groovy.transform.EqualsAndHashCode
import org.reactivestreams.Publisher

import static rx.Observable.just
import static rx.RxReactiveStreams.toPublisher

/**
 * Represents Patient Events
 *
 * @author Rahul Somasunderam
 */
@EqualsAndHashCode
@SuppressWarnings(['AbstractClassWithoutAbstractMethod', 'GrailsDomainReservedSqlKeywordName'])
abstract class PatientEvent implements BaseEvent<Long, Patient, Long, PatientEvent> {

    RevertEvent<Long, Patient, Long, PatientEvent> revertedBy
    Date timestamp
    long position
    Patient aggregate
    Publisher<Patient> getAggregateObservable() {
        toPublisher just(aggregate)
    }

    static transients = ['revertedBy']

    static constraints = {
    }
    @Override String toString() {
        "${timestamp.format('yyyyMMdd')} <$id, ${aggregate.id}, $position>"
    }
}

@Event(Patient)
@EqualsAndHashCode
class PatientCreated extends PatientEvent {
    String name

    @Override String toString() { "${super.toString()} created as $name" }
}

@Event(Patient)
@EqualsAndHashCode
class ProcedurePerformed extends PatientEvent {
    String code
    BigDecimal cost

    @Override String toString() { "${super.toString()} performed $code for $cost" }
}

@Event(Patient)
@EqualsAndHashCode
class PaymentMade extends PatientEvent {
    BigDecimal amount

    @Override String toString() { "${super.toString()} paid $amount" }
}

@EqualsAndHashCode
class PatientEventReverted extends PatientEvent
        implements RevertEvent<Long, Patient, Long, PatientEvent> {
    Long revertedEventId

    @Override String toString() { "${super.toString()} reverted $revertedEventId" }
}

@EqualsAndHashCode
class PatientDeprecatedBy extends PatientEvent
        implements DeprecatedBy<Long, Patient, Long, PatientEvent> {
    PatientDeprecates converse
    Patient deprecator

    Publisher<PatientDeprecates> getConverseObservable() { toPublisher(just(converse)) }
    Publisher<Patient> getDeprecatorObservable() { toPublisher(just(deprecator)) }

    @Override String toString() { "${super.toString()} deprecated by #${deprecator.id}" }
}

@EqualsAndHashCode
class PatientDeprecates extends PatientEvent
        implements Deprecates<Long, Patient, Long, PatientEvent> {
    PatientDeprecatedBy converse
    Patient deprecated

    Publisher<PatientDeprecatedBy> getConverseObservable() { toPublisher(just(converse)) }
    Publisher<Patient> getDeprecatedObservable() { toPublisher(just(deprecated)) }

    @Override String toString() { "${super.toString()} deprecates #${deprecated.id}" }
}
