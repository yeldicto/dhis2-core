package org.hisp.dhis.tracker.preheat.mappers;

import org.hisp.dhis.relationship.Relationship;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RelationshipMapper extends PreheatMapper<Relationship>
{
    RelationshipMapper INSTANCE = Mappers.getMapper( RelationshipMapper.class );

    Relationship map( Relationship relationship );
}
