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
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.log4j.Logger;
import org.mapfish.print.PDFUtils;
import org.mapfish.print.RenderingContext;
import org.mapfish.print.config.ColorWrapper;
import org.mapfish.print.utils.PJsonObject;

import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTemplate;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class LabelRenderer {

	public static final Logger LOGGER = Logger.getLogger(LabelRenderer.class);

	static void applyStyle(RenderingContext context, PdfContentByte dc,
			PJsonObject style, Geometry geometry, AffineTransform affineTransform) {
		/*
		 * See Feature/Vector.js for more information about labels
		 */
		String label = style.optString("label");
		boolean asHTML = style.optBool("asHTML", false);
		if (label != null && label.length() > 0) {
			Coordinate center = geometry.getCentroid().getCoordinate();
			center = GeometriesRenderer.transformCoordinate(center, affineTransform);
			if(asHTML) {
				int width = style.optInt("width", 300);
				
				try {
					List<Element> list = HTMLWorker.parseToList(new StringReader(label), null);
					Paragraph p = new Paragraph();
					for(Element element : list) {
						p.add(element);
					}
					PdfPCell pCell = new PdfPCell(p);
					pCell.setBorder(0);
					PdfPTable ptable1 = new PdfPTable(1);
		            ptable1.addCell(pCell);
		            ptable1.setTotalWidth(width);
		            ptable1.writeSelectedRows(0, -1, (float)center.x, (float)center.y, dc);
				} catch (IOException e) {
					LOGGER.error("Error printing HTML", e);
				}
				
			} else {
				/*
				 * Valid values for horizontal alignment: "l"=left, "c"=center,
				 * "r"=right. Valid values for vertical alignment: "t"=top,
				 * "m"=middle, "b"=bottom.
				 */
				String labelAlign = style.optString("labelAlign", "cm");
				float labelXOffset = style.optFloat("labelXOffset", (float) 0.0);
				float labelYOffset = style.optFloat("labelYOffset", (float) 0.0);
				String fontColor = style.optString("fontColor", "#000000");
				/* Supported itext fonts: COURIER, HELVETICA, TIMES_ROMAN */
				String fontFamily = style.optString("fontFamily", "HELVETICA");
				if (!"COURIER".equalsIgnoreCase(fontFamily)
						|| !"HELVETICA".equalsIgnoreCase(fontFamily)
						|| !"TIMES_ROMAN".equalsIgnoreCase(fontFamily)) {
	
					LOGGER.info("Font: '"+ fontFamily +
							"' not supported, supported fonts are 'HELVETICA', " +
							"'COURIER', 'TIMES_ROMAN', defaults to 'HELVETICA'");
					fontFamily = "HELVETICA";
				}
				String fontSize = style.optString("fontSize", "12");
				String fontWeight = style.optString("fontWeight", "normal");
				
	            
				float f = context.getStyleFactor();
				BaseFont bf = PDFUtils
						.getBaseFont(fontFamily, fontSize, fontWeight);
				float fontHeight = (float) Double.parseDouble(fontSize
						.toLowerCase().replaceAll("px", "")) * f;
				dc.setFontAndSize(bf, fontHeight);
				dc.setColorFill(ColorWrapper.convertColor(fontColor));
				dc.beginText();
				dc.setTextMatrix((float) center.x + labelXOffset * f,
	                (float) center.y + labelYOffset * f);
				dc.showTextAligned(
						PDFUtils.getHorizontalAlignment(labelAlign),
						label,
						(float) center.x + labelXOffset * f,
						(float) center.y
								+ labelYOffset
								* f
								- PDFUtils
										.getVerticalOffset(labelAlign, fontHeight),
						0);
				dc.endText();
			}
		}
	}
}
