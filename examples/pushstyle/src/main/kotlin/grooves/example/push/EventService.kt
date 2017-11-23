package grooves.example.push

import com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE
import com.github.rahulsom.grooves.queries.Grooves
import com.google.common.eventbus.Subscribe
import com.google.inject.Inject
import io.reactivex.Flowable
import io.reactivex.Flowable.empty
import io.reactivex.Flowable.just
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import org.jooq.DSLContext
import org.slf4j.LoggerFactory

class EventService {

    @Inject lateinit var database: Database
    @Inject lateinit var dslContext: DSLContext

    val log = LoggerFactory.getLogger(this.javaClass)

    // tag::documented[]
    val query =
            Grooves.versioned<String, Account, String, Transaction, String, Balance>()
                    .withSnapshot { version, account ->
                        log.info("getBalance($account, $version)")
                        Maybe.fromCallable { database.getBalance(account, version) }
                                .map { dbBalance -> Balance(dbBalance) }
                                .toFlowable()
                    }
                    .withEmptySnapshot { Balance() }
                    .withEvents { account, balance, version ->
                        val transaction =
                                ContextManager.get()?.get("transaction") as Transaction?
                        if (transaction != null)
                            just(transaction)
                        else
                            empty()
                    }
                    // .withApplyEvents { balance -> true }
                    .withDeprecator { balance, deprecatingAccount -> /* No op */ }
                    .withExceptionHandler { exception, balance, transaction ->
                        log.warn("$exception occurred")
                        just(CONTINUE)
                    }
                    .withEventHandler(this::updateBalance)
                    .build()

    private fun updateBalance(transaction: Transaction, balance: Balance) =
            when (transaction) {
                is Transaction.Deposit -> {
                    balance.balance += transaction.amount
                    just(CONTINUE)
                }
                is Transaction.Withdraw -> {
                    balance.balance -= transaction.amount
                    just(CONTINUE)
                }
            }
    // end::documented[]

    @Suppress("unused")
    @Subscribe
    fun onTransaction(transaction: Transaction) {
        log.info("Received $transaction")
        Flowable.just(mapOf("transaction" to transaction))
                .observeOn(ContextAwareScheduler)
                .doOnNext { ContextManager.set(it) }
                .flatMap { query.computeSnapshot(transaction.aggregate, transaction.position) }
                .observeOn(Schedulers.io())
                .blockingForEach { balance ->
                    log.info("Saving $balance")
                    dslContext.executeInsert(balance.toBalanceRecord())
                }
        log.info("End $transaction")
    }

}