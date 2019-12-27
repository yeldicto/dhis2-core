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

import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static java.awt.Color.decode;
import static java.awt.Font.BOLD;
import static java.awt.Font.PLAIN;
import static java.awt.Font.SANS_SERIF;
import static org.hisp.dhis.common.AnalyticsType.AGGREGATE;
import static org.hisp.dhis.common.DimensionalObject.DIMENSION_SEP;
import static org.hisp.dhis.system.util.MathUtils.ZERO;
import static org.jfree.chart.axis.CategoryLabelPositions.UP_45;
import static org.jfree.chart.plot.DatasetRenderingOrder.FORWARD;
import static org.jfree.chart.plot.PlotOrientation.HORIZONTAL;
import static org.jfree.chart.plot.PlotOrientation.VERTICAL;
import static org.jfree.chart.util.TableOrder.BY_ROW;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.hisp.dhis.common.AnalyticsType;
import org.hisp.dhis.common.DimensionalObjectUtils;
import org.hisp.dhis.common.NameableObject;
import org.hisp.dhis.common.NumericSortWrapper;
import org.hisp.dhis.dataelement.DataElementOperand;
import org.hisp.dhis.system.util.MathUtils;
import org.hisp.dhis.visualization.Visualization;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DialShape;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.MeterInterval;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import org.springframework.stereotype.Component;

/**
 * This component is responsible for providing methods related to chart
 * manipulation and generation. The implementation is coupled to the
 * Visualization object, which is used as the base for the chart generation.
 */
@Component
class ChartProvider
{

    private static final Font TITLE_FONT = new Font( SANS_SERIF, BOLD, 12 );

    private static final Font SUB_TITLE_FONT = new Font( SANS_SERIF, PLAIN, 11 );

    private static final Font LABEL_FONT = new Font( SANS_SERIF, PLAIN, 10 );

    private static final String TREND_PREFIX = "Trend - ";

    private static final Color[] COLORS = { decode( "#88be3b" ), decode( "#3b6286" ), decode( "#b7404c" ),
        decode( "#ff9f3a" ), decode( "#968f8f" ), decode( "#b7409f" ), decode( "#ffda64" ), decode( "#4fbdae" ),
        decode( "#b78040" ), decode( "#676767" ), decode( "#6a33cf" ), decode( "#4a7833" ) };

    private static final Color COLOR_LIGHT_GRAY = decode( "#dddddd" );

    private static final Color COLOR_LIGHTER_GRAY = decode( "#eeeeee" );

    private static final Color DEFAULT_BACKGROUND_COLOR = WHITE;

    /**
     * Generates a area chart.
     *
     * @param visualization
     * @param valueMap
     * 
     * @return the respective JFreeChart object.
     */
    JFreeChart area( final Visualization visualization, final Map<String, Object> valueMap )
    {
        final CategoryDataset dataSet = getCategoryDataSet( visualization, valueMap )[0];
        JFreeChart stackedAreaChart = ChartFactory.createStackedAreaChart( visualization.getName(),
            visualization.getDomainAxisLabel(), visualization.getRangeAxisLabel(), dataSet, VERTICAL,
            !visualization.isHideLegend(), false, false );

        setBasicConfig( stackedAreaChart, visualization );

        CategoryPlot plot = (CategoryPlot) stackedAreaChart.getPlot();
        plot.setOrientation( VERTICAL );
        plot.setRenderer( getStackedAreaRenderer() );

        CategoryAxis xAxis = plot.getDomainAxis();
        xAxis.setCategoryLabelPositions( UP_45 );
        xAxis.setLabelFont( LABEL_FONT );

        return stackedAreaChart;
    }

    /**
     * Generates a radar chart.
     *
     * @param visualization
     * @param valueMap
     * 
     * @return the respective JFreeChart object.
     */
    JFreeChart radar( final Visualization visualization, final Map<String, Object> valueMap )
    {
        final CategoryDataset dataSet = getCategoryDataSet( visualization, valueMap )[0];

        SpiderWebPlot plot = new SpiderWebPlot( dataSet, BY_ROW );
        plot.setLabelFont( LABEL_FONT );

        JFreeChart radarChart = new JFreeChart( visualization.getName(), TITLE_FONT, plot,
            !visualization.isHideLegend() );

        setBasicConfig( radarChart, visualization );

        return radarChart;
    }

