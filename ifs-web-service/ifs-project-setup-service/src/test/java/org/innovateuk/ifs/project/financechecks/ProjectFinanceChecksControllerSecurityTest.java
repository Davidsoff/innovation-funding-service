package org.innovateuk.ifs.project.financechecks;


import org.innovateuk.ifs.project.BaseProjectSetupControllerSecurityTest;
import org.innovateuk.ifs.project.financechecks.controller.ProjectFinanceChecksController;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;

public class ProjectFinanceChecksControllerSecurityTest extends BaseProjectSetupControllerSecurityTest<ProjectFinanceChecksController> {

        @Override
        protected Class<? extends ProjectFinanceChecksController> getClassUnderTest() {
            return ProjectFinanceChecksController.class;
        }

        @Test
        public void testPublicMethods() {
            assertSecured(() -> classUnderTest.viewFinanceChecks(null,123L, null),
                    permissionRules -> permissionRules.partnerCanAccessFinanceChecksSection(eq(123L), isA(UserResource.class)));
        }
    }