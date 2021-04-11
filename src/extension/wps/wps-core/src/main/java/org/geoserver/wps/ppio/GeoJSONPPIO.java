/* (c) 2014 Open Source Geospatial Foundation - all rights reserved
 * (c) 2001 - 2013 OpenPlans
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wps.ppio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.geoserver.config.GeoServer;
import org.geoserver.platform.GeoServerExtensions;
import org.geotools.feature.FeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.Geometry;

/**
 * Inputs and outputs feature collections in GeoJSON format using gt-geojson
 *
 * @author Andrea Aime - OpenGeo
 */
public abstract class GeoJSONPPIO extends CDataPPIO {
    GeoServer gs;

    protected GeoJSONPPIO(Class<?> clazz) {
        super(clazz, clazz, "application/json");
        this.gs = (GeoServer) GeoServerExtensions.bean("geoServer");
    }

    protected GeoJSONPPIO(Class<?> clazz, GeoServer gs) {
        super(clazz, clazz, "application/json");
        this.gs = gs;
    }

    @Override
    public abstract void encode(Object value, OutputStream os) throws IOException;

    @Override
    public abstract Object decode(InputStream input) throws Exception;

    @Override
    public abstract Object decode(String input) throws Exception;

    @Override
    public final String getFileExtension() {
        return "json";
    }

    public static class FeatureCollections extends GeoJSONPPIO {
        public FeatureCollections() {
            super(FeatureCollection.class);
        }

        protected FeatureCollections(GeoServer gs) {
            super(FeatureCollection.class, gs);
        }

        @Override
        public void encode(Object value, OutputStream os) throws IOException {
            int decimals = gs.getSettings().getNumDecimals();
            GeometryJSON js = new GeometryJSON(decimals);
            FeatureJSON json = new FeatureJSON(js);
            // commented out due to GEOT-3209
            // json.setEncodeFeatureCRS(true);
            // json.setEncodeFeatureCollectionCRS(true);
            json.writeFeatureCollection((FeatureCollection) value, os);
        }

        @Override
        public Object decode(InputStream input) throws Exception {
            return new FeatureJSON().readFeatureCollection(input);
        }

        @Override
        public Object decode(String input) throws Exception {
            return new FeatureJSON().readFeatureCollection(input);
        }
    }

    public static class Geometries extends GeoJSONPPIO {
        public Geometries() {
            super(Geometry.class);
        }

        protected Geometries(GeoServer gs) {
            super(Geometry.class, gs);
        }

        @Override
        public void encode(Object value, OutputStream os) throws IOException {
            int decimals = gs.getSettings().getNumDecimals();
            GeometryJSON json = new GeometryJSON(decimals);
            json.write((Geometry) value, os);
        }

        @Override
        public Object decode(InputStream input) throws Exception {
            return new GeometryJSON().read(input);
        }

        @Override
        public Object decode(String input) throws Exception {
            return new GeometryJSON().read(input);
        }
    }
}
