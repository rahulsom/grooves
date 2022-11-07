package com.github.rahulsom.grooves.asciidoctor

import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockProcessor
import org.asciidoctor.extension.Reader
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

/**
 * Renders an Events block as SVG.
 *
 * @author Rahul Somasunderam
 */
class EventsBlock(name: String) :
    BlockProcessor(name, mapOf("contexts" to listOf(":literal"), "content_model" to ":simple")) {

    override fun process(parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>?): Any {
        val projectDirAttr = parent.document.attributes["gradle-projectdir"] as String
        val outDir = File(projectDirAttr, "build/docs/asciidoc")

        val input = reader.readLines().joinToString("\n")

        var filename = (attributes ?: emptyMap()).get("2") as String? ?: md5(input)
        filename = if (filename.endsWith(".svg")) filename else "$filename.svg"

        SvgBuilder(input).write(File(outDir, filename))

        val newAttributes = mapOf<String, Any>(
            "type" to ":image",
            "target" to filename,
            "format" to "svg"
        )

        return createBlock(parent, "image", input, newAttributes, attributes?.mapKeys { it })
    }

    private fun md5(input: String) =
        MessageDigest.getInstance("MD5").let { md5 ->
            md5.update(input.toByteArray())
            val hash = BigInteger(1, md5.digest())
            hash.toString(16)
        }
}