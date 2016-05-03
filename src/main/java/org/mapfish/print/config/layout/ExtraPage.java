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

import java.util.ArrayList;
import java.util.List;

import org.mapfish.print.RenderingContext;
import org.mapfish.print.utils.PJsonObject;

import com.lowagie.text.DocumentException;

/**
 * Bean to configure a dynamic page before/between/after other pages.
 * It's "extraPage" in in the layout definition.
 * 
 */
public class ExtraPage extends Page {

	public static final String AFTER_LAST_PAGE = "afterLastPage";
	public static final String BEFORE_LAST_PAGE = "beforeLastPage";
	public static final String BEFORE_MAIN_PAGE = "beforeMainPage";
	
	private String renderOn = AFTER_LAST_PAGE;
	
	public void setRenderBlock(Block renderBlock) {
		if(items == null) {
			items = new ArrayList<Block>();
		}
		items.add(renderBlock);
	}

	public String getRenderOn() {
		return renderOn;
	}

	public void setRenderOn(String renderOn) {
		this.renderOn = renderOn;
	}
	
	public static ExtraPage createAfter(Position position, Block renderBlock, Page basePage) {
		ExtraPage page = new ExtraPage();
		if(basePage != null) {
			basePage.applyPageFormat(page);
		}
		switch(position) {
			case MAIN_PAGE:
				page.setRenderOn(BEFORE_LAST_PAGE);
				break;
			case TITLE_PAGE:
				page.setRenderOn(BEFORE_MAIN_PAGE);
				break;		
			default:
				page.setRenderOn(AFTER_LAST_PAGE);
				break;
		}
		
		page.setRenderBlock(renderBlock);
		for(Block block : basePage.items) {
			if(block.isRenderOnExtraPage()) {
				page.setRenderBlock(block);
			}
		}
		return page;
	} 

    /**
     * Called for each map requested by the client.
     */
    public void render(PJsonObject params, RenderingContext context) throws DocumentException {
    	List<Block> itemsToRender = new ArrayList<Block>();
    	itemsToRender.addAll(items);
    	while(itemsToRender.size()>0) {
    		Block item = itemsToRender.remove(0);
    		items.clear();
    		items.add(item);
    		while(item.hasExtraRendering()) {
    			for(Block block : itemsToRender) {
    				if(block.isRenderOnExtraPage()) {
    					items.add(block);
    				}
    			}
    			super.render(params, context);
    			
    			items.clear();
    			items.add(item);
    		}
    	}        
    }
    
    protected void defaultRender(PJsonObject params, RenderingContext context) throws DocumentException {
    	super.render(params, context);
    }

}