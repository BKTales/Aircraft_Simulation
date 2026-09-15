package eapli.aisafe.aircontrolarea.application;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.application.exceptions.OverlapBoundaryException;
import eapli.aisafe.aircontrolarea.domain.AirControlAreaName;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundaryService;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.aircontrolarea.domain.MinFuelRequirement;
import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.List;

public class AirControlAreaService {
    private final AirControlAreaRepository repository;
    private final GeographicBoundaryService collisionService;

    // Construtor vazio que ajuda a instaniar nos controllers facilmente
    public AirControlAreaService() {
        this(PersistenceContext.repositories().airControlArea());
    }

    public AirControlAreaService(AirControlAreaRepository repo) {
        this.repository = repo;
        this.collisionService = new GeographicBoundaryService();
    }

    public AirControlArea registerNewArea(String name, List<float[]> rawCoords, float minFuel) {
        List<GeographicCoords> coords = new ArrayList<>();
        for (float[] p : rawCoords) {
            coords.add(GeographicCoords.valueOf(p[0], p[1]));
        }

        GeographicBoundary newBoundary = GeographicBoundary.valueOf(coords);
        AirControlAreaName areaName = AirControlAreaName.valueOf(name);
        MinFuelRequirement fuel = MinFuelRequirement.valueOf(minFuel);

        Iterable<AirControlArea> existingAreas = repository.findAll();
        for (AirControlArea existing : existingAreas) {
            if (collisionService.checkCollision(newBoundary, existing.getGeographicBoundary())) {
                throw new OverlapBoundaryException(new ArrayList<>(newBoundary.getGeoCords()), new ArrayList<>(existing.getGeographicBoundary().getGeoCords()));
            }
        }

        AirControlArea newArea = new AirControlArea(areaName, newBoundary, fuel);

        return repository.save(newArea);
    }

    public Iterable<AirControlArea> availableAreas() {
        return repository.findAll();
    }
}
