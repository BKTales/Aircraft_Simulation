package eapli.aisafe.usermanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailDomainTest {

    @Test
    void createEmailDomainWithValidDomain() {
        final EmailDomain domain = new EmailDomain("aisafe.admin.com");

        assertNotNull(domain);
        assertEquals("aisafe.admin.com", domain.identity());
    }

    @Test
    void createEmailDomainWithEmptyDomainThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new EmailDomain(""));
    }

    @Test
    void createEmailDomainWithNullDomainThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new EmailDomain(null));
    }

    @Test
    void identityReturnsDomain() {
        final EmailDomain domain = new EmailDomain("aisafe.admin.com");

        assertEquals("aisafe.admin.com", domain.identity());
    }

    @Test
    void sameAsReturnsTrueForEqualDomains() {
        final EmailDomain domain1 = new EmailDomain("aisafe.admin.com");
        final EmailDomain domain2 = new EmailDomain("aisafe.admin.com");

        assertTrue(domain1.sameAs(domain2));
    }

    @Test
    void sameAsReturnsFalseForDifferentDomains() {
        final EmailDomain domain1 = new EmailDomain("aisafe.admin.com");
        final EmailDomain domain2 = new EmailDomain("aisafe.backoffice.com");

        assertFalse(domain1.sameAs(domain2));
    }
}