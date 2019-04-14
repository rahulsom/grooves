package com.github.rahulsom.grooves.asciidoctor

import spock.lang.Specification

import java.nio.file.Files

class SvgBuilderSpec extends Specification {
    void 'test SimpleEvents'() {
        given:
        def file = Files.createTempFile("SimpleEvents", "svg").toFile()
        when:
        new SvgBuilder('''\
            |Type,ID,Description
              + 1 2016-01-02 created as John Lennon
              + 2 2016-01-03 performed FLUSHOT for $ 32.40
              + 3 2016-01-04 performed GLUCOSETEST for $ 78.93
              + 4 2016-01-05 paid $ 100.25
            '''.stripIndent()).write(file)
        then:
        file.text != ''
    }
}
