package grooves.boot.jpa.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.github.rahulsom.grooves.api.snapshots.Snapshot
import groovy.transform.ToString

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinTable
import javax.persistence.OneToMany
import javax.persistence.OneToOne

@Entity
@ToString(includeSuperProperties = true, includeNames = true, includePackage = false)
class PatientHealth implements Snapshot<Patient, Long, Long, PatientEvent> {

    @GeneratedValue @Id Long id

    @Column(nullable = false) Long lastEventPosition
    @Column(nullable = false) @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ") Date lastEventTimestamp

    @OneToOne Patient deprecatedBy
    @OneToMany @JoinTable(name = "PatientHealthDeprecates") Set<Patient> deprecates
    @OneToOne Patient aggregate
    @OneToMany(cascade = CascadeType.ALL) List<Procedure> procedures
    String name

    int processingErrors = 0
}


