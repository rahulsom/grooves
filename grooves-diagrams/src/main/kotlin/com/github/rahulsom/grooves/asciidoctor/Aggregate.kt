package com.github.rahulsom.grooves.asciidoctor

import com.github.rahulsom.grooves.asciidoctor.Constants.aggregateHeight
import com.github.rahulsom.grooves.asciidoctor.Constants.aggregateWidth
import com.github.rahulsom.grooves.asciidoctor.Constants.eventLineHeight
import com.github.rahulsom.grooves.asciidoctor.Constants.eventSpace
import com.github.rahulsom.grooves.asciidoctor.Constants.offset
import com.github.rahulsom.grooves.asciidoctor.Constants.textLineHeight
import com.github.rahulsom.svg.G
import com.github.rahulsom.svg.ObjectFactory
import java.util.Date

/**
 * Represents an aggregate when rendering as an image
 *
 * @author Rahul Somasunderam
 */
class Aggregate(val counter: Int, var type: String, var id: String, var description: String) {
    var events: MutableList<Event> = mutableListOf()

    var index: Int = 0

    override fun toString(): String {
        return "|$type,$id,$description\n${events.joinToString("\n") { it.toString() }}"
    }

    fun buildSvg(dates: Map<Date, Double>): G {
        val y = index * eventLineHeight + offset
        val yMid = index * eventLineHeight + offset + aggregateHeight / 2

        val objectFactory = ObjectFactory()
        val g = objectFactory.createG()
            .withId("aggregate_$counter")
            .withClazz("aggregate")
        return g.withSVGDescriptionClassOrSVGAnimationClassOrSVGStructureClass(
            objectFactory.createRect(
                objectFactory.createRect().withX("10").withY(y.toString())
                    .withWidth("$aggregateWidth").withHeight("$aggregateHeight")
            ),
            objectFactory.createText(
                objectFactory.createText().withX("15").withY("${y + textLineHeight}")
                    .withClazz("type")
                    .withContent(type)
            ),
            objectFactory.createText(
                objectFactory.createText().withX("15").withY("${y + textLineHeight * 2}")
                    .withClazz("id")
                    .withContent(id)
            ),
            objectFactory.createText(
                objectFactory.createText().withX("10").withY("${y - 5}")
                    .withClazz("description")
                    .withContent(description)
            ),
            objectFactory.createLine(
                objectFactory.createLine().withX1("${10 + aggregateWidth}").withY1("$yMid")
                    .withX2("${dates.values.max()!! * eventSpace + 3 * aggregateWidth}")
                    .withY2("$yMid")
                    .withClazz("eventLine").withMarkerEnd("url(#triangle)")
            )
        )
    }
}