    /**
     * Generates a stacked bar chart.
     *
     * @param visualization
     * @param valueMap
     * 
     * @return the respective JFreeChart object.
     */
    JFreeChart stackedBar( final Visualization visualization, final Map<String, Object> valueMap )
    {
        final CategoryDataset dataSet = getCategoryDataSet( visualization, valueMap )[0];
        return getStackedBarOrColumnChart( visualization, dataSet, true );
    }

    /**
     * Generates a staked column chart.
     *
     * @param visualization
     * @param valueMap
     * 
     * @return the respective JFreeChart object.
     */
    JFreeChart stackedColumn( final Visualization visualization, final Map<String, Object> valueMap )
    {
        final CategoryDataset dataSet = getCategoryDataSet( visualization, valueMap )[0];
        return getStackedBarOrColumnChart( visualization, dataSet, false );
    }

    /**
     * An auxiliary method that generates a stacked bar or column chart.
     * 
     * @param visualization
     * @param dataSet
     * @param horizontal
     * 
     * @return the respective JFreeChart object.
     */
    private JFreeChart getStackedBarOrColumnChart( final Visualization visualization, final CategoryDataset dataSet,
        boolean horizontal )
    {
        JFreeChart stackedBarChart = ChartFactory.createStackedBarChart( visualization.getName(),
            visualization.getDomainAxisLabel(), visualization.getRangeAxisLabel(), dataSet, VERTICAL,
            !visualization.isHideLegend(), false, false );

        setBasicConfig( stackedBarChart, visualization );

        CategoryPlot plot = (CategoryPlot) stackedBarChart.getPlot();
        plot.setOrientation( horizontal ? HORIZONTAL : VERTICAL );
        plot.setRenderer( getStackedBarRenderer() );

        CategoryAxis xAxis = plot.getDomainAxis();
        xAxis.setCategoryLabelPositions( UP_45 );

        return stackedBarChart;
    }

    /**
     * Generates a pie chart.
     * 
     * @param visualization
     * @param valueMap
     *
     * @return the respective JFreeChart object.
     */
    JFreeChart pie( final Visualization visualization, final Map<String, Object> valueMap )
    {
        final CategoryDataset[] dataSets = getCategoryDataSet( visualization, valueMap );

        JFreeChart multiplePieChart = ChartFactory.createMultiplePieChart( visualization.getName(), dataSets[0], BY_ROW,
            !visualization.isHideLegend(), false, false );

        setBasicConfig( multiplePieChart, visualization );

        if ( multiplePieChart.getLegend() != null )
        {
            multiplePieChart.getLegend().setItemFont( SUB_TITLE_FONT );
        }

        MultiplePiePlot multiplePiePlot = (MultiplePiePlot) multiplePieChart.getPlot();
        JFreeChart pieChart = multiplePiePlot.getPieChart();
        pieChart.setBackgroundPaint( DEFAULT_BACKGROUND_COLOR );
        pieChart.getTitle().setFont( SUB_TITLE_FONT );

        PiePlot piePlot = (PiePlot) pieChart.getPlot();
        piePlot.setBackgroundPaint( DEFAULT_BACKGROUND_COLOR );
        piePlot.setOutlinePaint( DEFAULT_BACKGROUND_COLOR );
        piePlot.setLabelFont( LABEL_FONT );
        piePlot.setLabelGenerator( new StandardPieSectionLabelGenerator( "{2}" ) );
        piePlot.setSimpleLabels( true );
        piePlot.setIgnoreZeroValues( true );
        piePlot.setIgnoreNullValues( true );
        piePlot.setShadowXOffset( 0d );
        piePlot.setShadowYOffset( 0d );

        for ( int i = 0; i < dataSets[0].getColumnCount(); i++ )
        {
            piePlot.setSectionPaint( dataSets[0].getColumnKey( i ), COLORS[(i % COLORS.length)] );
        }

        return multiplePieChart;
    }

