package com.github.rahulsom.grooves.transformations.internal;

import com.github.rahulsom.grooves.transformations.Aggregate;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Creates an interface for the query
 *
 * @author Rahul Somasunderam
 */
@GroovyASTTransformation
public class AggregateASTTransformation extends AbstractASTTransformation {
    private static final Class<Aggregate> MY_CLASS = Aggregate.class;
    private static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    private static final Map<String, List<ClassNode>> eventsForAggregate = new LinkedHashMap<>();
    private final Logger log = Logger.getLogger(getClass().getName());

    static void addEventToAggregate(String aggregate, ClassNode event) {
        if (!eventsForAggregate.containsKey(aggregate)) {
            eventsForAggregate.put(aggregate, new ArrayList<>());
        }

        DefaultGroovyMethods.leftShift(eventsForAggregate.get(aggregate), event);
    }

    public static List<ClassNode> getEventsForAggregate(String aggregate) {
        if (!eventsForAggregate.containsKey(aggregate)) {
            eventsForAggregate.put(aggregate, new ArrayList<>());
        }

        return eventsForAggregate.get(aggregate);
    }

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode annotatedNode = DefaultGroovyMethods.asType(nodes[1], AnnotatedNode.class);
        AnnotationNode annotationNode = DefaultGroovyMethods.asType(nodes[0], AnnotationNode.class);

        if (MY_TYPE.equals(annotationNode.getClassNode()) && annotatedNode instanceof ClassNode) {
            final ClassNode theClassNode = DefaultGroovyMethods.asType(annotatedNode, ClassNode.class);
            log.fine(() -> MessageFormat.format("Storing entry for aggregate {0}", theClassNode.getName()));
            getEventsForAggregate(theClassNode.getName());
        }

    }
}
