package org.hisp.dhis.tracker.preheat.mappers;

import org.hisp.dhis.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper extends PreheatMapper<User>
{
    UserMapper INSTANCE = Mappers.getMapper( UserMapper.class );

    User map( User user );
}
