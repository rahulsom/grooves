package grooves.boot.jpa.domain

import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.DisjoinEvent
import com.github.rahulsom.grooves.api.events.JoinEvent
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.groovy.transformations.Event
import groovy.transform.EqualsAndHashCode
import org.reactivestreams.Publisher

import javax.persistence.Column
import javax.persistence.DiscriminatorColumn
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.persistence.Transient

import static io.reactivex.Flowable.empty
import static io.reactivex.Flowable.just

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = 'eventType')
@SuppressWarnings(['AbstractClassWithoutAbstractMethod'])
@EqualsAndHashCode(includes = ['aggregate', 'position'])
class ZipcodeEvent implements BaseEvent<Zipcode, Long, ZipcodeEvent> {
    @GeneratedValue @Id Long id
    @Transient RevertEvent<Zipcode, Long, ZipcodeEvent> revertedBy
    @Column(nullable = false) Date timestamp
    @Column(nullable = false) long position
    @OneToOne Zipcode aggregate

    Publisher<Zipcode> getAggregateObservable() {
        aggregate == null ? empty() : just(aggregate)
    }
}

@Event(Zipcode)
@Entity
class ZipcodeCreated extends ZipcodeEvent {
    String name

    @Override String toString() { "${aggregate} was created" }
}

@Event(Zipcode)
@Entity
// tag::joins[]
class ZipcodeGotPatient extends ZipcodeEvent // <1>
    implements JoinEvent<Zipcode, Long, ZipcodeEvent, Patient> { // <2>
    @ManyToOne Patient patient
    @Override Publisher<Patient> getJoinAggregateObservable() { just(patient) }
// end::joins[]
    @Override String toString() { "${aggregate} got ${patient}" }
// tag::joins[]
}
// end::joins[]

@Event(Zipcode)
@Entity
// tag::joins[]
class ZipcodeLostPatient extends ZipcodeEvent implements
    DisjoinEvent<Zipcode, Long, ZipcodeEvent, Patient> { // <3>
    @ManyToOne Patient patient
    @Override Publisher<Patient> getJoinAggregateObservable() { just(patient) } // <4>
// end::joins[]

    @Override String toString() { "${aggregate} lost ${patient}" }
// tag::joins[]
}
// end::joins[]