    /**
     * Generates a gauge chart.
     * 
     * @param visualization
     * @param valueMap
     * 
     * @return the respective JFreeChart object.
     */
    JFreeChart gauge( final Visualization visualization, final Map<String, Object> valueMap )
    {
        final CategoryDataset dataSet = getCategoryDataSet( visualization, valueMap )[0];

        Number number = dataSet.getValue( 0, 0 );
        ValueDataset valueDataSet = new DefaultValueDataset( number );

        MeterPlot meterPlot = new MeterPlot( valueDataSet );

        meterPlot.setUnits( "" );
        meterPlot.setRange( new Range( 0.0d, 100d ) );

        for ( int i = 0; i < 10; i++ )
        {
            double start = i * 10d;
            double end = start + 10d;
            String label = String.valueOf( start );

            meterPlot.addInterval(
                new MeterInterval( label, new Range( start, end ), COLOR_LIGHT_GRAY, null, COLOR_LIGHT_GRAY ) );
        }

        meterPlot.setMeterAngle( 180 );
        meterPlot.setDialBackgroundPaint( COLOR_LIGHT_GRAY );
        meterPlot.setDialShape( DialShape.CHORD );
        meterPlot.setNeedlePaint( COLORS[0] );
        meterPlot.setTickLabelsVisible( true );
        meterPlot.setTickLabelFont( LABEL_FONT );
        meterPlot.setTickLabelPaint( BLACK );
        meterPlot.setTickPaint( COLOR_LIGHTER_GRAY );
        meterPlot.setValueFont( TITLE_FONT );
        meterPlot.setValuePaint( BLACK );

        JFreeChart meterChart = new JFreeChart( visualization.getName(), meterPlot );
        setBasicConfig( meterChart, visualization );
        meterChart.removeLegend();

        return meterChart;
    }

    /**
     * Generates a bar chart.
     * 
     * @param visualization
     * @param valueMap
     *
     * @return the respective JFreeChart object.
     */
    JFreeChart bar( final Visualization visualization, final Map<String, Object> valueMap )
    {
        final CategoryDataset[] dataSets = getCategoryDataSet( visualization, valueMap );
        final CategoryPlot plot = new CategoryPlot( dataSets[0], new CategoryAxis(), new NumberAxis(), getBarRenderer() );
        plot.setOrientation( HORIZONTAL );
        return applyCommonDefinitions( visualization, dataSets, getLineRenderer(), plot );
    }

    /**
     * Generates a column chart.
     * 
     * @param visualization
     * @param valueMap
     *
     * @return the respective JFreeChart object.
     */
    JFreeChart column( final Visualization visualization, final Map<String, Object> valueMap )
    {
        final CategoryDataset[] dataSets = getCategoryDataSet( visualization, valueMap );
        CategoryPlot plot = new CategoryPlot( dataSets[0], new CategoryAxis(), new NumberAxis(), getBarRenderer() );
        plot.setOrientation( VERTICAL );
        return applyCommonDefinitions( visualization, dataSets, getLineRenderer(), plot );
    }

    /**
     * Generates a radar chart.
     * 
     * @param visualization
     * @param valueMap
     *
     * @return the respective JFreeChart object.
     */
    JFreeChart line( final Visualization visualization, final Map<String, Object> valueMap )
    {
        final CategoryDataset[] dataSets = getCategoryDataSet( visualization, valueMap );
        CategoryPlot plot = new CategoryPlot( dataSets[0], new CategoryAxis(), new NumberAxis(), getLineRenderer() );
        plot.setOrientation( VERTICAL );
        return applyCommonDefinitions( visualization, dataSets, getLineRenderer(), plot );
    }

