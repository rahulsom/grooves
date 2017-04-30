package grooves.example.rxrest

import grails.validation.Validateable

class ObjectRequest implements Validateable {
    Long id
    Date date
    Long version
}
