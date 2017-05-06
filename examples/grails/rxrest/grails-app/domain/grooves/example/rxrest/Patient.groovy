package grooves.example.rxrest

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.groovy.transformations.Aggregate
import grails.gorm.rx.rest.RxRestEntity
import groovy.transform.EqualsAndHashCode

/**
 * Represents a Patient
 */
@Aggregate
@EqualsAndHashCode(includes = ['uniqueId'])
class Patient implements RxRestEntity<Patient>, AggregateType<Long> {
    String uniqueId
    static constraints = {
    }

    @Override String toString() { "Patient($id, $uniqueId)" }
}
