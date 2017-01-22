package io.emu.route.matcher.mapmatch;

public class LonLat2Cell {
	double maxLon = 115.0;
	double minLon = 112.5;
	double maxLat = 24.00000002;
	double minLat = 22.0;
	double size = 50;

	int cols = (int) (Tools.distance(minLon, minLat, maxLon, minLat) / size) + 1;// 5163;
	int rows = (int) (Tools.distance(minLon, minLat, minLon, maxLat) / size) + 1;// 4430;

	double lonDelta = (maxLon - minLon) / cols;// 0.000484214603912454;
	double latDelta = (maxLat - minLat) / rows;// 0.0004514672731376979;

	public LonLat2Cell() {
	}

	public LonLat2Cell(double maxLon, double minLon, double maxLat,
			double minLat, double size) {
		super();
		this.maxLon = maxLon;
		this.minLon = minLon;
		this.maxLat = maxLat;
		this.minLat = minLat;
		this.size = size;

		this.cols = (int) (Tools.distance(minLon, minLat, maxLon, minLat) / size);// 5163;
		this.rows = (int) (Tools.distance(minLon, minLat, minLon, maxLat) / size);// 4430;

		this.lonDelta = (maxLon - minLon) / this.cols;// 0.000484214603912454;
		this.latDelta = (maxLat - minLat) / this.rows;// 0.0004514672731376979;

	}

	public String lonlat2cell(double lon, double lat) {
		if (lon > maxLon || lon < minLon || lat > maxLat || lat < minLat) {
			return "";
		}
		int c = (int) Math.floor((lon - minLon) / lonDelta);
		int r = (int) Math.floor((lat - minLat) / latDelta);
		String cellid = r + "_" + c;
		return cellid;

	}

	public double[] cellbound(int row, int col) {
		double milat = minLat + row * latDelta;
		double mxlat = minLat + (row + 1) * latDelta;
		double milon = minLon + col * lonDelta;
		double mxlon = minLon + (col + 1) * lonDelta;
		return new double[] { milon, milat, mxlon, mxlat };
	}

	public double[] cellboundbuffer(int row, int col) {
		double d[] = cellbound(row, col);
		return new double[] { d[0] - lonDelta, d[1] - latDelta,
				d[2] + lonDelta, d[3] + latDelta };
	}

	public static void main(String args[]) {
		// LonLat2Cell l=new LonLat2Cell(115.0,112.5,24.00000002,22.0,50);
		LonLat2Cell l = new LonLat2Cell();
		System.out.println(l.lonlat2cell(113.325820, 23.184608));
		System.out.println(l.lonlat2cell(112.5, 22.0));
		System.out.println(l.lonlat2cell(115.0, 24.00000002));

		double d[] = l.cellbound(2637, 1704);
		System.out.println(d[0] + "," + d[1] + "," + d[2] + "," + d[3] + ":"
				+ l.lonDelta + "_" + l.latDelta);
		d = l.cellboundbuffer(2637, 1704);
		System.out.println(d[0] + "," + d[1] + "," + d[2] + "," + d[3] + ":"
				+ l.lonDelta + "_" + l.latDelta);

	}
}
