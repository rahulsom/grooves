import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.groovy.transformations.Query
import com.github.rahulsom.grooves.queries.QuerySupport
import groovy.transform.CompileStatic
import org.reactivestreams.Publisher

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE
import static io.reactivex.Flowable.*

@CompileStatic @Query(aggregate = Account, snapshot = Balance)
class ValidESQuery implements QuerySupport<Long, Account, Long, Transaction, String, Balance,
        ValidESQuery> {
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

    Publisher<EventApplyOutcome> applyCashDeposit(CashDeposit event, Balance snapshot) {
        just CONTINUE
    }
    Publisher<EventApplyOutcome> applyCashWithdrawal(CashWithdrawal event, Balance snapshot) {
        just CONTINUE
    }
}

new ValidESQuery().computeSnapshot(new Account(), 0)