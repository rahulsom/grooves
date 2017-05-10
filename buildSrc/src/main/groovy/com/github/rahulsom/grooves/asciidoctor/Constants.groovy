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

    static final String CSS = '''
        .background {
            fill: #FCFCFC;
            stroke-width: 0.2;
            stroke: grey;
        }
        .aggregate {
            fill: azure;
            stroke-width: 0.2;
            stroke: grey;
        }
        .aggregateText {
            font-family: Menlo, monospace;
            font-size: 14px;
        }
        .aggregateHeader {
            font-family: "Times New Roman", serif;
            font-size: 16px;
            font-style: italic;
        }

        .eventLine {
            stroke: grey;
            stroke-width: 0.2;
        }

        .eventCreated {
            fill: #FCFCFC;
            stroke-width: 0.4;
            stroke: #222;
        }
        .event circle {
            fill: azure;
            stroke-width: 0.2;
            stroke: grey;
        }
        .event.Revert circle {
            fill: #ffe5e5;
        }
        .event text {
            font-family: Menlo, monospace;
            font-size: 12px;
            stroke: grey;
            stroke-width: 0.1;
            fill: grey;
        }
        .event circle.reverted {
            fill: lightgrey;
        }
        
        .event.DeprecatedBy circle {
            fill: black;
        }
        .event.DeprecatedBy text {
            fill: white;
            stroke-width: 0;
            font-weight: 100;
        }
        
        .event.Deprecates circle {
            fill: #e6c300;
        }
        .event.Deprecates text {
            fill: white;
            font-weight: bold;
        }
        '''
}
