package org.innovateuk.ifs.thread.security;

import org.innovateuk.ifs.BasePermissionRulesTest;
import org.innovateuk.ifs.finance.domain.ProjectFinance;
import org.innovateuk.ifs.threads.security.ProjectFinanceQueryPermissionRules;
import org.innovateuk.ifs.user.domain.Organisation;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.threads.resource.FinanceChecksSectionType;
import org.innovateuk.threads.resource.PostResource;
import org.innovateuk.threads.resource.QueryResource;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.innovateuk.ifs.finance.domain.builder.ProjectFinanceBuilder.newProjectFinance;
import static org.innovateuk.ifs.user.builder.RoleBuilder.newRole;
import static org.innovateuk.ifs.user.builder.RoleResourceBuilder.newRoleResource;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.UserRoleType.FINANCE_CONTACT;
import static org.innovateuk.ifs.user.resource.UserRoleType.PROJECT_FINANCE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ProjectFinanceQueryPermissionRulesTest extends BasePermissionRulesTest<ProjectFinanceQueryPermissionRules> {
    private QueryResource queryResource;
    private UserResource projectFinanceUser;
    private UserResource financeContactUser;
    private UserResource incorrectFinanceContactUser;

    @Before
    public void setUp() throws Exception {
        projectFinanceUser = projectFinanceUser();
        financeContactUser = getUserWithRole(FINANCE_CONTACT);

        queryResource = queryWithoutPosts();
        queryResource.posts.add(new PostResource(1L, projectFinanceUser, "The body", new ArrayList<>(), LocalDateTime.now()));

        incorrectFinanceContactUser = newUserResource().withId(1993L).withRolesGlobal(newRoleResource()
                .withType(FINANCE_CONTACT).build(1)).build();
        incorrectFinanceContactUser.setId(1993L);
    }

    private QueryResource queryWithoutPosts() {
        return new QueryResource(1L, 22L, new ArrayList<>(),
                FinanceChecksSectionType.VIABILITY, "First Query", true, LocalDateTime.now());
    }

    @Override
    protected ProjectFinanceQueryPermissionRules supplyPermissionRulesUnderTest() {
        return new ProjectFinanceQueryPermissionRules();
    }

    @Test
    public void testThatOnlyProjectFinanceProjectFinanceUsersCanCreateQueries() throws Exception {
        assertTrue(rules.onlyProjectFinanceUsersCanCreateQueries(queryResource, projectFinanceUser));
        assertFalse(rules.onlyProjectFinanceUsersCanCreateQueries(queryResource, financeContactUser));
    }

    @Test
    public void testThatNewQueryMustContainInitialPost() throws Exception {
        assertTrue(rules.onlyProjectFinanceUsersCanCreateQueries(queryResource, projectFinanceUser));
        assertFalse(rules.onlyProjectFinanceUsersCanCreateQueries(queryWithoutPosts(), financeContactUser));
    }

    @Test
    public void testThatNewQueryInitialPostAuthorMustBeTheCurrentUser() throws Exception {
        assertTrue(rules.onlyProjectFinanceUsersCanCreateQueries(queryResource, projectFinanceUser));
        UserResource anotherProjectFinanceUser = newUserResource().withId(675L)
                .withRolesGlobal(newRoleResource().withType(PROJECT_FINANCE).build(1)).build();
        assertFalse(rules.onlyProjectFinanceUsersCanCreateQueries(queryResource, anotherProjectFinanceUser));
    }

    @Test
    public void testThatFirstPostMustComeFromTheProjectFinanceUser() throws Exception {
        QueryResource queryWithoutPosts = queryWithoutPosts();
        assertTrue(rules.onlyProjectFinanceUsersOrFinanceContactAddPostToTheirQueries(queryWithoutPosts, projectFinanceUser));
        when(projectFinanceRepositoryMock.findOne(queryWithoutPosts.contextClassPk))
                .thenReturn(mockedProjectFinanceWithUserAsFinanceContact(financeContactUser));
        assertFalse(rules.onlyProjectFinanceUsersOrFinanceContactAddPostToTheirQueries(queryWithoutPosts, financeContactUser));
    }

    @Test
    public void testThatOnlyTheProjectFinanceUserOrTheCorrectFinanceContactCanReplyToAQuery() throws Exception {
        when(projectFinanceRepositoryMock.findOne(queryResource.contextClassPk))
                .thenReturn(mockedProjectFinanceWithUserAsFinanceContact(financeContactUser));
        assertTrue(rules.onlyProjectFinanceUsersOrFinanceContactAddPostToTheirQueries(queryResource, projectFinanceUser));
        assertTrue(rules.onlyProjectFinanceUsersOrFinanceContactAddPostToTheirQueries(queryResource, financeContactUser));
        assertFalse(rules.onlyProjectFinanceUsersOrFinanceContactAddPostToTheirQueries(queryResource, incorrectFinanceContactUser));
    }

    @Test
    public void testThatOnlyProjectFinanceUsersOrFinanceContactCanViewTheirQueries() {
        assertTrue(rules.onlyProjectFinanceUsersOrFinanceContactCanViewTheirQueries(queryResource, projectFinanceUser));
        when(projectFinanceRepositoryMock.findOne(queryResource.contextClassPk))
                .thenReturn(mockedProjectFinanceWithUserAsFinanceContact(financeContactUser));
        assertTrue(rules.onlyProjectFinanceUsersOrFinanceContactCanViewTheirQueries(queryResource, financeContactUser));
        assertFalse(rules.onlyProjectFinanceUsersOrFinanceContactCanViewTheirQueries(queryResource, incorrectFinanceContactUser));
    }


    private final ProjectFinance mockedProjectFinanceWithUserAsFinanceContact(UserResource user) {
        Organisation organisation = new Organisation();
        organisation.addUser(newUser().withRoles(newRole().withType(FINANCE_CONTACT).build(1)).withId(financeContactUser.getId()).build());
        return newProjectFinance().withOrganisation(organisation).build();
    }

}