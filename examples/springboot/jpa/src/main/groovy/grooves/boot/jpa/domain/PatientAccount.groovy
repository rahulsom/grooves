package grooves.boot.jpa.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.rahulsom.grooves.api.snapshots.Snapshot
import groovy.transform.ToString
import org.reactivestreams.Publisher

import javax.persistence.*

import static io.reactivex.Flowable.*

/**
 * Domain Model for Account information of a Patient
 *
 * @author Rahul Somasunderam
 */
@Entity
@ToString(includeSuperProperties = true, includeNames = true, includePackage = false)
@SuppressWarnings(['DuplicateNumberLiteral'])
// tag::documented[]
class PatientAccount implements Snapshot<Patient, Long, Long, PatientEvent> { // <1>

    @GeneratedValue @Id Long id

    @Column(nullable = false) long lastEventPosition // <2>

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    @Column(nullable = false) Date lastEventTimestamp // <3>

    @OneToOne Patient deprecatedBy
    @OneToMany @JoinTable(name = 'PatientAccountDeprecates') Set<Patient> deprecates
    @OneToOne Patient aggregate

    @Column(nullable = false) BigDecimal balance = 0.0
    @Column(nullable = false) BigDecimal moneyMade = 0.0
    @Column(nullable = false) String name

    int processingErrors = 0

    @Override @JsonIgnore Publisher<Patient> getAggregateObservable() { // <4>
        aggregate ? just(aggregate) : empty()
    }

    @Override @JsonIgnore Publisher<Patient> getDeprecatedByObservable() { // <5>
        deprecatedBy ? just(deprecatedBy) : empty()
    }

    @Override @JsonIgnore Publisher<Patient> getDeprecatesObservable() { // <6>
        fromIterable(deprecates.toList())
    }
}
// end::documented[]
