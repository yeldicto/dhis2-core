package org.hisp.dhis.tracker.preheat.mappers;

import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = DebugMapper.class)
public interface TrackedEntityInstanceMapper extends PreheatMapper<TrackedEntityInstance>
{
    TrackedEntityInstanceMapper INSTANCE = Mappers.getMapper( TrackedEntityInstanceMapper.class );

    TrackedEntityInstance map( TrackedEntityInstance trackedEntityInstance );
}
