package org.mapfish.print.map.readers;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.mapfish.print.MapPrinter;
import org.mapfish.print.MapTestBasic;
import org.mapfish.print.ShellMapPrinter;
import org.mapfish.print.Transformer;
import org.mapfish.print.map.renderers.TestTileRenderer;
import org.mapfish.print.utils.DistanceUnit;
import org.mapfish.print.utils.PJsonObject;
import org.pvalsecc.misc.FileUtilities;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class WMSMapReaderTest extends MapTestBasic {

	PJsonObject wmsSpec1;
		
	PJsonObject wmsSpec2;
		
	PJsonObject wmsSpec3;
	
	PJsonObject wmsSpec4;
		
	MapReaderFactoryFinder ff = new ClassPathXmlApplicationContext(ShellMapPrinter.DEFAULT_SPRING_CONTEXT).getBean(MapReaderFactoryFinder.class);
	MapReaderFactory factory= ff.getFactory("WMS");
	
	Transformer transformer = new Transformer(0.0f,0.0f , 100.0f,
            100.0f, 1000, 96, DistanceUnit.M,
            0.0, "EPSG:4326", false);
	
	public WMSMapReaderTest(String name) {
		super(name);		
	}

	protected void setUp() throws Exception {
        super.setUp();
        
        TestTileRenderer.lastURIs = null;
        
        wmsSpec1 = MapPrinter.parseSpec(FileUtilities.readWholeTextFile(new File(WMSMapReaderTest.class.getClassLoader()
                .getResource("layers/layer_with_filter1_spec.json").getFile())));
        
        wmsSpec2 = MapPrinter.parseSpec(FileUtilities.readWholeTextFile(new File(WMSMapReaderTest.class.getClassLoader()
                .getResource("layers/layer_with_filter2_spec.json").getFile())));
        
        wmsSpec3 = MapPrinter.parseSpec(FileUtilities.readWholeTextFile(new File(WMSMapReaderTest.class.getClassLoader()
                .getResource("layers/layer_with_filter3_spec.json").getFile())));
        
        wmsSpec4 = MapPrinter.parseSpec(FileUtilities.readWholeTextFile(new File(WMSMapReaderTest.class.getClassLoader()
                .getResource("layers/layer_with_no_filter_spec.json").getFile())));

    }

    protected void tearDown() throws Exception {

        super.tearDown();
    }
    
    public void testNoFilter() throws JSONException, UnsupportedEncodingException, URISyntaxException{

    	PJsonObject wms_full = new PJsonObject(wmsSpec4.getInternalObj(), "");
    	
        WMSMapReader wmsreader1 = getMapReader(wms_full);                
        assertTrue(wmsreader1.filters.size()>0);
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        wmsreader1.addCommonQueryParams(result, transformer, "EPSG:4326", false);        
        assertFalse(result.containsKey("CQL_FILTER"));
    }
    
    public void testMergeDifferentFilters() throws JSONException, UnsupportedEncodingException, URISyntaxException{

    	PJsonObject wms_full_1 = new PJsonObject(wmsSpec1.getInternalObj(), "");    	
    	WMSMapReader wmsreader1 = getMapReader(wms_full_1);                
        
        PJsonObject wms_full_2 = new PJsonObject(wmsSpec3.getInternalObj(), "");    	
        WMSMapReader wmsreader2 = getMapReader(wms_full_2);
        
        assertTrue(wmsreader1.canMerge(wmsreader2));
        wmsreader1.testMerge(wmsreader2);
        assertEquals(2, wmsreader1.layers.size());        
        wmsreader1.render(transformer, null, "EPSG:4326", false);
        List<URI> tiles = TestTileRenderer.lastURIs;
        assertTrue(tiles.size() > 0);
        String queryString = tiles.get(0).getRawQuery();
        String cqlFilter = getQueryParam(queryString, "CQL_FILTER");
        assertEquals(2, cqlFilter.split(";").length);          
    }
    
    public void testMergeDifferentFiltersWithEmptyOne() throws JSONException, UnsupportedEncodingException, URISyntaxException{

    	PJsonObject wms_full_1 = new PJsonObject(wmsSpec1.getInternalObj(), "");    	
    	WMSMapReader wmsreader1 = getMapReader(wms_full_1);                
        
        PJsonObject wms_full_2 = new PJsonObject(wmsSpec4.getInternalObj(), "");    	
        WMSMapReader wmsreader2 = getMapReader(wms_full_2);
        
        assertTrue(wmsreader1.canMerge(wmsreader2));
        wmsreader1.testMerge(wmsreader2);
        assertEquals(2, wmsreader1.layers.size());        
        wmsreader1.render(transformer, null, "EPSG:4326", false);
        List<URI> tiles = TestTileRenderer.lastURIs;
        assertTrue(tiles.size() > 0);
        String queryString = tiles.get(0).getRawQuery();
        String cqlFilter = getQueryParam(queryString, "CQL_FILTER");
        assertEquals(2, cqlFilter.split(";").length); 
        assertTrue(cqlFilter.indexOf("INCLUDE")>=0);
    }
    
    public void testFilter() throws JSONException, UnsupportedEncodingException, URISyntaxException{

    	PJsonObject wms_full = new PJsonObject(wmsSpec1.getInternalObj(), "");
    	
    	WMSMapReader wmsreader1 = getMapReader(wms_full);                
        assertTrue(wmsreader1.filters.size()>0);
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        wmsreader1.addCommonQueryParams(result, transformer, "EPSG:4326", false);        
        assertTrue(result.containsKey("CQL_FILTER"));
    }
    
    public void testMergeEqualFilters() throws JSONException, URISyntaxException, IOException{

    	PJsonObject wms_full_1 = new PJsonObject(wmsSpec1.getInternalObj(), "");    	
    	WMSMapReader wmsreader1 = getMapReader(wms_full_1);                
        
        PJsonObject wms_full_2 = new PJsonObject(wmsSpec2.getInternalObj(), "");    	
        WMSMapReader wmsreader2 = getMapReader(wms_full_2);
        
        assertTrue(wmsreader1.canMerge(wmsreader2));
        wmsreader1.testMerge(wmsreader2);
        assertEquals(2, wmsreader1.layers.size());        
        wmsreader1.render(transformer, null, "EPSG:4326", false);
        List<URI> tiles = TestTileRenderer.lastURIs;
        assertTrue(tiles.size() > 0);
        String queryString = tiles.get(0).getRawQuery();
        String cqlFilter = getQueryParam(queryString, "CQL_FILTER");
        assertEquals(2, cqlFilter.split(";").length);        
    }

	private String getQueryParam(String queryString, String key) {
		for(String keyValuePair : queryString.split("&")) {
			String[] keyValue = keyValuePair.split("=");
			if(keyValue[0].equalsIgnoreCase(key)) {
				return URLDecoder.decode(keyValue[1]);
			}
		}
		return null;
	}

	private WMSMapReader getMapReader(PJsonObject params) {
		List<? extends MapReader> readers = factory.create("WMS", context, params);
		if(readers != null && readers.size() > 0) {
			return (WMSMapReader)readers.get(0);
		}
		return null;
	}
	
}
