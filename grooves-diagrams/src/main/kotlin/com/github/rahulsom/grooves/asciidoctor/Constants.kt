package com.github.rahulsom.grooves.asciidoctor

/**
 * Constants used in computing dimensions while rendering Event Sourcing diagrams.
 *
 * @author Rahul Somasunderam
 */
object Constants {
    const val eventLineHeight: Int = 100
    const val aggregateHeight: Int = 40
    const val aggregateWidth: Int = 100
    const val eventSpace: Int = 50
    const val offset: Int = 45
    const val textLineHeight: Int = 18

    val CSS: String by lazy {
        Constants::class.java.getResourceAsStream("/esdiag.css").reader().readText()
    }
}