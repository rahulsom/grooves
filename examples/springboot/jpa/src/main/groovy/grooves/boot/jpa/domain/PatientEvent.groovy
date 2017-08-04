package grooves.boot.jpa.domain

import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.DeprecatedBy
import com.github.rahulsom.grooves.api.events.Deprecates
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.groovy.transformations.Event
import groovy.json.JsonBuilder
import groovy.transform.ToString
import rx.Observable

import javax.persistence.*

import static rx.Observable.empty
import static rx.Observable.just

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
abstract class PatientEvent implements BaseEvent<Long, Patient, Long, PatientEvent> { // <1>

    @GeneratedValue @Id Long id
    @Transient RevertEvent<Long, Patient, Long, PatientEvent> revertedBy // <2>
    @Column(nullable = false) String createdBy
    @Column(nullable = false) Date timestamp // <3>
    @Column(nullable = false) Long position //<4>
    @OneToOne Patient aggregate

    Observable<Patient> getAggregateObservable() { aggregate ? just(aggregate) : empty() } // <5>

}
// end::abstract[]

@ToString(includeSuperProperties = true, includeNames = true, includePackage = false)
//tag::created[]
@Entity
@Event(Patient) // <1>
class PatientCreated extends PatientEvent { // <2>
    String name

    @Override String getAudit() { new JsonBuilder([name: name]).toString() } // <3>
    @Override String toString() { "PatientCreated(name=$name)" }
}
//end::created[]

@ToString(includeSuperProperties = true, includeNames = true, includePackage = false)
@Entity
@Event(Patient)
class ProcedurePerformed extends PatientEvent {
    String code
    BigDecimal cost

    @Override String getAudit() { new JsonBuilder([code: code, cost: cost]).toString() }
    @Override String toString() { "ProcedurePerformed(code=$code, cost=$cost)" }
}

@ToString(includeSuperProperties = true, includeNames = true, includePackage = false)
@Entity
@Event(Patient)
class PaymentMade extends PatientEvent {
    BigDecimal amount

    @Override String getAudit() { new JsonBuilder([amount: amount]).toString() }
    @Override String toString() { "PaymentMade(amount=$amount)" }
}

@ToString(includeSuperProperties = true, includeNames = true, includePackage = false)
//tag::reverted[]
@Entity
class PatientEventReverted
        extends PatientEvent // <1>
        implements RevertEvent<Long, Patient, Long, PatientEvent> { // <2>
    Long revertedEventId // <3>

    @Override String getAudit() { new JsonBuilder([revertedEvent: revertedEventId]).toString() }
    @Override String toString() { "PatientEventReverted(revertedEventId=$revertedEventId)" }
}
//end::reverted[]

@Entity
class PatientDeprecatedBy extends PatientEvent implements
        DeprecatedBy<Long, Patient, Long, PatientEvent> {
    @OneToOne PatientDeprecates converse
    @OneToOne Patient deprecator

    Observable<PatientDeprecates> getConverseObservable() { just(converse) }
    Observable<Patient> getDeprecatorObservable() { just(deprecator) }

    @Override String getAudit() { new JsonBuilder([deprecatedBy: deprecator.id]).toString() }
    @Override String toString() { "PatientDeprecatedBy(deprecator=$deprecator)" }
}

@Entity
class PatientDeprecates extends PatientEvent
        implements Deprecates<Long, Patient, Long, PatientEvent> {
    @OneToOne PatientDeprecatedBy converse
    @OneToOne Patient deprecated

    Observable<PatientDeprecatedBy> getConverseObservable() { just(converse) }
    Observable<Patient> getDeprecatedObservable() { just(deprecated) }

    @Override String getAudit() { new JsonBuilder([deprecates: deprecated.id]).toString() }
    @Override String toString() { "PatientDeprecates(deprecated=$deprecated)" }
}
