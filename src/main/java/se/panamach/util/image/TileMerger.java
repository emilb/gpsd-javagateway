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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.collections.CollectionUtils;

import se.panamach.services.gps.type.Location;
import se.panamach.services.gps.type.TimePositionVelocity;


// 59.63910, 18.23618
// 59.63925, 18.23539 Mälsta hage
// 59.34040, 18.07627 Sturegatan/Karlavägen
public class TileMerger {

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
		
		long tnow = System.currentTimeMillis();
		int currY = topLeftTileY;
		for (int row = 0; row < noofRows; row++) {
			int currX = topLeftTileX;
			for (int col = 0; col < noofColumns; col++) {
				
				BufferedImage tile = TileFetcher.getTile(currX, currY, zoom);
			    g2d.drawImage(
			    		tile, 
			    		(int)(col * TILE_DIMENSION), 
			    		(int)(row * TILE_DIMENSION), null);
			    
//				Draws a box around each tile
//
//			    Color orginialColor = g2d.getColor();
//			    g2d.setColor(Color.darkGray);
//			    g2d.drawRect((int)(col * TILE_DIMENSION), (int)(row * TILE_DIMENSION), (int)TILE_DIMENSION, (int)TILE_DIMENSION);
//			    g2d.setColor(orginialColor);
			    
			    currX += 1;
			}
			currY += 1;			
		}
		System.out.println("Fetching and adding tiles took: " + (System.currentTimeMillis() - tnow) + "ms");
		tnow = System.currentTimeMillis();
		
//		BufferedImage compass = getCompass();
//		if (compass != null) {
//			g2d.drawImage(compass, 0, 0, null);
//		}
		
		markCurrentPosition(g2d);
		System.out.println("Marking current position took: " + (System.currentTimeMillis() - tnow) + "ms");
		tnow = System.currentTimeMillis();
		
		if (CollectionUtils.isNotEmpty(path))
			markPath(g2d);
		System.out.println("Marking path took: " + (System.currentTimeMillis() - tnow) + "ms");
		tnow = System.currentTimeMillis();
		
//		cropToCenterOnPosition(bi);
		 bi = cropToCenterOnPosition(bi);
		System.out.println("Cropping image took: " + (System.currentTimeMillis() - tnow) + "ms");
		tnow = System.currentTimeMillis();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ImageIO.write(bi, "png", baos);
	    
	    System.out.println("Writing to outputstream took: " + (System.currentTimeMillis() - tnow) + "ms");
		tnow = System.currentTimeMillis();
		
