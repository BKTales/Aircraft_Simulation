package eapli.aisafe.airportmanagement.application;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airportmanagement.domain.AirportIATACode;
import eapli.aisafe.airportmanagement.domain.AirportICAOCode;
import eapli.aisafe.airportmanagement.domain.Coordinates;
import eapli.aisafe.airportmanagement.repositories.AirportRepository;
import eapli.framework.domain.repositories.IntegrityViolationException;

/**
 * Domain service responsible for registering airports and querying related data.
 *
 * <p>Enforces uniqueness of IATA and ICAO codes and automatically resolves the
 * air control area that geographically contains the given coordinates before
 * persisting a new {@link Airport}.</p>
 *
 * @author aisafe team
 */
public class AirportService {

    private final AirportRepository airportRepository;
    private final AirControlAreaRepository areaRepository;

    /**
     * Creates a new {@code AirportService}.
     *
     * @param airportRepository the repository for {@link Airport} instances
     * @param areaRepository    the repository for air control areas
     * @throws IllegalArgumentException if either repository is {@code null}
     */
    public AirportService(final AirportRepository airportRepository,
                          final AirControlAreaRepository areaRepository) {
        if(airportRepository == null || areaRepository == null){
            throw new IllegalArgumentException("Airport repository and area repository are required.");
        }
        this.airportRepository = airportRepository;
        this.areaRepository = areaRepository;
    }

    /**
     * Registers a new airport, automatically associating it with the air control
     * area whose geographic boundary contains the given coordinates.
     *
     * @param iataCode        the airport's 3-letter IATA code
     * @param icaoCode        the airport's 4-character ICAO code
     * @param latitude        the latitude in decimal degrees (x axis)
     * @param longitude       the longitude in decimal degrees (y axis)
     * @param elevationMeters the elevation in metres above sea level
     * @return the persisted {@link Airport}
     * @throws NoAreaFoundForCoordinatesException    if no area contains the given coordinates
     * @throws AirportIATACodeAlreadyExistsException if the IATA code is already registered
     * @throws AirportICAOCodeAlreadyExistsException if the ICAO code is already registered
     */
    public Airport registerAirport(final String iataCode, final String icaoCode,
                                   final double latitude, final double longitude,
                                   final double elevationMeters) {
        final AreaCode areaCode = findContainingArea(latitude, longitude);

        final AirportIATACode iata = AirportIATACode.valueOf(iataCode);
        final AirportICAOCode icao = AirportICAOCode.valueOf(icaoCode);
        final Coordinates coordinates = Coordinates.valueOf(latitude, longitude, elevationMeters);

        if(airportRepository.ofIdentity(iata).isPresent()){
            throw new AirportIATACodeAlreadyExistsException(iataCode);
        }
        if(airportRepository.findByIcaoCode(icao).isPresent()){
            throw new AirportICAOCodeAlreadyExistsException(icaoCode);
        }

        final Airport airport = new Airport(iata, icao, coordinates, areaCode);
        try {
            return airportRepository.save(airport);
        } catch(final IntegrityViolationException ex) {
            if(airportRepository.ofIdentity(iata).isPresent()){
                throw new AirportIATACodeAlreadyExistsException(iataCode);
            }
            if(airportRepository.findByIcaoCode(icao).isPresent()){
                throw new AirportICAOCodeAlreadyExistsException(icaoCode);
            }
            throw ex;
        }
    }

    private AreaCode findContainingArea(final double latitude, final double longitude) {
        final GeographicCoords point = GeographicCoords.valueOf(latitude, longitude);
        for(final AirControlArea area : areaRepository.findAll()){
            if(area.getGeographicBoundary().contains(point)){
                return area.getAreaCode();
            }
        }
        throw new NoAreaFoundForCoordinatesException(latitude, longitude);
    }
}
