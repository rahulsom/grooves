import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.groovy.transformations.Query
import com.github.rahulsom.grooves.queries.QuerySupport
import groovy.transform.CompileStatic
import org.reactivestreams.Publisher

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE
import static rx.Observable.empty
import static rx.Observable.just
import static rx.RxReactiveStreams.toPublisher

@CompileStatic @Query(aggregate = Account, snapshot = Balance)
class ValidESQuery implements QuerySupport<Account, Long, Transaction, String, Balance,
        ValidESQuery> {
    @Override Balance createEmptySnapshot() { new Balance() }
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
        toPublisher(just(CONTINUE))
    }

    Publisher<EventApplyOutcome> applyCashDeposit(CashDeposit event, Balance snapshot) {
        toPublisher(just(CONTINUE))
    }
    Publisher<EventApplyOutcome> applyCashWithdrawal(CashWithdrawal event, Balance snapshot) {
        toPublisher(just(CONTINUE))
    }
}

new ValidESQuery().computeSnapshot(new Account(), 0)