package grooves.example.push

import com.google.inject.Inject
import grooves.example.pushstyle.Tables
import grooves.example.pushstyle.tables.records.BalanceRecord
import org.jooq.DSLContext

class Database {

    @Inject lateinit var dslContext: DSLContext

    fun getBalance(account: Account, version: Long?): BalanceRecord? =
            dslContext
                    .select()
                    .from(Tables.BALANCE)
                    .where(Tables.BALANCE.B_ACCOUNT.eq(account.id))
                    .and(Tables.BALANCE.B_VERSION.le(version ?: Long.MAX_VALUE))
                    .orderBy(Tables.BALANCE.B_VERSION.desc())
                    .limit(1)
                    .fetchAny() as BalanceRecord?

}