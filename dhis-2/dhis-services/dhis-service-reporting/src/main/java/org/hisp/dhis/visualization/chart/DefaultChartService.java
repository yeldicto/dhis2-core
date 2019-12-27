/*
 * Copyright (c) 2004-2019, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.visualization.chart;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.springframework.util.Assert.notNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.analytics.AnalyticsService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.visualization.ChartService;
import org.hisp.dhis.visualization.Visualization;
import org.jfree.chart.JFreeChart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for encapsulating the chart generation making usage of
 * the underline services and components required.
 */
@Service
public class DefaultChartService
    implements
    ChartService
{

    private final AnalyticsService analyticsService;

    private final OrganisationUnitService organisationUnitService;

    private final CurrentUserService currentUserService;

    private final ChartProvider chartProvider;

    public DefaultChartService( final AnalyticsService analyticsService,
        final OrganisationUnitService organisationUnitService, final CurrentUserService currentUserService,
        final ChartProvider chartProvider )
    {
        checkNotNull( analyticsService );
        checkNotNull( organisationUnitService );
        checkNotNull( currentUserService );
        checkNotNull( chartProvider );

        this.analyticsService = analyticsService;
        this.organisationUnitService = organisationUnitService;
        this.currentUserService = currentUserService;
        this.chartProvider = chartProvider;
    }

    /**
     * Generates a JFreeChart.
     *
     * @param visualization the chart to use as basis for the JFreeChart generation.
     * @param date the date to use as basis for relative periods, can be null.
     * @param organisationUnit the org unit to use as basis for relative units, will
     *        override the current user org unit if set, can be null.
     * @param format the i18n format.
     * @param currentUser the current logged-in user.
     * 
     * @return a JFreeChart object.
     */
    @Override
    @Transactional( readOnly = true )
    public JFreeChart generateChart( final Visualization visualization, final Date date,
        OrganisationUnit organisationUnit, final I18nFormat format, final User currentUser )
    {
        User user = (currentUser != null ? currentUser : currentUserService.getCurrentUser());

        if ( organisationUnit == null && user != null )
        {
            organisationUnit = user.getOrganisationUnit();
        }

        List<OrganisationUnit> atLevels = new ArrayList<>();
        List<OrganisationUnit> inGroups = new ArrayList<>();

        if ( visualization.hasOrganisationUnitLevels() )
        {
            atLevels.addAll( organisationUnitService.getOrganisationUnitsAtLevels(
                visualization.getOrganisationUnitLevels(), visualization.getOrganisationUnits() ) );
        }

        if ( visualization.hasItemOrganisationUnitGroups() )
        {
            inGroups.addAll( organisationUnitService.getOrganisationUnits(
                visualization.getItemOrganisationUnitGroups(), visualization.getOrganisationUnits() ) );
        }

        visualization.init( user, date, organisationUnit, atLevels, inGroups, format );

        JFreeChart resultChart = createChartFrom( visualization );

        visualization.clearTransientState();

        return resultChart;
    }

    /**
     * Returns a JFreeChart of type defined in the chart argument.
     * 
     * @param visualization
     * 
     * @return the JFreeChart graphic object.
     */
    private JFreeChart createChartFrom( final Visualization visualization )
    {
        notNull( visualization.getType(), "Visualization type cannot be null." );

        final Map<String, Object> valueMap = analyticsService.getAggregatedDataValueMapping( visualization );

        switch ( visualization.getType() )
        {
        case AREA:
            return chartProvider.area( visualization, valueMap );
        case BAR:
            return chartProvider.bar( visualization, valueMap );
        case COLUMN:
            return chartProvider.column( visualization, valueMap );
        case GAUGE:
            return chartProvider.gauge( visualization, valueMap );
        case LINE:
            return chartProvider.line( visualization, valueMap );
        case PIE:
            return chartProvider.pie( visualization, valueMap );
        case RADAR:
            return chartProvider.radar( visualization, valueMap );
        case STACKED_BAR:
            return chartProvider.stackedBar( visualization, valueMap );
        case STACKED_COLUMN:
            return chartProvider.stackedColumn( visualization, valueMap );
        default:
            throw new IllegalArgumentException( "Invalid chart type: " + visualization.getType() );
        }
    }
}
