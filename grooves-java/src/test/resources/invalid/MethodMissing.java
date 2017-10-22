package invalid;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.java.Query;
import com.github.rahulsom.grooves.queries.QuerySupport;
import domains.*;
import org.reactivestreams.Publisher;
import io.reactivex.Flowable;

import java.util.Date;

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE;
import static io.reactivex.Flowable.empty;
import static io.reactivex.Flowable.just;

@Query(aggregate = Account.class, snapshot = Balance.class)
class MethodMissing implements QuerySupport<Long, Account, Long, Transaction, String, Balance,
        IncorrectReturnType> {
    @Override
    public Balance createEmptySnapshot() {
        return new Balance();
    }

    @Override
    public Publisher<Balance> getSnapshot(long maxPosition, Account aggregate) {
        return empty();
    }

    @Override
    public Publisher<Balance> getSnapshot(Date maxTimestamp, Account aggregate) {
        return empty();
    }

    @Override
    public Publisher<Transaction> getUncomputedEvents(Account aggregate, Balance lastSnapshot, long version) {
        return empty();
    }

    @Override
    public Publisher<Transaction> getUncomputedEvents(Account aggregate, Balance lastSnapshot, Date snapshotTime) {
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
    public Publisher<EventApplyOutcome> onException(Exception e, Balance snapshot, Transaction event) {
        return just(CONTINUE);
    }

    public Publisher<EventApplyOutcome> applyCashWithdrawal(CashWithdrawal event, Balance snapshot) {
        return just(CONTINUE);
    }
}
