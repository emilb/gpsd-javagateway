package se.panamach.simulation;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import se.panamach.services.gps.type.Location;
import se.panamach.services.gps.type.TimePositionVelocity;
import se.panamach.util.map.MapUtils;

public class CreateSimulationLog {

	private List<DoubleTuple> tuples = new ArrayList<CreateSimulationLog.DoubleTuple>();
	private double speedInMetersPerSecond = 15d;

	public CreateSimulationLog() {
		initTuples();
	}
	
	public List<String> createListOfTPVs() {
		
		List<String> tpvs = new ArrayList<String>();
		List<TimePositionVelocity> result = new ArrayList<TimePositionVelocity>();
		
		int startTime = 1301123546;
		int timeIncrement = 1;
		
		TimePositionVelocity start = new TimePositionVelocity();
		start.speed = 0;
		start.latitude = tuples.get(0).d2;
		start.longitude = tuples.get(0).d1;
		
		for (int i = 1; i < tuples.size(); i++) {
			TimePositionVelocity end = new TimePositionVelocity(tuples.get(i).d2, tuples.get(i).d1);
			result.addAll(getAllReadingsBetween(start, end));
			start = result.get(result.size() - 1);
		}
		
		String tpvRow = "{\"class\":\"TPV\",\"tag\":\"MID2\",\"device\":\"/dev/ttyUSB0\",\"time\":%d.000,\"ept\":0.005,\"lat\":%f,\"lon\":%f,\"alt\":0.130,\"epx\":9.299,\"epy\":14.612,\"epv\":38.451,\"track\":%f,\"speed\":15.306,\"climb\":0.008,\"mode\":3}";
		
		// Add 16 minute pause before departure
		TimePositionVelocity tpvFirst = result.get(0);
		for (int i = 0; i < 16*60; i++) {
			tpvs.add(String.format(tpvRow, startTime, tpvFirst.latitude, tpvFirst.longitude, tpvFirst.track));
			startTime += timeIncrement;
		}
		
		for (TimePositionVelocity tpv : result) {
			
			tpvs.add(String.format(tpvRow, startTime, tpv.latitude, tpv.longitude, tpv.track));
			startTime += timeIncrement;
		}
		
		// Add 16 minute pause after arrival
		TimePositionVelocity tpvLast = result.get(result.size() - 1);
		for (int i = 0; i < 16*60; i++) {
			tpvs.add(String.format(tpvRow, startTime, tpvLast.latitude, tpvLast.longitude, tpvLast.track));
			startTime += timeIncrement;
		}
		
		return tpvs;
	}
	
	private List<TimePositionVelocity> getAllReadingsBetween(TimePositionVelocity l1, TimePositionVelocity l2) {
		List<TimePositionVelocity> result = new ArrayList<TimePositionVelocity>();
		double bearing = MapUtils.getStartBearing(l1, l2);
		
		TimePositionVelocity currentPos = l1;
		
		boolean notPassedEnd = true;
		while (notPassedEnd) {
			
			Location newPos = MapUtils.getLocationAfterTravel(currentPos, bearing, speedInMetersPerSecond);
			double newBearing = MapUtils.getStartBearing(newPos, l2);
			if (bearing - newBearing > 10) {
				notPassedEnd = false;
				result.add(l2);
			}
			else {
				currentPos = new TimePositionVelocity(newPos);
				currentPos.speed = speedInMetersPerSecond;
				currentPos.track = newBearing;
				result.add(currentPos);
				bearing = newBearing;
			}
		}
		
		return result;
	}
	
