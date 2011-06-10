package se.panamach.util.image;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.panamach.services.gps.type.Location;
import se.panamach.services.gps.type.TimePositionVelocity;
import se.panamach.util.common.ExecutionTimer;
import se.panamach.util.configuration.PanamaConfiguration;

public class TileMerger {

	private static Logger log = LoggerFactory.getLogger(TileMerger.class);
	
	private static double TILE_DIMENSION = 256;
	private static int TILE_DIMENSION_INT = (int)TILE_DIMENSION;
	
	private Location location;
	private List<Location> path;
	private int imageH, imageW, zoom;

	private int topLeftTileX, topLeftTileY, locationTileX, locationTileY;
	private int noofColumns, noofRows;
	
	
	public TileMerger(Location location, int imageW, int imageH, int zoom) {
		this.location = location;
		this.imageH = imageH;
		this.imageW = imageW;
		this.zoom = zoom;
		
		populateStartValues();
	}

	public void setPath(List<Location> path) {
		this.path = path;
	}

	private void populateStartValues() {
		locationTileX = TileFetcher.getTileX(location.latitude, location.longitude, zoom);
		locationTileY = TileFetcher.getTileY(location.latitude, location.longitude, zoom);
		
		noofRows = (int)(imageH / TILE_DIMENSION + 1)+2;
		noofColumns = (int)(imageW / TILE_DIMENSION + 1)+2;
		
		topLeftTileX = locationTileX - ((noofColumns-1) / 2);
		topLeftTileY = locationTileY - ((noofRows-1) / 2);
	}
	
