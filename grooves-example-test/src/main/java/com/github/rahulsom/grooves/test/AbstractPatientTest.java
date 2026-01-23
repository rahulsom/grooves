package com.github.rahulsom.grooves.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Base test to replicate across example projects.
 *
 * @author Rahul Somasunderam
 */
public abstract class AbstractPatientTest {
    /**
     * Provides a preconfigured RESTClient that can be given a path to execute a HTTP query.
     *
     * @return Preconfigured RESTClient
     */
    public abstract RestClient getRest();

    private List<String> ids;

    @BeforeEach
    void loadIds() {
        if (ids == null) {
            var resp = getRest().get(new RestRequest("patient"), new TypeReference<List<PatientDTO>>() {});
            ids = resp.getData().stream().map(PatientDTO::id).toList();
        }
    }

    static Date getDate(Object ts) {
        if (ts instanceof String s) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd").parse(s.substring(0, 10));
            } catch (ParseException e) {
                return null;
            }
        } else if (ts instanceof Long l) {
            return new Date(l);
        } else {
            return null;
        }
    }

    @Test
    @DisplayName("Patient List works")
    void patientListWorks() {
        final var resp = getRest().get(new RestRequest("patient"), new TypeReference<List<PatientDTO>>() {});
        assertThat(resp.getStatus()).isEqualTo(200);

        final var data = resp.getData();
        assertThat(data).isNotNull();
        assertThat(data.size()).isGreaterThan(1);

        final var first = data.get(0);
        assertThat(first.uniqueId()).isNotNull().isEqualTo("42");

        final var second = data.get(1);
        assertThat(second.uniqueId()).isNotNull().isEqualTo("43");
    }

    @ParameterizedTest
    @DisplayName("Paul McCartney's balance is correct at version {1}")
    @CsvSource(textBlock = """
            1, 0.0, 0.0
            2, 170.0, 0.0
            3, 248.93, 0.0
            4, 148.68, 100.25
            5, 69.75, 100.25
            6, 39.75, 130.25
            7, 69.75, 100.25
            8, 9.75, 160.25
            9, -50.25, 220.25
            """)
    void paulBalanceAtVersion(int version, BigDecimal balance, BigDecimal moneyMade) {
        final var rest = getRest();
        final var id = ids.get(3 - 1);
        final var resp = rest.get(
                new RestRequest("patient/account/" + id, Map.of("version", version)),
                new TypeReference<PatientAccountDTO>() {});

        assertThat(resp.getStatus()).isEqualTo(200);
        final var data = resp.getData();
        assertThat(data).isNotNull();
        final var balance1 = data.balance();
        assertThat(balance1).usingComparator(BigDecimal::compareTo).isEqualTo(balance);
        final var moneyMade1 = data.moneyMade();
        assertThat(moneyMade1).usingComparator(BigDecimal::compareTo).isEqualTo(moneyMade);
    }

    @ParameterizedTest
    @DisplayName("{2} - Show works")
    @CsvSource(textBlock = """
            1, 42, John Lennon
            2, 43, Ringo Starr
            """)
    void showWorks(int index, String uniqueId, String name) {
        final var rest = getRest();
        final var theId = ids.get(index - 1);

        final var resp = rest.get(new RestRequest("patient/show/" + theId), new TypeReference<PatientDTO>() {});

        assertThat(resp.getStatus()).isEqualTo(200);
        final var data = resp.getData();
        assertThat(data).isNotNull();
        assertThat(data.uniqueId()).isEqualTo(uniqueId);
        assertThat(data.id()).hasToString(theId);
    }

    @ParameterizedTest
    @DisplayName("{3} - Health works")
    @CsvSource(textBlock = """
            1, John Lennon, 6, 'FLUSHOT;GLUCOSETEST;ANNUALPHYSICAL'
            2, Ringo Starr, 6, 'ANNUALPHYSICAL;GLUCOSETEST;FLUSHOT'
            3, Paul McCartney, 9, 'ANNUALPHYSICAL'
            """)
    void healthWorks(int index, String name, int lastEventPos, String codesStr) {
        final var rest = getRest();
        final var theId = ids.get(index - 1);
        final var codes = List.of(codesStr.split(";"));

        final var resp = rest.get(new RestRequest("patient/health/" + theId), new TypeReference<PatientHealthDTO>() {});

        assertThat(resp.getStatus()).isEqualTo(200);
        final var data = resp.getData();
        assertThat(data).isNotNull();

        assertThat(data.getEffectiveAggregateId()).hasToString(theId);

        assertThat(data.name()).isEqualTo(name);
        assertThat(data.lastEventPosition()).isEqualTo(lastEventPos);

        final var procedures = data.procedures();
        assertThat(procedures).hasSize(codes.size());
        final var procedureCodes = procedures.stream().map(ProcedureDTO::code).toList();
        assertThat(procedureCodes).isEqualTo(codes);
    }

    @ParameterizedTest
    @DisplayName("{2} by Version {1} - Health works")
    @CsvSource(textBlock = """
            1, John Lennon, 1, ''
            2, John Lennon, 1, 'FLUSHOT'
            3, John Lennon, 1, 'FLUSHOT;GLUCOSETEST'
            5, John Lennon, 1, 'FLUSHOT;GLUCOSETEST;ANNUALPHYSICAL'
            1, Ringo Starr, 2, ''
            2, Ringo Starr, 2, 'ANNUALPHYSICAL'
            3, Ringo Starr, 2, 'ANNUALPHYSICAL;GLUCOSETEST'
            5, Ringo Starr, 2, 'ANNUALPHYSICAL;GLUCOSETEST;FLUSHOT'
            1, Paul McCartney, 3, ''
            2, Paul McCartney, 3, 'ANNUALPHYSICAL'
            3, Paul McCartney, 3, 'ANNUALPHYSICAL;GLUCOSETEST'
            5, Paul McCartney, 3, 'ANNUALPHYSICAL'
            """)
    void healthWorksByVersion(int version, String name, int index, String codesStr) {
        final var rest = getRest();
        final var theId = ids.get(index - 1);
        final var codes = codesStr.isEmpty() ? List.<String>of() : List.of(codesStr.split(";"));

        var resp = rest.get(
                new RestRequest("patient/health/" + theId, Map.of("version", version)),
                new TypeReference<PatientHealthDTO>() {});

        assertThat(resp.getStatus()).isEqualTo(200);
        final var data = resp.getData();
        assertThat(data).isNotNull();

        assertThat(data.getEffectiveAggregateId()).hasToString(theId);

        assertThat(data.name()).isEqualTo(name);
        assertThat(data.lastEventPosition()).isEqualTo(version);

        final var procedures = data.procedures();
        assertThat(procedures).hasSize(codes.size());
        final var procedureCodes = procedures.stream().map(ProcedureDTO::code).toList();
        assertThat(procedureCodes).isEqualTo(codes);
    }

    @ParameterizedTest
    @DisplayName("{2} by Date {1} - Health works")
    @CsvSource(textBlock = """
            2016-01-03, John Lennon, 1, 2, 'FLUSHOT'
            2016-01-09, Ringo Starr, 2, 2, 'ANNUALPHYSICAL'
            2016-01-15, Paul McCartney, 3, 2, 'ANNUALPHYSICAL'
            2016-01-16, Paul McCartney, 3, 3, 'ANNUALPHYSICAL;GLUCOSETEST'
            2016-01-18, Paul McCartney, 3, 5, 'ANNUALPHYSICAL'
            """)
    void healthWorksByDate(String date, String name, int index, int lastEventPos, String codesStr) {
        final var rest = getRest();
        final var theId = ids.get(index - 1);
        final var codes = codesStr.isEmpty() ? List.<String>of() : List.of(codesStr.split(";"));

        final var resp = rest.get(
                new RestRequest("patient/health/" + theId, Map.of("date", date + "T00:00:00.000Z")),
                new TypeReference<PatientHealthDTO>() {});

        assertThat(resp.getStatus()).isEqualTo(200);
        final var data = resp.getData();
        assertThat(data).isNotNull();

        assertThat(data.getEffectiveAggregateId()).hasToString(theId);

        assertThat(data.name()).isEqualTo(name);
        assertThat(data.lastEventPosition()).isEqualTo(lastEventPos);

        final var procedures = data.procedures();
        assertThat(procedures).hasSize(codes.size());
        final var procedureCodes = procedures.stream().map(ProcedureDTO::code).toList();
        assertThat(procedureCodes).isEqualTo(codes);
    }

    @ParameterizedTest
    @DisplayName("Farrokh Bulsara's balance is correct at version {0}")
    @CsvSource(textBlock = """
            1, 0.0, 0.0, 4,
            2, 170.0, 0.0, 4,
            3, 248.93, 0.0, 4,
            4, 148.68, 100.25, 5, '4'
            """)
    void farrokhBulsaraBalanceAtVersion(
            int version, BigDecimal balance, BigDecimal moneyMade, int aggregateIdIndex, String deprecatedIdsStr) {
        final var rest = getRest();
        final var theId4 = ids.get(4 - 1);
        final var theAggregateId = ids.get(aggregateIdIndex - 1);
        final var theDeprecatedIds = deprecatedIdsStr != null && !deprecatedIdsStr.isEmpty()
                ? Stream.of(deprecatedIdsStr.split(";"))
                        .map(i -> ids.get(Integer.parseInt(i) - 1))
                        .toList()
                : null;

        final var resp = rest.get(
                new RestRequest("patient/account/" + theId4, Map.of("version", version)),
                new TypeReference<PatientAccountDTO>() {});

        assertThat(resp.getStatus()).isEqualTo(200);
        final var data = resp.getData();
        assertThat(data).isNotNull();

        final var actualBalance = data.balance();
        assertThat(actualBalance).usingComparator(BigDecimal::compareTo).isEqualTo(balance);

        final var actualMoneyMade = data.moneyMade();
        assertThat(actualMoneyMade).usingComparator(BigDecimal::compareTo).isEqualTo(moneyMade);

        if (theDeprecatedIds != null) {
            assertThat(data.getEffectiveDeprecatesIds()).isEqualTo(theDeprecatedIds);
        }

        assertThat(data.getEffectiveAggregateId()).hasToString(theAggregateId);
    }

    @ParameterizedTest
    @DisplayName("Freddie Mercury's balance is correct at version {0}")
    @CsvSource(textBlock = """
            1, 0.0, 0.0, 5,
            2, -100.25, 100.25, 5,
            3, 148.68, 100.25, 5, '4'
            """)
    void freddieMercuryBalanceAtVersion(
            int version, BigDecimal balance, BigDecimal moneyMade, int aggregateIdIndex, String deprecatedIdsStr) {
        final var rest = getRest();
        final var theId5 = ids.get(5 - 1);

        final var resp = rest.get(
                new RestRequest("patient/account/" + theId5, Map.of("version", version)),
                new TypeReference<PatientAccountDTO>() {});

        assertThat(resp.getStatus()).isEqualTo(200);
        final var data = resp.getData();
        assertThat(data).isNotNull();

        final var actualBalance = data.balance();
        assertThat(actualBalance).usingComparator(BigDecimal::compareTo).isEqualTo(balance);

        final var actualMoneyMade = data.moneyMade();
        assertThat(actualMoneyMade).usingComparator(BigDecimal::compareTo).isEqualTo(moneyMade);

        assertThat(data.getEffectiveAggregateId()).hasToString(theId5);

        if (deprecatedIdsStr != null && !deprecatedIdsStr.isEmpty()) {
            final var theId4 = ids.get(4 - 1);
            final var theId4AsRange = List.of(theId4);
            assertThat(data.getEffectiveDeprecatesIds()).isEqualTo(theId4AsRange);
        }
    }

    @Test
    @DisplayName("Farrokh Bulsara and Freddie Mercury are merged")
    void farrokhBulsaraAndFreddieMercuryAreMerged() {
        final var rest = getRest();
        final var theId5 = ids.get(5 - 1);
        final var theId4 = ids.get(4 - 1);
        final var theId4AsRange = List.of(theId4);

        // Test Freddie Mercury (patient 5) - the target of the merge
        final var resp5 =
                rest.get(new RestRequest("patient/account/" + theId5), new TypeReference<PatientAccountDTO>() {});
        assertThat(resp5.getStatus()).isEqualTo(200);
        final var data5 = resp5.getData();
        assertThat(data5).isNotNull();

        assertThat(data5.getEffectiveAggregateId()).hasToString(theId5);

        // Check deprecated IDs
        assertThat(data5.getEffectiveDeprecatesIds()).isEqualTo(theId4AsRange);

        final var balance5 = data5.balance();
        assertThat(balance5).usingComparator(BigDecimal::compareTo).isEqualTo(new BigDecimal("148.68"));
        final var moneyMade5 = data5.moneyMade();
        assertThat(moneyMade5).usingComparator(BigDecimal::compareTo).isEqualTo(new BigDecimal("100.25"));
        assertThat(data5.lastEventPosition()).isEqualTo(3);

        final var lastEventTimestamp5 = getDate(data5.lastEventTimestamp());
        assertThat(lastEventTimestamp5).isNotNull();
        final var dateFormat = new SimpleDateFormat("yyyyMMdd", java.util.Locale.US);
        assertThat(dateFormat.format(lastEventTimestamp5)).isEqualTo("20160128");

        // Test Farrokh Bulsara (patient 4) - should redirect to patient 5
        final var resp4 =
                rest.get(new RestRequest("patient/account/" + theId4), new TypeReference<PatientAccountDTO>() {});
        assertThat(resp4.getStatus()).isEqualTo(200);
        final var data4 = resp4.getData();
        assertThat(data4).isNotNull();

        // Should redirect to patient 5's aggregate
        assertThat(data4.getEffectiveAggregateId()).hasToString(theId5);

        // Should show same deprecated IDs as patient 5
        assertThat(data4.getEffectiveDeprecatesIds()).isEqualTo(theId4AsRange);

        // Should show same balance as patient 5
        final var balance4 = data4.balance();
        assertThat(balance4).usingComparator(BigDecimal::compareTo).isEqualTo(new BigDecimal("148.68"));
        final var moneyMade4 = data4.moneyMade();
        assertThat(moneyMade4).usingComparator(BigDecimal::compareTo).isEqualTo(new BigDecimal("100.25"));
        assertThat(data4.lastEventPosition()).isEqualTo(3);

        final var lastEventTimestamp4 = getDate(data4.lastEventTimestamp());
        assertThat(lastEventTimestamp4).isNotNull();
        assertThat(dateFormat.format(lastEventTimestamp4)).isEqualTo("20160128");
    }

    @ParameterizedTest
    @DisplayName("Reverting a merge works - version {1} of {0} is {2}")
    @CsvSource(textBlock = """
            6, 1, Tina Fey, 0.0
            6, 2, Tina Fey, 170.00
            6, 3, Tina Fey, 248.93
            6, 4, Sarah Palin, 148.68
            6, 5, Tina Fey, 248.93
            7, 1, Sarah Palin, 0.0
            7, 2, Sarah Palin, -100.25
            7, 3, Sarah Palin, 148.68
            7, 4, Sarah Palin, -100.25
            """)
    void revertingMergeWorksByVersion(int patientIndex, int version, String name, BigDecimal balance) {
        final var rest = getRest();
        final var patientId = ids.get(patientIndex - 1);

        final var resp = rest.get(
                new RestRequest("patient/account/" + patientId, Map.of("version", version)),
                new TypeReference<PatientAccountDTO>() {});

        assertThat(resp.getStatus()).isEqualTo(200);
        final var data = resp.getData();
        assertThat(data).isNotNull();

        assertThat(data.name()).isEqualTo(name);

        final var actualBalance = data.balance();
        assertThat(actualBalance).usingComparator(BigDecimal::compareTo).isEqualTo(balance);
    }

    @ParameterizedTest
    @DisplayName("Reverting a merge works - on date {1} of {0} is {2}")
    @CsvSource(textBlock = """
            6, 2016-01-29, Tina Fey, 0.0
            6, 2016-01-30, Tina Fey, 170.00
            6, 2016-01-31, Tina Fey, 248.93
            6, 2016-02-03, Sarah Palin, 148.68
            6, 2016-02-05, Tina Fey, 248.93
            7, 2016-02-01, Sarah Palin, 0.0
            7, 2016-02-02, Sarah Palin, -100.25
            7, 2016-02-03, Sarah Palin, 148.68
            7, 2016-02-05, Sarah Palin, -100.25
            """)
    void revertingMergeWorksByDate(int patientIndex, String date, String name, BigDecimal balance) {
        final var rest = getRest();
        final var patientId = ids.get(patientIndex - 1);

        final var resp = rest.get(
                new RestRequest("patient/account/" + patientId, Map.of("date", date + "T00:00:00.000Z")),
                new TypeReference<PatientAccountDTO>() {});

        assertThat(resp.getStatus()).isEqualTo(200);
        final var data = resp.getData();
        assertThat(data).isNotNull();

        assertThat(data.name()).isEqualTo(name);

        final var actualBalance = data.balance();
        assertThat(actualBalance).usingComparator(BigDecimal::compareTo).isEqualTo(balance);
    }
}
