package io.emu.route.matcher.mapmatch;

import io.emu.route.matcher.pojo.CandidatePath;
import io.emu.route.matcher.pojo.CandidatePrj;
import io.emu.route.matcher.pojo.GPS;
import io.emu.route.matcher.pojo.Link;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import redis.clients.jedis.Jedis;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * TODO(这里用一句话描述这个类的作用)
 * 
 * @author Administrator
 * @version 2014年6月11日 下午7:46:05
 */
public class Main {

	static SimpleFeatureType createPointType() {

		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("Trace");
		builder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate reference
													// system

		builder.add("direction", String.class);
		builder.add("time", String.class);
		builder.add("speed", String.class);
		builder.add("bcm_keyst", String.class);
		builder.add("ems_engspd", String.class);
		builder.add("the_geom", Point.class);
		// build the type
		final SimpleFeatureType LOCATION = builder.buildFeatureType();

		return LOCATION;
	}

	static SimpleFeatureType createPolygonType() {

		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("Trace");
		builder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate reference
													// system
		// add attributes in order
		builder.add("the_geom", Polygon.class);
		// build the type
		final SimpleFeatureType LOCATION = builder.buildFeatureType();

		return LOCATION;
	}

	static SimpleFeatureType createLineStringType() {

		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("Trace");
		builder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate reference
													// system
		// add attributes in order
		builder.add("the_geom", LineString.class);
		// build the type
		final SimpleFeatureType LOCATION = builder.buildFeatureType();

		return LOCATION;
	}

	static SimpleFeatureType createMatchingType() {

		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("Trace");
		builder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate reference
													// system

		// add attributes in order

		builder.add("id", String.class);
		builder.add("intime", Long.class);
		builder.add("speed", Double.class);
		builder.add("prjDistance", Double.class);
		builder.add("prjDistanceFormSNode", Double.class);
		builder.add("length", Integer.class);
		builder.add("azimuth", Double.class);
		builder.add("azimuthDelta", Double.class);
		builder.add("cost", Double.class);
		builder.add("isdual", Boolean.class);
		builder.add("the_geom", MultiLineString.class);
		// build the type
		final SimpleFeatureType LOCATION = builder.buildFeatureType();

		return LOCATION;
	}

	static SimpleFeatureType createPathType() {

		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("Trace");
		builder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate reference
													// system

		// add attributes in order
		// builder.add("index", Integer.class);
		builder.add("id", Integer.class);
		builder.add("st", Long.class);
		builder.add("et", Long.class);
		builder.add("type", String.class);
		builder.add("len", Double.class);
		// builder.add("pathLength", Long.class);
		// builder.add("cost", Double.class);
		// builder.add("st_len", Double.class);
		// builder.add("ed_len", Double.class);
		// builder.add("st_prjfrom", Double.class);
		// builder.add("ed_prjfrom", Double.class);
		// builder.add("st_prj", Double.class);
		// builder.add("ed_prj", Double.class);
		builder.add("the_geom", LineString.class);
		// build the type
		final SimpleFeatureType LOCATION = builder.buildFeatureType();

		return LOCATION;
	}

	static SimpleFeatureType createNeoType() {

		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("Trace");
		builder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate reference
													// system

		// add attributes in order

		builder.add("id", String.class);

		builder.add("len", String.class);
		builder.add("index", String.class);
		builder.add("level", String.class);
		builder.add("the_geom", MultiLineString.class);
		// build the type
		final SimpleFeatureType LOCATION = builder.buildFeatureType();

		return LOCATION;
	}

