package grooves.example.pushstyle

import com.google.inject.Guice
import grooves.example.push.Application
import grooves.example.push.BankingModule
import org.junit.Assert
import org.junit.Test

class PushTest {
    val injector = Guice.createInjector(BankingModule)

    val application = injector.getInstance(Application::class.java)

    @Test
    fun testMissingAggregate() {
        val accountId = "A0000"

        Assert.assertEquals(null, application.getBalance(accountId))
    }

    @Test
    fun testTwoTransactionsOnOneAccount() {
        val accountId = "A0001"

        application.deposit(accountId, 1, "MAINST", 100)
        application.withdraw(accountId, 2, "MAINST", 50)

        Thread.sleep(100)

        Assert.assertEquals(50L, application.getBalance(accountId))
    }

    @Test
    fun testMultipleAccounts() {
        val firstAccount = "A0002"
        val secondAccount = "A0003"

        application.deposit(firstAccount, 1, "MAINST", 100)
        application.withdraw(firstAccount, 2, "MAINST", 50)

        Thread.sleep(100)

        Assert.assertEquals(50L, application.getBalance(firstAccount))
        Assert.assertEquals(null, application.getBalance(secondAccount))
    }
}