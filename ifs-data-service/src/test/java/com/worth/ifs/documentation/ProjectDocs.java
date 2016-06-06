package com.worth.ifs.documentation;

import com.worth.ifs.project.builder.ProjectResourceBuilder;
import org.springframework.restdocs.payload.FieldDescriptor;

import java.time.LocalDate;

import static com.worth.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class ProjectDocs {
    public static final FieldDescriptor[] projectResourceFields = {
            fieldWithPath("id").description("Id of the project (which will be same as id of corresponding application)"),
            fieldWithPath("targetStartDate").description("Expected target start date for the project"),
            fieldWithPath("address").description("Address where the project is expected to be executed from"),
            fieldWithPath("durationInMonths").description("Duration that the project is expeceted to last"),
            fieldWithPath("projectManager").description("Project manager designated for the project")
    };

    public static final ProjectResourceBuilder projectResourceBuilder = newProjectResource()
            .withId(1L)
            .withTargetStartDate(LocalDate.now())
            .withAddress(1L)
            .withDuration(1L)
            .withProjectManager(1L);
}