	public static void writePointShp(List<GPS> gps, String out)
			throws Exception {
		final SimpleFeatureType TYPE = createPointType();

		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", new File(out).toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);
		ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory
				.createNewDataStore(params);

		newDataStore.createSchema(TYPE);
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
		Transaction transaction = new DefaultTransaction("create");

		String typeName = newDataStore.getTypeNames()[0];
		SimpleFeatureSource featureSource = newDataStore
				.getFeatureSource(typeName);
		SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();

		System.out.println("SHAPE:" + SHAPE_TYPE);
		GeometryFactory geometryFactory = JTSFactoryFinder
				.getGeometryFactory(null);
		if (featureSource instanceof SimpleFeatureStore) {
			SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
			List<SimpleFeature> lst = new ArrayList<SimpleFeature>();

			for (GPS g : gps) {

				Object[] obj = {
						g.getDirection(),
						g.getTime(),
						g.getSpeed(),
						g.getKeyst(),
						g.getEngspd(),
						geometryFactory.createPoint(new Coordinate(g
								.getLongtitude(), g.getLatitude())) };
				lst.add(featureBuilder.buildFeature(null, obj));

			}

			SimpleFeatureCollection collection = new ListFeatureCollection(
					TYPE, lst);
			featureStore.setTransaction(transaction);
			try {
				featureStore.addFeatures(collection);
				transaction.commit();
			} catch (Exception problem) {
				problem.printStackTrace();
				transaction.rollback();
			} finally {
				transaction.close();
			}
		}
	}

	public static void writeLineShp(List<GPS> trace, String out)
			throws Exception {

		final SimpleFeatureType TYPE = createLineStringType();

		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", new File(out).toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);
		ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory
				.createNewDataStore(params);

		newDataStore.createSchema(TYPE);
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
		Transaction transaction = new DefaultTransaction("create");

		String typeName = newDataStore.getTypeNames()[0];
		SimpleFeatureSource featureSource = newDataStore
				.getFeatureSource(typeName);
		SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();

		System.out.println("SHAPE:" + SHAPE_TYPE);
		// GeometryFactory geometryFactory =
		// JTSFactoryFinder.getGeometryFactory( null );
		if (featureSource instanceof SimpleFeatureStore) {
			SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
			List<SimpleFeature> lst = new ArrayList<SimpleFeature>();

			int id = 0;
			GPS lastGps = null;
			for (GPS gps : trace) {

				// LineString prj = new LineString(new Coordinate[]{new
				// Coordinate(gps.getLongtitude(),gps.getLatitude()),});
				// for (CandidatePrj g : gps.getCandidates()) {

				if (id > 0) {
					LineString prj = new LineString(new Coordinate[] {
							new Coordinate(lastGps.getLongtitude(),
									lastGps.getLatitude()),
							new Coordinate(gps.getLongtitude(),
									gps.getLatitude()) }, new PrecisionModel(),
							4326);
					// Geometry geometry = MapUtil.parseWktString(s[9]);

					Object[] obj = { prj };
					lst.add(featureBuilder.buildFeature(id + "", obj));
				}
				lastGps = gps;
				id++;
				// }
			}

			SimpleFeatureCollection collection = new ListFeatureCollection(
					TYPE, lst);
			featureStore.setTransaction(transaction);
			try {
				featureStore.addFeatures(collection);
				transaction.commit();
			} catch (Exception problem) {
				problem.printStackTrace();
				transaction.rollback();
			} finally {
				transaction.close();
			}
		}

	}

