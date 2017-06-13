import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.groovy.transformations.Query
import com.github.rahulsom.grooves.queries.QuerySupport
import groovy.transform.CompileStatic
import rx.Observable

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE
import static rx.Observable.empty
import static rx.Observable.just

@CompileStatic @Query(aggregate = Account, snapshot = Balance)
class ValidESQuery implements QuerySupport<Long, Account, Long, Transaction, String, Balance,
        ValidESQuery> {
    @Override Balance createEmptySnapshot() { new Balance() }
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
    @Override Observable<Transaction> findEventsForAggregates(List<Account> aggregates) {
        empty()
    }
    @Override void addToDeprecates(Balance snapshot, Account deprecatedAggregate) {}

    @Override Observable<EventApplyOutcome> onException(Exception e, Balance snapshot, Transaction event) {
        just CONTINUE
    }

    Observable<EventApplyOutcome> applyCashDeposit(CashDeposit event, Balance snapshot) {
        just CONTINUE
    }
    Observable<EventApplyOutcome> applyCashWithdrawal(CashWithdrawal event, Balance snapshot) {
        just CONTINUE
    }
}

new ValidESQuery().computeSnapshot(new Account(), 0)