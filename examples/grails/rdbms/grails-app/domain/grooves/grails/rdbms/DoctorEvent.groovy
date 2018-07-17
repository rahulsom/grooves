package grooves.grails.rdbms

import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.DisjoinEvent
import com.github.rahulsom.grooves.api.events.JoinEvent
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.groovy.transformations.Event
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.reactivestreams.Publisher

import static rx.Observable.empty
import static rx.Observable.just
import static rx.RxReactiveStreams.toPublisher

/**
 * Represents an Event on a Doctor
 *
 * @author Rahul Somasunderam
 */
@SuppressWarnings(['AbstractClassWithoutAbstractMethod', 'GrailsDomainReservedSqlKeywordName'])
@ToString
@EqualsAndHashCode(includes = ['aggregate', 'position'])
abstract class DoctorEvent implements BaseEvent<Doctor, Long, DoctorEvent> {

    static transients = ['revertedBy']

    static constraints = {
    }

    Long id
    RevertEvent<Doctor, Long, DoctorEvent> revertedBy
    Date timestamp
    long position
    Doctor aggregate

    Publisher<Doctor> getAggregateObservable() {
        toPublisher(aggregate ? just(aggregate) : empty())
    }
}

@Event(Doctor)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class DoctorCreated extends DoctorEvent {
    static constraints = {
        name maxSize: 100
    }

    String name

    @Override String toString() { "Doctor $name created" }
}

@EqualsAndHashCode(includes = ['aggregate', 'position'])
//tag::joins[]
class DoctorGotPatient extends DoctorEvent // <1>
        implements JoinEvent<Doctor, Long, DoctorEvent, Patient> { // <2>
    Patient patient
    @Override Publisher<Patient> getJoinAggregateObservable() { toPublisher(just(patient)) }
//end::joins[]

    @Override String toString() { "Doctor $aggregate got $patient" }
//tag::joins[]
}
//end::joins[]

@EqualsAndHashCode(includes = ['aggregate', 'position'])
//tag::joins[]
class DoctorLostPatient extends DoctorEvent
        implements DisjoinEvent<Doctor, Long, DoctorEvent, Patient> { // <3>
    Patient patient
    @Override Publisher<Patient> getJoinAggregateObservable() { toPublisher(just(patient)) } // <4>
//end::joins[]

    @Override String toString() { "Doctor $aggregate lost $patient" }
//tag::joins[]
}
//end::joins[]

@EqualsAndHashCode(includes = ['aggregate', 'position'])
class DoctorAddedToZipcode extends DoctorEvent
        implements JoinEvent<Doctor, Long, DoctorEvent, Zipcode> {
    static transients = ['joinAggregate']

    Zipcode zipcode
    @Override Publisher<Zipcode> getJoinAggregateObservable() { toPublisher(just(zipcode)) }

    @Override String toString() { "Doctor $aggregate added to $zipcode" }
}

@EqualsAndHashCode(includes = ['aggregate', 'position'])
class DoctorRemovedFromZipcode extends DoctorEvent
        implements DisjoinEvent<Doctor, Long, DoctorEvent, Zipcode> {
    static transients = ['joinAggregate']

    Zipcode zipcode
    @Override Publisher<Zipcode> getJoinAggregateObservable() { toPublisher(just(zipcode)) }

    @Override String toString() { "Doctor $aggregate removed from $zipcode" }
}