	public byte[] generateMap() throws IOException {
		BufferedImage bi = new BufferedImage(imageW + TILE_DIMENSION_INT * 2, imageH + TILE_DIMENSION_INT * 2, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bi.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		ExecutionTimer et = new ExecutionTimer("generateMap");
		et.start();
		String timerKey = et.registerStartOfTimedAction("Fetching and adding tiles");
		int currY = topLeftTileY;
		for (int row = 0; row < noofRows; row++) {
			int currX = topLeftTileX;
			for (int col = 0; col < noofColumns; col++) {
				
				BufferedImage tile = TileFetcher.getTile(currX, currY, zoom);
			    g2d.drawImage(
			    		tile, 
			    		(int)(col * TILE_DIMENSION), 
			    		(int)(row * TILE_DIMENSION), null);
			    
			    // Draws a box around each tile
			    if (PanamaConfiguration.get().getBoolean("map.gridShow")) {
				    Color orginialColor = g2d.getColor();
				    Color gridColor = Color.decode(PanamaConfiguration.get().getString("map.gridColor"));
				    g2d.setColor(gridColor);
				    g2d.drawRect((int)(col * TILE_DIMENSION), (int)(row * TILE_DIMENSION), (int)TILE_DIMENSION, (int)TILE_DIMENSION);
				    g2d.setColor(orginialColor);
			    }
			    currX += 1;
			}
			currY += 1;			
		}
		log.debug("Fetching and adding tiles took: " + et.registerEndOfTimedAction(timerKey) + "ms");

		timerKey = et.registerStartOfTimedAction("Marking path");
		if (CollectionUtils.isNotEmpty(path))
			markPath(g2d);
		log.debug("Marking path took: " + et.registerEndOfTimedAction(timerKey) + "ms");

		timerKey = et.registerStartOfTimedAction("Marking current position");
		markCurrentPosition(g2d);
		log.debug("Marking current position took: " + et.registerEndOfTimedAction(timerKey) + "ms");
		
		timerKey = et.registerStartOfTimedAction("Cropping image");
		bi = cropToCenterOnPosition(bi);
		log.debug("Cropping image took: " + et.registerEndOfTimedAction(timerKey) + "ms");
		
		if (PanamaConfiguration.get().getBoolean("map.showCompass")) {
			BufferedImage compass = getCompass();
			if (compass != null) {
				timerKey = et.registerStartOfTimedAction("Drawing compass");
				bi.getGraphics().drawImage(compass, 0, 0, null);
				log.debug("Drawing compass took: " + et.registerEndOfTimedAction(timerKey) + "ms");
			}
		}
		
		timerKey = et.registerStartOfTimedAction("Writing to output stream");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ImageIO.write(bi, "png", baos);
	    log.debug("Writing to outputstream took: " + et.registerEndOfTimedAction(timerKey) + "ms");
	    et.end();
	    log.debug("\n" + et.toString());

	    return baos.toByteArray();
	}
	
	private BufferedImage getCompass() {
		InputStream is = TileMerger.class.getResourceAsStream("/images/compass.png");
		try {
			return ImageIO.read(is);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { is.close(); } catch (Exception ee) { ee.printStackTrace(); }
		}
		return null;
	}
	
	private BufferedImage cropToCenterOnPosition(BufferedImage largeMap) {
		Point2D position = transformFromLocationToPixelPoint(location);

		int cropX = (int)(position.getX() - imageW / 2);
		int cropY = (int)(position.getY() - imageH / 2);
		
		return largeMap.getSubimage(
				cropX, 
				cropY, 
				imageW, imageH);
	}
	
	private void markCurrentPosition(Graphics2D g2d) {
		Color positionColor = createColor(
				PanamaConfiguration.get().getString("map.currentPositionColor"),
				PanamaConfiguration.get().getInt("map.currentPositionColorAlpha"));
		
		g2d.setColor(positionColor);
	
		Point2D locationPosition = transformFromLocationToPixelPoint(location);
		
		g2d.fill(new Ellipse2D.Double(locationPosition.getX()-7, locationPosition.getY()-7, 15, 15));
		
		if (location instanceof TimePositionVelocity) {
			TimePositionVelocity tpv = (TimePositionVelocity) location;
			g2d.translate(locationPosition.getX(), locationPosition.getY());
			g2d.rotate(Math.toRadians(tpv.track));
			
			BasicStroke pathStroke = new BasicStroke(
					PanamaConfiguration.get().getInt("map.directionArrowWidth"), 
					BasicStroke.CAP_SQUARE, 
					BasicStroke.JOIN_MITER);
			
			Color c = createColor(
					PanamaConfiguration.get().getString("map.directionArrowColor"), 
					PanamaConfiguration.get().getInt("map.directionArrowAlpha"));
			
			g2d.setColor(c);
			g2d.setStroke(pathStroke);
			g2d.drawLine(0, 0, 0, -20);
			g2d.drawLine(0, -20, -4, -16);
			g2d.drawLine(0, -20, 4, -16);
			
			g2d.rotate(Math.toRadians(tpv.track * -1));
			g2d.translate(locationPosition.getX() * -1, locationPosition.getY() * -1);
		}
	}
	
	private void markPath(Graphics2D g2d) {
		Path2D path2d = new Path2D.Double();
		BasicStroke pathStroke = new BasicStroke(
				PanamaConfiguration.get().getInt("map.pathWidth"), 
				BasicStroke.CAP_ROUND, 
				BasicStroke.JOIN_ROUND);
		
		// Start from current position
		Point2D pStart = transformFromLocationToPixelPoint(location);
		path2d.moveTo(pStart.getX(), pStart.getY());
		
		for (Location loc : path) {
			Point2D p = transformFromLocationToPixelPoint(loc);
			path2d.lineTo(p.getX(), p.getY());
		}

		Shape strokeShape = pathStroke.createStrokedShape(path2d);
		
		Color pathColor = createColor(
				PanamaConfiguration.get().getString("map.pathColor"),
				PanamaConfiguration.get().getInt("map.pathColorAlpha"));
		
		g2d.setColor(pathColor);
		g2d.fill(strokeShape);
	}
	
	private Color createColor(String hexValue, int alpha) {
		Color c = Color.decode(hexValue);
		
		return new Color(
				c.getRed(),
				c.getGreen(),
				c.getBlue(),
				alpha);
	}
	
	private Point2D transformFromLocationToPixelPoint(Location loc) {
		int tileX = TileFetcher.getTileX(loc.latitude, loc.longitude, zoom);
		int tileY = TileFetcher.getTileY(loc.latitude, loc.longitude, zoom);
		
		double fx = TileFetcher.getTileXFraction(loc.latitude, loc.longitude, zoom);
		double fy = TileFetcher.getTileYFraction(loc.latitude, loc.longitude, zoom);
		
		// Calculate the offset within the current tile
		double dxp = TILE_DIMENSION * fx;
		double dyp = TILE_DIMENSION * fy;
		
		return new Point2D.Double(
				TILE_DIMENSION * (tileX - topLeftTileX) + dxp, 
				TILE_DIMENSION * (tileY - topLeftTileY) + dyp);
	}
}
