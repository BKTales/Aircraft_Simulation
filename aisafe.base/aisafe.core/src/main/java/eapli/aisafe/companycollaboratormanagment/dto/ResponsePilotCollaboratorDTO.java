/*
 * ... (MIT License header)
 */
package eapli.aisafe.companycollaboratormanagment.dto;

import eapli.framework.representations.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO for representing a company collaborator user.
 * Used to transport output data from the application layer to the UI.
 */
@DTO
@Data
@AllArgsConstructor
public class ResponsePilotCollaboratorDTO {

    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String securityClearanceExpiryDate;
    private String skillAssessmentDate;
    private int certificationCount;
}