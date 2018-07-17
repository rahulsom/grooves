package grooves.grails.rdbms

import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.DisjoinEvent
import com.github.rahulsom.grooves.api.events.JoinEvent
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.groovy.transformations.Event
import groovy.transform.EqualsAndHashCode
import org.reactivestreams.Publisher

import static rx.Observable.empty
import static rx.Observable.just
import static rx.RxReactiveStreams.toPublisher

/**
 * Base Zipcode Event
 *
 * @author Rahul Somasunderam
 */
@SuppressWarnings(['DuplicateStringLiteral', 'AbstractClassWithoutAbstractMethod',
        'GrailsDomainReservedSqlKeywordName', ])
@EqualsAndHashCode(includes = ['aggregate', 'position'])
abstract class ZipcodeEvent implements BaseEvent<Zipcode, Long, ZipcodeEvent> {
    static transients = ['revertedBy']

    static constraints = {
    }

    Long id
    RevertEvent<Zipcode, Long, ZipcodeEvent> revertedBy
    Date timestamp
    long position
    Zipcode aggregate
    Publisher<Zipcode> getAggregateObservable() {
        toPublisher(aggregate ? just(aggregate) : empty())
    }

    @Override String toString() { "ZipcodeEvent($id, $aggregateId)" }
}

@Event(Zipcode)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class ZipcodeCreated extends ZipcodeEvent {
    static constraints = {
        name maxSize: 100
    }

    String name

    @Override String toString() { "${aggregate} was created" }
}

@Event(Zipcode)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class ZipcodeGotPatient extends ZipcodeEvent implements
        JoinEvent<Zipcode, Long, ZipcodeEvent, Patient> {
    static transients = ['joinAggregate']

    Patient patient

    @Override Publisher<Patient> getJoinAggregateObservable() { toPublisher(just(patient)) }

    @Override String toString() { "${aggregate} got ${patient}" }
}

@Event(Zipcode)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class ZipcodeLostPatient extends ZipcodeEvent implements
        DisjoinEvent<Zipcode, Long, ZipcodeEvent, Patient> {
    static transients = ['joinAggregate']

    Patient patient

    @Override Publisher<Patient> getJoinAggregateObservable() { toPublisher(just(patient)) }

    @Override String toString() { "${aggregate} lost ${patient}" }
}

@Event(Zipcode)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class ZipcodeGotDoctor extends ZipcodeEvent implements
        JoinEvent<Zipcode, Long, ZipcodeEvent, Doctor> {
    static transients = ['joinAggregate']

    Doctor doctor

    @Override Publisher<Doctor> getJoinAggregateObservable() { toPublisher(just(doctor)) }

    @Override String toString() { "${aggregate} got ${doctor}" }
}

@Event(Zipcode)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class ZipcodeLostDoctor extends ZipcodeEvent implements
        DisjoinEvent<Zipcode, Long, ZipcodeEvent, Doctor> {
    static transients = ['joinAggregate']

    Doctor doctor

    @Override Publisher<Doctor> getJoinAggregateObservable() { toPublisher(just(doctor)) }

    @Override String toString() { "${aggregate} lost ${doctor}" }
}
