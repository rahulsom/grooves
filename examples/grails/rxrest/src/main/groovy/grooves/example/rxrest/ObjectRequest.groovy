package grooves.example.rxrest

import grails.databinding.BindingFormat
import grails.validation.Validateable

/**
 * Represents a request for an object
 *
 * @author Rahul Somasunderam
 */
class ObjectRequest implements Validateable {
    Long id
    @BindingFormat('yyyy-MM-dd') Date date
    Long version
}
