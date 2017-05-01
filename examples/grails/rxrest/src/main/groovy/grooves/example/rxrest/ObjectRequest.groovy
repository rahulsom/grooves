package grooves.example.rxrest

import grails.validation.Validateable

/**
 * Represents a request for an object
 *
 * @author Rahul Somasunderam
 */
class ObjectRequest implements Validateable {
    Long id
    Date date
    Long version
}
