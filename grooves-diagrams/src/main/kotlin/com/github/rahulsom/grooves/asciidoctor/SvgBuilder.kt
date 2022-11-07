package com.github.rahulsom.grooves.asciidoctor

import com.github.rahulsom.grooves.asciidoctor.Constants.CSS
import com.github.rahulsom.grooves.asciidoctor.Constants.aggregateWidth
import com.github.rahulsom.grooves.asciidoctor.Constants.eventLineHeight
import com.github.rahulsom.grooves.asciidoctor.Constants.eventSpace
import com.github.rahulsom.svg.ObjectFactory
import com.github.rahulsom.svg.Path
import com.github.rahulsom.svg.Rect
import com.github.rahulsom.svg.SVGMarkerClass
import com.github.rahulsom.svg.SVGStyleClass
import com.github.rahulsom.svg.Svg
import java.io.File
import java.lang.Boolean.TRUE
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT

/**
 * Builds an SVG from a text representation of an event sourced aggregate.
 *
 * @author Rahul Somasunderam
 */
class SvgBuilder(private val input: String) {
    private fun init() {
        var lastAggregate: Aggregate? = null
        input.split("\n")
            .forEach {
                if (it.startsWith("|")) {
                    lastAggregate = toAggregate(it)
                    lastAggregate!!.index = aggregates.size
                    aggregates.add(lastAggregate!!)
                }
                if (it.startsWith("  - ") || it.startsWith("  + ")) {
                    val event = toEvent(it)
                    lastAggregate!!.events.add(event)
                    allEvents.add(event)
                }
            }

        val justDates = aggregates.flatMap { it.events }.map { it.date }.sorted().distinct()
        minInterval = justDates.windowed(2, 1, false).map { a -> a[1].time - a[0].time }.minOrNull()!!
        justDates.forEach {
            dates[it] = (it.time - justDates[0].time) * 1.0 / minInterval
        }

        var diff = 0.0
        var lastV = 0.0

        dates.forEach { (k, v1) ->
            var v = v1
            if (v - lastV - diff > 3.0) {
                diff += v - (lastV + 3.0)
            }
            v -= diff
            dates[k] = v
            lastV = v
        }
    }

    private val aggregates: MutableList<Aggregate> = mutableListOf()
    var allEvents: MutableList<Event> = mutableListOf()
    var dates: MutableMap<Date, Double> = mutableMapOf()
    private val counter = AtomicInteger()
    private var minInterval: Long = 0

    private fun toEvent(it: String): Event {
        val m = Regex(" +([-+]) ([^ ]+) ([^ ]+) (.+)").matchEntire(it)!!
        val (sign, id, date, description) = m.destructured.toList()
        val type = computeEventType(description)
        SimpleDateFormat("yyyy-MM-dd").parse(date)
        val event = Event(counter.getAndIncrement(), id, SimpleDateFormat("yyyy-MM-dd").parse(date), description, type)
        if (sign == "-") {
            event.reverted = true
        }
        return event
    }

    private fun toAggregate(it: String): Aggregate {
        val (type, id, description) = it.replaceFirst(Regex("\\|"), "").split(",")
        return Aggregate(counter.getAndIncrement(), type, id, description)
    }

    private fun computeEventType(description: String) =
        when (description) {
            in Regex(".*revert.*") -> EventType.Revert
            in Regex(".*deprecates.*") -> EventType.Deprecates
            in Regex(".*deprecated.*") -> EventType.DeprecatedBy
            in Regex(".*disjoin.*") -> EventType.Disjoin
            in Regex(".*join.*") -> EventType.Join
            else -> EventType.Normal
        }

    fun write(file: File) {
        init()

        val svg = Svg().withHeight("${aggregates.size * eventLineHeight}")
            .withWidth("${dates.values.maxOrNull()!! * eventSpace + 4 * aggregateWidth}")

        svg.withSVGDescriptionClassOrSVGAnimationClassOrSVGStructureClass(
            ObjectFactory().createStyle(
                SVGStyleClass().withContent("/* <![CDATA[ */$CSS/* ]]> */")
            )
        )

        svg.withSVGDescriptionClassOrSVGAnimationClassOrSVGStructureClass(
            ObjectFactory().createRect(
                Rect().withX("0").withY("0").withHeight("${aggregates.size * eventLineHeight}").withWidth(
                    "${dates.values.maxOrNull()!! * eventSpace + 4 * aggregateWidth}"
                ).withClazz("background")
            ),
            ObjectFactory().createDefs(
                ObjectFactory().createDefs().withSVGDescriptionClassOrSVGAnimationClassOrSVGStructureClass(
                    ObjectFactory().createMarker(
                        SVGMarkerClass().withId("triangle").withViewBox("0 0 10 10")
                            .withRefX("0").withRefY("5")
                            .withMarkerWidth("10").withMarkerHeight("10")
                            .withOrient("auto").withMarkerUnits("userSpaceOnUse")
                            .withSVGDescriptionClassOrSVGAnimationClassOrSVGStructureClass(
                                ObjectFactory().createPath(Path().withD("M 0 0 L 10 5 L 0 10 z"))
                            )
                    )
                )
            )
        )

        aggregates.forEach { aggregate ->
            svg.withSVGDescriptionClassOrSVGAnimationClassOrSVGStructureClass(
                ObjectFactory().createG(
                    aggregate.buildSvg(
                        dates
                    )
                )
            )
            aggregate.events.forEach { event ->
                svg.withSVGDescriptionClassOrSVGAnimationClassOrSVGStructureClass(
                    ObjectFactory().createG(event.buildSvg(aggregate.index, this))
                )
            }
        }

        val jaxbContext = JAXBContext.newInstance(Svg::class.java)
        val marshaller = jaxbContext.createMarshaller()
        marshaller.setProperty(JAXB_FORMATTED_OUTPUT, TRUE)
        marshaller.marshal(svg, file.writer())
    }
}

operator fun Regex.contains(text: CharSequence): Boolean = this.matches(text)