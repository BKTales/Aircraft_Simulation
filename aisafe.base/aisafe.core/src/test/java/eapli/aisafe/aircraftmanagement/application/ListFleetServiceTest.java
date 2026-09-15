package eapli.aisafe.aircraftmanagement.application;



import eapli.aisafe.aircraftmanagement.AircraftTestFixtures;
import eapli.aisafe.aircraftmanagement.domain.Aircraft;

import eapli.aisafe.aircraftmanagement.domain.YearOfManufacture;

import eapli.aisafe.aircraftmanagement.domain.AircraftRegistration;

import eapli.aisafe.aircraftmanagement.domain.CabinConfiguration;

import eapli.aisafe.aircraftmanagement.domain.NumberOfFlightCrew;

import eapli.aisafe.aircraftmanagement.domain.OperationalStatus;

import eapli.aisafe.aircraftmanagement.domain.RegistrationCountry;

import eapli.aisafe.aircraftmanagement.repositories.AircraftRepository;

import eapli.aisafe.aircraftmodelmanagement.domain.AerodynamicCoefficients;

import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;

import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;

import eapli.aisafe.aircraftmodelmanagement.domain.AircraftType;

import eapli.aisafe.aircraftmodelmanagement.domain.ModelName;

import eapli.aisafe.aircraftmodelmanagement.domain.NumberOfEngines;

import eapli.aisafe.aircraftmodelmanagement.domain.NumberOfSeats;

import eapli.aisafe.aircraftmodelmanagement.domain.PerformanceSpec;

import eapli.aisafe.aircraftmodelmanagement.domain.WeightSpecification;

import eapli.aisafe.aircraftmodelmanagement.domain.WingGeometry;

import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;

import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;


import eapli.aisafe.manufacturermanagement.domain.CountryCode;
import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;

import eapli.aisafe.manufacturermanagement.domain.ManufacturerName;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;



import java.time.Year;

import java.util.ArrayList;

import java.util.List;

import java.util.Optional;



import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.when;



@ExtendWith(MockitoExtension.class)

class ListFleetServiceTest {



    @Mock

    private AircraftRepository aircraftRepository;



    @Mock

    private AircraftModelRepository aircraftModelRepository;



    private AircraftService service;

    private final IATACode tp = IATACode.valueOf("TP");

    private final IATACode fr = IATACode.valueOf("FR");



    private AircraftModel modelA320;

    private AircraftModel modelB737;



    @BeforeEach

    void setUp() {

        service = new AircraftService(aircraftRepository, aircraftModelRepository);

        modelA320 = model("A320", new Manufacturer(ManufacturerId.valueOf("MAN01"), new ManufacturerName("John"), new CountryCode("PT")), 180);

        modelB737 = model("B737",new Manufacturer(ManufacturerId.valueOf("MAN02"), new ManufacturerName("Carlos"), new CountryCode("ES")), 160);

    }



    @Test

    void ensureOnlyCompanyAircraftAreReturned() {

        when(aircraftRepository.findByOwnerCompany(tp)).thenReturn(List.of(

                aircraft("CS-TP1", "A320", tp, OperationalStatus.ACTIVE, 180, 5)));

        when(aircraftRepository.findByOwnerCompany(fr)).thenReturn(List.of(

                aircraft("CS-FR1", "B737", fr, OperationalStatus.ACTIVE, 160, 8)));



        final List<Aircraft> tpFleet = toList(service.listFleet(tp, FleetListCriteria.unfiltered()));

        assertEquals(1, tpFleet.size());

        assertEquals("CS-TP1", tpFleet.get(0).identity().toString());



        final List<Aircraft> frFleet = toList(service.listFleet(fr, FleetListCriteria.unfiltered()));

        assertEquals(1, frFleet.size());

        assertEquals("CS-FR1", frFleet.get(0).identity().toString());

    }



    @Test

    void ensureAllAircraftReturnedWhenNoFilterApplied() {

        when(aircraftRepository.findByOwnerCompany(tp)).thenReturn(List.of(

                aircraft("CS-A1", "A320", tp, OperationalStatus.ACTIVE, 150, 3),

                aircraft("CS-A2", "A320", tp, OperationalStatus.DECOMMISSIONED, 150, 10)));



        assertEquals(2, toList(service.listFleet(tp, FleetListCriteria.unfiltered())).size());

    }



    @Test

    void ensureFilterByModelReturnsCorrectAircraft() {

        when(aircraftRepository.findByOwnerCompanyAndModel(tp, AircraftModelId.valueOf("A320")))

                .thenReturn(List.of(aircraft("CS-M1", "A320", tp, OperationalStatus.ACTIVE, 150, 4)));



        final List<Aircraft> result = toList(service.listFleet(tp, FleetListCriteria.byModel(AircraftModelId.valueOf("A320"))));

        assertEquals(1, result.size());

        assertEquals("CS-M1", result.get(0).identity().toString());

    }



