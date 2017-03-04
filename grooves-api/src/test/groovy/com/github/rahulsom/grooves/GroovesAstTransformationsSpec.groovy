package com.github.rahulsom.grooves

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import spock.lang.Specification

class GroovesAstTransformationsSpec extends Specification {

    def 'test valid events'() {
        when:
        def retval = new GroovyShell().evaluate(this.class.getResource('/valid/ValidES.groovy').text)

        then:
        notThrown(MultipleCompilationErrorsException)
        retval instanceof Optional
        (retval as Optional).present
    }
    def 'test missing events'() {
        when:
        def retval = new GroovyShell().evaluate(this.class.getResource('/missing/MissingEvents.groovy').text)

        then:
        def exception = thrown(MultipleCompilationErrorsException)
        exception.errorCollector.errorCount == 2
        exception.errorCollector.errors[0].class == SyntaxErrorMessage
        (exception.errorCollector.errors[0] as SyntaxErrorMessage).cause.message == '''
                |Missing expected method com.github.rahulsom.grooves.api.EventApplyOutcome applyCashDeposit(missing.CashDeposit event, missing.Balance snapshot)
                | @ line 30, column 16.'''.stripMargin().replaceAll('^\n', '')
        exception.errorCollector.errors[1].class == SyntaxErrorMessage
        retval == null

    }
}
