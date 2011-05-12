package se.panamach.util.image;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

import se.panamach.util.map.LonLatBoundingBox;


public class TileFetcher {

	private static String TMP_IMAGE_DIR = "tmp/tiles";
	
	public static BufferedImage getTile(final int x, final int y, final int zoom) throws IOException {
		String path = TMP_IMAGE_DIR + "/" + zoom + "/" + x;
		File f = new File(path + "/" + y + ".png");
		if (f.exists()) {
			return ImageIO.read(f);
		}
		
		return saveAndReturnTile(x, y, zoom);
	}
	
	private static BufferedImage saveAndReturnTile(final int x, final int y, final int zoom) throws IOException {
		String path = TMP_IMAGE_DIR + "/" + zoom + "/" + x;
		File dir = new File(path);
		dir.mkdirs();
		        
        URL url = new URL(getTileUrl(x, y, zoom));
		InputStream is = url.openStream();
		OutputStream os = new FileOutputStream(path + "/" + y + ".png");

		byte[] b = new byte[2048];
		int length;

		while ((length = is.read(b)) != -1) {
			os.write(b, 0, length);
		}

		is.close();
		os.close();
        
	    return getTile(x, y, zoom);
	}
	
	public static String getTileUrl(final int x, final int y, final int zoom) {
		return ("http://tile.openstreetmap.org/" + zoom + "/" + x + "/" + y + ".png");
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
		
		return (int) Math
			.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1
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
	
	public static LonLatBoundingBox tile2boundingBox(final int x, final int y, final int zoom) {
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
