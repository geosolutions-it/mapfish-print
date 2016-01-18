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

package org.mapfish.print.map.renderers.vector;

import java.awt.geom.AffineTransform;

import org.mapfish.print.InvalidValueException;
import org.mapfish.print.RenderingContext;
import org.mapfish.print.config.ColorWrapper;
import org.mapfish.print.utils.PJsonObject;

import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

public class LineStringRenderer extends GeometriesRenderer<LineString> {
    protected static void applyStyle(RenderingContext context, PdfContentByte dc, PJsonObject style, PdfGState state) {
        if (style == null) return;
        if (style.optString("strokeColor") != null) {
            dc.setColorStroke(ColorWrapper.convertColor(style.getString("strokeColor")));
        }
        if (style.optString("strokeOpacity") != null) {
            state.setStrokeOpacity(style.getFloat("strokeOpacity"));
        }
        final float width = style.optFloat("strokeWidth", 1) * context.getStyleFactor();
        dc.setLineWidth(width);
        final String linecap = style.optString("strokeLinecap");
        if (linecap != null) {
            if (linecap.equalsIgnoreCase("butt")) {
                dc.setLineCap(PdfContentByte.LINE_CAP_BUTT);
            } else if (linecap.equalsIgnoreCase("round")) {
                dc.setLineCap(PdfContentByte.LINE_CAP_ROUND);
            } else if (linecap.equalsIgnoreCase("square")) {
                dc.setLineCap(PdfContentByte.LINE_CAP_PROJECTING_SQUARE);
            } else {
                throw new InvalidValueException("strokeLinecap", linecap);
            }
        }
        final String dashStyle = style.optString("strokeDashstyle");
        if (dashStyle != null) {
            if (dashStyle.equalsIgnoreCase("dot")) {
                final float[] def = new float[]{0.1f, 2 * width};
                dc.setLineDash(def, 0);
            } else if (dashStyle.equalsIgnoreCase("dash")) {
                final float[] def = new float[]{2 * width, 2 * width};
                dc.setLineDash(def, 0);
            } else if (dashStyle.equalsIgnoreCase("dashdot")) {
                final float[] def = new float[]{3 * width, 2 * width, 0.1f, 2 * width};
                dc.setLineDash(def, 0);
            } else if (dashStyle.equalsIgnoreCase("longdash")) {
                final float[] def = new float[]{4 * width, 2 * width};
                dc.setLineDash(def, 0);
            } else if (dashStyle.equalsIgnoreCase("longdashdot")) {
                final float[] def = new float[]{5 * width, 2 * width, 0.1f, 2 * width};
                dc.setLineDash(def, 0);
            } else if (dashStyle.equalsIgnoreCase("solid")) {

            } else {
                throw new InvalidValueException("strokeDashstyle", dashStyle);
            }
        }
    }

    protected void renderImpl(RenderingContext context, PdfContentByte dc, PJsonObject style, LineString geometry, AffineTransform affineTransform) {
        PdfGState state = new PdfGState();
        applyStyle(context, dc, style, state);
        dc.setGState(state);
        Coordinate[] coords = geometry.getCoordinates();
        if (coords.length < 2) return;
        Coordinate coord = (Coordinate) coords[0].clone();
        transformCoordinate(coord, affineTransform);
        dc.moveTo((float) coord.x, (float) coord.y);
        if (style.has("strokeLineJoinRadius")) {
        	int len = coords.length;
        	double radius = style.getDouble("strokeLineJoinRadius");
	        for (int i = 1; i < coords.length; i++) {
	        	
	        	if(i+1 < len){
					Coordinate ptPrev = transformCoordinate((Coordinate)coords[i - 1].clone(), affineTransform);
					Coordinate pt = transformCoordinate((Coordinate)coords[i].clone(), affineTransform);
					Coordinate ptNext = transformCoordinate((Coordinate)coords[i + 1].clone(), affineTransform);
							
					
					double firstPointX = pt.x - radius * (pt.x - ptPrev.x);
					double firstPointY = pt.y - radius * (pt.y - ptPrev.y);
					
					// ///////////////////////////////////////////////////////
					// Calculate a percentage of tow segments as 
					// radius before calculating new intermediate points
					// ///////////////////////////////////////////////////////
					double secondPointX = ptNext.x - radius * (ptNext.x - pt.x);
					double secondPointY = ptNext.y - radius * (ptNext.y - pt.y);
					dc.lineTo((float)firstPointX, (float)firstPointY);
					dc.curveTo((float)pt.x, (float)pt.y, (float)secondPointX, (float)secondPointY);
					
				}else{
					coord = (Coordinate) coords[i].clone();
		            transformCoordinate(coord, affineTransform);
		            dc.lineTo((float) coord.x, (float) coord.y);
				}
	        }
        } else {
	        for (int i = 1; i < coords.length; i++) {
	            coord = (Coordinate) coords[i].clone();
	            transformCoordinate(coord, affineTransform);
	            dc.lineTo((float) coord.x, (float) coord.y);
	        }
        }
        if (style.optBool("orientation", false)) {
        	drawArrows(dc, coords, style, affineTransform);
        }
        	
        if (style.optBool("stroke", true)) dc.stroke();
    }

