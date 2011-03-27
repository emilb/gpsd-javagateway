package se.panamach.gwt;

import org.springframework.beans.factory.annotation.Autowired;

import se.panamach.gwt.ui.GpsStatusPanel;
import se.panamach.gwt.ui.GpsTravelHistoryPanel;
import se.panamach.services.gps.PositionHistoryService;

import com.vaadin.Application;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Window;


public class GpsApplication extends Application {

	private static final long serialVersionUID = -1861491153182727518L;

	@Autowired
	PositionHistoryService historyService;

	@Override
    public void init() {
        Window mainWindow = new Window("gps");
        setMainWindow(mainWindow);

        
        TabSheet tabsheet = new TabSheet();
        tabsheet.setSizeFull();
        
        tabsheet.addTab(new GpsStatusPanel(historyService), "Current", null);
        tabsheet.addTab(new GpsTravelHistoryPanel(historyService), "Travel", null);
        
        
        mainWindow.addComponent(tabsheet);
    }
}
