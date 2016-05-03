package org.mapfish.print.config.layout;

import org.mapfish.print.RenderingContext;
import org.mapfish.print.utils.PJsonObject;

import com.lowagie.text.DocumentException;

public class DefaultExtraPage extends ExtraPage {

	@Override
	public void render(PJsonObject params, RenderingContext context)
			throws DocumentException {
		super.defaultRender(params, context);
	}
	
}
