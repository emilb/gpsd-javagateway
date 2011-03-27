package se.panamach.gwt.ui;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.vaadin.hezamu.googlemapwidget.GoogleMap;
import org.vaadin.hezamu.googlemapwidget.GoogleMap.MapControl;
import org.vaadin.hezamu.googlemapwidget.overlay.BasicMarker;
import org.vaadin.hezamu.googlemapwidget.overlay.Marker;
import org.vaadin.hezamu.googlemapwidget.overlay.PolyOverlay;
import org.vaadin.hezamu.googlemapwidget.overlay.Polygon;
import org.vaadin.kim.countdownclock.CountdownClock;

import se.panamach.services.gps.PositionHistoryService;
import se.panamach.services.gps.type.TimePositionVelocity;
import se.panamach.util.map.TpvUtils;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

public class GpsTravelHistoryPanel extends Panel {


	private static final long serialVersionUID = -4755678561511822896L;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final DecimalFormat df_0 = new DecimalFormat("#");
	private static final DecimalFormat df_2 = new DecimalFormat("#.##");
	
	PositionHistoryService historyService;
	
	Label longitudeVal, latitudeVal, altitudeVal, speedVal, speedKmhVal, trackVal;
	GoogleMap map;
	CountdownClock countDownClock;
	AtomicLong counter = new AtomicLong();
	PolyOverlay lastOverlay;
	
	public GpsTravelHistoryPanel(PositionHistoryService historyService) {
		this.historyService = historyService;
	}
	
	@Override
	public void attach() {
		super.attach();
		init();
		update();
	}

	public void update() {
		TimePositionVelocity tpv = historyService.getLastKnownPosition();
		if (tpv == null)
			return;
		
		longitudeVal.setValue(tpv.longitude);
		latitudeVal.setValue(tpv.latitude);
		altitudeVal.setValue(df_2.format(tpv.altitude));
		speedVal.setValue(df_2.format(tpv.speed));
		speedKmhVal.setValue(df_0.format(TpvUtils.convertMeterPerSecondToKmPerHour(tpv.speed)));
		trackVal.setValue(df_0.format(tpv.track));
		
		map.setCenter(new Point2D.Double(tpv.longitude, tpv.latitude));
		map.removeAllMarkers();
		
		map.addMarker(new BasicMarker(counter.getAndIncrement(),
				new Point2D.Double(tpv.longitude, tpv.latitude),
				"Current position at: " + sdf.format(new Date(tpv.time))));

		if (lastOverlay != null)
			map.removeOverlay(lastOverlay);
		
		List<TimePositionVelocity> positions = historyService.getTravelHistory(15);
		
		if (CollectionUtils.isEmpty(positions)) {
			this.requestRepaintRequests();
			return;
		}
		
		List<Point2D.Double> points = new ArrayList<Point2D.Double>();
		
		for (TimePositionVelocity tpvH : positions) {
			points.add(new Point2D.Double(tpvH.longitude, tpvH.latitude));
		}
		 // WTF: stupid java that can't cast list.toArray() in any usable way 
		Point2D.Double[] poss = new Point2D.Double[positions.size()];
		points.toArray(poss);
		
		lastOverlay = new Polygon(counter.getAndIncrement(), poss, "#f04040", 3, 0.5, "", 0, false);
		map.addPolyOverlay(lastOverlay);
		
		this.requestRepaintRequests();
	}
	
	private Marker getMarker(TimePositionVelocity tpv) {
		Date tpvTime = new Date((long)tpv.time);
		
		return new BasicMarker(counter.getAndIncrement(),
				new Point2D.Double(tpv.longitude, tpv.latitude),
				sdf.format(tpvTime));
	}
	
	private void init() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSizeFull();
		hl.setSpacing(true);
		
		hl.addComponent(getInfoPane());
		hl.addComponent(getMapPane());
		
		this.addComponent(hl);
	}
	
	private Panel getInfoPane() {
		longitudeVal = new Label();
		latitudeVal = new Label();
		altitudeVal = new Label();
		speedVal = new Label();
		speedKmhVal = new Label();
		trackVal = new Label();
		
		countDownClock = new CountdownClock();
		countDownClock.setDate(new Date(System.currentTimeMillis() + 5000));
		countDownClock.setFormat("Update in %s seconds");
		
		countDownClock.addListener(new CountdownClock.EndEventListener() {
			
			@Override
			public void countDownEnded(CountdownClock clock) {
				update();
				countDownClock.setDate(new Date(System.currentTimeMillis() + 5000));
			}
		});
		
		Panel p = new Panel();

		Label rLongitude = new Label("<b>Longitude:</b>");
		rLongitude.setContentMode(Label.CONTENT_XHTML);
		p.addComponent(rLongitude);
		p.addComponent(longitudeVal);
		
		Label rLatitude = new Label("<b>Latitude:</b>");
		rLatitude.setContentMode(Label.CONTENT_XHTML);
		p.addComponent(rLatitude);
		p.addComponent(latitudeVal);
		
		Label rAltitude = new Label("<b>Altitude (m):</b>");
		rAltitude.setContentMode(Label.CONTENT_XHTML);
		p.addComponent(rAltitude);
		p.addComponent(altitudeVal);
		
		Label rSpeed = new Label("<b>Speed (m/s):</b>");
		rSpeed.setContentMode(Label.CONTENT_XHTML);
		p.addComponent(rSpeed);
		p.addComponent(speedVal);
		
		Label rSpeedKmh = new Label("<b>Speed (km/h):</b>");
		rSpeedKmh.setContentMode(Label.CONTENT_XHTML);
		p.addComponent(rSpeedKmh);
		p.addComponent(speedKmhVal);
		
		Label rTrack = new Label("<b>Track (degrees from north):</b>");
		rTrack.setContentMode(Label.CONTENT_XHTML);
		p.addComponent(rTrack);
		p.addComponent(trackVal);
		
		p.addComponent(createSpacer(20));
		p.addComponent(new Button("Refresh", new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				update();
			}
		}));
		
		p.addComponent(createSpacer(20));
		p.addComponent(countDownClock);
		return p;
	}
	
	private Component createSpacer(int height) {
		Label l = new Label();
		l.setHeight(height + "px");
		return l;
	}
	
	private Component getMapPane() {
		map = new GoogleMap(this.getApplication(), "ABQIAAAAeVLd9P1jGkgL7vrCKwJ_0hR_x1-SbX4pPWHEjQyACcE9JRhHnBRtvyZGgt1-drdwG8bt-SLkBjtxzw");
		map.addControl(MapControl.MapTypeControl);
//		map.setSizeFull();
		map.setWidth("640px");
		map.setHeight("480px");
		
		return map;
	}
}
