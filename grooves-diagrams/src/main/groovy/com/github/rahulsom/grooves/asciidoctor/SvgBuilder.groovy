package com.github.rahulsom.grooves.asciidoctor

import com.github.rahulsom.svg.*
import com.github.sommeri.less4j.core.ThreadUnsafeLessCompiler
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import javax.xml.bind.JAXBContext

import static com.github.rahulsom.grooves.asciidoctor.Constants.*
import static java.lang.Boolean.TRUE
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT

/**
 * Builds an SVG from a text representation of an event sourced aggregate.
 *
 * @author Rahul Somasunderam
 */
@NewifySvg
@CompileStatic
class SvgBuilder {

    private List<Aggregate> aggregates = []
    List<Event> allEvents = []
    Map<Date, Double> dates
    private long minInterval = 0

    SvgBuilder(String input) {
        Aggregate lastAggregate = null

        input.split('\n').each { String it ->
            if (it.startsWith('|')) {
                lastAggregate = toAggregate(it)
                lastAggregate.index = aggregates.size()
                aggregates << lastAggregate
            }
            if (it.startsWith('  - ') || it.startsWith('  + ')) {
                Event event = toEvent(it)
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

    @CompileDynamic
    private Event toEvent(String it) {
        def m = it =~ / +([-+]) ([^ ]+) ([^ ]+) (.+)/
        def (_, sign, id, date, description) = m[0]
        EventType type = computeEventType(description)
        def event = new Event(id, Date.parse('yyyy-MM-dd', date), description, type)
        if (sign == '-') {
            event.reverted = true
        }
        event
    }

    @CompileDynamic
    private Aggregate toAggregate(String it) {
        Aggregate lastAggregate
        def parts = it.replaceFirst('\\|', '').split(',')
        lastAggregate = new Aggregate(*parts)
        lastAggregate
    }

    @CompileStatic
    private EventType computeEventType(String description) {
        switch (description) {
            case ~/.*revert.*/: return EventType.Revert
            case ~/.*deprecates.*/: return EventType.Deprecates
            case ~/.*deprecated.*/: return EventType.DeprecatedBy
            case ~/.*disjoin.*/: return EventType.Disjoin
            case ~/.*join.*/: return EventType.Join
            default: return EventType.Normal
        }
    }

    void write(File file) {

        def svg = new Svg(height: "${aggregates.size() * eventLineHeight}",
                width: "${dates.values().max() * eventSpace + 4 * aggregateWidth}").
                content { Context svgBody ->

                    def css = new ThreadUnsafeLessCompiler().compile(LESS).css

                    svgBody << new ObjectFactory().createStyle(new SVGStyleClass(
                            content: '/* <![CDATA[ */' + css + '/* ]]> */'))

                    svgBody << new Rect(x: '0', y: '0',
                            height: "${aggregates.size() * eventLineHeight}",
                            width: "${dates.values().max() * eventSpace + 4 * aggregateWidth}",
                            clazz: 'background')

                    svgBody << new Defs().content {
                        it << new SVGMarkerClass(id: 'triangle', viewBox: '0 0 10 10',
                                refX: '0', refY: '5', markerWidth: '10', markerHeight: '10',
                                orient: 'auto', markerUnits: 'userSpaceOnUse').content {
                            it << new Path(d: 'M 0 0 L 10 5 L 0 10 z')
                        }
                    }

                    aggregates.each { Aggregate aggregate ->
                        svgBody << aggregate.buildSvg(dates)
                        aggregate.events.each { Event event ->
                            svgBody << event.buildSvg(aggregate.index, this)
                        }
                    }
                }

        def jaxbContext = JAXBContext.newInstance(Svg)
        def marshaller = jaxbContext.createMarshaller()
        marshaller.setProperty JAXB_FORMATTED_OUTPUT, TRUE
        marshaller.marshal svg, file.newPrintWriter()

    }

}
