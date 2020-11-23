package org.hisp.dhis.tracker.preheat.mappers;

import org.hisp.dhis.program.ProgramInstance;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = DebugMapper.class)
public interface ProgramInstanceMapper extends PreheatMapper<ProgramInstance>
{
    ProgramInstanceMapper INSTANCE = Mappers.getMapper( ProgramInstanceMapper.class );

    ProgramInstance map( ProgramInstance programInstance );
}
