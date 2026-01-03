package com.github.rahulsom.grooves.groovy.transformations.internal;

import com.github.rahulsom.grooves.groovy.transformations.Aggregate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

/**
 * Registers an aggregate as something that could later be used for computing one or more
 * {@link com.github.rahulsom.grooves.api.snapshots.Snapshot}s in a class annotated as a
 * {@link com.github.rahulsom.grooves.groovy.transformations.Query}.
 *
 * @author Rahul Somasunderam
 */
@GroovyASTTransformation
public class AggregateASTTransformation extends AbstractASTTransformation {
    private static final Class<Aggregate> MY_CLASS = Aggregate.class;
    private static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    private static final Map<String, List<ClassNode>> EVENTS_FOR_AGGREGATE = new LinkedHashMap<>();
    private final Logger log = Logger.getLogger(getClass().getName());

    static void addEventToAggregate(String aggregate, ClassNode event) {
        getEventsForAggregate(aggregate).add(event);
    }

    static List<ClassNode> getEventsForAggregate(String aggregate) {
        return EVENTS_FOR_AGGREGATE.computeIfAbsent(aggregate, k -> new ArrayList<>());
    }

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        final var annotatedNode = (AnnotatedNode) nodes[1];
        final var annotationNode = (AnnotationNode) nodes[0];

        if (MY_TYPE.equals(annotationNode.getClassNode()) && annotatedNode instanceof ClassNode theClassNode) {
            log.fine(() -> MessageFormat.format("Storing entry for aggregate {0}", theClassNode.getName()));
            getEventsForAggregate(theClassNode.getName());
        }
    }
}
