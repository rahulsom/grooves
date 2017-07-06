package restserver

import grooves.grails.restserver.*
import org.grails.orm.hibernate.query.AbstractHibernateQuery
import org.springframework.core.convert.converter.Converter
import org.springframework.core.convert.support.DefaultConversionService

import java.util.function.Consumer
import java.util.function.Supplier

/**
 * Initializes Application with usable data.
 *
 * @author Rahul Somasunderam
 */
class BootStrap {
    static class PatientIntegerConverter implements Converter<Integer, Patient>{
        @Override Patient convert(Integer source) { Patient.load source }
    }
    static class PatientLongConverter implements Converter<Long, Patient>{
        @Override Patient convert(Long source) { Patient.load source }
    }
    static class StringDateConverter implements Converter<String, Date> {
        @Override Date convert(String date) { Date.parse("yyyy-MM-dd'T'HH:mmZ", date) }
    }

    def init = { servletContext ->

        (AbstractHibernateQuery.conversionService as DefaultConversionService).with {
            addConverter new PatientIntegerConverter()
            addConverter new PatientLongConverter()
            addConverter new StringDateConverter()
        }

        setupJohnLennon()
        setupRingoStarr()
        setupPaulMcCartney()
        setupFreddieMercury()
    }

    private Patient setupJohnLennon() {
        def patient = new Patient(uniqueId: '42').save(flush: true, failOnError: true)

        on(patient) {
            apply new PatientCreated(name: 'John Lennon')
            apply new ProcedurePerformed(code: 'FLUSHOT', cost: 32.40)
            apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)
            apply new PaymentMade(amount: 100.25)

            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            apply new PaymentMade(amount: 180.00)

        }
    }

    private Patient setupRingoStarr() {
        def patient = new Patient(uniqueId: '43').save(flush: true, failOnError: true)

        on(patient) {
            apply new PatientCreated(name: 'Ringo Starr')
            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)
            apply new PaymentMade(amount: 100.25)

            apply new ProcedurePerformed(code: 'FLUSHOT', cost: 32.40)
            apply new PaymentMade(amount: 180.00)

        }
    }

    private Patient setupPaulMcCartney() {
        def patient = new Patient(uniqueId: '44').save(flush: true, failOnError: true)

        on(patient) {
            apply new PatientCreated(name: 'Paul McCartney')
            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            def gluc = apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)
            apply new PaymentMade(amount: 100.25)
            apply new PatientEventReverted(revertedEventId: gluc.id)
            def pmt = apply new PaymentMade(amount: 30.00)

            apply new PatientEventReverted(revertedEventId: pmt.id)
            apply new PaymentMade(amount: 60.00)

            apply new PaymentMade(amount: 60.00)

        }

    }

    private Patient setupFreddieMercury() {
        def patient = new Patient(uniqueId: '45').save(flush: true, failOnError: true)
        def patient2 = new Patient(uniqueId: '46').save(flush: true, failOnError: true)

        on(patient) {
            apply new PatientCreated(name: 'Farrokh Bulsara')
            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)

        }

        on(patient2) {
            apply new PatientCreated(name: 'Freddie Mercury')
            apply new PaymentMade(amount: 100.25)

        }

        currDate += 1
        merge(patient, patient2)
        patient
    }

    /**
     *
     * @param self The aggregate to be deprecated
     * @param into The aggregate to survive
     * @return
     */
    private PatientDeprecatedBy merge(Patient self, Patient into) {
        def e1 = new PatientDeprecatedBy(aggregate: self, createdBy: 'anonymous', deprecator: into,
                timestamp: currDate,
                position: PatientEvent.countByAggregate(self) + 1,)
        def e2 = new PatientDeprecates(aggregate: into, createdBy: 'anonymous', deprecated: self,
                timestamp: currDate, converse: e1,
                position: PatientEvent.countByAggregate(into) + 1,)
        e1.converse = e2
        e2.save(flush: true, failOnError: true)
        e2.converse
    }

    Date currDate = Date.parse('yyyy-MM-dd', '2016-01-01')

    static class Applier {
        private Patient aggregate;
        private Consumer entityConsumer;
        private Supplier<Date> timestampSupplier;
        private Supplier<String> userSupplier;
        private Supplier<Long> positionSupplier;

        /**
         * Applies an event to an aggregate. This involves checking if any important fields are
         * missing and populating them based on the suppliers.
         *
         * @param event The event to be applied
         * @param < T >      The Type of event
         *
         * @return The event after persisting
         */
        public <T extends PatientEvent> T apply(T event) {
            event.setAggregate(aggregate);

            if (event.getCreatedBy() == null) {
                event.setCreatedBy(userSupplier.get());
            }
            if (event.getPosition() == null) {
                event.setPosition(positionSupplier.get());
            }
            if (event.getTimestamp() == null) {
                event.setTimestamp(timestampSupplier.get());
            }

            entityConsumer.accept(event);

            return event;
        }
    }
    Patient on(Patient patient, @DelegatesTo(Applier) Closure closure) {
        def eventSaver = { it.save(flush: true, failOnError: true) } as Consumer<PatientEvent>
        def positionSupplier = { PatientEvent.countByAggregate(patient) + 1 }
        def userSupplier = { 'anonymous' }
        def dateSupplier = { currDate += 1; currDate }

        closure.delegate = new Applier(
                aggregate: patient,
                entityConsumer: eventSaver, positionSupplier: positionSupplier,
                userSupplier: userSupplier, timestampSupplier: dateSupplier
        )

        closure.call()

        patient

    }

    def destroy = {
    }
}
