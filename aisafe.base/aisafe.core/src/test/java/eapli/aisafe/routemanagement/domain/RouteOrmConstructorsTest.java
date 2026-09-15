package eapli.aisafe.routemanagement.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RouteOrmConstructorsTest {

    @Test
    void protectedConstructorsExistForJpa() throws Exception {
        assertNotNull(newInstance(Route.class));
        assertNotNull(newInstance(RouteName.class));
        assertNotNull(newInstance(RouteSchedule.class));
        assertNotNull(newInstance(RouteRecurringSchedule.class));
        assertNotNull(newInstance(RecurringScheduleEntry.class));
        assertNotNull(newInstance(DeactivationDate.class));
    }

    private static Object newInstance(final Class<?> type) throws Exception {
        final Constructor<?> ctor = type.getDeclaredConstructor();
        ctor.setAccessible(true);
        return ctor.newInstance();
    }
}
