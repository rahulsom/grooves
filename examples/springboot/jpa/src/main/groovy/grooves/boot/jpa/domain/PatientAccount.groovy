package grooves.boot.jpa.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.rahulsom.grooves.api.snapshots.Snapshot
import groovy.transform.ToString
import rx.Observable

import javax.persistence.*

import static rx.Observable.*

/**
 * Domain Model for Account information of a Patient
 *
 * @author Rahul Somasunderam
 */
@Entity
@ToString(includeSuperProperties = true, includeNames = true, includePackage = false)
@SuppressWarnings(['DuplicateNumberLiteral'])
class PatientAccount implements Snapshot<Patient, Long, Long, PatientEvent> {

    @GeneratedValue @Id Long id

    @Column(nullable = false) Long lastEventPosition

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    @Column(nullable = false) Date lastEventTimestamp

    @OneToOne Patient deprecatedBy
    @OneToMany @JoinTable(name = 'PatientAccountDeprecates') Set<Patient> deprecates
    @OneToOne Patient aggregate

    @Column(nullable = false) BigDecimal balance = 0.0
    @Column(nullable = false) BigDecimal moneyMade = 0.0
    @Column(nullable = false) String name

    int processingErrors = 0

    @Override @JsonIgnore Observable<Patient> getAggregateObservable() {
        aggregate ? just(aggregate) : empty()
    }

    @Override @JsonIgnore Observable<Patient> getDeprecatedByObservable() {
        deprecatedBy ? just(deprecatedBy) : empty()
    }

    @Override @JsonIgnore Observable<Patient> getDeprecatesObservable() {
        from(deprecates.toList())
    }
}
