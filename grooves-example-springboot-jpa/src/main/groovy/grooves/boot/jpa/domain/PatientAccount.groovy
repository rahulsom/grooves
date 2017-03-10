package grooves.boot.jpa.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.github.rahulsom.grooves.api.Snapshot
import groovy.transform.ToString
import org.springframework.format.annotation.DateTimeFormat

import javax.persistence.*

@Entity
@ToString(includeSuperProperties = true, includeNames = true, includePackage = false)
class PatientAccount implements Snapshot<Patient, Long> {

    @GeneratedValue @Id Long id

    @Column(nullable = false) Long lastEvent
    @Column(nullable = false) @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ") Date lastEventTimestamp

    @OneToOne Patient deprecatedBy
    @OneToMany @JoinTable(name = 'PatientAccountDeprecates') Set<Patient> deprecates
    @OneToOne Patient aggregate

    @Column(nullable = false) BigDecimal balance = 0.0
    @Column(nullable = false) BigDecimal moneyMade = 0.0
    @Column(nullable = false) String name

    int processingErrors = 0
}
