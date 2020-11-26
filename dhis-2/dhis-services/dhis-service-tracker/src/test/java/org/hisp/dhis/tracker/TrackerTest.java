package org.hisp.dhis.tracker;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.dxf2.metadata.objectbundle.ObjectBundle;
import org.hisp.dhis.dxf2.metadata.objectbundle.ObjectBundleMode;
import org.hisp.dhis.dxf2.metadata.objectbundle.ObjectBundleParams;
import org.hisp.dhis.dxf2.metadata.objectbundle.ObjectBundleService;
import org.hisp.dhis.dxf2.metadata.objectbundle.ObjectBundleValidationService;
import org.hisp.dhis.dxf2.metadata.objectbundle.feedback.ObjectBundleValidationReport;
import org.hisp.dhis.importexport.ImportStrategy;
import org.hisp.dhis.render.RenderFormat;
import org.hisp.dhis.render.RenderService;
import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.report.TrackerImportReport;
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

    @Autowired
    private ObjectBundleService objectBundleService;

    @Autowired
    private ObjectBundleValidationService objectBundleValidationService;

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

    protected ObjectBundle setUpMetadata( String path )
        throws IOException
    {

        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( path ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportStrategy( ImportStrategy.CREATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidationReport validationReport = objectBundleValidationService.validate( bundle );
        assertTrue( validationReport.getErrorReports().isEmpty() );

        objectBundleService.commit( bundle );

        return bundle;
    }

    protected TrackerImportParams fromJson( String path )
        throws IOException
    {
        TrackerImportParams trackerImportParams = renderService.fromJson(
            new ClassPathResource( path ).getInputStream(),
            TrackerImportParams.class );
        trackerImportParams.setUser( currentUserService.getCurrentUser() );
        return trackerImportParams;
    }

    protected TrackerImportParams fromJson( String path, String userUid )
            throws IOException
    {
        TrackerImportParams trackerImportParams = renderService.fromJson(
                new ClassPathResource( path ).getInputStream(),
                TrackerImportParams.class );
        trackerImportParams.setUserId( userUid );
        return trackerImportParams;
    }

    /**
     * Makes sure that the Tracker entities in the provided TrackerBundle have the 'uid' attribute
     * identical to the json identifier.
     */
    protected TrackerBundle prepareForUpdate( TrackerBundle trackerBundle )
    {
        trackerBundle.getTrackedEntities().forEach( t -> t.setUid( t.getTrackedEntity() ) );
        trackerBundle.getEnrollments().forEach( t -> t.setUid( t.getEnrollment() ) );
        trackerBundle.getEvents().forEach( t -> t.setUid( t.getEvent() ) );

        return trackerBundle;
    }

    protected void assertNoImportErrors( TrackerImportReport report )
    {
        assertTrue( report.getValidationReport().getErrorReports().isEmpty() );
    }

}
