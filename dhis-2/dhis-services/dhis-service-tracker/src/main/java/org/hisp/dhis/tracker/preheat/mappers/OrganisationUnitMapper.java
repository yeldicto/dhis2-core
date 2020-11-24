package org.hisp.dhis.tracker.preheat.mappers;

import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = DebugMapper.class)
public interface OrganisationUnitMapper extends PreheatMapper<OrganisationUnit>
{
    OrganisationUnitMapper INSTANCE = Mappers.getMapper( OrganisationUnitMapper.class );

    // FIXME invert to ignore all and only map the required fields
    @Mapping( target = "translations", ignore = true )
    @Mapping( target = "favorites", ignore = true )
    @Mapping( target = "href", ignore = true )
    @Mapping( target = "parent", ignore = true )
    @Mapping( target = "openingDate", ignore = true )
    @Mapping( target = "closedDate", ignore = true )
    @Mapping( target = "comment", ignore = true )
    @Mapping( target = "url", ignore = true )
    @Mapping( target = "contactPerson", ignore = true )
    @Mapping( target = "address", ignore = true )
    @Mapping( target = "email", ignore = true )
    @Mapping( target = "phoneNumber", ignore = true )
    @Mapping( target = "groups", ignore = true )
    @Mapping( target = "dataSets", ignore = true )
    @Mapping( target = "programs", ignore = true )
    @Mapping( target = "users", ignore = true )
    @Mapping( target = "categoryOptions", ignore = true )
    @Mapping( target = "geometry", ignore = true )
    @Mapping( target = "attributeValues", ignore = true )
    @Mapping( target = "shortName", ignore = true )
    @Mapping( target = "description", ignore = true )
    @Mapping( target = "formName", ignore = true )
    @Mapping( target = "legendSets", ignore = true )
    @Mapping( target = "periodOffset", ignore = true )
    @Mapping( target = "path", ignore = true )
    @Mapping( target = "children", ignore = true )
    @Mapping( target = "geometryAsJson", ignore = true )
    @Mapping( target = "value", ignore = true )
    @Mapping( target = "groupNames", ignore = true )
    @Mapping( target = "lastUpdatedBy", ignore = true )
    @Mapping( target = "lastUpdated", ignore = true )
    @Mapping( target = "created", ignore = true )
    @Mapping( target = "hierarchyLevel", ignore = true )
    @Mapping( target = "sortedChildren", ignore = true )
    @Mapping( target = "grandChildren", ignore = true )
    @Mapping( target = "ancestors", ignore = true )
    @Mapping( target = "sortedGrandChildren", ignore = true )
    @Mapping( target = "childrenThisIfEmpty", ignore = true )
    @Mapping( target = "dimensionItemType", ignore = true )
    OrganisationUnit map( OrganisationUnit organisationUnit );
}
