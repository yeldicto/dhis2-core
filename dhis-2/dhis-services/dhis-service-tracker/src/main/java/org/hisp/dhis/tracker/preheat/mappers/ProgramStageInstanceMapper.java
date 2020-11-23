package org.hisp.dhis.tracker.preheat.mappers;

import org.hisp.dhis.program.ProgramStageInstance;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProgramStageInstanceMapper extends PreheatMapper<ProgramStageInstance>
{
    ProgramStageInstanceMapper INSTANCE = Mappers.getMapper( ProgramStageInstanceMapper.class );

    ProgramStageInstance map( ProgramStageInstance programStageInstance );
}