    @Test
    void ensureFilterByMakerReturnsCorrectAircraft() {
        when(aircraftRepository.findByOwnerCompany(tp)).thenReturn(List.of(
                aircraft("CS-MK1", "A320", tp, OperationalStatus.ACTIVE, 150, 5),
                aircraft("CS-MK2", "B737", tp, OperationalStatus.ACTIVE, 160, 6)));

        when(aircraftModelRepository.findByID(AircraftModelId.valueOf("A320"))).thenReturn(Optional.of(modelA320));
        when(aircraftModelRepository.findByID(AircraftModelId.valueOf("B737"))).thenReturn(Optional.of(modelB737));

        final List<Aircraft> result = toList(
                service.listFleet(tp, FleetListCriteria.byManufacturer(ManufacturerId.valueOf("MAN01"))));

        assertEquals(1, result.size());
        assertEquals("CS-MK1", result.get(0).identity().toString());
    }



    @Test

    void ensureFilterByCapacityExactlyReturnsCorrectAircraft() {

        when(aircraftRepository.findByOwnerCompany(tp)).thenReturn(List.of(

                aircraft("CS-C1", "A320", tp, OperationalStatus.ACTIVE, 180, 2),

                aircraft("CS-C2", "A320", tp, OperationalStatus.ACTIVE, 150, 3)));



        final List<Aircraft> result = toList(

                service.listFleet(tp, FleetListCriteria.byPassengerCapacity(180, FleetNumericComparison.EQUAL)));

        assertEquals(1, result.size());

        assertEquals(180, result.get(0).cabinConfiguration().totalSeats());

    }



    @Test

    void ensureFilterByCapacityGreaterThanReturnsCorrectAircraft() {

        when(aircraftRepository.findByOwnerCompany(tp)).thenReturn(List.of(

                aircraft("CS-C1", "A320", tp, OperationalStatus.ACTIVE, 180, 2),

                aircraft("CS-C2", "A320", tp, OperationalStatus.ACTIVE, 150, 3)));



        final List<Aircraft> result = toList(

                service.listFleet(tp, FleetListCriteria.byPassengerCapacity(160, FleetNumericComparison.GREATER_THAN)));

        assertEquals(1, result.size());

        assertEquals("CS-C1", result.get(0).identity().toString());

    }



    @Test

    void ensureFilterByCapacityLessThanReturnsCorrectAircraft() {

        when(aircraftRepository.findByOwnerCompany(tp)).thenReturn(List.of(

                aircraft("CS-C1", "A320", tp, OperationalStatus.ACTIVE, 180, 2),

                aircraft("CS-C2", "A320", tp, OperationalStatus.ACTIVE, 150, 3)));



        final List<Aircraft> result = toList(

                service.listFleet(tp, FleetListCriteria.byPassengerCapacity(160, FleetNumericComparison.LESS_THAN)));

        assertEquals(1, result.size());

        assertEquals("CS-C2", result.get(0).identity().toString());

    }



    @Test

    void ensureFilterByAgeExactlyReturnsCorrectAircraft() {

        when(aircraftRepository.findByOwnerCompany(tp)).thenReturn(List.of(

                aircraft("CS-AG1", "A320", tp, OperationalStatus.ACTIVE, 150, 12),

                aircraft("CS-AG2", "A320", tp, OperationalStatus.ACTIVE, 150, 5)));



        final List<Aircraft> result = toList(

                service.listFleet(tp, FleetListCriteria.byAge(12, FleetNumericComparison.EQUAL)));

        assertEquals(1, result.size());

        assertEquals(12, result.get(0).ageInYears());

    }



    @Test

    void ensureFilterByAgeGreaterThanReturnsCorrectAircraft() {

        when(aircraftRepository.findByOwnerCompany(tp)).thenReturn(List.of(

                aircraft("CS-AG1", "A320", tp, OperationalStatus.ACTIVE, 150, 12),

                aircraft("CS-AG2", "A320", tp, OperationalStatus.ACTIVE, 150, 5)));



        final List<Aircraft> result = toList(

                service.listFleet(tp, FleetListCriteria.byAge(8, FleetNumericComparison.GREATER_THAN)));

        assertEquals(1, result.size());

        assertEquals("CS-AG1", result.get(0).identity().toString());

    }



    @Test

