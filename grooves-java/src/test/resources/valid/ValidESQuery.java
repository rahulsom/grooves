package valid;

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE;
import static rx.Observable.empty;
import static rx.Observable.just;
import static rx.RxReactiveStreams.toPublisher;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.java.Query;
import com.github.rahulsom.grooves.queries.QuerySupport;
import domains.*;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

@Query(aggregate = Account.class, snapshot = Balance.class)
class ValidESQuery implements QuerySupport<Account, Long, Transaction, String, Balance> {
    @NotNull
    @Override
    public Balance createEmptySnapshot() {
        return new Balance();
    }

    @NotNull
    @Override
    public Publisher<Balance> getSnapshot(long maxPosition, @NotNull Account aggregate) {
        return toPublisher(empty());
    }

    @NotNull
    @Override
    public Publisher<Balance> getSnapshot(Date maxTimestamp, @NotNull Account aggregate) {
        return toPublisher(empty());
    }

    @NotNull
    @Override
    public Publisher<Transaction> getUncomputedEvents(@NotNull Account aggregate, Balance lastSnapshot, long version) {
        return toPublisher(empty());
    }

    @NotNull
    @Override
    public Publisher<Transaction> getUncomputedEvents(
            @NotNull Account aggregate, Balance lastSnapshot, @NotNull Date snapshotTime) {
        return toPublisher(empty());
    }

    @NotNull
    @Override
    public boolean shouldEventsBeApplied(Balance snapshot) {
        return true;
    }

    @NotNull
    @Override
    public void addToDeprecates(@NotNull Balance snapshot, Account deprecatedAggregate) {}

    @NotNull
    @Override
    public Publisher<EventApplyOutcome> onException(Exception e, Balance snapshot, Transaction event) {
        return toPublisher(just(CONTINUE));
    }

    public Publisher<EventApplyOutcome> applyCashDeposit(CashDeposit event, Balance snapshot) {
        return toPublisher(just(CONTINUE));
    }

    public Publisher<EventApplyOutcome> applyCashWithdrawal(CashWithdrawal event, Balance snapshot) {
        return toPublisher(just(CONTINUE));
    }
}
