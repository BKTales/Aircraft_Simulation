package eapli.aisafe.companycollaboratormanagment.application;

import eapli.framework.representations.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@DTO
@Data
@AllArgsConstructor
public class PilotCertificationSpec {

    private String aircraftModelId;
    private String startDate;
    private String endDate;

    public PilotCertificationSpec() {
    }
}