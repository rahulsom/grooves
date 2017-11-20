package grooves.example.push

import com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE
import com.github.rahulsom.grooves.queries.Grooves
import com.google.common.eventbus.Subscribe
import com.google.inject.Inject
import grooves.example.pushstyle.tables.records.BalanceRecord
import io.reactivex.Flowable
import io.reactivex.Flowable.just
import io.reactivex.Maybe
import org.jooq.DSLContext
import org.omg.CORBA.Object
import org.slf4j.LoggerFactory
import java.sql.Timestamp

class EventService {

    @Inject lateinit var database: Database
    @Inject lateinit var dslContext: DSLContext

    val log = LoggerFactory.getLogger(this::class.java)

    val query =
            Grooves.versioned<String, Account, String, Transaction, String, Balance>()
                    .withSnapshot { version, account ->
                        Maybe.fromCallable { database.getBalance(account, version) }
                                .map { dbBalance ->
                                    Balance(
                                            dbBalance.bId, Account(dbBalance.bAccount),
                                            dbBalance.balance, dbBalance.bVersion, dbBalance.bTime
                                    )
                                }
                                .toFlowable()
                    }
                    .withEmptySnapshot { Balance() }
                    .withEvents { transaction, balance, date ->
                        val t = (ContextManager.get() as Map<String, Transaction>).get("transaction")
                        Flowable.just(t)
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

    @Suppress("unused")
    @Subscribe
    fun onTransaction(newTransaction: Transaction) {

        val context = mapOf("transaction" to newTransaction)

        ContextManager.set(context)

        val snapshotPublisher =
                query.computeSnapshot(newTransaction.aggregate, newTransaction.position)

        Flowable.fromPublisher(snapshotPublisher)
                .subscribe { balance ->
                    dslContext.executeInsert(
                            BalanceRecord(
                                    balance.id,
                                    balance.lastEventPosition,
                                    Timestamp.from(balance.lastEventTimestamp?.toInstant()),
                                    balance.aggregate?.id,
                                    balance.balance
                            )
                    )
                }
    }

}