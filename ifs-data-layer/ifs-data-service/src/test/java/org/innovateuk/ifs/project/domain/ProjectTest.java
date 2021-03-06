package org.innovateuk.ifs.project.domain;

import org.innovateuk.ifs.address.domain.Address;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.project.resource.ApprovalType;
import org.innovateuk.ifs.user.domain.Organisation;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Predicate;

import static org.innovateuk.ifs.project.builder.PartnerOrganisationBuilder.newPartnerOrganisation;
import static org.innovateuk.ifs.project.builder.ProjectBuilder.newProject;
import static org.innovateuk.ifs.project.builder.ProjectUserBuilder.newProjectUser;
import static org.innovateuk.ifs.user.builder.OrganisationBuilder.newOrganisation;
import static org.innovateuk.ifs.invite.domain.ProjectParticipantRole.PROJECT_FINANCE_CONTACT;
import static org.innovateuk.ifs.invite.domain.ProjectParticipantRole.PROJECT_MANAGER;
import static org.innovateuk.ifs.invite.domain.ProjectParticipantRole.PROJECT_PARTNER;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class ProjectTest {
    Long id;
    Application application;
    Project project;
    LocalDate startDate;
    Address address;
    Long durationInMonths;
    String name;
    ZonedDateTime documentsSubmittedDate;

    @Before
    public void setUp() throws Exception {
        id = 0L;
        application = new Application();
        startDate = LocalDate.now();
        address = new Address();
        durationInMonths = 12L;
        name = "My Project";

        project = new Project(id, application, startDate, address, durationInMonths, name, documentsSubmittedDate, ApprovalType.UNSET);
    }

    @Test
    public void applicationShouldReturnCorrectAttributeValues() throws Exception {
        assertEquals(project.getId(), id);
        assertEquals(project.getApplication(), application);
        assertEquals(project.getTargetStartDate(), startDate);
        assertEquals(project.getAddress(), address);
        assertEquals(project.getDurationInMonths(), durationInMonths);
        assertEquals(project.getName(), name);
        assertEquals(project.getDocumentsSubmittedDate(), documentsSubmittedDate);
    }

    @Test
    public void testGetOrganisations() {
        Organisation org1 = newOrganisation().build();
        Organisation org2 = newOrganisation().build();
        List<PartnerOrganisation> partnerOrganisations = newPartnerOrganisation().withOrganisation(org1, org2).build(2);
        Project project = newProject().withPartnerOrganisations(partnerOrganisations).build();
        assertNotNull(project.getOrganisations());
        assertEquals(org1, project.getOrganisations().get(0));
        assertEquals(org2, project.getOrganisations().get(1));
    }

    @Test
    public void testGetOrganisationsFilter() {
        String orgName = "a name to filter on";
        Organisation org1 = newOrganisation().withName(orgName).build();
        newPartnerOrganisation().withProject(project).withOrganisation(org1).build(2);

        Predicate<Organisation> shouldRemove = o -> !orgName.equals(o.getName());
        Predicate<Organisation> shouldNotRemove = o -> orgName.equals(o.getName());
        assertNotNull(project.getOrganisations(shouldRemove));
        assertTrue(project.getOrganisations(shouldRemove).isEmpty());
        assertNotNull(project.getOrganisations(shouldNotRemove));
        assertEquals(1, project.getOrganisations(shouldNotRemove).size());
    }

    @Test
    public void testGetProjectUsersFilter() {
        Project project = newProject().withProjectUsers(newProjectUser().withRole(PROJECT_PARTNER).build(1)).build();
        Predicate<ProjectUser> shouldRemove = pu -> PROJECT_PARTNER != pu.getRole();
        Predicate<ProjectUser> shouldNotRemove = pu -> PROJECT_PARTNER == pu.getRole();
        assertNotNull(project.getProjectUsers(shouldRemove));
        assertTrue(project.getProjectUsers(shouldRemove).isEmpty());
        assertNotNull(project.getProjectUsers(shouldNotRemove));
        assertEquals(1, project.getProjectUsers(shouldNotRemove).size());
    }

    @Test
    public void testGetProjectUsersWithRole() {
        ProjectUser pu1 = newProjectUser().withRole(PROJECT_PARTNER).build();
        ProjectUser pu2 = newProjectUser().withRole(PROJECT_FINANCE_CONTACT).build();
        Project project = newProject().withProjectUsers(asList(pu1, pu2)).build();
        assertNotNull(project.getProjectUsersWithRole(PROJECT_PARTNER));
        assertEquals(1, project.getProjectUsersWithRole(PROJECT_PARTNER).size());
        assertEquals(pu1, project.getProjectUsersWithRole(PROJECT_PARTNER).get(0));
        assertNotNull(project.getProjectUsersWithRole(PROJECT_MANAGER));
        assertTrue(project.getProjectUsersWithRole(PROJECT_MANAGER).isEmpty());
    }

}
