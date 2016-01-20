package org.mapfish.print.map.renderers.vector;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.print.PrinterJob;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.mapfish.print.RenderingContext;
import org.mapfish.print.utils.PJsonObject;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ByteBuffer;
import com.lowagie.text.pdf.FontMapper;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfAppearance;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDestination;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfDocument;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfOCG;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfOutline;
import com.lowagie.text.pdf.PdfPSXObject;
import com.lowagie.text.pdf.PdfPatternPainter;
import com.lowagie.text.pdf.PdfShading;
import com.lowagie.text.pdf.PdfShadingPattern;
import com.lowagie.text.pdf.PdfSpotColor;
import com.lowagie.text.pdf.PdfStructureElement;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfTextArray;
import com.lowagie.text.pdf.PdfWriter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import junit.framework.TestCase;

public class LabelRendererTest extends TestCase {
	LabelRenderer renderer = new LabelRenderer();
	GeometryFactory gf = new GeometryFactory();
	Map<String, String> styleMap = new HashMap<String, String>();
	
	public void testRichText() {
		styleMap.clear();
		styleMap.put("label", "<i>text</i>");
		styleMap.put("width", "100");
		styleMap.put("height", "100");
		styleMap.put("asHTML", "true");
		
		PJsonObject style = new PJsonObject(new JSONObject(styleMap), "rich");
		
		Geometry geometry = gf.createPoint(new Coordinate(0,0));
		Document document = new Document();

		try {
			PdfWriter writer = PdfWriter.getInstance(document, new ByteArrayOutputStream());
			document.open();
			RenderingContext context = new RenderingContext(null, writer, null, null, null, null, null);
			writer.getDirectContent();
			final List<Boolean> text = new ArrayList<Boolean>();
			PdfContentByte wrapper = new PdfContentByte(writer) {
				@Override
				public void add(PdfContentByte other) {
					text.add(true);
				}
			};
			renderer.applyStyle(context, wrapper, style, geometry, new AffineTransform());
			assertTrue(text.size() > 0);
			
		} catch (DocumentException e) {
			fail();
		}
		
	}
	
}
