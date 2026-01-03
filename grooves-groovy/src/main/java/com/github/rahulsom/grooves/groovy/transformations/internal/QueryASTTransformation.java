package com.github.rahulsom.grooves.groovy.transformations.internal;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.groovy.transformations.Query;
import java.util.logging.Logger;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.reactivestreams.Publisher;

/**
 * Verifies that a Query interface implementation has all the extra methods it needs to process
 * events from its aggregate.
 *
 * @author Rahul Somasunderam
 */
@GroovyASTTransformation
public class QueryASTTransformation extends AbstractASTTransformation {

    private static final Class<Query> MY_CLASS = Query.class;
    private static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    private final Logger log = Logger.getLogger(getClass().getName());

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        final var annotatedNode = (AnnotatedNode) nodes[1];
        final var annotationNode = (AnnotationNode) nodes[0];

        if (MY_TYPE.equals(annotationNode.getClassNode()) && annotatedNode instanceof ClassNode theClassNode) {
            final var theSnapshot = annotationNode.getMember("snapshot");
            final var theAggregate = annotationNode.getMember("aggregate");
            log.fine("Checking " + theClassNode.getNameWithoutPackage() + " for methods");
            final var eventClasses = AggregateASTTransformation.getEventsForAggregate(
                    theAggregate.getType().getName());

            eventClasses.forEach(eventClass -> {
                final var methodName = "apply" + eventClass.getNameWithoutPackage();
                log.fine("  -> Checking for " + methodName);

                final var methodsByName = theClassNode.getMethods().stream()
                        .filter(it -> it.getName().equals(methodName))
                        .toList();

                final var methodSignature = String.format(
                        "%s %s(%s event, %s snapshot)",
                        getObservableEventApplyOutcome(),
                        methodName,
                        eventClass.getName(),
                        theSnapshot.getType().getName());

                if (methodsByName.isEmpty()) {
                    addError(String.format("Missing expected method %s", methodSignature), annotationNode);
                } else {
                    final var matchingMethod = methodsByName.stream()
                            .filter(implMethod -> {
                                final var parameters = implMethod.getParameters();
                                final var returnType = implMethod.getReturnType();
                                return parameters != null
                                        && parameters.length == 2
                                        && returnType.getName().equals(Publisher.class.getName())
                                        && returnType
                                                .getGenericsTypes()[0]
                                                .getType()
                                                .getName()
                                                .equals(EventApplyOutcome.class.getName())
                                        && parameters[0].getType().getName().equals(eventClass.getName())
                                        && parameters[1]
                                                .getType()
                                                .getName()
                                                .equals(theSnapshot.getType().getName());
                            })
                            .findFirst();

                    if (matchingMethod.isEmpty()) {
                        addError(
                                String.format(
                                        "Missing expected method %s. %s",
                                        methodSignature, "Signature was different when name matched"),
                                annotationNode);
                    }
                }
            });
        }
    }

    private String getObservableEventApplyOutcome() {
        return ClassHelper.make(Publisher.class).getName() + "<"
                + ClassHelper.make(EventApplyOutcome.class).getName() + ">";
    }
}