	private void drawArrows(PdfContentByte dc, Coordinate[] coords,
			PJsonObject style, AffineTransform affineTransform) {
		
        int len = coords.length;
        Coordinate prevArrow = null;
        for (int i = 0; i < len - 1; ++i) {
            Coordinate prevVertex = transformCoordinate((Coordinate) coords[i].clone(), affineTransform);
            Coordinate nextVertex = transformCoordinate((Coordinate) coords[i + 1].clone(), affineTransform);
            double x = (prevVertex.x + nextVertex.x) / 2;
            double y = (prevVertex.y + nextVertex.y) / 2;
            Coordinate arrow = new Coordinate(x, y);

            /*arrow.id = geometry.id + '_arrow_' + i;
            style = OpenLayers.Util.extend({}, style);
            style.graphicName = "arrow";
            style.pointRadius = 4;
            style.rotation = this.getOrientation(prevVertex, nextVertex);*/
            double rotation = getOrientation(prevVertex, nextVertex);
            double distance = 0.0;
            if (prevArrow != null) {
                    double w = prevArrow.x - arrow.x;
                    double h = prevArrow.y - arrow.y;
                    distance = Math.sqrt(w*w + h*h);
            }
			
			// ////////////////////////////////////////////////////////////////
            // Don't draw every arrow, ie. ensure that there is enough space
            // between two.
			// ////////////////////////////////////////////////////////////////
            if (prevArrow == null || distance > 40) {
                this.drawArrow(dc, arrow, rotation, 4.0);
                prevArrow = arrow;
            }
        }
		
	}

	private void drawArrow(PdfContentByte dc, Coordinate arrow, double rotation, double size) {
		double startAngle = (rotation + 135) % 360;
		double endAngle = (rotation + 225) % 360;
		
		double distX = Math.cos(startAngle * Math.PI / 180.0) * size;
		double distY = Math.sin(startAngle * Math.PI / 180.0) * size;

		double factor = 1.0; // rotation > Math.PI / 2 ? 1 : -1;
		
		Coordinate startPoint = new Coordinate(arrow.x + distX * factor, arrow.y + distY * factor);
		
		distX = Math.cos(endAngle * Math.PI / 180.0) * size;
		distY = Math.sin(endAngle * Math.PI / 180.0) * size;

		Coordinate endPoint = new Coordinate(arrow.x + distX * factor, arrow.y + distY * factor);
		
		dc.moveTo((float)startPoint.x, (float)startPoint.y);
		dc.lineTo((float)arrow.x, (float)arrow.y);
		dc.lineTo((float)endPoint.x, (float)endPoint.y);
	}

	private double getOrientation(Coordinate pt1, Coordinate pt2) {
		double x = pt2.x - pt1.x;
	    double y = pt2.y - pt1.y;

	    double rad = Math.asin(y / Math.sqrt(x * x + y * y));
	    
	    double angle = rad * 180.0 / Math.PI;
	    if (x < 0) {
	    	angle = 180 - angle;
	    }
	    if(angle < 0) {
	    	angle = 360 + angle;
	    }
	    return angle;
	}
}
