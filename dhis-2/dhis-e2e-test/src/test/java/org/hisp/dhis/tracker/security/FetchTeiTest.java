package org.hisp.dhis.tracker.security;

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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.hisp.dhis.ApiTest;
import org.hisp.dhis.actions.LoginActions;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.actions.UserActions;
import org.hisp.dhis.actions.tracker.TeiFields;
import org.hisp.dhis.actions.tracker.TrackedEntityInstanceActions;
import org.hisp.dhis.actions.tracker.fields.Attributes;
import org.hisp.dhis.actions.tracker.fields.DataValue;
import org.hisp.dhis.actions.tracker.fields.DataValueFields;
import org.hisp.dhis.actions.tracker.fields.Enrollment;
import org.hisp.dhis.actions.tracker.fields.EnrollmentFields;
import org.hisp.dhis.actions.tracker.fields.Event;
import org.hisp.dhis.actions.tracker.fields.EventFields;
import org.hisp.dhis.actions.tracker.fields.Note;
import org.hisp.dhis.actions.tracker.fields.NoteFields;
import org.hisp.dhis.actions.tracker.fields.Relationship;
import org.hisp.dhis.actions.tracker.fields.RelationshipAttributes;
import org.hisp.dhis.actions.tracker.fields.RelationshipFrom;
import org.hisp.dhis.actions.tracker.fields.RelationshipTo;
import org.hisp.dhis.actions.tracker.fields.TrackedEntityInstance;
import org.hisp.dhis.actions.tracker.fields.TrackedEntityInstanceAttributes;
import org.hisp.dhis.dto.ApiResponse;
import org.hisp.dhis.helpers.file.FileReaderUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hisp.dhis.actions.tracker.fields.TrackedEntityInstanceFields.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Luciano Fiandesio
 */
