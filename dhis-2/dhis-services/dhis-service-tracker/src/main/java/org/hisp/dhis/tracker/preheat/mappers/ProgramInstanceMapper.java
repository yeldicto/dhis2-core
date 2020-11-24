package org.hisp.dhis.tracker.preheat.mappers;

import org.hisp.dhis.program.ProgramInstance;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper( uses = DebugMapper.class )
public interface ProgramInstanceMapper extends PreheatMapper<ProgramInstance>
{
    ProgramInstanceMapper INSTANCE = Mappers.getMapper( ProgramInstanceMapper.class );

    @Mapping( target = "messageConversations", ignore = true )
    @Mapping( target = "relationshipItems", ignore = true )
    @Mapping( target = "translations", ignore = true )
    @Mapping( target = "favorites", ignore = true )
    @Mapping( target = "href", ignore = true )
    @Mapping( target = "programStageInstances", ignore = true )

    ProgramInstance map( ProgramInstance programInstance );
}
