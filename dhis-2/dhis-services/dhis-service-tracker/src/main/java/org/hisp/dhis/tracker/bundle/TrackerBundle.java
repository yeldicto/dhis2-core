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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hisp.dhis.rules.models.RuleEffect;
import org.hisp.dhis.tracker.AtomicMode;
import org.hisp.dhis.tracker.FlushMode;
import org.hisp.dhis.tracker.TrackerIdScheme;
import org.hisp.dhis.tracker.TrackerImportStrategy;
import org.hisp.dhis.tracker.ValidationMode;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.Event;
import org.hisp.dhis.tracker.domain.Relationship;
import org.hisp.dhis.tracker.domain.TrackedEntity;
import org.hisp.dhis.tracker.preheat.TrackerPreheat;
import org.hisp.dhis.tracker.validation.TrackerImportValidationContext;
import org.hisp.dhis.user.User;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackerBundle
{
    /**
     * User to use for import job.
     */
    private User user;

    /**
     * Should import be imported or just validated.
     */
    @Builder.Default
    private TrackerBundleMode importMode = TrackerBundleMode.COMMIT;

    /**
     * What identifiers to match on.
     */
    @Builder.Default
    private TrackerIdScheme identifier = TrackerIdScheme.UID;

    /**
     * Sets import strategy (create, update, etc).
     */
    @Builder.Default
    private TrackerImportStrategy importStrategy = TrackerImportStrategy.CREATE;

    /**
     * Should text pattern validation be skipped or not, default is not.
     */
    @JsonProperty
    private boolean skipTextPatternValidation;

    /**
     * Should side effects be skipped or not, default is not.
     */
    @JsonProperty
    private boolean skipSideEffects;

    /**
     * Should rule engine call be skipped or not, default is to skip.
     */
    @JsonProperty
    private boolean skipRuleEngine;

    /**
     * Should import be treated as a atomic import (all or nothing).
     */
    @Builder.Default
    private AtomicMode atomicMode = AtomicMode.ALL;

    /**
     * Flush for every object or per type.
     */
    @Builder.Default
    private FlushMode flushMode = FlushMode.AUTO;

    /**
     * Validation mode to use, defaults to fully validated objects.
     */
    @Builder.Default
    private ValidationMode validationMode = ValidationMode.FULL;

    /**
     * Preheat bundle for all attached objects (or null if preheater not run yet).
     */
    private TrackerPreheat preheat;

    /**
     * Tracked entities to import.
     */
    @Builder.Default
    private List<TrackedEntity> trackedEntities = new ArrayList<>();

    /**
     * Enrollments to import.
     */
    @Builder.Default
    private List<Enrollment> enrollments = new ArrayList<>();

    /**
     * Events to import.
     */
    @Builder.Default
    private List<Event> events = new ArrayList<>();

    /**
     * Relationships to import.
     */
    @Builder.Default
    private List<Relationship> relationships = new ArrayList<>();

    /**
     * Rule effects for Enrollments.
     */
    @Builder.Default
    private Map<String, List<RuleEffect>> enrollmentRuleEffects = new HashMap<>();

    /**
     * Rule effects for Events.
     */
    @Builder.Default
    private Map<String, List<RuleEffect>> eventRuleEffects = new HashMap<>();

    private TrackerImportValidationContext trackerImportValidationContext;

    @JsonProperty
    public String getUsername()
    {
        return User.username( user );
    }
    
    public Optional<TrackedEntity> getTrackedEntity( String id )
    {
        return this.trackedEntities.stream().filter( t -> t.getTrackedEntity().equals( id ) ).findFirst();
    }

    /**
     * Clone the current TrackerBundle
     * 
     * @return a copy of this TrackerBundle
     */
    public TrackerBundle copy()
    {
        TrackerBundle bundle = new TrackerBundle();
        bundle.setAtomicMode( this.atomicMode );
        bundle.setFlushMode( this.flushMode );
        bundle.setRelationships( this.relationships );
        bundle.setEvents( this.events );
        bundle.setEnrollments( this.enrollments );
        bundle.setTrackedEntities( this.trackedEntities );
        bundle.setEventRuleEffects( this.eventRuleEffects );
        bundle.setEnrollmentRuleEffects( this.enrollmentRuleEffects );
        bundle.setValidationMode( this.validationMode );
        bundle.setIdentifier( this.identifier );
        bundle.setPreheat( this.preheat );
        bundle.setUser( this.user );
        bundle.setImportStrategy( this.importStrategy );
        bundle.setSkipSideEffects( this.skipSideEffects );
        bundle.setImportMode( this.importMode );

        return bundle;
    }

    /**
     * Calculates the sum of all objects n this bundle.
     *
     */
    public int getBundleSize()
    {
        return this.getTrackedEntities().size() + this.getEnrollments().size() + this.getEvents().size()
            + this.getRelationships().size();
    }
}