	public static void writeMatchingShp(List<GPS> trace, String out)
			throws Exception {

		final SimpleFeatureType TYPE = createMatchingType();

		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", new File(out).toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);
		ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory
				.createNewDataStore(params);

		newDataStore.createSchema(TYPE);
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
		Transaction transaction = new DefaultTransaction("create");

		String typeName = newDataStore.getTypeNames()[0];
		SimpleFeatureSource featureSource = newDataStore
				.getFeatureSource(typeName);
		SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();

		System.out.println("SHAPE:" + SHAPE_TYPE);
		// GeometryFactory geometryFactory =
		// JTSFactoryFinder.getGeometryFactory( null );
		if (featureSource instanceof SimpleFeatureStore) {
			SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
			List<SimpleFeature> lst = new ArrayList<SimpleFeature>();

			int id = 0;
			for (GPS gps : trace) {

				// LineString prj = new LineString(new Coordinate[]{new
				// Coordinate(gps.getLongtitude(),gps.getLatitude()),});
				for (CandidatePrj g : gps.getCandidates()) {
					LineString prj = new LineString(new Coordinate[] {
							new Coordinate(gps.getLongtitude(),
									gps.getLatitude()), g.getPrjPoint() },
							new PrecisionModel(), 4326);
					// Geometry geometry = MapUtil.parseWktString(s[9]);

					Object[] obj = { g.getLink().getId(), g.getGps().getTime(),
							g.getGps().getSpeed(), g.getPrjDistance(),
							g.getPrjDistanceFormSNode(),
							g.getLink().getLength(), g.getAzimuth(),
							g.getAzimuthDelta(), g.getCost(),
							g.getLink().isDual(), prj };
					lst.add(featureBuilder.buildFeature((id++) + "", obj));

				}
			}

			SimpleFeatureCollection collection = new ListFeatureCollection(
					TYPE, lst);
			featureStore.setTransaction(transaction);
			try {
				featureStore.addFeatures(collection);
				transaction.commit();
			} catch (Exception problem) {
				problem.printStackTrace();
				transaction.rollback();
			} finally {
				transaction.close();
			}
		}

	}

	public static void writePathShp(List<List<CandidatePath>> paths, String out)
			throws Exception {

		final SimpleFeatureType TYPE = createPathType();

		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", new File(out).toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);
		ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory
				.createNewDataStore(params);

		newDataStore.createSchema(TYPE);
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
		Transaction transaction = new DefaultTransaction("create");

		String typeName = newDataStore.getTypeNames()[0];
		SimpleFeatureSource featureSource = newDataStore
				.getFeatureSource(typeName);
		SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();

		System.out.println("SHAPE:" + SHAPE_TYPE);
		// GeometryFactory geometryFactory =
		// JTSFactoryFinder.getGeometryFactory( null );
		if (featureSource instanceof SimpleFeatureStore) {
			SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
			List<SimpleFeature> lst = new ArrayList<SimpleFeature>();

			int id = 0;
			int pid = 0;
			Jedis jedis = new Jedis("172.16.52.6");
			for (List<CandidatePath> ca : paths) {

				// LineString prj = new LineString(new Coordinate[]{new
				// Coordinate(gps.getLongtitude(),gps.getLatitude()),});
				for (CandidatePath g : ca) {
					// LineString prj = new LineString(new Coordinate[]{
					// new
					// Coordinate(gps.getLongtitude(),gps.getLatitude()),g.getPrjPoint()},new
					// PrecisionModel(), 4326);
					// Geometry geometry = MapUtil.parseWktString(s[9]);

					Geometry ls = g.getSt().getLink().getLine();

					GPS gps = g.getSt().getGps();
					LineString prj = new LineString(
							new Coordinate[] {
									new Coordinate(gps.getLongtitude(),
											gps.getLatitude()),
									g.getSt().getPrjPoint() },
							new PrecisionModel(), 4326);

					// ls = ls.union(prj);

					if (g.getPath() != null) {
						for (Link l : g.getPath()) {
							String lid = l.getId().replace("-", "");
							Link rlink = CellTools.getLink(lid, jedis);
							// Object[] robj = {lid, rlink.getLine()};

							ls = ls.union(rlink.getLine());
							// lst.add(featureBuilder.buildFeature((id++) + "",
							// robj));
						}
					}

					ls = ls.union(g.getEd().getLink().getLine());

					gps = g.getEd().getGps();
					LineString prj2 = new LineString(
							new Coordinate[] {
									new Coordinate(gps.getLongtitude(),
											gps.getLatitude()),
									g.getEd().getPrjPoint() },
							new PrecisionModel(), 4326);
					// Geometry prjall = prj.union(prj2);

					Object[] obj1 = { pid, g.getSt().getGps().getTime(),
							g.getEd().getGps().getTime(), "1",
							g.getPathLength(), ls };

					Object[] obj2 = { pid, g.getSt().getGps().getTime(),
							g.getEd().getGps().getTime(), "2", -1.0d, prj };
					Object[] obj3 = { pid, g.getSt().getGps().getTime(),
							g.getEd().getGps().getTime(), "2", -1.0d, prj2 };

					lst.add(featureBuilder.buildFeature((id++) + "", obj1));
					lst.add(featureBuilder.buildFeature((id++) + "", obj2));
					lst.add(featureBuilder.buildFeature((id++) + "", obj3));
				}
				pid++;
			}

			SimpleFeatureCollection collection = new ListFeatureCollection(
					TYPE, lst);
			featureStore.setTransaction(transaction);
			try {
				featureStore.addFeatures(collection);
				transaction.commit();
			} catch (Exception problem) {
				problem.printStackTrace();
				transaction.rollback();
			} finally {
				transaction.close();
			}
		}

	}

