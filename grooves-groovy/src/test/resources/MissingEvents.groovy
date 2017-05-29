import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.groovy.transformations.Query
import com.github.rahulsom.grooves.queries.QuerySupport
import groovy.transform.CompileStatic
import rx.Observable

@CompileStatic @Query(aggregate = Account, snapshot = Balance)
class MissingEventsQuery implements QuerySupport<Long, Account, Long, Transaction, String, Balance> {
    @Override Balance createEmptySnapshot() { null }
    @Override Observable<Balance> getSnapshot(long maxPosition, Account aggregate) {
        Observable.empty()
    }
    @Override Observable<Balance> getSnapshot(Date maxTimestamp, Account aggregate) {
        Observable.empty()
    }
    @Override Observable<Transaction> getUncomputedEvents(Account aggregate, Balance lastSnapshot, long version) {
        Observable.empty()
    }
    @Override Observable<Transaction> getUncomputedEvents(Account aggregate, Balance lastSnapshot, Date snapshotTime) {
        Observable.empty()
    }
    @Override boolean shouldEventsBeApplied(Balance snapshot) { true }
    @Override Observable<Transaction> findEventsForAggregates(List<Account> aggregates) {
        Observable.empty()
    }
    @Override void addToDeprecates(Balance snapshot, Account deprecatedAggregate) {}

    @Override Observable<EventApplyOutcome> onException(Exception e, Balance snapshot, Transaction event) {
        null
    }
}

new MissingEventsQuery().computeSnapshot(new Account(), 0)
