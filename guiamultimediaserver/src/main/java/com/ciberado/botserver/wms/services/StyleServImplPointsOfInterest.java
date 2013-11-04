/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.botserver.wms.services;

import java.net.URL;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.filter.FilterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author ciberado
 */
public class StyleServImplPointsOfInterest implements StyleServ {
    
    private static final Logger log = LoggerFactory.getLogger(StyleServImplPointsOfInterest.class);

    @Value("#{config['debug']}")
    private boolean debug;
    @Value("#{config['maps.pictograms']}")
    private String pictogramFolder;
    private String fillColorPropertyName;
    private String iconPathPropertyName;
    static StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
    static FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);
    private Style style;

    public StyleServImplPointsOfInterest() {
    }

    @Override
    public Style getStyle() {
        if (style == null || debug) {
            style = styleFactory.createStyle();
            FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(
                    new Rule[]{
                        this.createRule("zoom min", 1070, 2500, 1),
                        this.createRule("zoom med", 500, 1070, 4),
                        this.createRule("zoom high", 200, 500, 8), // 200 was 266
                        this.createGraphicRule("zoom detail1", -1, 200, "picto_small1.png"),
                        this.createGraphicRule("zoom detail1", -1, 200, "picto_small2.png"),
                        this.createGraphicRule("zoom detail1", -1, 200, "picto_small3.png"),
                        this.createGraphicRule("zoom detail1", -1, 200, "picto_small4.png"),
                        this.createGraphicRule("zoom detail1", -1, 200, "picto_small5.png"),
                        this.createGraphicRule("zoom detail1", -1, 200, "picto_small6.png"),
                        this.createGraphicRule("zoom detail1", -1, 200, "picto_small7.png"),
                        this.createGraphicRule("zoom detail1", -1, 200, "picto_small8.png"),
                        this.createGraphicRule("zoom detail1", -1, 200, "picto_small9.png"),
                        this.createGraphicRule("zoom detail1", -1, 200, "picto_small10.png"),
                        this.createGraphicRule("zoom detail1", -1, 200, "picto_small11.png"),
                        this.createGraphicRule("zoom detail1", -1, 200, "picto_round.png"),});
            style.featureTypeStyles().add(fts);
        }
        return style;
    }

    protected Rule createGraphicRule(String name, double minScale, double maxScale, String iconName) {
        URL pictogramURL =
                StyleServImplPointsOfInterest.class.getClassLoader().getResource("META-INF/pictograms/" + iconName);
        log.warn("Loading " + pictogramURL);
        //ExternalGraphic icon = styleFactory.createExternalGraphic("file:" + pictogramFolder + "/" +iconName, "image/png");
        ExternalGraphic icon = styleFactory.createExternalGraphic(pictogramURL, "image/png");
        Graphic graphic = styleFactory.createDefaultGraphic();
        graphic.graphicalSymbols().add(icon);
        PointSymbolizer symbolizer = styleFactory.createPointSymbolizer(graphic, null);
        symbolizer.setGraphic(graphic);

        Rule rule = styleFactory.createRule();
        rule.setName(name);
        if (minScale != -1) {
            rule.setMinScaleDenominator(minScale);
        }
        if (maxScale != -1) {
            rule.setMaxScaleDenominator(maxScale);
        }

        rule.symbolizers().add(symbolizer);

        rule.setFilter(filterFactory.equal(
                           filterFactory.property(iconPathPropertyName), 
                           filterFactory.literal(iconName), true));
        return rule;
    }

    protected Rule createRule(String name, double minScale, double maxScale, int size) {
        Graphic gr = styleFactory.createDefaultGraphic();
        Mark mark = styleFactory.getCircleMark();
        mark.setStroke(styleFactory.createStroke(
                filterFactory.property(this.fillColorPropertyName),
                filterFactory.literal(1),
                filterFactory.literal(0.5)));
        mark.setFill(styleFactory.createFill(
                filterFactory.property(this.fillColorPropertyName),
                filterFactory.literal(1),
                filterFactory.literal(0.3),
                null));

        gr.graphicalSymbols().clear();
        //gr.setOpacity(Expression.NIL);
        gr.graphicalSymbols().add(mark);
        gr.setSize(filterFactory.literal(size));

        PointSymbolizer symbolizer = styleFactory.createPointSymbolizer(gr, null);

        Rule rule = styleFactory.createRule();
        rule.setName(name);
        if (minScale != -1) {
            rule.setMinScaleDenominator(minScale);
        }
        if (maxScale != -1) {
            rule.setMaxScaleDenominator(maxScale);
        }

        rule.symbolizers().add(symbolizer);

        //rule.setFilter(filterFactory.equal(filterFactory.property("POPULATION"), filterFactory.literal(50000), filterFactory.literal(true)));
        return rule;
    }

    public void setFillColorPropertyName(String fillColorPropertyName) {
        this.fillColorPropertyName = fillColorPropertyName;
    }

    public void setIconPathPropertyName(String iconPathPropertyName) {
        this.iconPathPropertyName = iconPathPropertyName;
    }
}
