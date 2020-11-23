package org.hisp.dhis.tracker.preheat;

import java.util.List;
import java.util.stream.Collectors;

import org.hisp.dhis.tracker.preheat.mappers.PreheatMapper;

/**
 * @author Luciano Fiandesio
 */
public class DetachUtils
{

    public static <T> List<T> detach( PreheatMapper<T> mapper, List<T> objects )
    {

        return objects.stream().map( mapper::map ).collect( Collectors.toList() );
    }

}
