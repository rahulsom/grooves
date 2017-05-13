package com.github.rahulsom.grooves.asciidoctor

/**
 * Constants used in computing dimensions while rendering Event Sourcing diagrams.
 *
 * @author Rahul Somasunderam
 */
class Constants {
    static final int eventLineHeight = 100
    static final int aggregateHeight = 40
    static final int aggregateWidth = 100
    static final int eventSpace = 50
    static final int offset = 45
    static final int textLineHeight = 18

    static final String LESS = Constants.class.getResourceAsStream('/esdiag.less').text
}
