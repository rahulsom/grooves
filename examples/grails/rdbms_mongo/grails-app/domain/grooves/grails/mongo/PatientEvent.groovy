package grooves.grails.mongo

import com.github.rahulsom.grooves.api.events.*
import com.github.rahulsom.grooves.groovy.transformations.Event
import groovy.json.JsonBuilder
import groovy.transform.EqualsAndHashCode
import rx.Observable

import static rx.Observable.just

/**
 * Represents a Patient Event
 *
 * @author Rahul Somasunderam
 */
@SuppressWarnings(['AbstractClassWithoutAbstractMethod', 'GrailsDomainReservedSqlKeywordName'])
@EqualsAndHashCode
// tag::abstract[]
abstract class PatientEvent implements BaseEvent<Long, Patient, Long, PatientEvent> { // <1>

    RevertEvent<Long, Patient, Long, PatientEvent> revertedBy  // <2>
    String createdBy
    Date timestamp  // <3>
    Long position  // <4>
    Patient aggregate
    Observable<Patient> getAggregateObservable() { just(aggregate) }  // <5>

    static transients = ['revertedBy'] // <6>

    static constraints = {
    }
    // end::abstract[]

    String getTs() { timestamp.format('yyyy-MM-dd') }

    @Override String toString() { "PatientEvent($id, $aggregateId, $ts)" }
// tag::abstract[]
}
// end::abstract[]

@EqualsAndHashCode
//tag::created[]
@Event(Patient) // <1>
class PatientCreated extends PatientEvent { // <2>
    String name

    @Override String getAudit() { new JsonBuilder([name: name]).toString() } // <3>
    //end::created[]
    @Override String toString() {
        "<${aggregateId}.$id> $ts created as ${name}" }
//tag::created[]
}
//end::created[]

@EqualsAndHashCode
class PatientAddedToZipcode extends PatientEvent implements
        JoinEvent<Long, Patient, Long, PatientEvent, Long, Zipcode> {
    Zipcode zipcode
    @Override Observable<Zipcode> getJoinAggregateObservable() { just zipcode }

    @Override String getAudit() { new JsonBuilder([zipcodeId: zipcode?.id]).toString() }
    @Override String toString() {
        "<${aggregateId}.$id> $ts sent to zipcode ${zipcode.uniqueId}" }

    static transients = ['joinAggregate']
}

@EqualsAndHashCode
class PatientRemovedFromZipcode extends PatientEvent implements
        DisjoinEvent<Long, Patient, Long, PatientEvent, Long, Zipcode> {
    Zipcode zipcode
    @Override Observable<Zipcode> getJoinAggregateObservable() { just zipcode }

    @Override String getAudit() { new JsonBuilder([zipcodeId: zipcode?.id]).toString() }
    @Override String toString() {
        "<${aggregateId}.$id> $ts removed from zipcode ${zipcode.uniqueId}" }

    static transients = ['joinAggregate']
}

@Event(Patient)
@EqualsAndHashCode
class ProcedurePerformed extends PatientEvent {
    String code
    BigDecimal cost

    @Override String getAudit() { new JsonBuilder([code: code, cost: cost]).toString() }
    @Override String toString() {
        "<${aggregateId}.$id> $ts performed $code for \$ $cost" }
}

@Event(Patient)
@EqualsAndHashCode
class PaymentMade extends PatientEvent {
    BigDecimal amount

    @Override String getAudit() { new JsonBuilder([amount: amount]).toString() }
    @Override String toString() { "<${aggregateId}.$id> $ts paid \$ $amount" }
}

//tag::reverted[]
@EqualsAndHashCode
class PatientEventReverted
        extends PatientEvent // <1>
        implements RevertEvent<Long, Patient, Long, PatientEvent> { // <2>
    Long revertedEventId // <3>

    @Override String getAudit() { new JsonBuilder([revertedEvent: revertedEventId]).toString() }
    //end::reverted[]
    @Override String toString() {
        "<${aggregateId}.$id> $ts reverted #$revertedEventId" }
    //tag::reverted[]
}
//end::reverted[]

@EqualsAndHashCode
class PatientDeprecatedBy extends PatientEvent implements
        DeprecatedBy<Long, Patient, Long, PatientEvent> {
    static hasOne = [
            converse: PatientDeprecates,
    ]
    Patient deprecator

    Observable<PatientDeprecates> getConverseObservable() { just(converse) }
    Observable<Patient> getDeprecatorObservable() { just(deprecator) }

    @Override String getAudit() { new JsonBuilder([deprecatedBy: deprecator.id]).toString() }
    @Override String toString() {
        "<${aggregateId}.$id> $ts deprecated by #${deprecator.id}" }
}

@EqualsAndHashCode
class PatientDeprecates extends PatientEvent implements
        Deprecates<Long, Patient, Long, PatientEvent> {
    static belongsTo = [
            converse: PatientDeprecatedBy,
    ]
    Patient deprecated

    Observable<PatientDeprecatedBy> getConverseObservable() { just(converse) }
    Observable<Patient> getDeprecatedObservable() { just(deprecated) }

    @Override String getAudit() { new JsonBuilder([deprecates: deprecated.id]).toString() }
    @Override String toString() {
        "<${aggregateId}.$id> $ts deprecates #${deprecated.id}" }
}
