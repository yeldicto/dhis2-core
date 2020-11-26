package org.hisp.dhis.tracker.bundle;

/*
 * Copyright (c) 2004-2020, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.dxf2.metadata.objectbundle.ObjectBundle;
import org.hisp.dhis.dxf2.metadata.objectbundle.ObjectBundleMode;
import org.hisp.dhis.dxf2.metadata.objectbundle.ObjectBundleParams;
import org.hisp.dhis.dxf2.metadata.objectbundle.ObjectBundleService;
import org.hisp.dhis.dxf2.metadata.objectbundle.ObjectBundleValidationService;
import org.hisp.dhis.dxf2.metadata.objectbundle.feedback.ObjectBundleValidationReport;
import org.hisp.dhis.eventdatavalue.EventDataValue;
import org.hisp.dhis.importexport.ImportStrategy;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.render.RenderFormat;
import org.hisp.dhis.tracker.TrackerImportParams;
import org.hisp.dhis.tracker.TrackerImportService;
import org.hisp.dhis.tracker.TrackerTest;
import org.hisp.dhis.tracker.preheat.TrackerPreheat;
import org.hisp.dhis.tracker.preheat.TrackerPreheatService;
import org.hisp.dhis.tracker.report.TrackerImportReport;
import org.hisp.dhis.user.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class EventDataValueTest
    extends TrackerTest
{
    @Autowired
    private ObjectBundleService objectBundleService;

    @Autowired
    private ObjectBundleValidationService objectBundleValidationService;

    @Autowired
    private TrackerBundleService trackerBundleService;

    @Autowired
    private TrackerImportService trackerImportService;

    @Autowired
    private IdentifiableObjectManager manager;

    @Autowired
    private TrackerPreheatService preheatService;


    @Override
    protected void initTest()
        throws IOException
    {
        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "tracker/simple_metadata.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportStrategy( ImportStrategy.CREATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidationReport validationReport = objectBundleValidationService.validate( bundle );
        assertTrue( validationReport.getErrorReports().isEmpty() );

        objectBundleService.commit( bundle );

        final User userA = userService.getUser( "M5zQapPyTZI" );

        InputStream inputStream = new ClassPathResource( "tracker/single_tei.json" ).getInputStream();

        TrackerImportParams teiParams = renderService.fromJson( inputStream, TrackerImportParams.class );
        teiParams.setUserId( userA.getUid() );
        params.setUser( userA );
        TrackerImportReport teiImportReport = trackerImportService.importTracker( teiParams );

        assertTrue( teiImportReport.getValidationReport().getErrorReports().isEmpty() );

        TrackerImportParams enrollmentParams = renderService
            .fromJson( new ClassPathResource( "tracker/single_enrollment.json" ).getInputStream(),
                TrackerImportParams.class );
        enrollmentParams.setUserId( userA.getUid() );
        TrackerImportReport enrollmentImportReport = trackerImportService.importTracker( enrollmentParams );

        assertTrue( enrollmentImportReport.getValidationReport().getErrorReports().isEmpty() );
    }

    @Test
    public void testEventDataValue()
        throws IOException
    {
        TrackerImportParams trackerImportParams = fromJson( "tracker/event_with_data_values.json" );

        trackerBundleService.commit( trackerBundleService.create( trackerImportParams ) );

        List<ProgramStageInstance> events = manager.getAll( ProgramStageInstance.class );
        assertEquals( 1, events.size() );

        ProgramStageInstance psi = events.get( 0 );

        Set<EventDataValue> eventDataValues = psi.getEventDataValues();

        assertEquals( 4, eventDataValues.size() );
    }

    @Test
    public void testTrackedEntityProgramAttributeValueUpdate()
        throws IOException
    {
        TrackerImportParams trackerImportParams = fromJson( "tracker/event_with_data_values.json" );

        TrackerPreheat preheat = preheatService.preheat(trackerImportParams);
        TrackerBundle bundle = trackerBundleService.create(trackerImportParams);
        bundle.setPreheat( preheat );
        trackerBundleService.commit( bundle );

        List<ProgramStageInstance> events = manager.getAll( ProgramStageInstance.class );
        assertEquals( 1, events.size() );

        ProgramStageInstance psi = events.get( 0 );

        Set<EventDataValue> eventDataValues = psi.getEventDataValues();

        assertEquals( 4, eventDataValues.size() );

        // update

        trackerImportParams = fromJson( "tracker/event_with_updated_data_values.json" );
        // make sure that the uid property is populated as well - otherwise update will
        // not work
        trackerImportParams.getEvents().get( 0 ).setUid( trackerImportParams.getEvents().get( 0 ).getEvent() );

        preheat = preheatService.preheat( trackerImportParams );
        bundle = trackerBundleService.create( trackerImportParams );
        bundle.setPreheat( preheat );

        trackerBundleService.commit( bundle );

        List<ProgramStageInstance> updatedEvents = manager.getAll( ProgramStageInstance.class );
        assertEquals( 1, updatedEvents.size() );

        ProgramStageInstance updatedPsi = events.get( 0 );

        assertEquals( 3, updatedPsi.getEventDataValues().size() );
        List<String> values = updatedPsi.getEventDataValues()
            .stream()
            .map( EventDataValue::getValue )
            .collect( Collectors.toList() );

        assertThat( values, hasItem( "First" ) );
        assertThat( values, hasItem( "Second" ) );
        assertThat( values, hasItem( "Fourth updated" ) );

    }
}
