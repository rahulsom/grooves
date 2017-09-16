package com.github.rahulsom.grooves.java;

import com.google.testing.compile.Compilation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static com.google.testing.compile.JavaFileObjects.forResource;

@RunWith(JUnit4.class)
public class QueryProcessorTest {
    private static final String EAC_CLASS = "com.github.rahulsom.grooves.api.EventApplyOutcome";
    private static JavaFileObject account = forResource("domains/Account.java");
    private static JavaFileObject transaction = forResource("domains/Transaction.java");
    private static JavaFileObject cashDeposit = forResource("domains/CashDeposit.java");
    private static JavaFileObject cashWithdrawal = forResource("domains/CashWithdrawal.java");
    private static JavaFileObject balance = forResource("domains/Balance.java");

    @Test
    public void testValid() throws Exception {
        JavaFileObject balanceQuery = forResource("valid/ValidESQuery.java");
        Compilation compilation =
                javac()
                        .withProcessors(new QueryProcessor())
                        .compile(account, transaction, cashDeposit, cashWithdrawal, balance,
                                balanceQuery);
        assertThat(compilation).succeededWithoutWarnings();
    }

    @Test
    public void testMethodMissing() throws Exception {
        JavaFileObject balanceQuery = forResource("invalid/MethodMissing.java");
        Compilation compilation =
                javac()
                        .withProcessors(new QueryProcessor())
                        .compile(account, transaction, cashDeposit, cashWithdrawal, balance,
                                balanceQuery);
        assertThat(compilation).failed();
        assertThat(compilation)
                .hadErrorContaining("Method not implemented\n    rx.Observable<" + EAC_CLASS
                        + "> applyCashDeposit(domains.CashDeposit,domains.Balance)")
                .inFile(balanceQuery)
                .onLine(15)
                .atColumn(1);
    }

    /*
    @Test
    public void testIncorrectReturnType() throws Exception {
        JavaFileObject balanceQuery = forResource("invalid/IncorrectReturnType.java");
        Compilation compilation =
                javac()
                        .withProcessors(new QueryProcessor())
                        .compile(account, transaction, cashDeposit, cashWithdrawal, balance,
                                balanceQuery);
        assertThat(compilation).failed();
        assertThat(compilation)
                .hadErrorContaining("incompatible types: no instance(s) of type variable(s) T "
                        + "exist so that rx.Observable<T> conforms to " + EAC_CLASS)
                .inFile(balanceQuery)
                .onLine(15)
                .atColumn(1);
    }
    */
}