package grooves.boot.jpa.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
@ToString(includeSuperProperties = true, includeNames = true, includePackage = false)
@EqualsAndHashCode(includes = ['code', 'date'])
class Procedure {
    @GeneratedValue @Id Long id
    String code
    Date date
}
