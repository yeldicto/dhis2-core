package org.hisp.dhis.tracker.preheat.mappers;

import org.hisp.dhis.program.ProgramStage;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = DebugMapper.class)
public interface ProgramStageMapper extends PreheatMapper<ProgramStage>
{
    ProgramStageMapper INSTANCE = Mappers.getMapper( ProgramStageMapper.class );

    ProgramStage map( ProgramStage programStage );
}
