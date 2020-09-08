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

package org.hisp.dhis.tracker.converter;

import com.fasterxml.jackson.databind.util.StdConverter;
import com.google.common.collect.Maps;
import org.apache.xmlbeans.impl.xb.ltgfmt.Code;
import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.tracker.bundle.TrackerBundleParams;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.Event;
import org.hisp.dhis.tracker.domain.Relationship;
import org.hisp.dhis.tracker.domain.TrackedEntity;
import org.hisp.dhis.tracker.domain.TrackerDataBundle;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Converts a {@see TrackerDataBundle} containing a nested Tracked Entity structure into a "flat" structure
 * <p>
 * Assuming a structure like:
 * <pre>
 *
 * TrackerBundleParams
 *   |
 *   __TEI
 *      |_ENROLLMENT 1
 *      |      |
 *      |      |_ EVENT 1
 *      |      |
 *      |      |_ EVENT 2
 *      |
 *      |_ENROLLMENT 2
 *            |
 *            |_ EVENT 3
 *            |_ EVENT 4
 * </pre>
 * <p>
 * This converter will transform the object into:
 *
 * <pre>
 *
 * TrackerBundleParams
 *  |
 *  |___TEI
 *  |___ENROLLMENT 1, ENROLLMENT 2
 *  |
 *  |___EVENT 1, EVENT 2, EVENT 3, EVENT 4
 *
 * </pre>
 * <p>
 * This converter also assigns UIDs to Tracked Entities, Enrollment and Events if the payload does not contain UIDs
 *
 * @author Luciano Fiandesio
 * @author Stian Sandvold
 */
