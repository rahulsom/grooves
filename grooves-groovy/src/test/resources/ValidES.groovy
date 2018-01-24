import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.groovy.transformations.Query
import com.github.rahulsom.grooves.queries.QuerySupport
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull
import org.reactivestreams.Publisher

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE
import static rx.Observable.empty
import static rx.Observable.just
import static rx.RxReactiveStreams.toPublisher

@CompileStatic @Query(aggregate = Account, snapshot = Balance)
class ValidESQuery implements QuerySupport<Account, Long, Transaction, String, Balance> {
    @NotNull @Override Balance createEmptySnapshot() { new Balance() }
    @NotNull @Override Publisher<Balance> getSnapshot(long maxPosition, Account aggregate) {
        toPublisher(empty())
    }
    @NotNull @Override Publisher<Balance> getSnapshot(@NotNull Date maxTimestamp, @NotNull Account aggregate) {
        toPublisher(empty())
    }
    @NotNull @Override Publisher<Transaction> getUncomputedEvents(@NotNull Account aggregate, @NotNull Balance lastSnapshot, long version) {
        toPublisher(empty())
    }
    @NotNull @Override Publisher<Transaction> getUncomputedEvents(@NotNull Account aggregate, @NotNull Balance lastSnapshot, @NotNull Date snapshotTime) {
        toPublisher(empty())
    }
    @NotNull @Override boolean shouldEventsBeApplied(@NotNull Balance snapshot) { true }
    @NotNull @Override void addToDeprecates(@NotNull Balance snapshot, @NotNull Account deprecatedAggregate) {}

    @NotNull @Override Publisher<EventApplyOutcome> onException(@NotNull Exception e, @NotNull Balance snapshot, @NotNull Transaction event) {
        toPublisher(just(CONTINUE))
    }

    @NotNull Publisher<EventApplyOutcome> applyCashDeposit(CashDeposit event, Balance snapshot) {
        toPublisher(just(CONTINUE))
    }
    @NotNull Publisher<EventApplyOutcome> applyCashWithdrawal(CashWithdrawal event, Balance snapshot) {
        toPublisher(just(CONTINUE))
    }
}

new ValidESQuery().computeSnapshot(new Account(), 0)