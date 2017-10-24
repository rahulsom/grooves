package grooves.grails.mongo

import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.DisjoinEvent
import com.github.rahulsom.grooves.api.events.JoinEvent
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.groovy.transformations.Event
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.reactivestreams.Publisher

import static rx.Observable.just

/**
 * Represents an Event on a Doctor
 *
 * @author Rahul Somasunderam
 */
@SuppressWarnings(['AbstractClassWithoutAbstractMethod', 'GrailsDomainReservedSqlKeywordName'])
@ToString(includePackage = false)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
abstract class DoctorEvent implements BaseEvent<Long, Doctor, Long, DoctorEvent> {

    RevertEvent<Long, Doctor, Long, DoctorEvent> revertedBy
    Date timestamp
    Long position
    Doctor aggregate

    Publisher<Doctor> getAggregateObservable() { just(aggregate) toPublisher() }

    static transients = ['revertedBy']

    static constraints = {
    }
}

@Event(Doctor)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class DoctorCreated extends DoctorEvent {
    String name

    @Override String toString() { "Doctor $name created" }
}

@Event(Doctor)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class DoctorGotPatient extends DoctorEvent
        implements JoinEvent<Long, Doctor, Long, DoctorEvent, Long, Patient> {
    Patient patient
    @Override Publisher<Patient> getJoinAggregateObservable() { just patient toPublisher() }

    @Override String toString() { "Doctor $aggregate got $patient" }
}

@Event(Doctor)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class DoctorLostPatient extends DoctorEvent
        implements DisjoinEvent<Long, Doctor, Long, DoctorEvent, Long, Patient> {
    Patient patient
    @Override Publisher<Patient> getJoinAggregateObservable() { just patient toPublisher() }

    @Override String toString() { "Doctor $aggregate lost $patient" }
}

@EqualsAndHashCode(includes = ['aggregate', 'position'])
class DoctorAddedToZipcode extends DoctorEvent
        implements JoinEvent<Long, Doctor, Long, DoctorEvent, Long, Zipcode> {
    Zipcode zipcode
    @Override Publisher<Zipcode> getJoinAggregateObservable() { just(zipcode) toPublisher() }

    @Override String toString() { "Doctor $aggregate added to $zipcode" }

    static transients = ['joinAggregate']
}

@EqualsAndHashCode(includes = ['aggregate', 'position'])
class DoctorRemovedFromZipcode extends DoctorEvent
        implements DisjoinEvent<Long, Doctor, Long, DoctorEvent, Long, Zipcode> {
    Zipcode zipcode
    @Override Publisher<Zipcode> getJoinAggregateObservable() { just(zipcode) toPublisher() }

    @Override String toString() { "Doctor $aggregate removed from $zipcode" }

    static transients = ['joinAggregate']
}