public class TrackerDataBundleConverter
    extends
    StdConverter<TrackerDataBundle, TrackerDataBundle>
{
    @Override
    public TrackerDataBundle convert( TrackerDataBundle bundle )
    {
        if ( !bundle.isEmpty() )
        {

            // pre assign UIDs to entities, if UIDs are missing
            generateUid( bundle );

            flattenPayload( bundle );
        }

        return bundle;
    }

    private void flattenStructure( TrackerDataBundle in, TrackerDataBundle out )
    {
        Map<String, TrackedEntity> trackedEntities = new HashMap<>();
        Map<String, Enrollment> enrollments = new HashMap<>();
        Map<String, Event> events = new HashMap<>();
        Map<String, Relationship> relationships = new HashMap<>();


        /*
         * We start by going through all "main" collections of TrackerDataBundle
         */
        for ( TrackedEntity te : in.getTrackedEntities() )
        {
            List<Enrollment> enrollmentList = te.getEnrollments();
            List<Relationship> relationshipList = te.getRelationships();

            /*
             * We add missing UID
             */
            if ( StringUtils.isEmpty( te.getTrackedEntity() ) )
            {
                te.setTrackedEntity( CodeGenerator.generateUid() );
            }

            /*
             * We proceed to check all enrollments
             */
            if ( !enrollmentList.isEmpty() )
            {
                for( Enrollment enrollment :  enrollmentList )
                {
                    List<Event> eventList = enrollment.getEvents();
                    List<Relationship> relationshipList1 = enrollment.getRelationships();

                    /*
                     * We add missing UIDs
                     */
                    if ( StringUtils.isEmpty( enrollment.getEnrollment() ) )
                    {
                        enrollment.setEnrollment( CodeGenerator.generateUid() );
                    }

                    /*
                     * We add missing reference to parent
                     */
                    if ( StringUtils.isEmpty( enrollment.getTrackedEntity() ) )
                    {
                        enrollment.setTrackedEntity( te.getTrackedEntity() );
                    }

                    for( Event event : eventList )
                    {
                        List<Relationship> relationshipList2 = enrollment.getRelationships();

                        /*
                         * We add missing UIDs
                         */
                        if ( StringUtils.isEmpty( event.getEvent() ) )
                        {
                            event.setEvent( CodeGenerator.generateUid() );
                        }

                        /*
                         * We add missing reference to parent
                         */
                        if ( StringUtils.isEmpty( event.getTrackedEntity() ) )
                        {
                            event.setTrackedEntity( te.getTrackedEntity() );
                        }

                        if ( StringUtils.isEmpty( event.getEnrollment() ) )
                        {
                            event.setEnrollment( enrollment.getEnrollment() );
                        }


                    }

                }


            }

        }

    }

    private void handleRelationships( List<Relationship> relationships )
    {
        if ( relationships == null )
        {
            return;
        }

        for ( Relationship relationship : relationships )
        {
            if ( StringUtils.isEmpty( relationship.getRelationship( ) ) )
            {
                relationship.setRelationship( CodeGenerator.generateUid() );
            }
        }
    }

    private void flattenPayload( TrackerBundleParams bundle )
    {

        List<Event> events = new ArrayList<>();

        List<Enrollment> enrollments = new ArrayList<>();

        Set<Relationship> relationships = new HashSet<>();

        // Iterate over **all** enrollments
        for ( Enrollment enrollment : bundle.getTrackedEntities().stream()
            .flatMap( l -> l.getEnrollments().stream() ).collect( Collectors.toList() ) )
        {
            // collect all events from enrollments and add them to the flattened events collection
            events.addAll( enrollment.getEvents().stream().map( e -> addParent( e, enrollment.getEnrollment() ) )
                .collect( Collectors.toList() ) );

            // Extract relationships from enrollment
            relationships.addAll( enrollment.getRelationships() );

            // Extract relationships from events
            relationships
                .addAll( events.stream().flatMap( l -> l.getRelationships().stream() ).collect( Collectors.toSet() ) );

            // remove events and relationships from enrollment
            enrollment.setEvents( Collections.emptyList() );
            enrollment.setRelationships( Collections.emptyList() );
            // remove relationships from events
            events.forEach( e -> e.setRelationships( Collections.emptyList() ) );
        }

        for ( TrackedEntity trackedEntity : bundle.getTrackedEntities() )
        {
            enrollments.addAll( trackedEntity.getEnrollments().stream()
                .map( e -> addParent( e, trackedEntity.getTrackedEntity() ) ).collect( Collectors.toList() ) );

            // Extract relationships from Tracked Entity
            relationships.addAll( trackedEntity.getRelationships() );

            // remove enrollments and relationships from tracked entities
            trackedEntity.setEnrollments( Collections.emptyList() );
            trackedEntity.setRelationships( Collections.emptyList() );
        }

        bundle.getEvents().addAll( events );
        bundle.getEnrollments().addAll( enrollments );
        bundle.getRelationships().addAll( relationships );
    }

    /**
     * Make sure that the Enrollment has the parent ID correctly set
     */
    private Enrollment addParent( Enrollment enrollment, String trackerEntityId )
    {
        if ( StringUtils.isEmpty( enrollment.getTrackedEntity() ) )
        {
            enrollment.setTrackedEntity( trackerEntityId );
        }
        return enrollment;
    }

    /**
     * Make sure that the Event has the parent ID correctly set
     */
    private Event addParent( Event event, String enrollmentId )
    {

        if ( StringUtils.isEmpty( event.getEnrollment() ) )
        {
            event.setEnrollment( enrollmentId );
        }
        return event;
    }

    private void generateUid( TrackerDataBundle bundle )
    {
        List<TrackedEntity> trackedEntities = bundle.getTrackedEntities();

        for ( TrackedEntity trackedEntity : trackedEntities )
        {
            if ( StringUtils.isEmpty( trackedEntity.getTrackedEntity() ) )
            {
                trackedEntity.setTrackedEntity( CodeGenerator.generateUid() );
            }

            Map<Relationship, String> relationshipsWithUid = Maps.newHashMap();

            generateRelationshipUids( trackedEntity.getRelationships(), relationshipsWithUid );

            List<Enrollment> enrollments = trackedEntity.getEnrollments();

            for ( Enrollment enrollment : enrollments )
            {
                // Assign an UID to Enrollment if no UID is present
                if ( StringUtils.isEmpty( enrollment.getEnrollment() ) )
                {
                    enrollment.setEnrollment( CodeGenerator.generateUid() );
                }

                generateRelationshipUids( enrollment.getRelationships(), relationshipsWithUid );

                // Assign an UID to Events if no UID is present
                enrollment.getEvents()
                    .stream()
                    .peek( event -> generateRelationshipUids( event.getRelationships(), relationshipsWithUid ) )
                    .filter( e -> StringUtils.isEmpty( e.getEvent() ) )
                    .forEach( e -> e.setEvent( CodeGenerator.generateUid() ) );
            }

        }
    }

    private void generateRelationshipUids( Collection<Relationship> relationships,
        Map<Relationship, String> relationshipsWithUid )
    {
        for ( Relationship entityRelationship : relationships )
        {
            if ( StringUtils.isEmpty( entityRelationship.getRelationship() ) )
            {
                String uid = relationshipsWithUid.get( entityRelationship );
                if ( uid != null )
                {
                    entityRelationship.setRelationship( uid );
                }
                else
                {
                    entityRelationship.setRelationship( CodeGenerator.generateUid() );
                }
                relationshipsWithUid.put( entityRelationship, entityRelationship.getRelationship() );
            }
        }
    }
}