    void ensureFilterByAgeLessThanReturnsCorrectAircraft() {

        when(aircraftRepository.findByOwnerCompany(tp)).thenReturn(List.of(

                aircraft("CS-AG1", "A320", tp, OperationalStatus.ACTIVE, 150, 12),

                aircraft("CS-AG2", "A320", tp, OperationalStatus.ACTIVE, 150, 5)));



        final List<Aircraft> result = toList(

                service.listFleet(tp, FleetListCriteria.byAge(8, FleetNumericComparison.LESS_THAN)));

        assertEquals(1, result.size());

        assertEquals("CS-AG2", result.get(0).identity().toString());

    }



    @Test

    void ensureModelsUsedInFleetReturnsDistinctModels() {

        when(aircraftRepository.findByOwnerCompany(tp)).thenReturn(List.of(

                aircraft("CS-1", "A320", tp, OperationalStatus.ACTIVE, 150, 3),

                aircraft("CS-2", "A320", tp, OperationalStatus.ACTIVE, 160, 4),

                aircraft("CS-3", "B737", tp, OperationalStatus.ACTIVE, 140, 5)));

        when(aircraftModelRepository.findByID(AircraftModelId.valueOf("A320"))).thenReturn(Optional.of(modelA320));

        when(aircraftModelRepository.findByID(AircraftModelId.valueOf("B737"))).thenReturn(Optional.of(modelB737));



        final List<AircraftModel> models = service.modelsUsedInFleet(tp);

        assertEquals(2, models.size());

        assertEquals("A320", models.get(0).identity().toString());

        assertEquals("B737", models.get(1).identity().toString());

    }



    @Test

    void ensureManufacturersUsedInFleetReturnsDistinctMakers() {

        when(aircraftRepository.findByOwnerCompany(tp)).thenReturn(List.of(

                aircraft("CS-1", "A320", tp, OperationalStatus.ACTIVE, 150, 3),

                aircraft("CS-2", "B737", tp, OperationalStatus.ACTIVE, 160, 4),

                aircraft("CS-3", "B737", tp, OperationalStatus.ACTIVE, 140, 5)));

        when(aircraftModelRepository.findByID(AircraftModelId.valueOf("A320"))).thenReturn(Optional.of(modelA320));

        when(aircraftModelRepository.findByID(AircraftModelId.valueOf("B737"))).thenReturn(Optional.of(modelB737));



        final List<ManufacturerId> makers = service.manufacturersUsedInFleet(tp);

        assertEquals(2, makers.size());

        assertEquals("MAN01", makers.get(0).toString());

        assertEquals("MAN02", makers.get(1).toString());

    }



    @Test

    void ensureDecommissionedAircraftAreIncludedInList() {

        when(aircraftRepository.findByOwnerCompany(tp)).thenReturn(List.of(

                aircraft("CS-D1", "A320", tp, OperationalStatus.DECOMMISSIONED, 150, 15)));



        final List<Aircraft> result = toList(service.listFleet(tp, FleetListCriteria.unfiltered()));

        assertEquals(1, result.size());

        assertFalse(result.get(0).isActive());

    }



    @Test

    void ensureEmptyListReturnedWhenNoAircraftMatchFilter() {

        when(aircraftRepository.findByOwnerCompany(tp)).thenReturn(List.of(

                aircraft("CS-AG1", "A320", tp, OperationalStatus.ACTIVE, 150, 5)));



        assertTrue(toList(service.listFleet(tp, FleetListCriteria.byAge(99, FleetNumericComparison.EQUAL))).isEmpty());

    }



    private static List<Aircraft> toList(final Iterable<Aircraft> iterable) {

        final List<Aircraft> list = new ArrayList<>();

        iterable.forEach(list::add);

        return list;

    }



    private static Aircraft aircraft(final String reg,

                                     final String modelId,

                                     final IATACode owner,

                                     final OperationalStatus status,

                                     final int seats,

                                     final int age) {

        return new Aircraft(
                new AircraftRegistration(reg),
                AircraftTestFixtures.stubModel(modelId),
                AircraftTestFixtures.stubEngine("E1"),
                CabinConfiguration.ofEconomyBusinessFirst(seats, 0, 0),
                new RegistrationCountry("PT"),
                owner,
                status,
                NumberOfFlightCrew.valueOf(2),
                YearOfManufacture.valueOf(Year.now().getValue() - age));
    }



    private static AircraftModel model(final String id, final Manufacturer maker, final int maxSeats) {

        return new AircraftModel(

                AircraftModelId.valueOf(id),

                ModelName.valueOf("Model-" + id),

                AircraftType.PASSENGER,

                maker,

                WeightSpecification.valueOf(100, 80, 50),

                WingGeometry.valueOf(120, 35),

                AerodynamicCoefficients.valueOf(0.02, 1.5),

                PerformanceSpec.valueOf(12000, 230, 24000, 5000),

                NumberOfEngines.valueOf(2),

                NumberOfSeats.valueOf(maxSeats));

    }

}

