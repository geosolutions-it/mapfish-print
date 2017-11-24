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

package org.mapfish.print.config.layout;

import org.mapfish.print.InvalidJsonValueException;
import org.mapfish.print.RenderingContext;
import org.mapfish.print.utils.PJsonArray;
import org.mapfish.print.utils.PJsonObject;

import com.itextpdf.text.DocumentException;


/**
 * Bean to configure the dynamic page before between other pages.
 * It's "dynamicImagesPage" in in the layout definition.
 * <p/>
 * See http://trac.mapfish.org/trac/mapfish/wiki/PrintModuleServer#ServersideConfiguration
 */
public class DynamicImagesPage extends ExtraPage {

	/**
     * Called for each map requested by the client.
     */
	@Override
    public void render(PJsonObject params, RenderingContext context) throws DocumentException {
        // validate if have images inside
        if (!params.has("imagePages")) {
            throw new InvalidJsonValueException(params, "imagePages", null);
        }else{
        	PJsonArray imagePages = params.getJSONArray("imagePages");
        	for(int i = 0; i< imagePages.size(); i++){
                super.render(imagePages.getJSONObject(i), context);
        	}
        }
    }
	
}