		return baos.toByteArray();
	}
	
	private BufferedImage getCompass() {
		InputStream is = TileMerger.class.getResourceAsStream("/images/compass.png");
		try {
			return ImageIO.read(is);
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
		
//		Graphics2D g2d = largeMap.createGraphics();
//		Color orginialColor = g2d.getColor();
//	    g2d.setColor(Color.ORANGE);
//	    g2d.drawRect(cropX, 
//				cropY, 
//				imageW, imageH);
//	    g2d.setColor(orginialColor);
		return largeMap.getSubimage(
				cropX, 
				cropY, 
				imageW, imageH);
	}
	
	private void markCurrentPosition(Graphics2D g2d) {
		g2d.setColor(Color.magenta);
		Point2D locationPosition = transformFromLocationToPixelPoint(location);
		g2d.fill(new Ellipse2D.Double(locationPosition.getX()-7, locationPosition.getY()-7, 15, 15));
		
		if (location instanceof TimePositionVelocity) {
			TimePositionVelocity tpv = (TimePositionVelocity) location;
			g2d.translate(locationPosition.getX(), locationPosition.getY());
			g2d.rotate(Math.toRadians(tpv.track));
			
			BasicStroke pathStroke = new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
			Color c = new Color(0, 0, 0, 0.5f);
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
		BasicStroke pathStroke = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		
		// Start from current position
		Point2D pStart = transformFromLocationToPixelPoint(location);
		path2d.moveTo(pStart.getX(), pStart.getY());
		
		for (Location loc : path) {
			Point2D p = transformFromLocationToPixelPoint(loc);
			path2d.lineTo(p.getX(), p.getY());
		}

		Shape strokeShape = pathStroke.createStrokedShape(path2d);
		float alpha = 0.5f;
		Color c = new Color(0, 0, 1, alpha);
		g2d.setColor(c);
		g2d.fill(strokeShape);
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
	
	public static void main(String[] args) {
		try {
			TileMerger tm = new TileMerger(new Location(60.09403840817271,19.926366806030273), 1200, 1200, 16);
			
			
			
			List<Location> path = new ArrayList<Location>();
			path.add(new Location(59.31970328324409,18.08177947998047));
			path.add(new Location(59.31760106457572,18.100318908691406));
			path.add(new Location(59.31795144338167,18.118515014648438));
			path.add(new Location(59.31970328324409,18.13465118408203));
			path.add(new Location(59.319177740764545,18.149070739746094));
			path.add(new Location(59.3237321723325,18.16143035888672));
			path.add(new Location(59.327760583893905,18.168296813964844));
			path.add(new Location(59.330737798587116,18.17516326904297));
			path.add(new Location(59.33572842625428,18.189411163330078));
			path.add(new Location(59.337304261639986,18.20486068725586));
			path.add(new Location(59.34203132920842,18.21859359741211));
			path.add(new Location(59.34973328908452,18.233699798583984));
			path.add(new Location(59.35725851675815,18.250694274902344));
			path.add(new Location(59.36460712885123,18.276100158691406));
			path.add(new Location(59.37011754373908,18.29996109008789));
			path.add(new Location(59.368893084438405,18.333778381347656));
			path.add(new Location(59.366618971344025,18.368968963623047));
			path.add(new Location(59.36338247066926,18.397808074951172));
			path.add(new Location(59.3618953263137,18.41205596923828));
			path.add(new Location(59.356296080673246,18.430938720703125));
			path.add(new Location(59.35437112663404,18.438491821289062));
			path.add(new Location(59.362070287855786,18.445358276367188));
			path.add(new Location(59.367231247556525,18.447589874267578));
			path.add(new Location(59.380348797213124,18.441410064697266));
			path.add(new Location(59.38681825326465,18.437461853027344));
			path.add(new Location(59.394422656785224,18.444156646728516));
			path.add(new Location(59.406219216211284,18.424758911132812));
			path.add(new Location(59.416701599410416,18.405017852783203));
			path.add(new Location(59.42045699721976,18.39334487915039));
			path.add(new Location(59.435561714896785,18.37566375732422));
			path.add(new Location(59.43975142472438,18.379268646240234));
			path.add(new Location(59.442544276385085,18.398666381835938));
			path.add(new Location(59.4504851882224,18.4295654296875));
			path.add(new Location(59.467146314256134,18.437461853027344));
			path.add(new Location(59.47865601186644,18.443470001220703));
			path.add(new Location(59.48562967808269,18.448963165283203));
			path.add(new Location(59.497045947346486,18.46578598022461));
			path.add(new Location(59.504799836616236,18.476943969726562));
			path.add(new Location(59.51403248163582,18.487930297851562));
			path.add(new Location(59.517515842036566,18.501663208007812));
			path.add(new Location(59.52352379324845,18.520889282226562));
			path.add(new Location(59.5307493309692,18.54166030883789));
			path.add(new Location(59.54067103951734,18.570327758789062));
			path.add(new Location(59.54867590277158,18.592472076416016));
			path.add(new Location(59.55876628048537,18.620452880859375));
			path.add(new Location(59.567723306213026,18.652725219726562));
			path.add(new Location(59.57407004653665,18.674697875976562));
			path.add(new Location(59.579459488820675,18.677616119384766));
			path.add(new Location(59.58102400392204,18.68328094482422));
			path.add(new Location(59.58310991088034,18.7042236328125));
			path.add(new Location(59.58736823598668,18.726539611816406));
			path.add(new Location(59.59275554821197,18.748855590820312));
			path.add(new Location(59.60057430455003,18.770313262939453));
			path.add(new Location(59.609520205675445,18.79537582397461));
			path.add(new Location(59.61776916872406,18.820953369140625));
			path.add(new Location(59.62844639664704,18.847217559814453));
			path.add(new Location(59.6392069956019,18.872623443603516));
			path.add(new Location(59.64987740825445,18.89820098876953));
			path.add(new Location(59.66002416440409,18.92446517944336));
			path.add(new Location(59.67172814615966,18.953475952148438));
			path.add(new Location(59.68221490983823,18.980426788330078));
			path.add(new Location(59.68594082071901,18.990554809570312));
			path.add(new Location(59.69235188329587,19.013214111328125));
			path.add(new Location(59.699887644364004,19.037933349609375));
			path.add(new Location(59.70776806274006,19.064369201660156));
			path.add(new Location(59.71218374982292,19.078445434570312));
			path.add(new Location(59.719888161315126,19.114151000976562));
			path.add(new Location(59.724388794209865,19.133548736572266));
			path.add(new Location(59.729581079770036,19.163761138916016));
			path.add(new Location(59.73468604121765,19.19534683227539));
			path.add(new Location(59.73979022332947,19.226932525634766));
			path.add(new Location(59.74498011782209,19.259891510009766));
			path.add(new Location(59.750169206500594,19.292850494384766));
			path.add(new Location(59.75570334633109,19.319629669189453));
			path.add(new Location(59.76218749994764,19.34640884399414));
			path.add(new Location(59.769102543156585,19.373016357421875));
			path.add(new Location(59.774028637875425,19.392757415771484));
			path.add(new Location(59.7885434260604,19.42485809326172));
			path.add(new Location(59.79510756602249,19.43927764892578));
			path.add(new Location(59.8001161722783,19.459705352783203));
			path.add(new Location(59.807886658399255,19.498329162597656));
			path.add(new Location(59.81565533369437,19.536781311035156));
			path.add(new Location(59.82488906962289,19.582443237304688));
			path.add(new Location(59.834551547747516,19.59909439086914));
			path.add(new Location(59.85041953447475,19.609909057617188));
			path.add(new Location(59.85869548182996,19.615230560302734));
			path.add(new Location(59.870847048044425,19.645614624023438));
			path.add(new Location(59.88376937310767,19.678573608398438));
			path.add(new Location(59.90288519732573,19.700889587402344));
			path.add(new Location(59.92061346766061,19.72148895263672));
			path.add(new Location(59.941169842244726,19.74492073059082));
			path.add(new Location(59.96832943091925,19.777536392211914));
			path.add(new Location(59.999243619559756,19.81461524963379));
			path.add(new Location(60.02704171133857,19.84551429748535));
			path.add(new Location(60.055844682567525,19.877443313598633));
			path.add(new Location(60.06355549916499,19.88945960998535));
			path.add(new Location(60.06903765044199,19.917612075805664));
			path.add(new Location(60.08308150550736,19.925851821899414));
			path.add(new Location(60.09403840817271,19.926366806030273));

			
			/* Stockholm
			path.add(new Location(59.33628, 18.07746));
			path.add(new Location(59.33704, 18.07369));
			path.add(new Location(59.33868, 18.07491));
			path.add(new Location(59.33903, 18.07326));
			path.add(new Location(59.34081, 18.07418));
			path.add(new Location(59.34040, 18.07627));
			*/
			
			for (Location l : path) {
				System.out.println("path.add(new Location(" + l.longitude + "," + l.latitude + "));");
			}
			
			tm.setPath(path);
			
			tm.generateMap();
			//getImage(1200, 1200, 18, 59.34040, 18.07627);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
