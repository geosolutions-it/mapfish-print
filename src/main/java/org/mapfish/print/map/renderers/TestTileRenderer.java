package org.mapfish.print.map.renderers;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.mapfish.print.RenderingContext;
import org.mapfish.print.Transformer;
import org.mapfish.print.map.ParallelMapTileLoader;

public class TestTileRenderer extends TileRenderer {

	public static List<URI> lastURIs = null;
	
	@Override
	public void render(Transformer transformer, List<URI> urls,
			ParallelMapTileLoader parallelMapTileLoader,
			RenderingContext context, float opacity, int nbTilesHorizontal,
			float offsetX, float offsetY, long bitmapTileW, long bitmapTileH)
			throws IOException {
		lastURIs = urls;

	}

}
