package org.hisp.dhis.tracker.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.collect.ImmutableList;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.Event;
import org.hisp.dhis.tracker.domain.Relationship;
import org.hisp.dhis.tracker.domain.TrackedEntity;
import org.hisp.dhis.tracker.domain.TrackerDataBundle;

import java.io.IOException;
import java.util.List;

/**
 * The TrackerDataBundleDeserializer is used to deserialize the payload from the tracker api /tracker. To set up
 * the TrackerDataBundle object that contains immutable lists, we need to manually deserialize and set up these
 * collections.
 */
public class TrackerDataBundleDeserializer
    extends
    StdDeserializer<TrackerDataBundle>
{

    protected TrackerDataBundleDeserializer( Class<?> vc )
    {
        super( vc );
    }

    protected TrackerDataBundleDeserializer()
    {
        this( null );
    }

    @Override
    public TrackerDataBundle deserialize( JsonParser jsonParser, DeserializationContext deserializationContext )
        throws IOException, JsonProcessingException
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = jsonParser.getCodec().readTree( jsonParser );

        List<TrackedEntity> trackedEntities = mapper
            .convertValue( getTrackerObjectCollection( "trackedEntities", root ), List.class );
        List<Enrollment> enrollments = mapper
            .convertValue( getTrackerObjectCollection( "enrollments", root ), List.class );
        List<Event> events = mapper
            .convertValue( getTrackerObjectCollection( "events", root ), List.class );
        List<Relationship> relationships = mapper
            .convertValue( getTrackerObjectCollection( "relationships", root ), List.class );

        return new TrackerDataBundle( ImmutableList.copyOf( trackedEntities ), ImmutableList.copyOf( enrollments ),
            ImmutableList.copyOf( events ), ImmutableList.copyOf( relationships ) );
    }

    /**
     * This helper method fetches the JsonNode of "property", checks if it exists (Not null) and that it's an Array.
     * @param property The property representing the array
     * @param root the roo JsonNode where the property exists.
     * @return A JsonNode if the property exists and is an array, or null.
     * @throws IOException If the property exists and is not an array, we throw an exception.
     */
    private JsonNode getTrackerObjectCollection( String property, JsonNode root )
        throws IOException
    {
        JsonNode node = root.get( property );

        if ( node != null )
        {
            if ( !node.isArray() )
            {
                throw new IOException( "Property '" + property + "' is not an Array" );
            }
        }

        return node;
    }
}

