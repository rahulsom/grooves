package com.github.rahulsom.grooves.asciidoctor

import groovy.transform.CompileStatic
import org.asciidoctor.Asciidoctor
import org.asciidoctor.extension.spi.ExtensionRegistry

/**
 * Registers {@link EventsBlock} as <code>esdiag</code>
 *
 * @author Rahul Somasunderam
 */
@CompileStatic
class GroovesExtensionRegistry implements ExtensionRegistry {
    void register(Asciidoctor asciidoctor) {
        asciidoctor.javaExtensionRegistry().block 'esdiag', EventsBlock
    }
}