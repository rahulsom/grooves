package grooves.grails.mongo

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
abstract class ZipcodeEvent implements BaseEvent<Long, Zipcode, Long, ZipcodeEvent> {

    RevertEvent<Long, Zipcode, Long, ZipcodeEvent> revertedBy
    Date timestamp
    Long position
    Zipcode aggregate
    Publisher<Zipcode> getAggregateObservable() {
        toPublisher(aggregate ? just(aggregate) : empty())
    }

    static transients = ['revertedBy']

    static constraints = {
    }

    @Override String toString() { "ZipcodeEvent($id, $aggregateId)" }
}

@Event(Zipcode)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class ZipcodeCreated extends ZipcodeEvent {
    String name

    @Override String toString() { "${aggregate} was created" }
}

@Event(Zipcode)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class ZipcodeGotPatient extends ZipcodeEvent implements
        JoinEvent<Long, Zipcode, Long, ZipcodeEvent, Long, Patient> {
    Patient patient

    @Override Publisher<Patient> getJoinAggregateObservable() { toPublisher(just(patient)) }

    @Override String toString() { "${aggregate} got ${patient}" }

    static transients = ['joinAggregate']
}

@Event(Zipcode)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class ZipcodeLostPatient extends ZipcodeEvent implements
        DisjoinEvent<Long, Zipcode, Long, ZipcodeEvent, Long, Patient> {
    Patient patient

    @Override Publisher<Patient> getJoinAggregateObservable() { toPublisher(just(patient)) }

    @Override String toString() { "${aggregate} lost ${patient}" }

    static transients = ['joinAggregate']
}

@Event(Zipcode)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class ZipcodeGotDoctor extends ZipcodeEvent implements
        JoinEvent<Long, Zipcode, Long, ZipcodeEvent, Long, Doctor> {
    Doctor doctor

    @Override Publisher<Doctor> getJoinAggregateObservable() { toPublisher(just(doctor)) }

    @Override String toString() { "${aggregate} got ${doctor}" }

    static transients = ['joinAggregate']
}

@Event(Zipcode)
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class ZipcodeLostDoctor extends ZipcodeEvent implements
        DisjoinEvent<Long, Zipcode, Long, ZipcodeEvent, Long, Doctor> {
    Doctor doctor

    @Override Publisher<Doctor> getJoinAggregateObservable() { toPublisher(just(doctor)) }

    @Override String toString() { "${aggregate} lost ${doctor}" }

    static transients = ['joinAggregate']
}
