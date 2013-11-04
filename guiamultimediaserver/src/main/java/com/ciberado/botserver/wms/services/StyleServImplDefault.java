/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.wms.services;

import java.awt.Color;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.filter.FilterFactory;


/**
 *
 * @author ciberado
 */
public class StyleServImplDefault implements StyleServ {

    static StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
    static FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);

    private Style style;

    public StyleServImplDefault() {
    }

    public Style getStyle() {
        if (style == null) {
            style = styleFactory.createStyle();
            createLineStyle();
            createPointStyle();
            createPolygonStyle();
        }
        return style;
    }
    
    private void createLineStyle() {
        Stroke stroke = styleFactory.createStroke(
                filterFactory.literal(Color.BLUE),
                filterFactory.literal(1));

        LineSymbolizer sym = styleFactory.createLineSymbolizer(stroke, null);
        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(sym);
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
        style.featureTypeStyles().add(fts);
    }

    private void createPolygonStyle() {
        Stroke stroke = styleFactory.createStroke(
                filterFactory.literal(Color.gray),
                filterFactory.literal(1),
                filterFactory.literal(0.5));

        Fill fill = styleFactory.createFill(
                filterFactory.literal(new Color(152,251,152)),
                filterFactory.literal(0.5));

        PolygonSymbolizer sym = styleFactory.createPolygonSymbolizer(stroke, fill, null);

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(sym);
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
        style.featureTypeStyles().add(fts);
    }

    protected void createPointStyle() {
        Graphic gr = styleFactory.createDefaultGraphic();

        Mark mark = styleFactory.getCircleMark();

        mark.setStroke(styleFactory.createStroke(
                filterFactory.literal(Color.gray), filterFactory.literal(1)));

        mark.setFill(styleFactory.createFill(filterFactory.literal(Color.white)));

        gr.graphicalSymbols().clear();
        gr.graphicalSymbols().add(mark);
        gr.setSize(filterFactory.literal(5));

        PointSymbolizer sym = styleFactory.createPointSymbolizer(gr, null);

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(sym);
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
        style.featureTypeStyles().add(fts);
    }

}
