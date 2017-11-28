/*
 * Copyright (C) 2013  Camptocamp
 *
 * This file is part of MapFish Print
 *
 * MapFish Print is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MapFish Print is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MapFish Print.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mapfish.print.map.readers;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mapfish.print.RenderingContext;
import org.mapfish.print.Transformer;
import org.mapfish.print.map.renderers.TileRenderer;
import org.mapfish.print.utils.PJsonArray;
import org.mapfish.print.utils.PJsonObject;

/**
 * Support the OSM tile layout.
 */
public class OsmMapReader extends TileableMapReader {
    public static class Factory implements MapReaderFactory {
        @Override
        public List<? extends MapReader> create(String type, RenderingContext context,
                PJsonObject params) {
            return Collections.singletonList(new OsmMapReader("t", context, params));
        }
    }

    protected final String layer;

    protected OsmMapReader(String layer, RenderingContext context, PJsonObject params) {
        super(context, params);
        this.layer = layer;
        PJsonArray maxExtent = params.getJSONArray("maxExtent");
        PJsonArray tileSize = params.getJSONArray("tileSize");

        final PJsonArray resolutions = params.getJSONArray("resolutions");
        final String extension = params.getString("extension");
        final int width = tileSize.getInt(0);
        final int hieght = tileSize.getInt(1);
        final float minX = maxExtent.getFloat(0);
        final float minY = maxExtent.getFloat(1);
        final float maxX = maxExtent.getFloat(2);
        final float maxY = maxExtent.getFloat(3);

        tileCacheLayerInfo = new OsmLayerInfo(resolutions, width, hieght, minX, minY, maxX, maxY,
                extension);
    }
    @Override
    protected TileRenderer.Format getFormat() {
        return TileRenderer.Format.BITMAP;
    }
    @Override
    protected void addCommonQueryParams(Map<String, List<String>> result, Transformer transformer, String srs, boolean first) {
        //not much query params for this protocol...
    }
    @Override
    protected URI getTileUri(URI commonUri, Transformer transformer, double minGeoX, double minGeoY, double maxGeoX, double maxGeoY, long w, long h) throws URISyntaxException, UnsupportedEncodingException {
        double targetResolution = (maxGeoX - minGeoX) / w;
        OsmLayerInfo.ResolutionInfo resolution = tileCacheLayerInfo
                .getNearestResolution(targetResolution);

        int tileX = (int) Math
                .round((minGeoX - tileCacheLayerInfo.getMinX()) / (resolution.value * w));
        int tileY = (int) Math
                .round((tileCacheLayerInfo.getMaxY() - minGeoY) / (resolution.value * h));

        int tileX1 = tileX;
        int tileY1 = tileY - 1;

        // Wrap Date Line
        if (resolution.index < 3) {
            tileX = (int) (tileX < 0 ? Math.pow(resolution.index, 2) + tileX : tileX);
            tileY = (int) (tileY < 0 ? Math.pow(resolution.index, 2) + tileY : tileY);
    
            tileX1 = (int) Math.round(tileX % Math.pow(resolution.index, 2));
            tileY1 = (int) Math.round(tileY % Math.pow(resolution.index, 2)) - 1;
    
            tileY1 = (int) (tileY1 < 0 ? Math.pow(resolution.index, 2) + tileY1 : tileY1);
        }

        StringBuilder path = new StringBuilder();
        if (!commonUri.getPath().endsWith("/")) {
            path.append('/');
        }

        path.append(String.format("%d", resolution.index));
        path.append('/').append(tileX1);
        path.append('/').append(tileY1);
        path.append('.').append(tileCacheLayerInfo.getExtension());

        return new URI(commonUri.getScheme(), commonUri.getUserInfo(), commonUri.getHost(),
                commonUri.getPort(), commonUri.getPath() + path, commonUri.getQuery(),
                commonUri.getFragment());
    }

    @Override
    public boolean testMerge(MapReader other) {
        return false;
    }
    @Override
    public boolean canMerge(MapReader other) {
        return false;
    }
    @Override
    public String toString() {
        return layer;
    }
}