	public static void main(String[] args) {
		CreateSimulationLog sim = new CreateSimulationLog();
//		List<TimePositionVelocity> positions = sim.createListOfTPVs();
//		for (TimePositionVelocity tpv : positions) {
//			System.out.println(tpv);
//		}
		
		List<String> positions = sim.createListOfTPVs();
		System.out.println(positions.size());
		try {
			FileWriter outFile = new FileWriter(new File("birkacruises_gps_simulation.log"));
			PrintWriter out = new PrintWriter(outFile);
			for (String pos : positions) {
				out.println(pos);
			}
			outFile.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	private void initTuples() {
		// lon / lat
		tuples.add(new DoubleTuple(18.08177947998047,59.31970328324409));
		tuples.add(new DoubleTuple(18.100318908691406,59.31760106457572));
		tuples.add(new DoubleTuple(18.118515014648438,59.31795144338167));
		tuples.add(new DoubleTuple(18.13465118408203,59.31970328324409));
		tuples.add(new DoubleTuple(18.149070739746094,59.319177740764545));
		tuples.add(new DoubleTuple(18.16143035888672,59.3237321723325));
		tuples.add(new DoubleTuple(18.168296813964844,59.327760583893905));
		tuples.add(new DoubleTuple(18.17516326904297,59.330737798587116));
		tuples.add(new DoubleTuple(18.189411163330078,59.33572842625428));
		tuples.add(new DoubleTuple(18.20486068725586,59.337304261639986));
		tuples.add(new DoubleTuple(18.21859359741211,59.34203132920842));
		tuples.add(new DoubleTuple(18.233699798583984,59.34973328908452));
		tuples.add(new DoubleTuple(18.250694274902344,59.35725851675815));
		tuples.add(new DoubleTuple(18.276100158691406,59.36460712885123));
		tuples.add(new DoubleTuple(18.29996109008789,59.37011754373908));
		tuples.add(new DoubleTuple(18.333778381347656,59.368893084438405));
		tuples.add(new DoubleTuple(18.368968963623047,59.366618971344025));
		tuples.add(new DoubleTuple(18.397808074951172,59.36338247066926));
		tuples.add(new DoubleTuple(18.41205596923828,59.3618953263137));
		tuples.add(new DoubleTuple(18.430938720703125,59.356296080673246));
		tuples.add(new DoubleTuple(18.438491821289062,59.35437112663404));
		tuples.add(new DoubleTuple(18.445358276367188,59.362070287855786));
		tuples.add(new DoubleTuple(18.447589874267578,59.367231247556525));
		tuples.add(new DoubleTuple(18.441410064697266,59.380348797213124));
		tuples.add(new DoubleTuple(18.437461853027344,59.38681825326465));
		tuples.add(new DoubleTuple(18.444156646728516,59.394422656785224));
		tuples.add(new DoubleTuple(18.424758911132812,59.406219216211284));
		tuples.add(new DoubleTuple(18.405017852783203,59.416701599410416));
		tuples.add(new DoubleTuple(18.39334487915039,59.42045699721976));
		tuples.add(new DoubleTuple(18.37566375732422,59.435561714896785));
		tuples.add(new DoubleTuple(18.379268646240234,59.43975142472438));
		tuples.add(new DoubleTuple(18.398666381835938,59.442544276385085));
		tuples.add(new DoubleTuple(18.4295654296875,59.4504851882224));
		tuples.add(new DoubleTuple(18.437461853027344,59.467146314256134));
		tuples.add(new DoubleTuple(18.443470001220703,59.47865601186644));
		tuples.add(new DoubleTuple(18.448963165283203,59.48562967808269));
		tuples.add(new DoubleTuple(18.46578598022461,59.497045947346486));
		tuples.add(new DoubleTuple(18.476943969726562,59.504799836616236));
		tuples.add(new DoubleTuple(18.487930297851562,59.51403248163582));
		tuples.add(new DoubleTuple(18.501663208007812,59.517515842036566));
		tuples.add(new DoubleTuple(18.520889282226562,59.52352379324845));
		tuples.add(new DoubleTuple(18.54166030883789,59.5307493309692));
		tuples.add(new DoubleTuple(18.570327758789062,59.54067103951734));
		tuples.add(new DoubleTuple(18.592472076416016,59.54867590277158));
		tuples.add(new DoubleTuple(18.620452880859375,59.55876628048537));
		tuples.add(new DoubleTuple(18.652725219726562,59.567723306213026));
		tuples.add(new DoubleTuple(18.674697875976562,59.57407004653665));
		tuples.add(new DoubleTuple(18.677616119384766,59.579459488820675));
		tuples.add(new DoubleTuple(18.68328094482422,59.58102400392204));
		tuples.add(new DoubleTuple(18.7042236328125,59.58310991088034));
		tuples.add(new DoubleTuple(18.726539611816406,59.58736823598668));
		tuples.add(new DoubleTuple(18.748855590820312,59.59275554821197));
		tuples.add(new DoubleTuple(18.770313262939453,59.60057430455003));
		tuples.add(new DoubleTuple(18.79537582397461,59.609520205675445));
		tuples.add(new DoubleTuple(18.820953369140625,59.61776916872406));
		tuples.add(new DoubleTuple(18.847217559814453,59.62844639664704));
		tuples.add(new DoubleTuple(18.872623443603516,59.6392069956019));
		tuples.add(new DoubleTuple(18.89820098876953,59.64987740825445));
		tuples.add(new DoubleTuple(18.92446517944336,59.66002416440409));
		tuples.add(new DoubleTuple(18.953475952148438,59.67172814615966));
		tuples.add(new DoubleTuple(18.980426788330078,59.68221490983823));
		tuples.add(new DoubleTuple(18.990554809570312,59.68594082071901));
		tuples.add(new DoubleTuple(19.013214111328125,59.69235188329587));
		tuples.add(new DoubleTuple(19.037933349609375,59.699887644364004));
		tuples.add(new DoubleTuple(19.064369201660156,59.70776806274006));
		tuples.add(new DoubleTuple(19.078445434570312,59.71218374982292));
		tuples.add(new DoubleTuple(19.114151000976562,59.719888161315126));
		tuples.add(new DoubleTuple(19.133548736572266,59.724388794209865));
		tuples.add(new DoubleTuple(19.163761138916016,59.729581079770036));
		tuples.add(new DoubleTuple(19.19534683227539,59.73468604121765));
		tuples.add(new DoubleTuple(19.226932525634766,59.73979022332947));
		tuples.add(new DoubleTuple(19.259891510009766,59.74498011782209));
		tuples.add(new DoubleTuple(19.292850494384766,59.750169206500594));
		tuples.add(new DoubleTuple(19.319629669189453,59.75570334633109));
		tuples.add(new DoubleTuple(19.34640884399414,59.76218749994764));
		tuples.add(new DoubleTuple(19.373016357421875,59.769102543156585));
		tuples.add(new DoubleTuple(19.392757415771484,59.774028637875425));
		tuples.add(new DoubleTuple(19.42485809326172,59.7885434260604));
		tuples.add(new DoubleTuple(19.43927764892578,59.79510756602249));
		tuples.add(new DoubleTuple(19.459705352783203,59.8001161722783));
		tuples.add(new DoubleTuple(19.498329162597656,59.807886658399255));
		tuples.add(new DoubleTuple(19.536781311035156,59.81565533369437));
		tuples.add(new DoubleTuple(19.582443237304688,59.82488906962289));
		tuples.add(new DoubleTuple(19.59909439086914,59.834551547747516));
		tuples.add(new DoubleTuple(19.609909057617188,59.85041953447475));
		tuples.add(new DoubleTuple(19.615230560302734,59.85869548182996));
		tuples.add(new DoubleTuple(19.645614624023438,59.870847048044425));
		tuples.add(new DoubleTuple(19.678573608398438,59.88376937310767));
		tuples.add(new DoubleTuple(19.700889587402344,59.90288519732573));
		tuples.add(new DoubleTuple(19.72148895263672,59.92061346766061));
		tuples.add(new DoubleTuple(19.74492073059082,59.941169842244726));
		tuples.add(new DoubleTuple(19.777536392211914,59.96832943091925));
		tuples.add(new DoubleTuple(19.81461524963379,59.999243619559756));
		tuples.add(new DoubleTuple(19.84551429748535,60.02704171133857));
		tuples.add(new DoubleTuple(19.877443313598633,60.055844682567525));
		tuples.add(new DoubleTuple(19.88945960998535,60.06355549916499));
		tuples.add(new DoubleTuple(19.917612075805664,60.06903765044199));
		tuples.add(new DoubleTuple(19.925851821899414,60.08308150550736));
		tuples.add(new DoubleTuple(19.926366806030273,60.09403840817271));
	}
	
	public class DoubleTuple {
		public double d1, d2;
		
		public DoubleTuple(double d1, double d2) {
			this.d1 = d1;
			this.d2 = d2;
		}
		
		public String toString() {
			return d1 + "," + d2;
		}
	}
}
