package com.github.rahulsom.grooves.asciidoctor

/**
 * Constants used in computing dimensions while rendering Event Sourcing diagrams.
 *
 * @author Rahul Somasunderam
 */
object Constants {
    val eventLineHeight: Int = 100
    val aggregateHeight: Int = 40
    val aggregateWidth: Int = 100
    val eventSpace: Int = 50
    val offset: Int = 45
    val textLineHeight: Int = 18

    val CSS: String = Constants::class.java.getResourceAsStream("/esdiag.css").reader().readText()
}