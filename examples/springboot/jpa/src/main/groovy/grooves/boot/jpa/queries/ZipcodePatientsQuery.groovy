package grooves.boot.jpa.queries

import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.queries.JoinSupport
import grooves.boot.jpa.domain.*
import grooves.boot.jpa.repositories.ZipcodeEventRepository
import grooves.boot.jpa.repositories.ZipcodePatientsRepository
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import org.reactivestreams.Publisher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE
import static io.reactivex.Flowable.fromIterable
import static io.reactivex.Flowable.just

@Transactional
@Component
// tag::joins[]
class ZipcodePatientsQuery implements JoinSupport< // <1>
    Zipcode, // <2>
    Long, ZipcodeEvent, // <3>
    Patient, // <4>
    Long, ZipcodePatients, // <5>
    ZipcodeGotPatient, ZipcodeLostPatient // <6>
    > { // <7>

    final Class<ZipcodeGotPatient> joinEventClass = ZipcodeGotPatient // <8>
    final Class<ZipcodeLostPatient> disjoinEventClass = ZipcodeLostPatient // <9>
// end::joins[]
    @Autowired ZipcodeEventRepository zipcodeEventRepository
    @Autowired ZipcodePatientsRepository zipcodePatientsRepository

    @Override
    Publisher<ZipcodeEvent> getUncomputedEvents(
        @NotNull Zipcode aggregate, @Nullable ZipcodePatients lastSnapshot, @NotNull Date snapshotTime) {
        fromIterable(zipcodeEventRepository.getUncomputedEventsByDateRange(
            aggregate, lastSnapshot.lastEventTimestamp, snapshotTime))
    }

    @Override
    Publisher<ZipcodePatients> getSnapshot(@Nullable Date maxTimestamp, @NotNull Zipcode aggregate) {
        fromIterable(zipcodePatientsRepository.findAllByAggregateIdAndLastEventTimestampLessThan(
            aggregate.id, maxTimestamp))
    }

    @Override
    Publisher<ZipcodeEvent> getUncomputedEvents(
        @NotNull Zipcode aggregate, @Nullable ZipcodePatients lastSnapshot, long version) {
        fromIterable(zipcodeEventRepository.getUncomputedEventsByVersion(
            aggregate, lastSnapshot.lastEventPosition, version))
    }

    @Override
    Publisher<ZipcodePatients> getSnapshot(long maxPosition, @NotNull Zipcode aggregate) {
        fromIterable(zipcodePatientsRepository.findAllByAggregateIdAndLastEventPositionLessThan(
            aggregate.id, maxPosition))
    }

    @Override
    ZipcodePatients createEmptySnapshot() {
        new ZipcodePatients(deprecatesIds: '', joinedIds: '')
    }

    @Override
    boolean shouldEventsBeApplied(@NotNull ZipcodePatients snapshot) {
        true
    }

    @Override
    void addToDeprecates(@NotNull ZipcodePatients snapshot, @NotNull Zipcode deprecatedAggregate) {
        // ignore for now
    }

    @Override
    Publisher<EventApplyOutcome> onException(
        @NotNull Exception e, @NotNull ZipcodePatients snapshot, @NotNull ZipcodeEvent event) {
        just(CONTINUE)
    }

// tag::joins[]
    // Skipping familiar methods (10)
}
// end::joins[]
