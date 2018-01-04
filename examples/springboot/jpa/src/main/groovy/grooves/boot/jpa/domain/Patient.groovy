package grooves.boot.jpa.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.github.rahulsom.grooves.groovy.transformations.Aggregate

// tag::documented[]
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
@Aggregate // <1>
// end::documented[]
@EqualsAndHashCode(includes = ['uniqueId'])
// tag::documented[]
class Patient { //<2>
    @GeneratedValue @Id Long id // <3>
    String uniqueId
}
// end::documented[]