    /**
     * Creates a base JFreeChart instance using common definitions required
     * for bar, column and line charts.
     * 
     * @param visualization
     * @param dataSets
     * @param lineRenderer
     * @param plot
     * 
     * @return a base JFreeChart object for bar, column and line charts.
     */
    private JFreeChart applyCommonDefinitions( final Visualization visualization, final CategoryDataset[] dataSets,
        final LineAndShapeRenderer lineRenderer, final CategoryPlot plot )
    {
        if ( visualization.isRegression() )
        {
            plot.setDataset( 1, dataSets[1] );
            plot.setRenderer( 1, lineRenderer );
        }

        JFreeChart jFreeChart = new JFreeChart( visualization.getName(), TITLE_FONT, plot,
            !visualization.isHideLegend() );

        setBasicConfig( jFreeChart, visualization );

        if ( visualization.isTargetLine() )
        {
            plot.addRangeMarker( getMarker( visualization.getTargetLineValue(), visualization.getTargetLineLabel() ) );
        }

        if ( visualization.isBaseLine() )
        {
            plot.addRangeMarker( getMarker( visualization.getBaseLineValue(), visualization.getBaseLineLabel() ) );
        }

        if ( visualization.isHideSubtitle() )
        {
            jFreeChart.addSubtitle( getSubTitle( visualization ) );
        }

        plot.setDatasetRenderingOrder( FORWARD );

        // ---------------------------------------------------------------------
        // Category label positions
        // ---------------------------------------------------------------------

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions( UP_45 );
        domainAxis.setLabel( visualization.getDomainAxisLabel() );

        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setLabel( visualization.getRangeAxisLabel() );
        return jFreeChart;
    }

    /**
     * Returns a CategoryDataset[] containing a regular Dataset and a regression Dataset
     * based on the given aggregateValueMap.
     * 
     * @param visualization
     * @param aggregateValueMap
     * 
     * @return a CategoryDataset[] object
     */
    private CategoryDataset[] getCategoryDataSet( final Visualization visualization,
        Map<String, Object> aggregateValueMap )
    {
        DefaultCategoryDataset regularDataSet = new DefaultCategoryDataset();
        DefaultCategoryDataset regressionDataSet = new DefaultCategoryDataset();

        SimpleRegression regression = new SimpleRegression();

        aggregateValueMap = DimensionalObjectUtils.getSortedKeysMap( aggregateValueMap );

        java.util.List<NameableObject> seriez = new ArrayList<>( visualization.getColumns() );
        List<NameableObject> categories = new ArrayList<>( visualization.getRows() );

        if ( visualization.hasSortOrder() )
        {
            categories = getSortedCategories( categories, visualization, aggregateValueMap );
        }

        for ( NameableObject series : seriez )
        {
            double categoryIndex = 0;

            for ( NameableObject category : categories )
            {
                categoryIndex++;

                String key = getKey( series, category, AGGREGATE );

                Object object = aggregateValueMap.get( key );

                Number value = object != null && object instanceof Number ? (Number) object : null;

                if ( value != null )
                {
                    regularDataSet.addValue( value, series.getShortName(), category.getShortName() );
                }

                if ( visualization.isRegression() && value != null && value instanceof Double
                    && !MathUtils.isEqual( (Double) value, ZERO ) )
                {
                    regression.addData( categoryIndex, (Double) value );
                }
            }

            if ( visualization.isRegression() ) // Period must be category
            {
                categoryIndex = 0;

                for ( NameableObject category : visualization.getRows() )
                {
                    final double value = regression.predict( categoryIndex++ );

                    // Enough values must exist for regression

                    if ( !Double.isNaN( value ) )
                    {
                        regressionDataSet.addValue( value, TREND_PREFIX + series.getShortName(),
                            category.getShortName() );
                    }
                }
            }
        }

        return new CategoryDataset[] { regularDataSet, regressionDataSet };
    }

    /**
     * Returns a default stacked bar renderer.
     * 
     * @return a pre-defined StackedBarRenderer object.
     */
    private StackedBarRenderer getStackedBarRenderer()
    {
        StackedBarRenderer renderer = new StackedBarRenderer();

        for ( int i = 0; i < COLORS.length; i++ )
        {
            renderer.setSeriesPaint( i, COLORS[i] );
            renderer.setShadowVisible( false );
        }

        return renderer;
    }

    /**
     * Returns a default stacked area renderer.
     * 
     * @return a pre-defined AreaRenderer object.
     */
    private AreaRenderer getStackedAreaRenderer()
    {
        StackedAreaRenderer renderer = new StackedAreaRenderer();

        for ( int i = 0; i < COLORS.length; i++ )
        {
            renderer.setSeriesPaint( i, COLORS[i] );
        }

        return renderer;
    }

