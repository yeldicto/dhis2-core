package org.hisp.dhis.tracker;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import org.hisp.dhis.tracker.bundle.TrackerBundleMode;

@Value
@Builder
/**
 * TrackerImportOptions represents the complete set of parameters the tracker import api /tracker supports.
 * asd
 */
public class TrackerImportOptions
{

    /**
     * The importMode option decides the primary purpose of the import request: commit or validate changes.
     * The default option is to commit changes.
     */
    @JsonProperty
    @Builder.Default
    TrackerBundleMode importMode = TrackerBundleMode.COMMIT;

    /**
     * The importStrategy decides the primary purpose of the import. See @see TrackerImportStrategy for more information.
     * CREATE is the default strategy.
     */
    @JsonProperty
    @Builder.Default
    TrackerImportStrategy importStrategy = TrackerImportStrategy.CREATE;

    /**
     * Indentifiers is a wrapper objects for a collections of sub-options that are partly interconnected. The option
     * allows us to specify the type of identifier that should be used to identify references in the payload. For
     * example, we can use UID (Default), CODE or ATTRIBUTE. The default is AUTO, which resolved to UID. idScheme option
     * will set the main identifier, but can be overridden by specific options, for example orgUnitIdScheme will override
     * all OrganisationUnit references.
     */
    @JsonProperty
    @Builder.Default
    TrackerIdentifierParams identifiers = new TrackerIdentifierParams();

    /**
     * The atomicMode option allows us to decide how tolerant the importer should be to any errors found. The ALL mode
     * requires all data to pass validation, while the OBJECT mode will just skip any data that fails validation, and
     * commit the remaining data. Should only be used by clients that understands the implications.
     */
    @JsonProperty
    @Builder.Default
    AtomicMode atomicMode = AtomicMode.ALL;

    /**
     * The flushMode is primarily used to debug potential issues, and refers to the frequency of when the importer
     * should "flush" or push data to the database. All clients should always leave this to AUTO.
     */
    @JsonProperty
    @Builder.Default
    FlushMode flushMode = FlushMode.AUTO;

    /**
     * The validationMode decides how thorough the importer should perform the validation, for example in the case of
     * finding a validation error. FULL will validate all the data, uncovering any issue it can find for each record of
     * data. FAIL_FAST will just return the first error found for each record. SKIP will skip all validation. SKIP is
     * never recommended, as it can end up corrupting the database with invalid data.
     * <p>
     * The default is FULL
     */
    @JsonProperty
    @Builder.Default
    ValidationMode validationMode = ValidationMode.FULL;

    /**
     * TODO: This is being worked on by Enrico, so I will leave this for the time beeing // Stian
     */
    @JsonProperty
    @Builder.Default
    TrackerBundleReportMode reportMode = TrackerBundleReportMode.ERRORS;

    /**
     * The skipTextPatternValidation option allows the importer to skip validating trackedEntity attributes
     * that are automatically generated. The validation consists of two checks: Does the new value match the expected
     * pattern? If not, have we previously reserved this new value to be used?
     * <p>
     * This option should only be true in special cases. For example when the pattern has changed, but values previously
     * created needs to be imported.
     */
    @JsonProperty
    @Builder.Default
    boolean skipTextPatternValidation = false;

    /**
     * SideEffects refers to processes that are initiated after the import finished, as a result of certain conditions
     * in the import process. Examples of sideEffects are scheduling events and sending messages.
     * <p>
     * Currently, the skipSideEffects option will skip sending messages. The option should only be used when the client
     * understands the implications of skipping the sideEffects.
     */
    @JsonProperty
    @Builder.Default
    boolean skipSideEffects = false;

}
