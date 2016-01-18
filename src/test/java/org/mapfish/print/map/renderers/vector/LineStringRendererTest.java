package org.mapfish.print.map.renderers.vector;

import java.awt.geom.AffineTransform;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.json.JSONObject;
import org.mapfish.print.RenderingContext;
import org.mapfish.print.utils.PJsonObject;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class LineStringRendererTest extends TestCase {
	LineStringRenderer renderer = new LineStringRenderer();
	GeometryFactory gf = new GeometryFactory();
	Map<String, String> styleMap = new HashMap<String, String>();
	
	public void testCurvedLines() {
		styleMap.clear();
		styleMap.put("strokeColor", "red");
		styleMap.put("strokeWidth", "2");
		styleMap.put("orientation", "false");
		styleMap.put("strokeLineJoinRadius", "0.5");
		
		PJsonObject style = new PJsonObject(new JSONObject(styleMap), "curved");
		
		Coordinate[] coordinates = new Coordinate[] {
			new Coordinate(0,0),
			new Coordinate(1,0),
			new Coordinate(2,1),
			new Coordinate(2,2),
			new Coordinate(1,3),
			new Coordinate(0,3),
			new Coordinate(-1,2),
			new Coordinate(-1,1),
			new Coordinate(0,0)
		};
		
		Geometry geometry = gf.createLineString(coordinates);
		Document document = new Document();

		try {
			PdfWriter writer = PdfWriter.getInstance(document, new ByteArrayOutputStream());
			document.open();
			RenderingContext context = new RenderingContext(null, writer, null, null, null, null, null);
			writer.getDirectContent();
			final List<Boolean> curves = new ArrayList<Boolean>();
			PdfContentByte wrapper = new PdfContentByte(writer) {

				@Override
				public void curveTo(float x2, float y2, float x3, float y3) {
					curves.add(true);
				}
				
			};
			renderer.render(context, wrapper, style, geometry, new AffineTransform());
			assertEquals(coordinates.length - 2, curves.size());
		} catch (DocumentException e) {
			fail();
		}
		
	}
	
	public void testArrows() {
		styleMap.clear();
		styleMap.put("strokeColor", "red");
		styleMap.put("strokeWidth", "2");
		styleMap.put("orientation", "false");
		styleMap.put("orientation", "true");
		
		PJsonObject style = new PJsonObject(new JSONObject(styleMap), "curved");
		
		Coordinate[] coordinates = new Coordinate[] {
			new Coordinate(0,0),
			new Coordinate(1,0)
		};
		
		Geometry geometry = gf.createLineString(coordinates);
		Document document = new Document();

		try {
			PdfWriter writer = PdfWriter.getInstance(document, new ByteArrayOutputStream());
			document.open();
			RenderingContext context = new RenderingContext(null, writer, null, null, null, null, null);
			writer.getDirectContent();
			final List<Boolean> curves = new ArrayList<Boolean>();
			PdfContentByte wrapper = new PdfContentByte(writer) {

				@Override
				public void lineTo(float x, float y) {
					curves.add(true);
				}
				
				
			};
			renderer.render(context, wrapper, style, geometry, new AffineTransform());
			assertEquals(3, curves.size());
		} catch (DocumentException e) {
			fail();
		}
		
	}
}
