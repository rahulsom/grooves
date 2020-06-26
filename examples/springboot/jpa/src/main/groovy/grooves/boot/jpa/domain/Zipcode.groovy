package grooves.boot.jpa.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.github.rahulsom.grooves.groovy.transformations.Aggregate
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@SuppressWarnings(['DuplicateStringLiteral'])
@Entity
@Table(uniqueConstraints = [
    @UniqueConstraint(name = 'UK_ZIPCODE_UNIQUEID', columnNames = ['uniqueId']),
])
@ToString(includeSuperProperties = true, includeNames = true, includePackage = false)
@JsonIgnoreProperties(['hibernateLazyInitializer', 'handler'])
@Aggregate
@EqualsAndHashCode(includes = ['uniqueId'])
class Zipcode {
    @GeneratedValue @Id Long id
    String uniqueId

    @Override
    String toString() { "Zipcode($id, $uniqueId)" }

}
