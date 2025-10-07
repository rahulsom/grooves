package invalid;

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE;
import static rx.Observable.empty;
import static rx.Observable.just;
import static rx.RxReactiveStreams.toPublisher;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.java.Query;
import com.github.rahulsom.grooves.queries.QuerySupport;
import domains.Account;
import domains.Balance;
import domains.CashDeposit;
import domains.CashWithdrawal;
import domains.Transaction;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

@Query(aggregate = Account.class, snapshot = Balance.class)
class IncorrectReturnType implements QuerySupport<Account, Long, Transaction, String, Balance> {
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
        return empty();
    }

    @NotNull
    @Override
    public Publisher<Transaction> getUncomputedEvents(
            @NotNull Account aggregate, Balance lastSnapshot, @NotNull Date snapshotTime) {
        return empty();
    }

    @NotNull
    @Override
    public boolean shouldEventsBeApplied(@NotNull Balance snapshot) {
        return true;
    }

    @NotNull
    @Override
    public void addToDeprecates(@NotNull Balance snapshot, @NotNull Account deprecatedAggregate) {}

    @NotNull
    @Override
    public Publisher<EventApplyOutcome> onException(
            @NotNull Exception e, @NotNull Balance snapshot, @NotNull Transaction event) {
        return toPublisher(just(CONTINUE));
    }

    public Publisher<EventApplyOutcome> applyCashDeposit(CashDeposit event, Balance snapshot) {
        return toPublisher(just(CONTINUE));
    }

    public EventApplyOutcome applyCashWithdrawal(CashWithdrawal event, Balance snapshot) {
        return CONTINUE;
    }
}
