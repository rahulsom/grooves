package com.github.rahulsom.grooves.asciidoctor

import com.github.rahulsom.grooves.asciidoctor.Constants.AGGREGATE_HEIGHT
import com.github.rahulsom.grooves.asciidoctor.Constants.AGGREGATE_WIDTH
import com.github.rahulsom.grooves.asciidoctor.Constants.EVENT_LINE_HEIGHT
import com.github.rahulsom.grooves.asciidoctor.Constants.EVENT_SPACE
import com.github.rahulsom.grooves.asciidoctor.Constants.OFFSET
import com.github.rahulsom.grooves.asciidoctor.Constants.TEXT_LINE_HEIGHT
import com.github.rahulsom.svg.G
import com.github.rahulsom.svg.ObjectFactory
import java.util.Date

/**
 * Represents an aggregate when rendering as an image
 *
 * @author Rahul Somasunderam
 */
class Aggregate(private val counter: Int, var type: String, var id: String, var description: String) {
    var events: MutableList<Event> = mutableListOf()

    var index: Int = 0

    override fun toString(): String {
        return "|$type,$id,$description\n${events.joinToString("\n") { it.toString() }}"
    }

    fun buildSvg(dates: Map<Date, Double>): G {
        val y = index * EVENT_LINE_HEIGHT + OFFSET
        val yMid = index * EVENT_LINE_HEIGHT + OFFSET + AGGREGATE_HEIGHT / 2

        val objectFactory = ObjectFactory()
        val g =
            objectFactory.createG()
                .withId("aggregate_$counter")
                .withClazz("aggregate")
        return g.withSVGDescriptionClassOrSVGAnimationClassOrSVGStructureClass(
            objectFactory.createRect(
                objectFactory.createRect().withX("10").withY(y.toString())
                    .withWidth("$AGGREGATE_WIDTH").withHeight("$AGGREGATE_HEIGHT"),
            ),
            objectFactory.createText(
                objectFactory.createText().withX("15").withY("${y + TEXT_LINE_HEIGHT}")
                    .withClazz("type")
                    .withContent(type),
            ),
            objectFactory.createText(
                objectFactory.createText().withX("15").withY("${y + TEXT_LINE_HEIGHT * 2}")
                    .withClazz("id")
                    .withContent(id),
            ),
            objectFactory.createText(
                objectFactory.createText().withX("10").withY("${y - 5}")
                    .withClazz("description")
                    .withContent(description),
            ),
            objectFactory.createLine(
                objectFactory.createLine().withX1("${10 + AGGREGATE_WIDTH}").withY1("$yMid")
                    .withX2("${dates.values.maxOrNull()!! * EVENT_SPACE + 3 * AGGREGATE_WIDTH}")
                    .withY2("$yMid")
                    .withClazz("eventLine").withMarkerEnd("url(#triangle)"),
            ),
        )
    }
}