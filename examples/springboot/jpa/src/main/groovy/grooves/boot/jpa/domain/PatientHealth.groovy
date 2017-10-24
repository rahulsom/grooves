package grooves.boot.jpa.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.rahulsom.grooves.api.snapshots.JavaSnapshot
import groovy.transform.ToString
import org.reactivestreams.Publisher

import javax.persistence.*

import static reactor.core.publisher.Flux.fromIterable
import static reactor.core.publisher.Mono.empty
import static reactor.core.publisher.Mono.just

/**
 * Domain Model for Patient Health
 *
 * @author Rahul Somasunderam
 */
@Entity
@ToString(includeSuperProperties = true, includeNames = true, includePackage = false)
class PatientHealth implements JavaSnapshot<Long, Patient, Long, Long, PatientEvent> {

    @GeneratedValue @Id Long id

    @Column(nullable = false) Long lastEventPosition

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    @Column(nullable = false) Date lastEventTimestamp

    @OneToOne Patient deprecatedBy
    @OneToMany @JoinTable(name = 'PatientHealthDeprecates') Set<Patient> deprecates
    @OneToOne Patient aggregate
    @OneToMany(cascade = CascadeType.ALL) List<Procedure> procedures
    String name

    int processingErrors = 0

    @Override @JsonIgnore Publisher<Patient> getAggregateObservable() {
        aggregate ? just(aggregate) : empty()
    }

    @Override @JsonIgnore Publisher<Patient> getDeprecatedByObservable() {
        deprecatedBy ? just(deprecatedBy) : empty()
    }

    @Override @JsonIgnore Publisher<Patient> getDeprecatesObservable() {
        fromIterable(deprecates.toList())
    }

}
