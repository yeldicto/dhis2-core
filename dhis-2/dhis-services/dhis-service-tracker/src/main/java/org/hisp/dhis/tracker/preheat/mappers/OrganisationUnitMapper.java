package org.hisp.dhis.tracker.preheat.mappers;

import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper( )
public interface OrganisationUnitMapper extends PreheatMapper<OrganisationUnit>
{
    OrganisationUnitMapper INSTANCE = Mappers.getMapper( OrganisationUnitMapper.class );

    // @BeanMapping(ignoreByDefault = true)
    // @Mapping( target = "id")
    // @Mapping( target = "uid")
    // @Mapping( target = "code")
    // @Mapping( target = "user")
    // @Mapping( target = "publicAccess")
    // @Mapping( target = "externalAccess")
    // @Mapping( target = "userGroupAccesses")
    // @Mapping( target = "userAccesses")
    OrganisationUnit map( OrganisationUnit organisationUnit );
}
