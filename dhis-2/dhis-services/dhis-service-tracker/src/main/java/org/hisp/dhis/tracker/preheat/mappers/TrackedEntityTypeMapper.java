package org.hisp.dhis.tracker.preheat.mappers;

import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityType;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TrackedEntityTypeMapper
{
    TrackedEntityTypeMapper INSTANCE = Mappers.getMapper(TrackedEntityTypeMapper.class);

    TrackedEntityType map(TrackedEntityType trackedEntityType);
}
