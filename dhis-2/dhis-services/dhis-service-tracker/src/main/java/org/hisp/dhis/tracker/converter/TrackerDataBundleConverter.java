package org.hisp.dhis.tracker.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.collect.ImmutableList;
import org.hisp.dhis.tracker.domain.*;

import java.io.IOException;
import java.util.List;

public class TrackerDataBundleConverter
    extends
    StdDeserializer<TrackerDataBundle>
{

    protected TrackerDataBundleConverter( Class<?> vc )
    {
        super( vc );
    }

    @Override
    public TrackerDataBundle deserialize( JsonParser jsonParser, DeserializationContext deserializationContext )
        throws IOException, JsonProcessingException
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = jsonParser.getCodec().readTree( jsonParser );

        List<TrackedEntity> trackedEntities = mapper.convertValue( root.get( "trackedEntities" ), List.class );
        List<Enrollment> enrollments = mapper.convertValue( root.get( "enrollment" ), List.class );
        List<Event> events = mapper.convertValue( root.get( "events" ), List.class );
        List<Relationship> relationships = mapper.convertValue( root.get( "relationships" ), List.class );

        return new TrackerDataBundle( ImmutableList.copyOf( trackedEntities ), ImmutableList.copyOf( enrollments ),
            ImmutableList.copyOf( events ), ImmutableList.copyOf( relationships ) );
    }
}

