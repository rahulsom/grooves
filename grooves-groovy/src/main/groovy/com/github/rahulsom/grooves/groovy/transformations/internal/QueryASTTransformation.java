package com.github.rahulsom.grooves.groovy.transformations.internal;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.groovy.transformations.Query;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.reactivestreams.Publisher;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
        AnnotatedNode annotatedNode = (AnnotatedNode) nodes[1];
        final AnnotationNode annotationNode = (AnnotationNode) nodes[0];

        if (MY_TYPE.equals(annotationNode.getClassNode()) && annotatedNode instanceof ClassNode) {
            final Expression theSnapshot = annotationNode.getMember("snapshot");
            Expression theAggregate = annotationNode.getMember("aggregate");
            final ClassNode theClassNode = (ClassNode) annotatedNode;
            log.fine("Checking " + theClassNode.getNameWithoutPackage() + " for methods");
            List<ClassNode> eventClasses =
                    AggregateASTTransformation.getEventsForAggregate(
                            theAggregate.getType().getName());

            eventClasses.forEach(eventClass -> {
                final String methodName = "apply" + eventClass.getNameWithoutPackage();
                log.fine("  -> Checking for " + methodName);

                List<MethodNode> methodsByName =
                        theClassNode.getMethods().stream()
                                .filter(it -> it.getName().equals(methodName))
                                .collect(Collectors.toList());

                final String methodSignature = String.format("%s %s(%s event, %s snapshot)",
                        getPublisherOfEventApplyOutcome(), methodName,
                        eventClass.getName(), theSnapshot.getType().getName());

                final String methodSignatureString = String.valueOf(methodSignature);
                if (methodsByName.isEmpty()) {
                    addError(
                            String.format("Missing expected method %s", methodSignatureString),
                            annotationNode);
                } else {
                    Optional<MethodNode> matchingMethod = methodsByName.stream()
                            .filter(implMethod -> {
                                final Parameter[] parameters = implMethod.getParameters();
                                final ClassNode returnType = implMethod.getReturnType();
                                return parameters != null && parameters.length == 2
                                        && returnType.getName()
                                                .equals(Publisher.class.getName())
                                        && returnType.getGenericsTypes()[0].getType().getName()
                                                .equals(EventApplyOutcome.class.getName())
                                        && parameters[0].getType().getName()
                                                .equals(eventClass.getName())
                                        && parameters[1].getType().getName()
                                                .equals(theSnapshot.getType().getName());
                            })
                            .findFirst();

                    if (!matchingMethod.isPresent()) {
                        addError(String.format("Missing expected method %s. %s",
                                methodSignatureString, "Signature was different when name matched"),
                                annotationNode);
                    }
                }
            });

        }

    }

    private String getPublisherOfEventApplyOutcome() {
        return ClassHelper.make(Publisher.class).getName() + "<"
                + ClassHelper.make(EventApplyOutcome.class).getName() + ">";
    }
}
