package valid;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.java.Query;
import com.github.rahulsom.grooves.queries.QuerySupport;
import domains.*;
import rx.Observable;

import java.util.Date;

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE;
import static rx.Observable.empty;
import static rx.Observable.just;

@Query(aggregate = Account.class, snapshot = Balance.class)
class ValidESQuery implements QuerySupport<Long, Account, Long, Transaction, String, Balance,
        ValidESQuery> {
    @Override
    public Balance createEmptySnapshot() {
        return new Balance();
    }

    @Override
    public Observable<Balance> getSnapshot(long maxPosition, Account aggregate) {
        return empty();
    }

    @Override
    public Observable<Balance> getSnapshot(Date maxTimestamp, Account aggregate) {
        return empty();
    }

    @Override
    public Observable<Transaction> getUncomputedEvents(Account aggregate, Balance lastSnapshot, long version) {
        return empty();
    }

    @Override
    public Observable<Transaction> getUncomputedEvents(Account aggregate, Balance lastSnapshot, Date snapshotTime) {
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
    public Observable<EventApplyOutcome> onException(Exception e, Balance snapshot, Transaction event) {
        return just(CONTINUE);
    }

    public Observable<EventApplyOutcome> applyCashDeposit(CashDeposit event, Balance snapshot) {
        return just(CONTINUE);
    }

    public Observable<EventApplyOutcome> applyCashWithdrawal(CashWithdrawal event, Balance snapshot) {
        return just(CONTINUE);
    }
}
