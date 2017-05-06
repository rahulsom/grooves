package grooves.grails.mongo

import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.DisjoinEvent
import com.github.rahulsom.grooves.api.events.JoinEvent
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.groovy.transformations.Event
import groovy.json.JsonBuilder
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import rx.Observable

import static rx.Observable.just

/**
 * Represents an Event on a Doctor
 *
 * @author Rahul Somasunderam
 */
@SuppressWarnings(['AbstractClassWithoutAbstractMethod', 'GrailsDomainReservedSqlKeywordName'])
@ToString
@EqualsAndHashCode(includes = ['aggregate', 'position', 'createdBy'])
abstract class DoctorEvent implements BaseEvent<Doctor, Long, DoctorEvent> {

    RevertEvent<Doctor, Long, DoctorEvent> revertedBy
    String createdBy
    Date timestamp
    Long position
    Doctor aggregate

    Observable<Doctor> getAggregateObservable() { just(aggregate) }

    static transients = ['revertedBy']

    static constraints = {
    }
}

@Event(Doctor)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class DoctorCreated extends DoctorEvent {
    String name

    @Override String getAudit() { new JsonBuilder([name: name]).toString() }
    @Override String toString() { "Doctor $name created" }
}

@Event(Doctor)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class DoctorGotPatient extends DoctorEvent
        implements JoinEvent<Doctor, Long, DoctorEvent, Patient> {
    Patient patient
    @Override Patient getJoinAggregate() { patient }
    @Override void setJoinAggregate(Patient rollupAggregate) { patient = rollupAggregate }

    @Override String getAudit() { new JsonBuilder([patientId: joinAggregate?.id]).toString() }
    @Override String toString() { "Doctor $aggregate got $patient" }
}

@Event(Doctor)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class DoctorLostPatient extends DoctorEvent
        implements DisjoinEvent<Doctor, Long, DoctorEvent, Patient> {
    Patient patient
    @Override Patient getJoinAggregate() { patient }
    @Override void setJoinAggregate(Patient rollupAggregate) { patient = rollupAggregate }

    @Override String getAudit() { new JsonBuilder([patientId: joinAggregate?.id]).toString() }
    @Override String toString() { "Doctor $aggregate lost $patient" }
}

@EqualsAndHashCode(includes = ['aggregate', 'position'])
class DoctorAddedToZipcode extends DoctorEvent
        implements JoinEvent<Doctor, Long, DoctorEvent, Zipcode> {
    Zipcode zipcode
    @Override Zipcode getJoinAggregate() { zipcode }
    @Override void setJoinAggregate(Zipcode rollupAggregate) { zipcode = rollupAggregate }

    @Override String getAudit() { new JsonBuilder([zipcodeId: joinAggregate?.id]).toString() }
    @Override String toString() { "Doctor $aggregate added to $zipcode" }

    static transients = ['joinAggregate']
}

@EqualsAndHashCode(includes = ['aggregate', 'position'])
class DoctorRemovedFromZipcode extends DoctorEvent
        implements DisjoinEvent<Doctor, Long, DoctorEvent, Zipcode> {
    Zipcode zipcode
    @Override Zipcode getJoinAggregate() { zipcode }
    @Override void setJoinAggregate(Zipcode rollupAggregate) { zipcode = rollupAggregate }

    @Override String getAudit() { new JsonBuilder([zipcodeId: joinAggregate?.id]).toString() }
    @Override String toString() { "Doctor $aggregate removed from $zipcode" }

    static transients = ['joinAggregate']
}

