package org.innovateuk.ifs.documentation;

import org.springframework.restdocs.payload.FieldDescriptor;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class AssessorCreatedInviteResourceDocs {

    public static final FieldDescriptor[] assessorCreatedInviteResourceFields = {
            fieldWithPath("firstName").description("First name of the invitee"),
            fieldWithPath("lastName").description("Last name of the invitee"),
            fieldWithPath("innovationArea").description("Innovation area of the invitee"),
            fieldWithPath("compliant").description("Flag to signify if the invitee is compliant. An invitee who is also an existing assessor is compliant if, and only if they’ve completed their Skills and Business Type, and they’ve completed their DoI, and they’ve signed a Contract."),
            fieldWithPath("email").description("E-mail address of the invitee"),
    };
}