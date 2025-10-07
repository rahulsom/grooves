package grooves.boot.jpa.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.rahulsom.grooves.api.snapshots.Join
import grooves.boot.jpa.util.RepositoryProvider
import groovy.transform.EqualsAndHashCode
import groovy.transform.Immutable
import org.reactivestreams.Publisher

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

import static io.reactivex.Flowable.*

/**
 * Represents a join between Zipcode and Patient
 *
 * @author Rahul Somasunderam
 */
@Entity
@EqualsAndHashCode(includes = ['aggregateId', 'lastEventPosition',])
class ZipcodePatients implements Join<Zipcode, Long, Patient, Long, ZipcodeEvent> {
    @GeneratedValue @Id Long id
    long lastEventPosition
    Date lastEventTimestamp
    String processingErrors = ''

    Long aggregateId

    Zipcode getAggregate() {
        RepositoryProvider.zipcodeRepository.getReferenceById(aggregateId)
    }

    @Override
    @JsonIgnore
    Publisher<Zipcode> getAggregateObservable() {
        aggregateId ? defer { just aggregate } : empty()
    }

    void setAggregate(Zipcode aggregate) {
        this.aggregateId = aggregate.id
    }

    @Override
    @JsonIgnore
    Publisher<Zipcode> getDeprecatedByObservable() {
        deprecatedBy ? just(deprecatedBy) : empty()
    }
    Long deprecatedById

    Zipcode getDeprecatedBy() {
        deprecatedById ?
                RepositoryProvider.zipcodeRepository.getReferenceById(deprecatedById) : null
    }

    void setDeprecatedBy(Zipcode aggregate) {
        deprecatedById = aggregate.id
    }

    @Override
    @JsonIgnore
    Publisher<Zipcode> getDeprecatesObservable() {
        fromIterable(deprecates)
    }
    String deprecatesIds

    Set<Zipcode> getDeprecates() {
        deprecatesIds.split(',').findAll { it }
        .collect { RepositoryProvider.zipcodeRepository.getReferenceById(it.toLong()) }.toSet()
    }

    void setDeprecates(Set<Zipcode> deprecates) {
        deprecatesIds = deprecates*.id.join(',')
    }

    String joinedIds

    @Override
    String toString() {
        "ZipcodePatients{id=$id, lastEvent=($lastEventPosition, $lastEventTimestamp), " +
                "aggregateId=$aggregateId}"
    }

    void addJoinedAggregate(Patient patient) {
        List<String> j = joinedIds.split(',')
        j.add(patient.id.toString())
        joinedIds = j.findAll { it }.join(',')
    }

    void removeJoinedAggregate(Patient patient) {
        List<String> j = joinedIds.split(',')
        j.remove(patient.id.toString())
        joinedIds = j.join(',')
    }

    Representation toJson() {
        new Representation(
                id,
                aggregateId,
                joinedIds.split(',').findAll { it != '' }.toList()*.toInteger() as Set,
                lastEventPosition
                )
    }

    @Immutable
    static class Representation {
        Long id
        Long aggregateId
        Set<Integer> joinedIds
        Long lastEventPosition
    }
}
