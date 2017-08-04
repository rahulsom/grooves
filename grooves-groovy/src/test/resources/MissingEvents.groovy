import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.groovy.transformations.Query
import com.github.rahulsom.grooves.queries.QuerySupport
import groovy.transform.CompileStatic
import rx.Observable

import static rx.Observable.empty

@CompileStatic @Query(aggregate = Account, snapshot = Balance)
class MissingEventsQuery implements QuerySupport<Long, Account, Long, Transaction, String, Balance,
        MissingEventsQuery> {
    @Override Balance createEmptySnapshot() { null }
    @Override Observable<Balance> getSnapshot(long maxPosition, Account aggregate) {
        empty()
    }
    @Override Observable<Balance> getSnapshot(Date maxTimestamp, Account aggregate) {
        empty()
    }
    @Override Observable<Transaction> getUncomputedEvents(Account aggregate, Balance lastSnapshot, long version) {
        empty()
    }
    @Override Observable<Transaction> getUncomputedEvents(Account aggregate, Balance lastSnapshot, Date snapshotTime) {
        empty()
    }
    @Override boolean shouldEventsBeApplied(Balance snapshot) { true }

    @Override void addToDeprecates(Balance snapshot, Account deprecatedAggregate) {}

    @Override Observable<EventApplyOutcome> onException(Exception e, Balance snapshot, Transaction event) {
        null
    }
}

new MissingEventsQuery().computeSnapshot(new Account(), 0)
