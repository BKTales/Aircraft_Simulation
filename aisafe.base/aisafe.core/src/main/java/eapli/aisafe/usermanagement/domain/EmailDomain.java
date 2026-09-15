package eapli.aisafe.usermanagement.domain;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name = "Email_Domain")
public class EmailDomain implements Serializable, AggregateRoot<String> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "emailDomain")
    private String domain;

    protected EmailDomain() {
        // for JPA
    }

    public EmailDomain(String domain){
        Preconditions.nonEmpty(domain);
        this.domain = domain;
    }

    @Override
    public boolean sameAs(Object other) {
        return this.domain.equals(((EmailDomain) other).domain);
    }

    @Override
    public String identity() {
        return this.domain;
    }
}
