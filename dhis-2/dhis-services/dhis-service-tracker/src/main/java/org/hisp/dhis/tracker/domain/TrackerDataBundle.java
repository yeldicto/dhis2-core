package org.hisp.dhis.tracker.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.hisp.dhis.tracker.converter.TrackerDataBundleConverter;

@Value
@Builder
@AllArgsConstructor
@JsonDeserialize( using = TrackerDataBundleConverter.class )
/**
 * The TrackerDataBundle is a wrapper object for all types of Tracker objects. The class should define the basic
 * structure of the import or export payload in the tracker api /tracker.
 * This basic structure supports both the flattened structure, as naturally defined in this class, but also the
 * nested structure, where tracker objects can be embedded inside other tracker objects. One example is Enrollments
 * embedded in a TrackedEntity.
 */
public class TrackerDataBundle
{

    /**
     * An immutable list of trackedEntities.
     */
    @JsonProperty
    ImmutableList<TrackedEntity> trackedEntities;

    /**
     * An immutable list of enrollments
     */
    @JsonProperty
    ImmutableList<Enrollment> enrollments;

    /**
     * An immutable list of events
     */
    @JsonProperty
    ImmutableList<Event> events;

    /**
     * An immutable list of relationships
     */
    @JsonProperty
    ImmutableList<Relationship> relationships;

    /**
     * Checks if the TrackerDataBundle contains data or not, by checking if all collections are empty or not.
     *
     * @return true if all collections are empty, false if any collection contains data.
     */
    public boolean isEmpty()
    {
        return trackedEntities.isEmpty() && enrollments.isEmpty() && events.isEmpty() && relationships.isEmpty();
    }

}
