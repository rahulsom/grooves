package grooves.example.push

import com.github.rahulsom.grooves.api.snapshots.Snapshot
import grooves.example.pushstyle.tables.records.BalanceRecord
import io.reactivex.Flowable
import org.reactivestreams.Publisher
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*

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
            balance.bId,
            Account(balance.bAccount),
            balance.balance,
            balance.bVersion,
            Date.from(balance.bTime.toInstant())
        )

    fun toBalanceRecord(): BalanceRecord {
        return BalanceRecord(
            id,
            lastEventPosition,
            OffsetDateTime.ofInstant(lastEventTimestamp?.toInstant(), ZoneId.systemDefault()),
            aggregate?.id,
            balance
        )
    }

    override fun getAggregateObservable(): Publisher<Account> =
        Flowable.fromIterable(listOfNotNull(aggregate))

    override fun getDeprecatesObservable() =
        Flowable.fromIterable(deprecates)

    override fun getDeprecatedByObservable(): Publisher<Account> =
        Flowable.fromIterable(listOfNotNull(deprecatedBy))

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