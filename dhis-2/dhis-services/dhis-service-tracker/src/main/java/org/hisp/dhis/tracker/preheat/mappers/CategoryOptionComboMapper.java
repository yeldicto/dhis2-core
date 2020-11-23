package org.hisp.dhis.tracker.preheat.mappers;

import org.hisp.dhis.category.CategoryOptionCombo;
import org.hisp.dhis.program.ProgramInstance;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CategoryOptionComboMapper extends PreheatMapper<CategoryOptionCombo>
{
    CategoryOptionComboMapper INSTANCE = Mappers.getMapper( CategoryOptionComboMapper.class );

    CategoryOptionCombo map( ProgramInstance CategoryOptionCombo );
}
