package org.mapfish.print.map.readers;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mapfish.print.MapTestBasic;
import org.mapfish.print.utils.PJsonObject;
import org.pvalsecc.misc.MatchAllSet;
import org.pvalsecc.misc.URIUtils;

public class XYZLayerTest extends MapTestBasic {

    private XyzMapReader xyzreader;

    PJsonObject xyzSpec;


    @Before
    public void setUp() throws Exception {
        super.setUp();

        xyzSpec = loadJson("layers/xyz_layer_spec.json");
    }

    @Test
    public void testUriWithoutFormat() throws IOException, JSONException, URISyntaxException {
        String test_format = null;
        String expected_url = xyzSpec.getString("baseURL") + "/7/64/63.gif";

        JSONObject xyz_full = xyzSpec.getInternalObj();
        xyz_full.put("path_format", test_format);
        xyzSpec = new PJsonObject(xyz_full, "");

        xyzreader = new XyzMapReader("foo", context, xyzSpec);

        URI outputuri = xyzreader.getTileUri(URIUtils.addParams(xyzreader.baseUrl, new HashMap<String, List<String>>(), new MatchAllSet<String>()), null, -180, -90, 180, 90, 256, 256);

        assertEquals("Default format (null path_format) did not get created correctly", expected_url, outputuri.toURL().toString());
    }

    @Test
    public void testUriWithBasicFormat() throws IOException, JSONException, URISyntaxException {
        String test_format = "${z}_${x}_${y}_static.${extension}";
        String expected_url = xyzSpec.getString("baseURL") + "/7_64_63_static.gif";


        JSONObject xyz_full = xyzSpec.getInternalObj();
        xyz_full.put("path_format", test_format);
        xyzSpec = new PJsonObject(xyz_full, "");

        xyzreader = new XyzMapReader("foo", context, xyzSpec);

        URI outputuri = xyzreader.getTileUri(URIUtils.addParams(xyzreader.baseUrl, new HashMap<String, List<String>>(), new MatchAllSet<String>()), null, -180, -90, 180, 90, 256, 256);

        assertEquals("Custom format without any string formatter did not get created correctly", expected_url, outputuri.toURL().toString());
    }

    @Test
    public void testUriWithDigitFormat() throws IOException, JSONException, URISyntaxException {
        String test_format = "${zzz}_${x}_${yy}_static.${extension}";
        String expected_url = xyzSpec.getString("baseURL") + "/007_64_63_static.gif";


        JSONObject xyz_full = xyzSpec.getInternalObj();
        xyz_full.put("path_format", test_format);
        xyzSpec = new PJsonObject(xyz_full, "");

        xyzreader = new XyzMapReader("foo", context, xyzSpec);

        URI outputuri = xyzreader.getTileUri(URIUtils.addParams(xyzreader.baseUrl, new HashMap<String, List<String>>(), new MatchAllSet<String>()), null, -180, -90, 180, 90, 256, 256);

        assertEquals("Custom format relying on the string formatter did not get created correctly", expected_url, outputuri.toURL().toString());

        //Test another format
        test_format = "${zzzz}_${xx}_${yyy}_static.${extension}";
        expected_url = xyzSpec.getString("baseURL") + "/0007_64_063_static.gif";

        xyz_full.put("path_format", test_format);
        xyzSpec = new PJsonObject(xyz_full, "");

        xyzreader = new XyzMapReader("foo", context, xyzSpec);

        outputuri = xyzreader.getTileUri(URIUtils.addParams(xyzreader.baseUrl, new HashMap<String, List<String>>(), new MatchAllSet<String>()), null, -180, -90, 180, 90, 256, 256);

        assertEquals("Custom format relying on the string formatter did not get created correctly", expected_url, outputuri.toURL().toString());

    }
}
