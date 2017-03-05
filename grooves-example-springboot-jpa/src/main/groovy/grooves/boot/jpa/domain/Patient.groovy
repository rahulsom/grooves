package grooves.boot.jpa.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.github.rahulsom.grooves.api.AggregateType
import groovy.transform.ToString

import javax.persistence.*

@Entity
@Table(uniqueConstraints = [
        @UniqueConstraint(name = 'UK_PATIENT_UNIQUEID', columnNames = ['uniqueId'])
])
@ToString(includeSuperProperties = true, includeNames = true, includePackage = false)
@JsonIgnoreProperties(["hibernateLazyInitializer", "handler"])
class Patient implements AggregateType<Long> {
    @GeneratedValue @Id Long id
    String uniqueId
}
