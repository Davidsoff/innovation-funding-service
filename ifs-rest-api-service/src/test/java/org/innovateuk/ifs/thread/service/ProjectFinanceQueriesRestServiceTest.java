package org.innovateuk.ifs.thread.service;

import org.innovateuk.ifs.BaseRestServiceUnitTest;
import org.innovateuk.ifs.project.finance.service.ProjectFinanceQueriesRestService;
import org.innovateuk.threads.resource.QueryResource;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.queryResourceListType;
import static org.junit.Assert.assertSame;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

public class ProjectFinanceQueriesRestServiceTest extends BaseRestServiceUnitTest<ProjectFinanceQueriesRestService> {
    private final static String serviceURL = "/project/finance/queries";

    @Override
    protected ProjectFinanceQueriesRestService registerRestServiceUnderTest() {
        return new ProjectFinanceQueriesRestService();
    }

    @Test
    public void test_findAll() throws Exception {
        final List<QueryResource> expected = asList(queryWithId(33L), queryWithId(92L));
        setupGetWithRestResultExpectations(serviceURL + "/all/22", queryResourceListType(), expected, OK);
        final List<QueryResource> response = service.findAll(22L).getSuccessObject();
        assertSame(expected, response);
    }

    private QueryResource queryWithId(Long id) {
        return new QueryResource(id, null, null, null, null, false, null);

    }

    @Test
    public void test_findOne() throws Exception {
        final QueryResource query1 = queryWithId(33L);
        setupGetWithRestResultExpectations(serviceURL + "/33", QueryResource.class, query1, OK);
        final QueryResource response = service.findOne(33L).getSuccessObject();
        assertSame(query1, response);
    }

    @Test
    public void test_create() throws Exception {
        final QueryResource query1 = queryWithId(33L);
        setupPostWithRestResultExpectations(serviceURL, Long.class, query1, 33L, CREATED);
        final Long response = service.create(query1).getSuccessObject();
        assertSame(query1.id, response);
    }

}