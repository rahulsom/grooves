package grooves.boot.jpa.domain

import com.fasterxml.jackson.annotation.JsonFormat
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

/**
 * Represents a Healthcare Procedure
 */
@Entity
@ToString(includeSuperProperties = true, includeNames = true, includePackage = false)
@EqualsAndHashCode(includes = ['code', 'date'])
class Procedure {
    @GeneratedValue @Id Long id
    String code
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ") Date date
}
