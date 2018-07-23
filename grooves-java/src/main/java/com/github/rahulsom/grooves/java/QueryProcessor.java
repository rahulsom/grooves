package com.github.rahulsom.grooves.java;

import com.google.auto.service.AutoService;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.util.*;

import static javax.tools.Diagnostic.Kind.ERROR;

@SuppressWarnings("unused")
@SupportedAnnotationTypes({
        QueryProcessor.AGGREGATE_ANNOTATION,
        QueryProcessor.EVENT_ANNOTATION,
        QueryProcessor.QUERY_ANNOTATION,
})
@AutoService(Processor.class)
public class QueryProcessor extends AbstractProcessor {

    static final String QUERY_ANNOTATION = "com.github.rahulsom.grooves.java.Query";
    static final String EVENT_ANNOTATION = "com.github.rahulsom.grooves.java.Event";
    static final String AGGREGATE_ANNOTATION = "com.github.rahulsom.grooves.java.Aggregate";

    private static final String ERROR_MESSAGE = "Method not implemented";
    private static final String OBSERVABLE_TYPE = "rx.Observable";
    private static final String EVENT_APPLY_OUTCOME_TYPE =
            "com.github.rahulsom.grooves.api.EventApplyOutcome";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> aggregates = new LinkedHashSet<>();
        Set<? extends Element> eventTypes = new LinkedHashSet<>();
        Set<? extends Element> queryTypes = new LinkedHashSet<>();

        for (TypeElement annotation : annotations) {
            switch (annotation.getSimpleName().toString()) {
                case "Aggregate":
                    aggregates = roundEnv.getElementsAnnotatedWith(annotation);
                    break;
                case "Event":
                    eventTypes = roundEnv.getElementsAnnotatedWith(annotation);
                    break;
                case "Query":
                    queryTypes = roundEnv.getElementsAnnotatedWith(annotation);
                    break;
                default:
                    throw new RuntimeException("Unexpected annotation");
            }
        }

        Map<String, List<String>> eventsByAggregate = new HashMap<>();
        loadAggregates(aggregates, eventsByAggregate);
        loadEvents(eventTypes, eventsByAggregate);

        for (Element queryType : queryTypes) {
            checkQuery(eventsByAggregate, queryType);
        }

        return true;
    }

    private void checkQuery(Map<String, List<String>> eventsByAggregate, Element queryType) {
        for (AnnotationMirror annotationMirror : queryType.getAnnotationMirrors()) {
            String annotationClassName = annotationMirror.getAnnotationType().toString();
            if (annotationClassName.equals(QUERY_ANNOTATION)) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues =
                        annotationMirror.getElementValues();
                List<String> expectedMethods =
                        getExpectedMethods(elementValues, eventsByAggregate);

                for (String expectedMethod : expectedMethods) {
                    checkMethod(queryType, annotationMirror, expectedMethod);
                }
            }
        }
    }

    private void checkMethod(
            Element queryType, AnnotationMirror annotationMirror, String expectedMethod) {
        boolean found = queryType.getEnclosedElements()
                .stream()
                .anyMatch(it -> it.toString().equals(expectedMethod));
        if (!found) {
            String msg = String.format("%s%n  %s<%s> %s",
                    ERROR_MESSAGE, OBSERVABLE_TYPE, EVENT_APPLY_OUTCOME_TYPE,
                    expectedMethod);
            Messager messager = processingEnv.getMessager();
            messager.printMessage(ERROR, msg, queryType, annotationMirror);
        }
    }

    @NotNull
    private List<String> getExpectedMethods(
            Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues,
            Map<String, List<String>> eventsByAggregate) {
        ArrayList<String> expectedMethods = new ArrayList<>();
        getAttributeClass(elementValues, "aggregate()")
                .map(eventsByAggregate::get)
                .ifPresent(eventClasses -> getAttributeClass(elementValues, "snapshot()")
                        .ifPresent(snapshotClass -> {
                            for (String event : eventClasses) {
                                String[] parts = event.split("\\.");
                                String simpleClassName = parts[parts.length - 1];
                                String methodDescription =
                                        String.format("apply%s(%s,%s)",
                                                simpleClassName, event, snapshotClass);
                                expectedMethods.add(methodDescription);
                            }
                        }));

        return expectedMethods;
    }

    @NotNull
    private Optional<String> getAttributeClass(
            Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues,
            String attributeName) {
        return elementValues
                .entrySet()
                .stream()
                .filter(it -> it.getKey().toString().equals(attributeName))
                .map(it -> it.getValue().getValue().toString())
                .findFirst();
    }

    private void loadEvents(
            Set<? extends Element> eventTypes, Map<String, List<String>> eventsByAggregate) {
        for (Element eventType : eventTypes) {
            for (AnnotationMirror annotationMirror : eventType.getAnnotationMirrors()) {
                String annotationClassName = annotationMirror.getAnnotationType().toString();
                if (annotationClassName.equals(EVENT_ANNOTATION)) {
                    Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues =
                            annotationMirror.getElementValues();
                    getAttributeClass(elementValues, "value()")
                            .map(eventsByAggregate::get)
                            .ifPresent(map -> map.add(eventType.toString()));
                }
            }
        }
    }

    private void loadAggregates(
            Set<? extends Element> aggregateTypes, Map<String, List<String>> eventsByAggregate) {
        for (Element aggregateType : aggregateTypes) {
            eventsByAggregate.put(aggregateType.toString(), new ArrayList<>());
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
