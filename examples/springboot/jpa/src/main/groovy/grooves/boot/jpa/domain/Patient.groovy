package grooves.boot.jpa.domain

// tag::documented[]
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.github.rahulsom.grooves.api.AggregateType
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.persistence.*

// end::documented[]
/**
 * Domain model for Patient
 *
 * @author Rahul Somasunderam
 */
@SuppressWarnings(['DuplicateStringLiteral'])
// tag::documented[]
@Entity
@Table(uniqueConstraints = [
        @UniqueConstraint(name = 'UK_PATIENT_UNIQUEID', columnNames = ['uniqueId']),
])
@ToString(includeSuperProperties = true, includeNames = true, includePackage = false)
@JsonIgnoreProperties(['hibernateLazyInitializer', 'handler'])
// end::documented[]
@EqualsAndHashCode(includes = ['uniqueId'])
// tag::documented[]
class Patient implements AggregateType<Long> { //<1>
    @GeneratedValue @Id Long id // <2>
    String uniqueId
}
// end::documented[]
