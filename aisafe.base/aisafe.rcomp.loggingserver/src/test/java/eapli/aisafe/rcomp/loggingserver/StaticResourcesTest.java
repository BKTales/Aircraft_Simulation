package eapli.aisafe.rcomp.loggingserver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StaticResourcesTest {

    @Test
    void contentTypeForJavaScript() {
        assertEquals("application/javascript; charset=utf-8", StaticResources.contentType("assets/index.js"));
    }

    @Test
    void contentTypeForHtml() {
        assertEquals("text/html; charset=utf-8", StaticResources.contentType("index.html"));
    }

    @Test
    void isSpaRouteRecognizesDashboardPaths() {
        assertTrue(StaticResources.isSpaRoute("/"));
        assertTrue(StaticResources.isSpaRoute("/events"));
        assertTrue(StaticResources.isSpaRoute("/active-users"));
        assertFalse(StaticResources.isSpaRoute("/api/events"));
    }
}
