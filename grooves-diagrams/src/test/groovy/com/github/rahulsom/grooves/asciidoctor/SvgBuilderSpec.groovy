package com.github.rahulsom.grooves.asciidoctor

import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files

class SvgBuilderSpec extends Specification {
    @Unroll
    void 'test #name'() {
        given:
        def file = Files.createTempFile("SimpleEvents", "svg").toFile()

        when:
        new SvgBuilder(this.class.getResourceAsStream("/${name}.esdiag.txt")?.text).write(file)
        if (!new File("src/test/resources/${name}.esdiag.svg").exists()) {
            new File("src/test/resources/${name}.esdiag.svg").text = file.text
        }

        then:
        file.text == this.class.getResourceAsStream("/${name}.esdiag.svg")?.text

        where:
        name << [
                'SimpleEvents',
                'RevertEvent',
                'RevertEventEffective',
                'RevertOnRevert',
                'RevertOnRevertEffective',
                'MergeAggregates',
                'MergeAggregatesEffective',
                'RevertMergeBefore',
                'RevertMergeAfter',
        ]
    }
}
