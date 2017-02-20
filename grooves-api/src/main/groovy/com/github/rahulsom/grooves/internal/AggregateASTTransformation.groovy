package com.github.rahulsom.grooves.internal

import com.github.rahulsom.grooves.annotations.Aggregate
import groovy.transform.CompileStatic
import groovy.util.logging.Log
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static org.codehaus.groovy.ast.ClassHelper.make

/**
 * Creates an interface for the query
 *
 * @author Rahul Somasunderam
 *
 */
@CompileStatic
@GroovyASTTransformation()
@Log
class AggregateASTTransformation extends AbstractASTTransformation {

    private static final Class<Aggregate> MY_CLASS = Aggregate.class
    private static final ClassNode MY_TYPE = make(MY_CLASS)
    static final Map<String, List<ClassNode>> eventsForAggregate = [:]

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source)
        AnnotatedNode annotatedNode = nodes[1] as AnnotatedNode
        AnnotationNode annotationNode = nodes[0] as AnnotationNode

        if (MY_TYPE == annotationNode.classNode && annotatedNode instanceof ClassNode) {
            def theClassNode = annotatedNode as ClassNode
            log.fine "Storing entry for aggregate ${theClassNode.name}"
            getEventsForAggregate(theClassNode.name)
        }
    }

    static void addEventToAggregate(String aggregate, ClassNode event) {
        if (!eventsForAggregate.containsKey(aggregate)) {
            eventsForAggregate[aggregate] = []
        }
        eventsForAggregate[aggregate] << event
    }

    static List<ClassNode> getEventsForAggregate(String aggregate) {
        if (!eventsForAggregate.containsKey(aggregate)) {
            eventsForAggregate[aggregate] = []
        }
        eventsForAggregate[aggregate]
    }

}
