package com.github.rahulsom.grooves.grails

import groovy.transform.CompileStatic
import org.reactivestreams.Publisher
import rx.Observable
import rx.RxReactiveStreams
import rx.Single

@CompileStatic
class RxJavaPublisherExtension {

    static <T> Publisher<T> toPublisher(Observable<T> observable) {
        RxReactiveStreams.toPublisher(observable)
    }

    static <T> Publisher<T> toPublisher(Single<T> single) {
        RxReactiveStreams.toPublisher(single)
    }

    static <T> Observable<T> toObservable(Publisher<T> publisher) {
        RxReactiveStreams.toObservable(publisher)
    }

    static <T> Single<T> toSingle(Publisher<T> single) {
        RxReactiveStreams.toSingle(single)
    }

}
