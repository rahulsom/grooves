package invalid;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.java.Query;
import com.github.rahulsom.grooves.queries.QuerySupport;
import domains.Account;
import domains.Balance;
import domains.CashWithdrawal;
import domains.Transaction;
import org.reactivestreams.Publisher;

import java.util.Date;

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE;
import static rx.Observable.empty;
import static rx.Observable.just;
import static rx.RxReactiveStreams.toPublisher;

@Query(aggregate = Account.class, snapshot = Balance.class)
class MethodMissing implements QuerySupport<Long, Account, Long, Transaction, String, Balance,
        IncorrectReturnType> {
    @Override
    public Balance createEmptySnapshot() {
        return new Balance();
    }

    @Override
    public Publisher<Balance> getSnapshot(long maxPosition, Account aggregate) {
        return toPublisher(empty());
    }

    @Override
    public Publisher<Balance> getSnapshot(Date maxTimestamp, Account aggregate) {
        return toPublisher(empty());
    }

    @Override
    public Publisher<Transaction> getUncomputedEvents(
            Account aggregate, Balance lastSnapshot, long version) {
        return empty();
    }

    @Override
    public Publisher<Transaction> getUncomputedEvents(
            Account aggregate, Balance lastSnapshot, Date snapshotTime) {
        return empty();
    }

    @Override
    public boolean shouldEventsBeApplied(Balance snapshot) {
        return true;
    }

    @Override
    public void addToDeprecates(Balance snapshot, Account deprecatedAggregate) {
    }

    @Override
    public Publisher<EventApplyOutcome> onException(
            Exception e, Balance snapshot, Transaction event) {
        return toPublisher(just(CONTINUE));
    }

    public Publisher<EventApplyOutcome> applyCashWithdrawal(
            CashWithdrawal event, Balance snapshot) {
        return toPublisher(just(CONTINUE));
    }
}
