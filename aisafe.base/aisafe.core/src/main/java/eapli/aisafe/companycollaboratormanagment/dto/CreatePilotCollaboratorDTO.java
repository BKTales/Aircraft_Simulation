package eapli.aisafe.companycollaboratormanagment.dto;

import eapli.aisafe.companycollaboratormanagment.application.PilotCertificationSpec;
import eapli.framework.representations.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@DTO
@Data
@AllArgsConstructor
public class CreatePilotCollaboratorDTO {

    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String securityClearanceStartDate;
    private String skillDate;
    List<PilotCertificationSpec> certifications;
}