package grooves.boot.jpa.domain

import com.github.rahulsom.grooves.annotations.Event
import com.github.rahulsom.grooves.api.BaseEvent
import com.github.rahulsom.grooves.api.RevertEvent
import groovy.json.JsonBuilder
import groovy.transform.ToString

import javax.persistence.*

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "eventType")
abstract class PatientEvent implements BaseEvent<Patient, PatientEvent> {

    @GeneratedValue @Id Long id
    @Transient RevertEvent<Patient, PatientEvent> revertedBy
    @Column(nullable = false) String createdBy
    @Column(nullable = false) Date timestamp
    @Column(nullable = false) Long position
    @OneToOne Patient aggregate

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
class PatientEventReverted extends PatientEvent implements RevertEvent<Patient, PatientEvent> {
    Long revertedEventId

    @Override
    String getAudit() { new JsonBuilder([revertedEvent: revertedEventId]).toString() }
}