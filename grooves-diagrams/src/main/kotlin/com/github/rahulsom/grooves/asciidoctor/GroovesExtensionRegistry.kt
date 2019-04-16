package com.github.rahulsom.grooves.asciidoctor

import org.asciidoctor.Asciidoctor
import org.asciidoctor.extension.spi.ExtensionRegistry

/**
 * Registers [EventsBlock] as `esdiag`
 *
 * @author Rahul Somasunderam
 */
class GroovesExtensionRegistry : ExtensionRegistry {
    override fun register(asciidoctor: Asciidoctor) {
        asciidoctor.javaExtensionRegistry().block("esdiag", EventsBlock::class.java)
    }
}