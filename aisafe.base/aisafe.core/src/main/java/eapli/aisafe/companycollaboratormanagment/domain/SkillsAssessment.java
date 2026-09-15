package eapli.aisafe.companycollaboratormanagment.domain;

import eapli.aisafe.companycollaboratormanagment.application.InvalidSkillsAssessmentDate;
import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class SkillsAssessment implements ValueObject {

    private static final int VALIDITY_YEARS = 5;

    @Column(name = "ASSESSMENT_DATE")
    private LocalDate assessmentDate;

    protected SkillsAssessment() {}

    private SkillsAssessment(final LocalDate assessmentDate) {
        Preconditions.nonNull(assessmentDate, "Assessment date cannot be null");
        if (assessmentDate.isAfter(LocalDate.now())) {
            throw new InvalidSkillsAssessmentDate("Assessment date cannot be in the future");
        }
        this.assessmentDate = assessmentDate;
    }

    public static SkillsAssessment valueOf(final LocalDate date) {
        return new SkillsAssessment(date);
    }

    public boolean isValid() {
        return assessmentDate.plusYears(VALIDITY_YEARS).isAfter(LocalDate.now());
    }

    public LocalDate date() {
        return assessmentDate;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SkillsAssessment that = (SkillsAssessment) o;
        return Objects.equals(assessmentDate, that.assessmentDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assessmentDate);
    }

    @Override
    public String toString() {
        return "SkillsAssessment{assessmentDate=" + assessmentDate + '}';
    }
}