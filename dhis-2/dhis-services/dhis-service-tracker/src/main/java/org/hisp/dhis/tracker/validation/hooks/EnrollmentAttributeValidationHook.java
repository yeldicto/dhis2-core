package org.hisp.dhis.tracker.validation.hooks;

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

import com.google.common.collect.Maps;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.security.Authorities;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;
import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Attribute;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.preheat.PreheatHelper;
import org.hisp.dhis.tracker.report.TrackerErrorCode;
import org.hisp.dhis.tracker.report.TrackerErrorReport;
import org.hisp.dhis.tracker.report.ValidationErrorReporter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.hisp.dhis.tracker.report.ValidationErrorReporter.newReport;

/**
 * @author Morten Svanæs <msvanaes@dhis2.org>
 */
@Component
public class EnrollmentAttributeValidationHook
    extends AbstractTrackerValidationHook
{

    @Override
    public int getOrder()
    {
        return 105;
    }

    @Override
    public List<TrackerErrorReport> validate( TrackerBundle bundle )
    {
        if ( bundle.getImportStrategy().isDelete() )
        {
            return Collections.emptyList();
        }

        ValidationErrorReporter reporter = new ValidationErrorReporter( bundle, this.getClass() );

        for ( Enrollment enrollment : bundle.getEnrollments() )
        {
            reporter.increment( enrollment );

            Program program = PreheatHelper.getProgram( bundle, enrollment.getProgram() );
            TrackedEntityInstance trackedEntityInstance = PreheatHelper
                .getTrackedEntityInstance( bundle, enrollment.getTrackedEntity() );

            Map<String, String> attributeValueMap = Maps.newHashMap();
            for ( Attribute attribute : enrollment.getAttributes() )
            {
                if ( attribute.getAttribute() == null )
                {
                    reporter.addError( newReport( TrackerErrorCode.E1075 )
                        .addArg( attribute ) );
                }

                if ( attribute.getValue() == null )
                {
                    reporter.addError( newReport( TrackerErrorCode.E1076 )
                        .addArg( attribute ) );
                }

                if ( attribute.getAttribute() == null || attribute.getValue() == null )
                {
                    continue;
                }

                attributeValueMap.put( attribute.getAttribute(), attribute.getValue() );

                TrackedEntityAttribute teAttribute = PreheatHelper
                    .getTrackedEntityAttribute( bundle, attribute.getAttribute() );
                if ( teAttribute == null )
                {
                    reporter.addError( newReport( TrackerErrorCode.E1017 )
                        .addArg( attribute ) );
                }
                else
                {
                    validateAttrValueType( reporter, attribute, teAttribute );

                    //NOTE: this is perf killing
                    validateAttributeUniqueness( reporter,
                        attribute.getValue(),
                        teAttribute,
                        trackedEntityInstance,
                        trackedEntityInstance.getOrganisationUnit() );
                }
            }

            if ( program == null || trackedEntityInstance == null )
            {
                continue;
            }

            validateMandatoryAttributes( bundle, reporter, program, trackedEntityInstance, attributeValueMap );
        }

        return reporter.getReportList();
    }

    private void validateMandatoryAttributes( TrackerBundle bundle, ValidationErrorReporter errorReporter,
        Program program, TrackedEntityInstance trackedEntityInstance, Map<String, String> attributeValueMap )
    {
        Objects.requireNonNull( program, "Program can't be null" );
        Objects.requireNonNull( trackedEntityInstance, "TrackedEntityInstance can't be null" );
        Objects.requireNonNull( attributeValueMap, "AttributeValueMap can't be null" );

        // NOTE: This is my attempt to fix this after impl. Abyot's comments on the initial/original version.
        // 1. Get all tei attributes, map attrValue attr. into set of attr.
        Set<TrackedEntityAttribute> trackedEntityAttributes = trackedEntityInstance.getTrackedEntityAttributeValues()
            .stream()
            .map( TrackedEntityAttributeValue::getAttribute )
            .collect( Collectors.toSet() );

        // 2. Map all program attr. that match tei attr. into map. of attr:ismandatory
        Map<TrackedEntityAttribute, Boolean> mandatoryMap = program.getProgramAttributes().stream()
            .filter( v -> trackedEntityAttributes.contains( v.getAttribute() ) )
            .collect( Collectors.toMap(
                ProgramTrackedEntityAttribute::getAttribute,
                ProgramTrackedEntityAttribute::isMandatory ) );

        for ( Map.Entry<TrackedEntityAttribute, Boolean> entry : mandatoryMap.entrySet() )
        {
            TrackedEntityAttribute attribute = entry.getKey();
            Boolean attributeIsMandatory = entry.getValue();

            boolean userIsAuthorizedToIgnoreRequiredValueValidation = !bundle.getUser()
                .isAuthorized( Authorities.F_IGNORE_TRACKER_REQUIRED_VALUE_VALIDATION.getAuthority() );

            boolean hasMissingAttribute = attributeIsMandatory
                && !userIsAuthorizedToIgnoreRequiredValueValidation
                && !attributeValueMap.containsKey( attribute.getUid() );

            if ( hasMissingAttribute )
            {
                // Missing mandatory attribute
                errorReporter.addError( newReport( TrackerErrorCode.E1018 )
                    .addArg( attribute ) );
            }

            // Remove program attr. from enrollment attr. list (
            attributeValueMap.remove( attribute.getUid() );
        }

        if ( !attributeValueMap.isEmpty() )
        {
            // Only program attributes is allowed for enrollment!
            errorReporter.addError( newReport( TrackerErrorCode.E1019 )
                .addArg( attributeValueMap.keySet().stream()
                    .map( key -> key + "=" + attributeValueMap.get( key ) )
                    .collect( Collectors.joining( ", " ) ) ) );
        }
    }
}