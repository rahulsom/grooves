package com.github.rahulsom.grooves.asciidoctor

import groovy.transform.CompileStatic
import org.asciidoctor.Asciidoctor
import org.asciidoctor.extension.spi.ExtensionRegistry
import org.kordamp.jipsy.ServiceProviderFor

/**
 * Registers {@link EventsBlock} as <code>esdiag</code>
 *
 * @author Rahul Somasunderam
 */
@CompileStatic
@ServiceProviderFor(ExtensionRegistry)
class GroovesExtensionRegistry implements ExtensionRegistry {
    void register(Asciidoctor asciidoctor) {
        asciidoctor.javaExtensionRegistry().block 'esdiag', EventsBlock
    }
}