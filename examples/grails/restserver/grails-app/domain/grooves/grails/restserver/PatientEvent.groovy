package grooves.grails.restserver

import grails.rest.Resource
import restserver.BsonController

/**
 * Represents Patient Events
 *
 * @author Rahul Somasunderam
 */
@SuppressWarnings(['AbstractClassWithoutAbstractMethod', 'GrailsDomainReservedSqlKeywordName'])
@Resource(superClass = BsonController)
abstract class PatientEvent {

    String createdBy
    Date timestamp
    Long position
    Patient aggregate

    static constraints = {
    }
    @Override String toString() { "PatientEvent $id" }
}

class PatientCreated extends PatientEvent {
    String name

    @Override String toString() { "<$id> created" }
}

class ProcedurePerformed extends PatientEvent {
    String code
    BigDecimal cost

    @Override String toString() { "<$id> performed $code for $cost" }
}

class PaymentMade extends PatientEvent {
    BigDecimal amount

    @Override String toString() { "<$id> paid $amount" }
}

class PatientEventReverted extends PatientEvent {
    Long revertedEventId

    @Override String toString() { "<$id> reverted $revertedEventId" }
}

class PatientDeprecatedBy extends PatientEvent {
    PatientDeprecates converse
    Patient deprecator

    @Override String toString() { "<$id> deprecated by #${deprecator.id}" }
}

class PatientDeprecates extends PatientEvent {
    PatientDeprecatedBy converse
    Patient deprecated

    @Override String toString() { "<$id> deprecates #${deprecated.id}" }
}
