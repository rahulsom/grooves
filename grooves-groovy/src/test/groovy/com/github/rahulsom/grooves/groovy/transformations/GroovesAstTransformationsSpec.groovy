package com.github.rahulsom.grooves.groovy.transformations

import io.reactivex.Observable
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.reactivestreams.Publisher
import spock.lang.Shared
import spock.lang.Specification

class GroovesAstTransformationsSpec extends Specification {

    def 'test valid events'() {
        when:
        def retval = new GroovyShell().evaluate(compose('/ValidES.groovy'))

        then:
        notThrown(MultipleCompilationErrorsException)
        retval instanceof Publisher
        Observable.fromPublisher(retval as Publisher).blockingFirst()
    }

    def 'test missing events'() {
        when:
        def retval = new GroovyShell().evaluate(compose('/MissingEvents.groovy'))

        then:
        def exception = thrown(MultipleCompilationErrorsException)
        exception.errorCollector.errorCount == 2

        when: 'I inspect the errors'
        def errors = exception.errorCollector.errors

        then:
        errors[0] instanceof SyntaxErrorMessage
        def message0 = (errors[0] as SyntaxErrorMessage).cause.message
        message0.matches(/Missing expected method .+Publisher<.+EventApplyOutcome> applyCashDeposit\(.+CashDeposit event, .+Balance snapshot\)\n.+/)

        errors[1].class == SyntaxErrorMessage
        def message1 = (errors[1] as SyntaxErrorMessage).cause.message
        message1.matches(/Missing expected method .+Publisher<.+EventApplyOutcome> applyCashWithdrawal\(.+CashWithdrawal event, .+Balance snapshot\)\n.+/)
        retval == null

    }

    @Shared private static int packageId = 0

    private String compose(String name) {
        "package test.p${packageId++};\n" +
                this.class.getResource('/domains.groovy').text +
                this.class.getResource(name).text
    }

}
