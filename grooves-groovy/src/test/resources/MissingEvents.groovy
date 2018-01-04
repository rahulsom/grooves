import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.groovy.transformations.Query
import com.github.rahulsom.grooves.queries.QuerySupport
import groovy.transform.CompileStatic
import org.reactivestreams.Publisher

import static rx.Observable.empty
import static rx.RxReactiveStreams.toPublisher

@CompileStatic @Query(aggregate = Account, snapshot = Balance)
class MissingEventsQuery implements QuerySupport<Account, Long, Transaction, String, Balance> {
    @Override Balance createEmptySnapshot() { null }
    @Override Publisher<Balance> getSnapshot(long maxPosition, Account aggregate) {
        toPublisher(empty())
    }
    @Override Publisher<Balance> getSnapshot(Date maxTimestamp, Account aggregate) {
        toPublisher(empty())
    }
    @Override Publisher<Transaction> getUncomputedEvents(Account aggregate, Balance lastSnapshot, long version) {
        toPublisher(empty())
    }
    @Override Publisher<Transaction> getUncomputedEvents(Account aggregate, Balance lastSnapshot, Date snapshotTime) {
        toPublisher(empty())
    }
    @Override boolean shouldEventsBeApplied(Balance snapshot) { true }

    @Override void addToDeprecates(Balance snapshot, Account deprecatedAggregate) {}

    @Override Publisher<EventApplyOutcome> onException(Exception e, Balance snapshot, Transaction event) {
        null
    }
}

new MissingEventsQuery().computeSnapshot(new Account(), 0)
