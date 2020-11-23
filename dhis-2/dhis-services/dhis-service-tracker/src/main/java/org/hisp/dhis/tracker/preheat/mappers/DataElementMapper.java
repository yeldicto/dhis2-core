package org.hisp.dhis.tracker.preheat.mappers;

import org.hisp.dhis.dataelement.DataElement;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DataElementMapper extends PreheatMapper<DataElement>
{
    DataElementMapper INSTANCE = Mappers.getMapper( DataElementMapper.class );

    DataElement map( DataElement dataElement );
}
