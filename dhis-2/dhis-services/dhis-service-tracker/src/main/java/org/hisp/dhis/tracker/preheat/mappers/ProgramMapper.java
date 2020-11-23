package org.hisp.dhis.tracker.preheat.mappers;

import org.hisp.dhis.program.Program;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = DebugMapper.class)
public interface ProgramMapper extends PreheatMapper<Program>
{
    ProgramMapper INSTANCE = Mappers.getMapper( ProgramMapper.class );

    Program map( Program programStage );
}
