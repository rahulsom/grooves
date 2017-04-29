package grooves.grails.rxmongo

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.groovy.transformations.Aggregate
import grails.gorm.rx.mongodb.RxMongoEntity
import groovy.transform.EqualsAndHashCode

/**
 * Represents a Patient
 */
@Aggregate
@EqualsAndHashCode(includes = ['uniqueId'])
class Patient implements RxMongoEntity, AggregateType<String> {
    String id
    String uniqueId
    static constraints = {
    }

    @Override String toString() { "Patient($id, $uniqueId)" }
}