public class FetchTeiTest
    extends ApiTest
{

    private TrackedEntityInstanceActions teiActions;

    private RestApiActions metadataActions;

    private UserActions userActions;

    private final String _ROOT_OU = "WyJBYmQRgl9";

    private final String _USERA_SEARCHSCOPE = "pLTgxXoP6g0";

    private final String _USERB_SEARCHSCOPE = "Nql7v63V421";

    private final String _USER_PASSWORD = "District123!";

    private final TeiFields _TET_FIELDS = TeiFields.builder().trackedEntityInstance(
        TrackedEntityInstance.builder().fields( Collections.singletonList( trackedEntityType ) ).build() ).build();

    @BeforeAll
    public void before()
    {
        JsonObject metadata = null;
        JsonObject data = null;
        teiActions = new TrackedEntityInstanceActions();
        metadataActions = new RestApiActions( "/metadata" );
        userActions = new UserActions();

        // ------------------------------------------------------------
        // - Import pre-made metadata.
        // ------------------------------------------------------------

        try
        {
            metadata = new FileReaderUtils()
                .readJsonAndGenerateData( new File( "src/test/resources/tracker/tracker-access/metadata.json" ) );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        metadataActions.post( metadata );

        // ------------------------------------------------------------
        // - Set passwords for imported users
        // ------------------------------------------------------------

        // UserAll
        userActions.updateUserPassword( "F2uakVm05am", _USER_PASSWORD );

        // UserA
        userActions.updateUserPassword( "Dgthym7nDMK", _USER_PASSWORD );

        // UserB
        userActions.updateUserPassword( "yzslJpqqfnL", _USER_PASSWORD );

        // ------------------------------------------------------------
        // - Import basic data (tei, enrollments, events)
        // ------------------------------------------------------------

        try
        {
            data = new FileReaderUtils()
                .readJsonAndGenerateData( new File( "src/test/resources/tracker/tracker-access/data.json" ) );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        teiActions.post( data );

    }

    @Test
    public void trackedEntityTypeDataAccessUserA()
    {

        // Sharing for TET for userA
        List<String> allowedTet = asList( "Q73gg8GWg5A", "cbFOVZUx39N" );
        List<String> notAllowedTet = asList( "JS5vX0sfsim" );

        // Change logged in user to userA
        new LoginActions().loginAsUser( "UserA", _USER_PASSWORD );

        // Run query and assert result
        JsonArray teis = teiActions
            .getTrackedEntityInstances( _USERA_SEARCHSCOPE, "DESCENDANTS", _TET_FIELDS, false, 100, false, false,
                false ).getBody().getAsJsonArray( "trackedEntityInstances" );

        assertEquals( 6, teis.size() );

        teis.forEach( tei ->
        {
            String tet = tei.getAsJsonObject().get( "trackedEntityType" ).getAsString();
            assertTrue( allowedTet.contains( tet ) && !notAllowedTet.contains( tet ) );
        } );
    }

    @Test
    public void trackedEntityTypeDataAccessUserB()
    {

        // Sharing for TET for userA
        List<String> allowedTet = asList( "cbFOVZUx39N" );
        List<String> notAllowedTet = asList( "JS5vX0sfsim", "Q73gg8GWg5A" );

        // Change logged in user to userA
        new LoginActions().loginAsUser( "UserB", _USER_PASSWORD );

        // Run query and assert result
        JsonArray teis = teiActions
            .getTrackedEntityInstances( _USERB_SEARCHSCOPE, "DESCENDANTS", _TET_FIELDS, false, 100, false, false,
                false ).getBody().getAsJsonArray( "trackedEntityInstances" );

        assertEquals( 6, teis.size() );

        teis.forEach( tei ->
        {
            String tet = tei.getAsJsonObject().get( "trackedEntityType" ).getAsString();
            assertTrue( allowedTet.contains( tet ) && !notAllowedTet.contains( tet ) );
        } );
    }

    @Test
    public void trackedEntityTypeDataAccessUserAll()
    {

        // Sharing for TET for userA
        List<String> allowedTet = asList( "cbFOVZUx39N", "JS5vX0sfsim", "Q73gg8GWg5A" );

        // Change logged in user to userA
        new LoginActions().loginAsUser( "UserAll", _USER_PASSWORD );

        // Run query and assert result
        JsonArray teis = teiActions
            .getTrackedEntityInstances( _ROOT_OU, "DESCENDANTS", _TET_FIELDS, false, 100, false, false,
                false ).getBody().getAsJsonArray( "trackedEntityInstances" );

        assertEquals( 6, teis.size() );

        teis.forEach( tei ->
        {
            String tet = tei.getAsJsonObject().get( "trackedEntityType" ).getAsString();
            assertTrue( allowedTet.contains( tet ) );
        } );
    }

    private void assertCount( JsonObject response, int count )
    {

        assertEquals( count, response.getAsJsonArray( "trackedEntityInstances" ).size() );

    }

    private TeiFields getTeiFields()
    {

        return TeiFields.builder()
            .trackedEntityInstance( TrackedEntityInstance.builder()
                .fields( asList(
                    trackedEntityInstance,
                    created,
                    lastUpdated,
                    orgUnit,
                    trackedEntityType,
                    coordinates,
                    featureType,
                    deleted ) )
                .attributes( Attributes.builder().attributes( asList(
                    TrackedEntityInstanceAttributes.attribute,
                    TrackedEntityInstanceAttributes.value,
                    TrackedEntityInstanceAttributes.created,
                    TrackedEntityInstanceAttributes.lastUpdated ) ).build() )
                .relationships( Relationship.builder()
                    .fields( asList( RelationshipAttributes.trackedEntityInstanceA,
                        RelationshipAttributes.trackedEntityInstanceB,
                        RelationshipAttributes.relationship,
                        RelationshipAttributes.relationshipName,
                        RelationshipAttributes.relationshipType,
                        RelationshipAttributes.created,
                        RelationshipAttributes.lastUpdated ) )
                    .relationshipFrom( RelationshipFrom.builder()
                        .trackedEntityInstanceFields( Collections.singletonList( trackedEntityInstance ) )
                        .enrollmentFields( Collections.singletonList( EnrollmentFields.enrollment ) )
                        .eventFields( Collections.singletonList( EventFields.event ) )
                        .build() )
                    .relationshipTo( RelationshipTo.builder()
                        .trackedEntityInstanceFields( Collections.singletonList( trackedEntityInstance ) )
                        .enrollmentFields( Collections.singletonList( EnrollmentFields.enrollment ) )
                        .eventFields( Collections.singletonList( EventFields.event ) )
                        .build() )

                    .build() )
                .enrollment( Enrollment.builder()
                    .note( Note.builder()
                        .fields(
                            asList( NoteFields.note, NoteFields.value, NoteFields.storedBy, NoteFields.storedDate ) )
                        .build() )
                    .fields( asList(
                        EnrollmentFields.enrollment,
                        EnrollmentFields.created,
                        EnrollmentFields.lastUpdated,
                        EnrollmentFields.orgUnit,
                        EnrollmentFields.program,
                        EnrollmentFields.enrollmentDate,
                        EnrollmentFields.incidentDate,
                        EnrollmentFields.followup,
                        EnrollmentFields.status,
                        EnrollmentFields.deleted,
                        EnrollmentFields.trackedEntityInstance,
                        EnrollmentFields.coordinate ) )
                    .event( Event.builder()
                        .fields( asList( EventFields.event,
                            EventFields.enrollment,
                            EventFields.created,
                            EventFields.lastUpdated,
                            EventFields.status,
                            EventFields.coordinate,
                            EventFields.program,
                            EventFields.programStage,
                            EventFields.orgUnit,
                            EventFields.eventDate,
                            EventFields.completedDate,
                            EventFields.deleted,
                            EventFields.dueDate,
                            EventFields.attributeOptionCombo ) )
                        .dataValue( DataValue.builder()
                            .fields( asList(
                                DataValueFields.dataElement,
                                DataValueFields.storedBy,
                                DataValueFields.value,
                                DataValueFields.created,
                                DataValueFields.lastUpdated,
                                DataValueFields.providedElsewhere ) )
                            .build() )

                        .build() )
                    .build() )
                .build() )
            .build();
    }
}
