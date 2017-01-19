package org.icgemu.route.matcher.matching.mapmatch;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Tools {
	private final static  double EARTH_RADIUS = 6378.137;//����뾶
	private static SimpleDateFormat format=new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	
	/**
	 * @param d
	 * @return
	 */
	private static double rad(double d)
	{
	   return d * Math.PI / 180.0;
	}

	/** ��������֮��Ĵ�Բ����
	 * @param lat1 ��һ����γ��
	 * @param lng1 ��һ���㾭��
	 * @param lat2 �ڶ�����γ��
	 * @param lng2 �ڶ����㾭��
	 * @return ������Բ����
	 */
	public static double distance(double lng1,double lat1, double lng2, double lat2)
	{
	   double radLat1 = rad(lat1);
	   double radLat2 = rad(lat2);
	   double a = radLat1 - radLat2;
	   double b = rad(lng1) - rad(lng2);

	   double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
	    Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
	   s = s * EARTH_RADIUS*1000;
//	   s = Math.round(s * 10000d) / 10000d;
	   return s;
	}
	
	
}
