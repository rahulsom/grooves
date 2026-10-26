package com.github.rahulsom.grooves.groovy.transformations

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.junit.jupiter.api.Test
import org.reactivestreams.Publisher

import static org.junit.jupiter.api.Assertions.*
import static rx.RxReactiveStreams.toObservable

class GroovesAstTransformationsTest {

    @Test
    void 'test valid events'() {
        def retval = new GroovyShell().evaluate(compose('/ValidES.groovy'))

        assertInstanceOf(Publisher, retval)
        toObservable(retval as Publisher).toBlocking().first()
    }

    @Test
    void 'test missing events'() {
        def exception = assertThrows(MultipleCompilationErrorsException) {
            new GroovyShell().evaluate(compose('/MissingEvents.groovy'))
        }

        assert exception.errorCollector.errorCount == 2

        def errors = exception.errorCollector.errors

        assert errors[0] instanceof SyntaxErrorMessage
        def message0 = (errors[0] as SyntaxErrorMessage).cause.message
        def expected0 = /Missing expected method .+Publisher<.+EventApplyOutcome>/ +
                / applyCashDeposit\(.+CashDeposit event, .+Balance snapshot\)\n.+/
        assert message0.matches(expected0)

        assert errors[1].class == SyntaxErrorMessage
        def message1 = (errors[1] as SyntaxErrorMessage).cause.message
        def expected1 = /Missing expected method .+Publisher<.+EventApplyOutcome>/ +
                / applyCashWithdrawal\(.+CashWithdrawal event, .+Balance snapshot\)\n.+/
        assert message1.matches(expected1)
    }

    private static int packageId = 0

    private String compose(String name) {
        "package test.p${packageId++};\n" +
                this.class.getResource('/domains.groovy').text +
                this.class.getResource(name).text
    }
}
