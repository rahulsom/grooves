package com.github.rahulsom.grooves.asciidoctor

import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import static Constants.*

/**
 * Builds an SVG from a text representation of an event sourced aggregate.
 *
 * @author Rahul Somasunderam
 */
class SvgBuilder {

    private List<Aggregate> aggregates = []
    List<Event> allEvents = []
    Map<Date, Double> dates
    private long minInterval = 0

    SvgBuilder(String input) {
        Aggregate lastAggregate = null

        input.split('\n').each {
            if (it.startsWith('|')) {
                def parts = it.replaceFirst('\\|', '').split(',')
                lastAggregate = new Aggregate(*parts)
                lastAggregate.index = aggregates.size()
                aggregates << lastAggregate
            }
            if (it.startsWith('  - ') || it.startsWith('  + ')) {
                def m = it =~ / +([-+]) ([^ ]+) ([^ ]+) (.+)/
                def parts = m[0] as List<String>
                def (_, sign, id, date, description) = m[0]
                EventType type = computeEventType(description)
                def event = new Event(id, Date.parse('yyyy-MM-dd', date), description, type)
                if (sign == '-') {
                    event.reverted = true
                }
                lastAggregate.events << event
                allEvents << event
            }
        }

        def justDates = aggregates*.events*.date.flatten().sort().unique() as List<Date>
        minInterval = justDates.collate(2, 1, false).collect { a -> a[1].time - a[0].time }.min()
        dates = justDates.collectEntries { [it, (it.time - justDates[0].time) * 1.0 / minInterval] }

        double diff = 0.0
        double lastV = 0.0

        dates.each { k, v ->
            if (v - lastV - diff > 3.0) {
                diff += v - (lastV + 3.0)
            }
            v -= diff
            dates[k] = v
            lastV = v
        }
    }

    private EventType computeEventType(String description) {
        EventType type = EventType.Normal
        switch (description) {
            case ~/.*revert.*/: type = EventType.Revert; break
            case ~/.*deprecates.*/: type = EventType.Deprecates; break
            case ~/.*deprecated.*/: type = EventType.DeprecatedBy; break
            case ~/.*join.*/: type = EventType.Join; break
            case ~/.*disjoin.*/: type = EventType.Disjoin; break
        }
        type
    }

    void write(File file) {
        def m = new StreamingMarkupBuilder().bind { builder ->
            builder.svg(xmlns: "http://www.w3.org/2000/svg",
                    height: aggregates.size() * eventLineHeight,
                    width: dates.values().max() * eventSpace + 4 * aggregateWidth) {
                mkp.comment "Generated on ${new Date()} from\n${aggregates.join('\n')}\n"
                buildStyle(builder, CSS)
                builder.rect x: 0, y: 0,
                        width: dates.values().max() * eventSpace + 4 * aggregateWidth ,
                        height: aggregates.size() * eventLineHeight ,
                        class: 'background'
                defs {
                    marker(id: 'triangle', viewBox: '0 0 10 10', refX: 0, refY: 5,
                            markerWidth: 10, markerHeight: 10, orient: 'auto',
                            markerUnits: "userSpaceOnUse") {
                        path d: 'M 0 0 L 10 5 L 0 10 z'
                    }
                }
                aggregates.each { Aggregate aggregate ->
                    mkp.comment ' '
                    mkp.comment "|${aggregate.type},${aggregate.id},${aggregate.description}"
                    mkp.comment ' '
                    aggregate.buildSvg(builder, dates)
                    aggregate.events.each { Event event ->
                        event.buildSvg(builder, aggregate.index, this)
                    }
                }
            }
        }

        XmlUtil.serialize(m, file.newPrintWriter())
    }

    private void buildStyle(builder, String css) {
        builder.style { mkp.yieldUnescaped '/* <![CDATA[ */' + css + '/* ]]> */' }
    }
}
