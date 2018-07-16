package grooves.example.push

import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.api.snapshots.Snapshot
import grooves.example.pushstyle.tables.records.BalanceRecord
import io.reactivex.Flowable
import org.reactivestreams.Publisher
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

data class Account(val id: String)

sealed class Transaction(
    override val id: String,
    override var aggregate: Account?,
    override var timestamp: Date,
    override var position: Long
) :
    BaseEvent<Account, String, Transaction> {

    override fun getAggregateObservable(): Publisher<Account> =
        Flowable.fromIterable(listOf(aggregate).filter { it != null })

    override var revertedBy: RevertEvent<Account, String, Transaction>?
        get() = null
        set(value) {}

    data class Deposit(
        override val id: String,
        override var aggregate: Account?,
        override var timestamp: Date,
        override var position: Long,
        val atmId: String,
        val amount: Long
    ) :
        Transaction(id, aggregate, timestamp, position)

    data class Withdraw(
        override val id: String,
        override var aggregate: Account?,
        override var timestamp: Date,
        override var position: Long,
        val atmId: String,
        val amount: Long
    ) :
        Transaction(id, aggregate, timestamp, position)
}

class Balance() : Snapshot<Account, String, String, Transaction> {
    override var id: String? = UUID.randomUUID().toString()
    internal var aggregate: Account? = null
    override var lastEventTimestamp: Date? = Date()
    override var lastEventPosition: Long = 0
    var deprecates = mutableListOf<Account>()
    internal var deprecatedBy: Account? = null

    var balance: Long = 0

    constructor(
        id: String?,
        aggregate: Account?,
        balance: Long,
        lastEventPosition: Long,
        lastEventTimestamp: Date
    ) : this() {
        this.id = id
        this.aggregate = aggregate
        this.balance = balance
        this.lastEventPosition = lastEventPosition
        this.lastEventTimestamp = lastEventTimestamp
    }

    constructor(balance: BalanceRecord) :
            this(
                balance.bId, Account(balance.bAccount), balance.balance,
                balance.bVersion, balance.bTime
            )

    fun toBalanceRecord(): BalanceRecord {
        return BalanceRecord(
            id, lastEventPosition, Timestamp.from(lastEventTimestamp?.toInstant()),
            aggregate?.id, balance
        )
    }

    override fun getAggregateObservable(): Publisher<Account> =
        Flowable.fromIterable(listOf(aggregate).filter { it != null })

    override fun getDeprecatesObservable() =
        Flowable.fromIterable(deprecates)

    override fun getDeprecatedByObservable(): Publisher<Account> =
        Flowable.fromIterable(listOf(deprecatedBy).filter { it != null })

    override fun setAggregate(aggregate: Account) {
        this.aggregate = aggregate
    }

    override fun setDeprecatedBy(deprecatingAggregate: Account) {
        deprecatedBy = deprecatingAggregate
    }

    override fun toString(): String {
        val idPart = id?.substring(24)
        val aggPart = aggregate?.id
        val ts = lastEventTimestamp.let {
            SimpleDateFormat("HH:mm:ss,SSS").format(it)
        }
        return "Balance(id=$idPart, aggregate=$aggPart, lastEventTimestamp=$ts, " +
                "version=$lastEventPosition, balance=$balance)"
    }
}