    /**
     * Returns a default bar renderer.
     * 
     * @return a pre-defined BarRenderer object.
     */
    private BarRenderer getBarRenderer()
    {
        BarRenderer renderer = new BarRenderer();

        renderer.setMaximumBarWidth( 0.07 );

        for ( int i = 0; i < COLORS.length; i++ )
        {
            renderer.setSeriesPaint( i, COLORS[i] );
            renderer.setShadowVisible( false );
        }

        return renderer;
    }

    /**
     * Returns a default line renderer.
     * 
     * @return a pre-defined LineAndShapeRenderer object.
     */
    private LineAndShapeRenderer getLineRenderer()
    {
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();

        for ( int i = 0; i < COLORS.length; i++ )
        {
            renderer.setSeriesPaint( i, COLORS[i] );
        }

        return renderer;
    }

    /**
     * Creates a key based on the given input. Sorts the key on its components to
     * remove significance of column order.
     * 
     * @param series
     * @param category
     * @param analyticsType
     * 
     * @return the created key.
     */
    private String getKey( final NameableObject series, final NameableObject category,
        final AnalyticsType analyticsType )
    {
        String key = series.getUid() + DIMENSION_SEP + category.getUid();

        // Replace potential operand separator with dimension separator

        key = AGGREGATE.equals( analyticsType ) ? key.replace( DataElementOperand.SEPARATOR, DIMENSION_SEP ) : key;

        // TODO fix issue with keys including -.

        return DimensionalObjectUtils.sortKey( key );
    }

    /**
     * Returns a list of sorted nameable objects. Sorting is defined per the
     * corresponding value in the given value map.
     * 
     * @param categories
     * @param visualization
     * @param valueMap
     * 
     * @return a List<NameableObject> of sorted categories.
     */
    private List<NameableObject> getSortedCategories( final List<NameableObject> categories,
        final Visualization visualization, final Map<String, Object> valueMap )
    {
        NameableObject series = visualization.getColumns().get( 0 );

        int sortOrder = visualization.getSortOrder();

        List<NumericSortWrapper<NameableObject>> list = new ArrayList<>();

        for ( NameableObject category : categories )
        {
            String key = getKey( series, category, AGGREGATE );

            Object value = valueMap.get( key );

            if ( value instanceof Number )
            {
                list.add( new NumericSortWrapper<>( category, (Double) value, sortOrder ) );
            }
        }

        Collections.sort( list );

        return NumericSortWrapper.getObjectList( list );
    }

    /**
     * Sets basic configuration including title font, subtitle, background paint and
     * anti-alias on the given JFreeChart.
     * 
     * @param jFreeChart
     * @param visualization
     */
    private void setBasicConfig( final JFreeChart jFreeChart, final Visualization visualization )
    {
        jFreeChart.getTitle().setFont( TITLE_FONT );

        jFreeChart.setBackgroundPaint( DEFAULT_BACKGROUND_COLOR );
        jFreeChart.setAntiAlias( true );

        if ( !visualization.isHideTitle() )
        {
            jFreeChart.addSubtitle( getSubTitle( visualization ) );
        }

        Plot plot = jFreeChart.getPlot();
        plot.setBackgroundPaint( DEFAULT_BACKGROUND_COLOR );
        plot.setOutlinePaint( DEFAULT_BACKGROUND_COLOR );
    }

    private TextTitle getSubTitle( final Visualization visualization )
    {
        TextTitle textTitle = new TextTitle();

        String title = visualization.hasTitle() ? visualization.getTitle() : visualization.generateTitle();

        textTitle.setFont( SUB_TITLE_FONT );
        textTitle.setText( title );

        return textTitle;
    }

    /**
     * Returns a horizontal line marker for the given x value and label.
     *
     * @param value
     * @param label
     *
     * @return a Marker containing the given value and label.
     */
    private Marker getMarker( final Double value, final String label )
    {
        Marker marker = new ValueMarker( value );
        marker.setPaint( BLACK );
        marker.setStroke( new BasicStroke( 1.1f ) );
        marker.setLabel( label );
        marker.setLabelOffset( new RectangleInsets( -10, 50, 0, 0 ) );
        marker.setLabelFont( SUB_TITLE_FONT );

        return marker;
    }
}