	public static List<GPS> readFile(String file) throws IOException {
		List<GPS> rst = new ArrayList<GPS>();
		BufferedReader bf = new BufferedReader(new FileReader(new File(file)));
		String line = bf.readLine();
		long lastT = Long.MIN_VALUE;
		while (line != null) {
			// System.out.println(line);
			String[] s = line.split("\t");
			line = bf.readLine();
			long tdae = Long.parseLong(s[2]);
			if ((lastT != Long.MIN_VALUE) && (tdae - lastT) < 5 * 1000) {
				System.out.println(lastT + " -> " + tdae);

				continue;
			}
			lastT = tdae;
			String bcm_keyst = s[3];
			String bcs_vehspd = s[4];
			String ems_engspd = s[5];

			String lon02 = s[8];
			String lat02 = s[9];

			if (lon02.equalsIgnoreCase("NULL")
					|| lat02.equalsIgnoreCase("NULL")
					|| bcs_vehspd.equalsIgnoreCase("NULL")) {

				continue;
			}

			double lon = Double.parseDouble(lon02);
			double lat = Double.parseDouble(lat02);

			if (lon > 115.0 || lon < 112.5 || lat > 24.00000002 || lat < 22.0)
				continue;
			rst.add(new GPS(lon, lat, -1, tdae, Double.parseDouble(bcs_vehspd),
					Double.parseDouble(ems_engspd), Integer.parseInt(bcm_keyst)));
			// line = bf.readLine().trim();
		}
		bf.close();
		return rst;
	}

	public static void test2() {
		GPS g = new GPS(113.26005999999999574, 23.18110300000000024,
				5.7567378116777475, 1440519133000l, 42.75, 2065.0, 2);

		Maps.getCandidateLinks(g);

	}

	public static void test1() throws Exception {
		String infile = "D:/map/LMGGN1S54E1000139.csv";
		// ProjectionMapping.convert("D:/map/LMGGN1S59E1000153.csv", outfile);
		// writePointShp(readFile(infile),"D:/map/LMGGN1S59E1000153-1.shp");

		List<GPS> trace = readFile(infile);
		Maps.setAzimuth(trace);

		// writePointShp(trace,"D:/map/LMGGN1S54E1000139-p1.shp");
		// writeLineShp(trace,"D:/map/LMGGN1S54E1000139-l1.shp");
		// for(GPS gps: trace){
		// Maps.getCandidateLinks(gps);
		// }

		List<List<CandidatePath>> paths = Maps.getRoute(trace);

		// writeMatchingShp(trace,"D:/map/LMGGN1S54E1000139-m1.shp");
		// writePathShp(paths,"D:/map/LMGGN1S54E1000139-r2.shp");
		Maps.output(paths, "out.csv");
	}

	/**
	 * TODO(功能描述)
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		test1();
	}

}
