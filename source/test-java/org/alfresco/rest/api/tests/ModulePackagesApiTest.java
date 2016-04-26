
package org.alfresco.rest.api.tests;

import static org.alfresco.rest.api.tests.util.RestApiUtil.parsePaging;
import static org.alfresco.rest.api.tests.util.RestApiUtil.parseRestApiEntries;
import static org.alfresco.rest.api.tests.util.RestApiUtil.parseRestApiEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.alfresco.rest.api.model.ModulePackage;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Basic modulepackages api calls
 * @author Gethin James.
 */
public class ModulePackagesApiTest extends AbstractBaseApiTest
{
    public static final String MODULEPACKAGES = "modulepackages";
    protected String nonAdminUserName;

    @Before
    public void setup() throws Exception
    {
        this.nonAdminUserName = createUser("nonAdminUser" + System.currentTimeMillis());
    }

    @Test
    public void testAllModulePackages() throws Exception
    {
        HttpResponse response = getAll(MODULEPACKAGES, nonAdminUserName, null, HttpStatus.SC_OK);
        assertNotNull(response);

        PublicApiClient.ExpectedPaging paging = parsePaging(response.getJsonResponse());
        assertNotNull(paging);

        if (paging.getCount() > 0)
        {
            List<ModulePackage> modules = parseRestApiEntries(response.getJsonResponse(), ModulePackage.class);
            assertNotNull(modules);
            assertEquals(paging.getCount().intValue(), modules.size());
        }

    }

    @Test
    public void testSingleModulePackage() throws Exception
    {
        HttpResponse response = getSingle(MODULEPACKAGES, nonAdminUserName, "NonSENSE_NOTFOUND", HttpStatus.SC_NOT_FOUND);
        assertNotNull(response);

        response = getSingle(MODULEPACKAGES, nonAdminUserName, "alfresco-simple-module", HttpStatus.SC_OK);
        assertNotNull(response);

        ModulePackage simpleModule = parseRestApiEntry(response.getJsonResponse(),ModulePackage.class);
        assertNotNull(simpleModule);
        assertTrue("Simple module must be the correct version","1.0.0-SNAPSHOT".equals(simpleModule.getVersion().toString()));
    }

    @Override
    public String getScope()
    {
        return "private";
    }
}
