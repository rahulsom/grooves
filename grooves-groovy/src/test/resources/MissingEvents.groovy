import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.groovy.transformations.Query
import com.github.rahulsom.grooves.queries.QuerySupport
import groovy.transform.CompileStatic
import org.reactivestreams.Publisher

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE
import static io.reactivex.Flowable.empty
import static io.reactivex.Flowable.just

@CompileStatic @Query(aggregate = Account, snapshot = Balance)
class MissingEventsQuery implements QuerySupport<Long, Account, Long, Transaction, String, Balance,
        MissingEventsQuery> {
    @Override Balance createEmptySnapshot() { new Balance() }
    @Override Publisher<Balance> getSnapshot(long maxPosition, Account aggregate) {
        empty()
    }
    @Override Publisher<Balance> getSnapshot(Date maxTimestamp, Account aggregate) {
        empty()
    }
    @Override Publisher<Transaction> getUncomputedEvents(Account aggregate, Balance lastSnapshot, long version) {
        empty()
    }
    @Override Publisher<Transaction> getUncomputedEvents(Account aggregate, Balance lastSnapshot, Date snapshotTime) {
        empty()
    }
    @Override boolean shouldEventsBeApplied(Balance snapshot) { true }
    @Override void addToDeprecates(Balance snapshot, Account deprecatedAggregate) {}

    @Override Publisher<EventApplyOutcome> onException(Exception e, Balance snapshot, Transaction event) {
        just CONTINUE
    }
}

new MissingEventsQuery().computeSnapshot(new Account(), 0)
