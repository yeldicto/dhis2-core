package org.hisp.dhis.tracker.preheat.mappers;

import org.hisp.dhis.common.IdentifiableObject;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;

/**
 * @author Luciano Fiandesio
 */
public class DebugMapper
{
    private long start = 0;

    @BeforeMapping
    public void before( Object anySource )
    {
        if ( anySource != null )
        {
            String uid = "";
            if ( anySource instanceof IdentifiableObject )
            {
                uid = ((IdentifiableObject) anySource).getUid();
            }
            System.out.println( anySource.getClass().getSimpleName() + " -> " + uid );
        }
        else
        {
            System.out.println( "unknown source" );
        }
        start = System.currentTimeMillis();
    }

    @AfterMapping
    public void after()
    {
        System.out.println( System.currentTimeMillis() - start );
    }
}
