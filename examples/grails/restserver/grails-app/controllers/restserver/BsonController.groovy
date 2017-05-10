package restserver

import grails.rest.RestfulController
import grooves.grails.restserver.Patient
import grooves.grails.restserver.PatientEvent
import groovy.json.JsonSlurper
import org.grails.datastore.bson.json.JsonReader
import org.grails.datastore.bson.query.BsonQuery

/**
 * Performs Restful queries using BSON
 */
abstract class BsonController<T> extends RestfulController<T> {
    BsonController(Class<T> resource) {
        super(resource)
    }
    BsonController(Class<T> resource, boolean readOnly) {
        super(resource, readOnly)
    }
    @Override
    protected List<T> listAllResources(Map params) {
        params.q ? performQuery(params.q) :
                super.listAllResources(params)
    }

    private List<T> performQuery(String q) {
        if (q.contains('$in')) {
            def query = new JsonSlurper().parseText(q)
            def aggregateIds = query['aggregate']['$in']
            PatientEvent.findAllByAggregateInList(aggregateIds.collect {Patient.load(it)}, params)
        } else {
            BsonQuery.parse(resource, new JsonReader(q)).list(params)
        }
    }

    @Override
    Object index(Integer max) {
        log.info "Query: ${params.q}"

        params.max = Math.min(max ?: 10, 100)
        [
                ("${resourceName}Count".toString()): countResources(),
                ("${resourceName}List".toString()): listAllResources(params)
        ]
    }
}
