package com.github.rahulsom.grooves.asciidoctor

/**
 * Constants used in computing dimensions while rendering Event Sourcing diagrams.
 *
 * @author Rahul Somasunderam
 */
object Constants {
    const val EVENT_LINE_HEIGHT: Int = 100
    const val AGGREGATE_HEIGHT: Int = 40
    const val AGGREGATE_WIDTH: Int = 100
    const val EVENT_SPACE: Int = 50
    const val OFFSET: Int = 45
    const val TEXT_LINE_HEIGHT: Int = 18

    val CSS: String by lazy {
        Constants::class.java
            .getResourceAsStream("/esdiag.css")
            .reader()
            .readText()
    }
}