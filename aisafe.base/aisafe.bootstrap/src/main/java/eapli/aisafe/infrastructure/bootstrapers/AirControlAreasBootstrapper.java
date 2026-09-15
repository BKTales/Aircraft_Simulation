package eapli.aisafe.infrastructure.bootstrapers;

import eapli.aisafe.aircontrolarea.application.AirControlAreaService;
import eapli.aisafe.aircontrolarea.application.exceptions.OverlapBoundaryException;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Bootstraps sample air control areas for development and demo purposes.
 *
 * <p>Non-overlapping regions across mainland Portugal, Madeira and the Azores.
 * Each polygon is defined as {@code {latitude, longitude}} pairs (i.e. x=lat, y=lon).</p>
 *
 * <p>This bootstrapper is idempotent: if the areas already exist, the overlap
 * detection will throw and the exception is silently consumed.</p>
 */
public class AirControlAreasBootstrapper implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(AirControlAreasBootstrapper.class);

    @Override
    public boolean execute() {
        final AirControlAreaService service = new AirControlAreaService(
                PersistenceContext.repositories().airControlArea());

        final List<float[]> lisboaBoundary = Arrays.asList(
                new float[]{36.4f, -10.1f},
                new float[]{39.4f, -10.1f},
                new float[]{39.4f, -6.4f},
                new float[]{36.4f, -6.4f}
        );

        final List<float[]> portoBoundary = Arrays.asList(
                new float[]{39.5f, -9.6f},
                new float[]{42.2f, -9.6f},
                new float[]{42.2f, -5.9f},
                new float[]{39.5f, -5.9f}
        );

        /** Covers MAD (LEMD) for US085 LAPR4 OPO→MAD fixture; does not overlap existing TMAs. */
        final List<float[]> centroIberiaBoundary = Arrays.asList(
                new float[]{39.4f, -4.8f},
                new float[]{41.6f, -4.8f},
                new float[]{41.6f, -2.2f},
                new float[]{39.4f, -2.2f}
        );

        final List<float[]> minhoBoundary = Arrays.asList(
                new float[]{42.25f, -9.2f},
                new float[]{43.8f, -9.2f},
                new float[]{43.8f, -6.8f},
                new float[]{42.25f, -6.8f}
        );

        final List<float[]> madeiraBoundary = Arrays.asList(
                new float[]{32.2f, -17.9f},
                new float[]{33.8f, -17.9f},
                new float[]{33.8f, -16.0f},
                new float[]{32.2f, -16.0f}
        );

        final List<float[]> azoresBoundary = Arrays.asList(
                new float[]{37.0f, -26.5f},
                new float[]{39.2f, -26.5f},
                new float[]{39.2f, -24.5f},
                new float[]{37.0f, -24.5f}
        );

        registerArea(service, "TMA Lisboa", lisboaBoundary, 2000.0f);
        registerArea(service, "TMA Porto", portoBoundary, 2000.0f);
        registerArea(service, "TMA Minho", minhoBoundary, 1800.0f);
        registerArea(service, "TMA Madeira", madeiraBoundary, 1500.0f);
        registerArea(service, "TMA Acores", azoresBoundary, 1500.0f);
        registerArea(service, "TMA Centro Iberia", centroIberiaBoundary, 2000.0f);

        return true;
    }

    private void registerArea(final AirControlAreaService service,
                               final String name,
                               final List<float[]> boundary,
                               final float minFuel) {
        try {
            service.registerNewArea(name, boundary, minFuel);
        } catch (final OverlapBoundaryException ex) {
            LOGGER.debug("Assuming air control area '{}' already exists (overlap detected).", name);
        } catch (final IllegalArgumentException ex) {
            LOGGER.warn("Could not register air control area '{}': {}", name, ex.getMessage());
        }
    }
}
