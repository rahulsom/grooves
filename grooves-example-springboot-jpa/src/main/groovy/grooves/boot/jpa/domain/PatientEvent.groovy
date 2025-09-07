package grooves.boot.jpa.domain

import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.DeprecatedBy
import com.github.rahulsom.grooves.api.events.Deprecates
import com.github.rahulsom.grooves.api.events.DisjoinEvent
import com.github.rahulsom.grooves.api.events.JoinEvent
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.groovy.transformations.Event
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.reactivestreams.Publisher

import jakarta.persistence.*

import static io.reactivex.Flowable.*

/**
 * Domain Model for Patient Event
 *
 * @author Rahul Somasunderam
 */
// tag::abstract[]
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = 'eventType')
@SuppressWarnings(['AbstractClassWithoutAbstractMethod'])
abstract class PatientEvent implements BaseEvent<Patient, Long, PatientEvent> { // <1>

    @GeneratedValue @Id Long id
    @Transient RevertEvent<Patient, Long, PatientEvent> revertedBy // <2>
    @Column(nullable = false) Date timestamp // <3>
    @Column(nullable = false) long position //<4>
    @ManyToOne Patient aggregate

    Publisher<Patient> getAggregateObservable() { // <5>
        aggregate == null ? empty() : just(aggregate)
    }

}
// end::abstract[]

@ToString(includeSuperProperties = true, includeNames = true, includePackage = false)
// tag::created[]
@Entity
@Event(Patient) // <1>
class PatientCreated extends PatientEvent { // <2>
    String name

    @Override String toString() { "PatientCreated(name=$name)" }
}
// end::created[]

@ToString(includeSuperProperties = true, includeNames = true, includePackage = false)
@Entity
@Event(Patient)
class ProcedurePerformed extends PatientEvent {
    String code
    BigDecimal cost

    @Override String toString() { "ProcedurePerformed(code=$code, cost=$cost)" }
}

@ToString(includeSuperProperties = true, includeNames = true, includePackage = false)
@Entity
@Event(Patient)
class PaymentMade extends PatientEvent {
    BigDecimal amount

    @Override String toString() { "PaymentMade(amount=$amount)" }
}

@ToString(includeSuperProperties = true, includeNames = true, includePackage = false)
// tag::reverted[]
@Entity
class PatientEventReverted extends PatientEvent // <1>
        implements RevertEvent<Patient, Long, PatientEvent> { // <2>
    Long revertedEventId // <3>

    @Override String toString() { "PatientEventReverted(revertedEventId=$revertedEventId)" }
}
// end::reverted[]

@Entity
class PatientDeprecatedBy extends PatientEvent implements
        DeprecatedBy<Patient, Long, PatientEvent> {
    @OneToOne PatientDeprecates converse
    @OneToOne Patient deprecator

    Publisher<PatientDeprecates> getConverseObservable() { just(converse) }
    Publisher<Patient> getDeprecatorObservable() { just(deprecator) }

    @Override String toString() { "PatientDeprecatedBy(deprecator=$deprecator)" }
}

@Entity
class PatientDeprecates extends PatientEvent
        implements Deprecates<Patient, Long, PatientEvent> {
    @OneToOne PatientDeprecatedBy converse
    @OneToOne Patient deprecated

    Publisher<PatientDeprecatedBy> getConverseObservable() { just(converse) }
    Publisher<Patient> getDeprecatedObservable() { just(deprecated) }

    @Override String toString() { "PatientDeprecates(deprecated=$deprecated)" }
}

@Entity
@EqualsAndHashCode
class PatientAddedToZipcode extends PatientEvent implements
    JoinEvent<Patient, Long, PatientEvent, Zipcode> {

    @ManyToOne Zipcode zipcode
    @Override Publisher<Zipcode> getJoinAggregateObservable() { just(zipcode) }

    @Override String toString() {
        "<${aggregate.uniqueId}> ${timestamp} sent to zipcode ${zipcode.uniqueId}" }
}

@Entity
@EqualsAndHashCode
class PatientRemovedFromZipcode extends PatientEvent implements
    DisjoinEvent<Patient, Long, PatientEvent, Zipcode> {

    @ManyToOne Zipcode zipcode
    @Override Publisher<Zipcode> getJoinAggregateObservable() { just(zipcode) }

    @Override String toString() {
        "<${aggregate.uniqueId}> ${timestamp} removed from zipcode ${zipcode.uniqueId}" }
}
