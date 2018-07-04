package grooves.grails.rdbms

import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.DeprecatedBy
import com.github.rahulsom.grooves.api.events.Deprecates
import com.github.rahulsom.grooves.api.events.DisjoinEvent
import com.github.rahulsom.grooves.api.events.JoinEvent
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
// tag::abstract[]
abstract class PatientEvent implements BaseEvent<Patient, Long, PatientEvent> { // <1>

    Long id
    RevertEvent<Patient, Long, PatientEvent> revertedBy  // <2>
    Date timestamp  // <3>
    long position  // <4>
    Patient aggregate
    Publisher<Patient> getAggregateObservable() {
        toPublisher just(aggregate)
    }  // <5>

    static transients = ['revertedBy'] // <6>

    static constraints = {
    }
    // end::abstract[]
    @Override String toString() {
        "${timestamp.format('yyyyMMdd')} <$id, ${aggregate.id}, $position>"
    }
// tag::abstract[]
}
// end::abstract[]

@EqualsAndHashCode
//tag::created[]
@Event(Patient) // <1>
class PatientCreated extends PatientEvent { // <2>
    String name

    //end::created[]
    @Override String toString() { "${super.toString()} created as $name" }
//tag::created[]
}
//end::created[]

@EqualsAndHashCode
class PatientAddedToZipcode extends PatientEvent implements
        JoinEvent<Patient, Long, PatientEvent, Zipcode> {
    Zipcode zipcode
    @Override Publisher<Zipcode> getJoinAggregateObservable() { toPublisher(just(zipcode)) }

    @Override String toString() {
        "<${aggregateId}.$id> $ts sent to zipcode ${zipcode.uniqueId}" }

    static transients = ['joinAggregate']
}

@EqualsAndHashCode
class PatientRemovedFromZipcode extends PatientEvent implements
        DisjoinEvent<Patient, Long, PatientEvent, Zipcode> {
    Zipcode zipcode
    @Override Publisher<Zipcode> getJoinAggregateObservable() { toPublisher(just(zipcode)) }

    @Override String toString() {
        "<${aggregateId}.$id> $ts removed from zipcode ${zipcode.uniqueId}" }

    static transients = ['joinAggregate']
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

//tag::reverted[]
@EqualsAndHashCode
class PatientEventReverted extends PatientEvent // <1>
        implements RevertEvent<Patient, Long, PatientEvent> { // <2>
    Long revertedEventId // <3>

    //end::reverted[]
    @Override String toString() { "${super.toString()} reverted $revertedEventId" }
    //tag::reverted[]
}
//end::reverted[]

@EqualsAndHashCode(excludes = ['converse'])
class PatientDeprecatedBy extends PatientEvent
        implements DeprecatedBy<Patient, Long, PatientEvent> {
    PatientDeprecates converse
    Patient deprecator

    Publisher<PatientDeprecates> getConverseObservable() { toPublisher(just(converse)) }
    Publisher<Patient> getDeprecatorObservable() { toPublisher(just(deprecator)) }

    @Override String toString() { "${super.toString()} deprecated by #${deprecator.id}" }
}

@EqualsAndHashCode(excludes = ['converse'])
class PatientDeprecates extends PatientEvent
        implements Deprecates<Patient, Long, PatientEvent> {
    PatientDeprecatedBy converse
    Patient deprecated

    Publisher<PatientDeprecatedBy> getConverseObservable() { toPublisher(just(converse)) }
    Publisher<Patient> getDeprecatedObservable() { toPublisher(just(deprecated)) }

    @Override String toString() { "${super.toString()} deprecates #${deprecated.id}" }

    static constraints = {
        converse nullable: true
    }
}
