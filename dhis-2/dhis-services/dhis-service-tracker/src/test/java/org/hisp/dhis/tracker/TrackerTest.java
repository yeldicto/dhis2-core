package org.hisp.dhis.tracker;

import java.io.IOException;

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.render.RenderService;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Luciano Fiandesio
 */
public abstract class TrackerTest extends DhisSpringTest
{
    @Autowired
    private RenderService _renderService;

    @Autowired
    private UserService _userService;

    @Autowired
    protected CurrentUserService currentUserService;

    @Override
    protected void setUpTest()
        throws IOException
    {

        preCreateInjectAdminUserWithoutPersistence();

        renderService = _renderService;
        userService = _userService;

        initTest();
    }

    protected abstract void initTest()
        throws IOException;

    protected TrackerImportParams fromJson( String path )
        throws IOException
    {
        TrackerImportParams trackerImportParams = renderService.fromJson(
            new ClassPathResource( path ).getInputStream(),
            TrackerImportParams.class );
        trackerImportParams.setUser( currentUserService.getCurrentUser() );
        return trackerImportParams;
    }

}
