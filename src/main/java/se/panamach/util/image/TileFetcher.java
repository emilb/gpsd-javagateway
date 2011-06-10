package se.panamach.util.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;

import javax.imageio.ImageIO;

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.cache.HTTPCache;
import org.codehaus.httpcache4j.cache.MemoryCacheStorage;
import org.codehaus.httpcache4j.resolver.HTTPClientResponseResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.panamach.util.configuration.PanamaConfiguration;
import se.panamach.util.map.LonLatBoundingBox;

public class TileFetcher {

	private static Logger log = LoggerFactory.getLogger(TileFetcher.class);

	private static HTTPCache cache = new HTTPCache(new MemoryCacheStorage(),
			HTTPClientResponseResolver.createMultithreadedInstance());

	public static BufferedImage getTile(final int x, final int y, final int zoom)
			throws IOException {
		HTTPRequest request = new HTTPRequest(
				URI.create(getTileUrl(x, y, zoom)));
		HTTPResponse response = null;
		try {
			response = cache.doCachedRequest(request);
			return ImageIO.read(response.getPayload().getInputStream());
		} finally {
			if (response != null) {
				response.consume();
			}
		}
	}

	public static String getTileUrl(final int x, final int y, final int zoom) {
		return (PanamaConfiguration.get().getString("tileServer") + zoom + "/"
				+ x + "/" + y + ".png");
	}

	public static String getTileUrl(final double lat, final double lon,
			final int zoom) {
		int xtile = getTileX(lat, lon, zoom);
		int ytile = getTileY(lat, lon, zoom);
		return getTileUrl(xtile, ytile, zoom);
	}

	public static int getTileX(final double lat, final double lon,
			final int zoom) {
		return (int) Math.floor((lon + 180) / 360 * (1 << zoom));
	}

	public static double getTileXFraction(final double lat, final double lon,
			final int zoom) {

		double val = (lon + 180) / 360 * (1 << zoom);
		return val - Math.floor(val);
	}

	public static int getTileY(final double lat, final double lon,
			final int zoom) {

		return (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1
				/ Math.cos(Math.toRadians(lat)))
				/ Math.PI)
				/ 2 * (1 << zoom));
	}

	public static double getTileYFraction(final double lat, final double lon,
			final int zoom) {

		double val = (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1
				/ Math.cos(Math.toRadians(lat)))
				/ Math.PI)
				/ 2 * (1 << zoom);
		return val - Math.floor(val);
	}

	public static LonLatBoundingBox tile2boundingBox(final int x, final int y,
			final int zoom) {
		LonLatBoundingBox bb = new LonLatBoundingBox();
		bb.north = tile2lat(y, zoom);
		bb.south = tile2lat(y + 1, zoom);
		bb.west = tile2lon(x, zoom);
		bb.east = tile2lon(x + 1, zoom);
		return bb;
	}

	static double tile2lon(int x, int z) {
		return x / Math.pow(2.0, z) * 360.0 - 180;
	}

	static double tile2lat(int y, int z) {
		double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
		return Math.toDegrees(Math.atan(Math.sinh(n)));
	}

}
