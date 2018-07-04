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

import org.json.JSONException;
import org.json.JSONWriter;
import org.mapfish.print.InvalidValueException;
import org.mapfish.print.RenderingContext;
import org.mapfish.print.utils.PJsonArray;
import org.mapfish.print.utils.PJsonObject;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;

/**
 * Config and logic for one layout instance.
 */
public class Layout {
    private MetaData metaData;

    private TitlePage titlePage;

    private MainPage mainPage;

    private DynamicImagesPage dynamicImagesPage;

    private DefaultExtraPage extraPage;

    private LastPage lastPage;

    private String outputFilename;

    public void render(PJsonObject params, RenderingContext context) throws DocumentException {
        if (metaData != null) {
            metaData.render(params, context);
        }

        if (titlePage != null && params.optBool("includeTitlePage", true)) {
            titlePage.render(params, context);
        }
        renderExtraPages(ExtraPage.BEFORE_MAIN_PAGE, params, context);

        if (mainPage != null) {
            PJsonArray pages = params.getJSONArray("pages");
            for (int i = 0; i < pages.size(); ++i) {
                final PJsonObject cur = pages.getJSONObject(i);
                mainPage.render(cur, context);
            }
        }

        renderExtraPages(ExtraPage.BEFORE_LAST_PAGE, params, context);

        if (lastPage != null && params.optBool("includeLastPage", true)) {
            lastPage.render(params, context);
        }

        renderExtraPages(ExtraPage.AFTER_LAST_PAGE, params, context);
    }

    private void renderExtraPages(String position, PJsonObject params, RenderingContext context)
            throws DocumentException {
        for (ExtraPage extraPage : context.getExtraPages()) {
            if (position.equals(extraPage.getRenderOn())) {
                extraPage.render(params, context);
            }
        }
        if (this.extraPage != null && position.equals(this.extraPage.getRenderOn())
                && params.optBool("includeExtraPage", true)) {
            this.extraPage.render(params, context);
        }
        // deprecated: please, use extra pages
        if (dynamicImagesPage != null) {
            if (position.equals(dynamicImagesPage.getRenderOn())) {
                dynamicImagesPage.render(params, context);
            }
        }
    }

    public void setTitlePage(TitlePage titlePage) {
        this.titlePage = titlePage;
    }

    public MainPage getMainPage() {
        return mainPage;
    }

    public void setMainPage(MainPage mainPage) {
        this.mainPage = mainPage;
    }

    public void setLastPage(LastPage lastPage) {
        this.lastPage = lastPage;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

    /**
     * Taken for compatibility, please use extraPages.
     * 
     * @deprecated
     * @return
     */
    public DynamicImagesPage getDynamicImagesPage() {
        return dynamicImagesPage;
    }

    /**
     * Taken for compatibility, please use extraPages.
     * 
     * @deprecated
     * @return
     */
    public void setDynamicImagesPage(DynamicImagesPage dynamicImagesPage) {
        this.dynamicImagesPage = dynamicImagesPage;
    }

    public DefaultExtraPage getExtraPage() {
        return extraPage;
    }

    public void setExtraPage(DefaultExtraPage extraPage) {
        this.extraPage = extraPage;
    }

    public Rectangle getFirstPageSize(RenderingContext context, PJsonObject params) {
        if (titlePage != null) {
            return titlePage.getPageSizeRect(context, params);
        } else {
            return mainPage.getPageSizeRect(context, params);
        }
    }

    public void printClientConfig(JSONWriter json) throws JSONException {
        mainPage.printClientConfig(json);
    }

    public boolean isSupportLegacyReader() {
        return metaData!=null && metaData.isSupportLegacyReader();
    }

    /**
     * Called just after the config has been loaded to check it is valid.
     * @throws InvalidValueException When there is a problem
     */
    public void validate() {
        if(mainPage==null) throw new InvalidValueException("mainPage", "null");
        mainPage.validate();

        if(titlePage!=null) titlePage.validate();
        if(lastPage !=null) lastPage.validate();
    }

    public String getOutputFilename() {
        return outputFilename;
    }

    public void setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename;
    }
}
