package valid

import com.github.rahulsom.grooves.annotations.*
import com.github.rahulsom.grooves.api.*
import groovy.transform.CompileStatic

@CompileStatic @Aggregate class Account implements AggregateType<Long> {
    Long id
}

@CompileStatic abstract class Transaction implements BaseEvent<Account, Transaction> {
    Account aggregate
    RevertEvent<Account, Transaction> revertedBy
    Long id, position
    Date timestamp
    String createdBy, audit
}

@CompileStatic @Event(Account) class CashDeposit extends Transaction {}

@CompileStatic @Event(Account) class CashWithdrawal extends Transaction {}

@CompileStatic class Balance implements Snapshot<Account, String> {
    String id
    Long lastEvent
    Account aggregate, deprecatedBy
    Set<Account> deprecates
}


@CompileStatic @Query(aggregate = Account, snapshot = Balance)
class BalanceQuery implements QueryUtil<Account, Transaction, Balance> {
    @Override Balance createEmptySnapshot() { new Balance() }
    @Override Optional<Balance> getSnapshot(long startWithEvent, Account aggregate) { Optional.empty() }
    @Override void detachSnapshot(Balance retval) {}
    @Override List<Transaction> getUncomputedEvents(Account aggregate, Balance lastSnapshot, long lastEvent) { [] }
    @Override boolean shouldEventsBeApplied(Balance snapshot) { true }
    @Override List<Transaction> findEventsForAggregates(List<Account> aggregates) { [] }
    @Override void addToDeprecates(Balance snapshot, Account otherAggregate) {}
    @Override Transaction unwrapIfProxy(Transaction event) { event }
    @Override EventApplyOutcome onException(Exception e, Balance snapshot, Transaction event) {}

    EventApplyOutcome applyCashDeposit(CashDeposit event, Balance snapshot) { EventApplyOutcome.CONTINUE }
    EventApplyOutcome applyCashWithdrawal(CashWithdrawal event, Balance snapshot) { EventApplyOutcome.CONTINUE }
}

new BalanceQuery().computeSnapshot(new Account(), 0)
