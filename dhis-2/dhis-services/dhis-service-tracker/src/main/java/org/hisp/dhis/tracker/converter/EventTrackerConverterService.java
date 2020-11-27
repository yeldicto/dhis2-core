package org.hisp.dhis.tracker.converter;

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

import static com.google.api.client.util.Preconditions.checkNotNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hisp.dhis.category.CategoryOption;
import org.hisp.dhis.category.CategoryOptionCombo;
import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.eventdatavalue.EventDataValue;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramType;
import org.hisp.dhis.tracker.TrackerIdScheme;
import org.hisp.dhis.tracker.domain.DataValue;
import org.hisp.dhis.tracker.domain.EnrollmentStatus;
import org.hisp.dhis.tracker.domain.Event;
import org.hisp.dhis.tracker.preheat.TrackerPreheat;
import org.hisp.dhis.user.User;
import org.hisp.dhis.util.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Service
public class EventTrackerConverterService
    implements TrackerConverterService<Event, ProgramStageInstance>
{

    private final NotesConverterService notesConverterService;

    public EventTrackerConverterService( NotesConverterService notesConverterService )
    {
        checkNotNull( notesConverterService );

        this.notesConverterService = notesConverterService;
    }

    @Override
    public Event to( ProgramStageInstance programStageInstance )
    {
        List<Event> events = to( Collections.singletonList( programStageInstance ) );

        if ( events.isEmpty() )
        {
            return null;
        }

        return events.get( 0 );
    }

    @Override
    public List<Event> to( List<ProgramStageInstance> programStageInstances )
    {
        List<Event> events = new ArrayList<>();

        programStageInstances.forEach( psi -> {
            Event event = new Event();
            event.setEvent( psi.getUid() );

            if ( psi.getProgramInstance().getEntityInstance() != null )
            {
                event.setTrackedEntity( psi.getProgramInstance().getEntityInstance().getUid() );
            }

            event.setFollowUp( psi.getProgramInstance().getFollowup() );
            event.setEnrollmentStatus( EnrollmentStatus.fromProgramStatus( psi.getProgramInstance().getStatus() ) );
            event.setStatus( psi.getStatus() );
            event.setOccurredAt( DateUtils.getIso8601NoTz( psi.getExecutionDate() ) );
            event.setScheduledAt( DateUtils.getIso8601NoTz( psi.getDueDate() ) );
            event.setStoredBy( psi.getStoredBy() );
            event.setCompletedBy( psi.getCompletedBy() );
            event.setCompletedAt( DateUtils.getIso8601NoTz( psi.getCompletedDate() ) );
            event.setCreatedAt( DateUtils.getIso8601NoTz( psi.getCreated() ) );
            event.setUpdatedAt( DateUtils.getIso8601NoTz( psi.getLastUpdated() ) );
            event.setGeometry( psi.getGeometry() );
            event.setDeleted( psi.isDeleted() );

            OrganisationUnit ou = psi.getOrganisationUnit();

            if ( ou != null )
            {
                event.setOrgUnit( ou.getUid() );
            }

            Program program = psi.getProgramInstance().getProgram();

            event.setProgram( program.getUid() );
            event.setEnrollment( psi.getProgramInstance().getUid() );
            event.setProgramStage( psi.getProgramStage().getUid() );
            event.setAttributeOptionCombo( psi.getAttributeOptionCombo().getUid() );
            event.setAttributeCategoryOptions( psi.getAttributeOptionCombo()
                .getCategoryOptions().stream().map( CategoryOption::getUid ).collect( Collectors.joining( ";" ) ) );

            Set<EventDataValue> dataValues = psi.getEventDataValues();

            for ( EventDataValue dataValue : dataValues )
            {
                DataValue value = new DataValue();
                value.setCreatedAt( DateUtils.getIso8601NoTz( dataValue.getCreated() ) );
                value.setUpdatedAt( DateUtils.getIso8601NoTz( dataValue.getLastUpdated() ) );
                value.setDataElement( dataValue.getDataElement() );
                value.setValue( dataValue.getValue() );
                value.setProvidedElsewhere( dataValue.getProvidedElsewhere() );
                value.setStoredBy( dataValue.getStoredBy() );

                event.getDataValues().add( value );
            }

            events.add( event );
        } );

        return events;
    }

    @Override
    public ProgramStageInstance from( TrackerPreheat preheat, Event event )
    {
        ProgramStageInstance programStageInstance = preheat.getEvent( TrackerIdScheme.UID, event.getEvent() );
        return from( preheat, event, programStageInstance );
    }

    @Override
    public List<ProgramStageInstance> from( TrackerPreheat preheat, List<Event> events )
    {
        return events
            .stream()
            .map( e -> from( preheat, e ) )
            .collect( Collectors.toList() );
    }

    @Override
    public ProgramStageInstance fromForRuleEngine( TrackerPreheat preheat, Event event )
    {
        return from( preheat, event, null );
    }

    private ProgramStageInstance from( TrackerPreheat preheat, Event event, ProgramStageInstance programStageInstance )
    {
        ProgramStage programStage = preheat.get( TrackerIdScheme.UID, ProgramStage.class, event.getProgramStage() );
        OrganisationUnit organisationUnit = preheat
            .get( TrackerIdScheme.UID, OrganisationUnit.class, event.getOrgUnit() );

        if ( isNewEntity( programStageInstance ) )
        {
            Date now = new Date();

            programStageInstance = new ProgramStageInstance();
            programStageInstance.setUid( !StringUtils.isEmpty( event.getEvent() ) ? event.getEvent() : event.getUid() );
            programStageInstance.setCreated( now );
            programStageInstance.setCreatedAtClient( now );
            programStageInstance.setLastUpdated( now );
            programStageInstance.setLastUpdatedAtClient( now );
            programStageInstance.setProgramInstance(
                getProgramInstance( preheat, TrackerIdScheme.UID, event.getEnrollment(), programStage.getProgram() ) );
        }

        programStageInstance.setProgramStage( programStage );
        programStageInstance.setOrganisationUnit( organisationUnit );
        programStageInstance.setExecutionDate( DateUtils.parseDate( event.getOccurredAt() ) );
        programStageInstance.setDueDate( DateUtils.parseDate( event.getScheduledAt() ) );

        String attributeOptionCombo = event.getAttributeOptionCombo();

        if ( attributeOptionCombo != null )
        {
            programStageInstance.setAttributeOptionCombo(
                preheat.get( TrackerIdScheme.UID, CategoryOptionCombo.class, event.getAttributeOptionCombo() ) );
        }
        else
        {
            programStageInstance.setAttributeOptionCombo( (CategoryOptionCombo) preheat.getDefaults().get( CategoryOptionCombo.class ) );
        }

        programStageInstance.setGeometry( event.getGeometry() );
        programStageInstance.setStatus( event.getStatus() );

        if ( programStageInstance.isCompleted() )
        {
            Date completedDate = DateUtils.parseDate( event.getCompletedAt() );

            if ( completedDate == null )
            {
                completedDate = new Date();
            }

            programStageInstance.setCompletedDate( completedDate );
            programStageInstance.setCompletedBy( event.getCompletedBy() );
        }

        if ( isNotEmpty( event.getNotes() ) )
        {
            programStageInstance.getComments().addAll( notesConverterService.from( preheat, event.getNotes() ) );
        }

        if ( programStage.isEnableUserAssignment() )
        {
            User assignedUser = preheat.get( TrackerIdScheme.UID, User.class, event.getAssignedUser() );
            programStageInstance.setAssignedUser( assignedUser );
        }

        return programStageInstance;
    }

    private ProgramInstance getProgramInstance( TrackerPreheat preheat, TrackerIdScheme identifier, String enrollment,
        Program program )
    {
        if ( ProgramType.WITH_REGISTRATION == program.getProgramType() )
        {
            return preheat.getEnrollment( identifier, enrollment );
        }

        if ( ProgramType.WITHOUT_REGISTRATION == program.getProgramType() )
        {
            return preheat.getProgramInstancesWithoutRegistration( program.getUid() );
        }

        // no valid enrollment given and program not single event, just return null
        return null;
    }
}
