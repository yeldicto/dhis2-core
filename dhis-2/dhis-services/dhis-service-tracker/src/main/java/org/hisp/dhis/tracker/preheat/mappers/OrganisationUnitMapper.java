package org.hisp.dhis.tracker.preheat.mappers;

import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrganisationUnitMapper extends PreheatMapper<OrganisationUnit>
{
    OrganisationUnitMapper INSTANCE = Mappers.getMapper( OrganisationUnitMapper.class );

    OrganisationUnit map( OrganisationUnit organisationUnit );
}
