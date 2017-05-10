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

import static rx.Observable.just

/**
 * Domain Model for Patient Event
 *
 * @author Rahul Somasunderam
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = 'eventType')
@SuppressWarnings(['AbstractClassWithoutAbstractMethod'])
abstract class PatientEvent implements BaseEvent<Patient, Long, PatientEvent> {

    @GeneratedValue @Id Long id
    @Transient RevertEvent<Patient, Long, PatientEvent> revertedBy
    @Column(nullable = false) String createdBy
    @Column(nullable = false) Date timestamp
    @Column(nullable = false) Long position
    @OneToOne Patient aggregate

    Observable<Patient> getAggregateObservable() { just(aggregate) }

}

@Entity
@Event(Patient)
@ToString(includeSuperProperties = true, includeNames = true, includePackage = false)
class PatientCreated extends PatientEvent {
    String name

    @Override
    String getAudit() { new JsonBuilder([name: name]).toString() }
}

@Entity
@Event(Patient)
@ToString(includeSuperProperties = true, includeNames = true, includePackage = false)
class ProcedurePerformed extends PatientEvent {
    String code
    BigDecimal cost

    @Override
    String getAudit() { new JsonBuilder([code: code, cost: cost]).toString() }
}

@Entity
@Event(Patient)
@ToString(includeSuperProperties = true, includeNames = true, includePackage = false)
class PaymentMade extends PatientEvent {
    BigDecimal amount

    @Override
    String getAudit() { new JsonBuilder([amount: amount]).toString() }
}

@Entity
@ToString(includeSuperProperties = true, includeNames = true, includePackage = false)
class PatientEventReverted extends PatientEvent implements
        RevertEvent<Patient, Long, PatientEvent> {
    Long revertedEventId

    @Override
    String getAudit() { new JsonBuilder([revertedEvent: revertedEventId]).toString() }
}

@Entity
class PatientDeprecatedBy extends PatientEvent implements
        DeprecatedBy<Patient, Long, PatientEvent> {
    @OneToOne PatientDeprecates converse
    @OneToOne Patient deprecator

    Observable<PatientDeprecates> getConverseObservable() { just(converse) }
    Observable<Patient> getDeprecatorObservable() { just(deprecator) }

    @Override String getAudit() { new JsonBuilder([deprecatedBy: deprecator.id]).toString() }
}

@Entity
class PatientDeprecates extends PatientEvent implements Deprecates<Patient, Long, PatientEvent> {
    @OneToOne PatientDeprecatedBy converse
    @OneToOne Patient deprecated

    Observable<PatientDeprecatedBy> getConverseObservable() { just(converse) }
    Observable<Patient> getDeprecatedObservable() { just(deprecated) }

    @Override String getAudit() { new JsonBuilder([deprecates: deprecated.id]).toString() }
}
