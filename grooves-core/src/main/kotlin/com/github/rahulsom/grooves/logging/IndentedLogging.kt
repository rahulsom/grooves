package com.github.rahulsom.grooves.logging

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import kotlin.concurrent.getOrSet

@Aspect
class IndentedLogging {
    companion object {
        private val indentLevel = ThreadLocal<Int>()
        private const val INITIAL_INDENT = 1
        fun stepIn() = indentLevel.set(indentLevel.getOrSet { INITIAL_INDENT } + 1)
        fun stepOut() = indentLevel.set(indentLevel.getOrSet { INITIAL_INDENT } - 1)

        @JvmStatic
        fun indent() = "".padStart(indentLevel.getOrSet { INITIAL_INDENT } * 2)
        private fun eventsToString(it: List<*>): Any = "<... ${it.size} item(s)>"
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Around(value = "@annotation(trace)", argNames = "trace")
    @ExperimentalStdlibApi
    fun around(joinPoint: ProceedingJoinPoint, trace: Trace): Any? {
        val signature = joinPoint.signature
        val classWithFunction = joinPoint.target.javaClass
        val loggerName = classWithFunction.name.replace(Regex("\\\$\\\$Lambda.*$"), "")
        val log = LoggerFactory.getLogger(loggerName)
        val methodName =
            if (signature.name == "invoke") {
                signature.declaringType.simpleName.replaceFirstChar { x -> x.lowercaseChar() }
            } else {
                signature.name
            }

        val args = joinPoint.args.map { if (it is List<*>) eventsToString(it) else it }.joinToString(", ")
        log.trace("${indent()}$methodName($args)")
        stepIn()

        try {
            val result = joinPoint.proceed()
            stepOut()
            val listRender = if (result is List<*>) eventsToString(result) else result
            log.trace("${indent()}$methodName($args) --> $listRender")
            return result
        } catch (t: Throwable) {
            stepOut()
            log.trace("${indent()}$methodName($args) ~~> $t")
            throw t
        }
    }
}