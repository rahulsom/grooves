package grooves.grails.mongo

import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.DisjoinEvent
import com.github.rahulsom.grooves.api.events.JoinEvent
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.groovy.transformations.Event
import groovy.json.JsonBuilder
import groovy.transform.EqualsAndHashCode
import rx.Observable

import static rx.Observable.just

/**
 * Base Zipcode Event
 *
 * @author Rahul Somasunderam
 */
@SuppressWarnings(['DuplicateStringLiteral', 'AbstractClassWithoutAbstractMethod',
        'GrailsDomainReservedSqlKeywordName', ])
@EqualsAndHashCode(includes = ['aggregate', 'position'])
abstract class ZipcodeEvent implements BaseEvent<Zipcode, Long, ZipcodeEvent> {

    RevertEvent<Zipcode, Long, ZipcodeEvent> revertedBy
    String createdBy
    Date timestamp
    Long position
    Zipcode aggregate
    Observable<Zipcode> getAggregateObservable() { just(aggregate) }

    static transients = ['revertedBy']

    static constraints = {
    }

    @Override String toString() { "ZipcodeEvent($id, $aggregateId)" }
}

@Event(Zipcode)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class ZipcodeCreated extends ZipcodeEvent {
    String name

    @Override String getAudit() { new JsonBuilder([name: name]).toString() }
    @Override String toString() { "${aggregate} was created" }
}

@Event(Zipcode)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class ZipcodeGotPatient extends ZipcodeEvent implements
        JoinEvent<Zipcode, Long, ZipcodeEvent, Patient> {
    Patient patient

    @Override String getAudit() { new JsonBuilder([patientId: joinAggregate?.id]).toString() }
    @Override Patient getJoinAggregate() { patient }
    @Override void setJoinAggregate(Patient rollupAggregate) { this.patient = rollupAggregate }
    @Override String toString() { "${aggregate} got ${patient}" }

    static transients = ['joinAggregate']
}

@Event(Zipcode)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class ZipcodeLostPatient extends ZipcodeEvent implements
        DisjoinEvent<Zipcode, Long, ZipcodeEvent, Patient> {
    Patient patient

    @Override String getAudit() { new JsonBuilder([patientId: joinAggregate?.id]).toString() }
    @Override Patient getJoinAggregate() { patient }
    @Override void setJoinAggregate(Patient rollupAggregate) { this.patient = rollupAggregate }
    @Override String toString() { "${aggregate} lost ${patient}" }

    static transients = ['joinAggregate']
}

@Event(Zipcode)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class ZipcodeGotDoctor extends ZipcodeEvent implements
        JoinEvent<Zipcode, Long, ZipcodeEvent, Doctor> {
    Doctor doctor

    @Override String getAudit() { new JsonBuilder([doctorId: joinAggregate?.id]).toString() }
    @Override Doctor getJoinAggregate() { doctor }
    @Override void setJoinAggregate(Doctor rollupAggregate) { this.doctor = rollupAggregate }
    @Override String toString() { "${aggregate} got ${doctor}" }

    static transients = ['joinAggregate']
}

@Event(Zipcode)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class ZipcodeLostDoctor extends ZipcodeEvent implements
        DisjoinEvent<Zipcode, Long, ZipcodeEvent, Doctor> {
    Doctor doctor

    @Override String getAudit() { new JsonBuilder([doctorId: joinAggregate?.id]).toString() }
    @Override Doctor getJoinAggregate() { doctor }
    @Override void setJoinAggregate(Doctor rollupAggregate) { this.doctor = rollupAggregate }
    @Override String toString() { "${aggregate} lost ${doctor}" }

    static transients = ['joinAggregate']
}
