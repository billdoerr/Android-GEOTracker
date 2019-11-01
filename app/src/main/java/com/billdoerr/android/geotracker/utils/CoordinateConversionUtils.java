/*
 * Author: Sami Salkosuo, sami.salkosuo@fi.ibm.com
 *
 * https://www.ibm.com/developerworks/java/library/j-coordconvert/
 *
 * (c) Copyright IBM Corp. 2007
 */
package com.billdoerr.android.geotracker.utils;

import android.location.Location;

import androidx.annotation.NonNull;

import java.util.Hashtable;
import java.util.Map;

import static java.lang.Math.abs;

@SuppressWarnings("ALL")
public class CoordinateConversionUtils
{

    public CoordinateConversionUtils() {
    //  Pass
    }

  /*
   * Below conversions taken from:
   * https://stackoverflow.com/questions/21382230/how-to-format-gps-latitude-and-longitude
   *
   * Positive latitude is above the equator (N), and negative latitude is below the equator (S).
   * Positive longitude is east of the prime meridian, while negative longitude is west of the
   * prime meridian (a north-south line that runs through a point in England).
   *
   */

  /**
   * Converts meters per second to knots.
    * @param speed float
   * @return float Value in knots
   */
  public static float mpsToKnots(float speed) {
    // knots = meters per second × 1.943844
    return speed *= 1.943844;
  }

  /**
   * Converts meters per second to kilometers per hour.
   * @param speed float
   * @return float Value in kilometers per hour
   */
  public static float mpsToKmHr(float speed) {
    return speed *= 3.6;
  }

  /**
   * Converst meters per second to miles per hour.
   * @param speed float
   * @return float Value in miles per hour
   */
  public static float mpsToMph(float speed) {
    return speed *= 2.236936;
  }

  /**
   * Converts meters to feet.
   * @param meters double
   * @return double Value in feet
   */
  public static double mToFt(double meters) {
    return meters /= 0.3048;
  }

  /**
   * Converts meters to feet.
   * @param meters float
   * @return float Value in feet
   */
  public static float mToFt(float meters) {
    return meters /= 0.3048;
  }

  /**
   * Converts decimal degrees to degrees, minutes, seconds.  Returns a coordinate as a String representation.
   *
   * @param latitude double
   * @param decimalPlace int
   * @return String
   */
  public static String latitudeAsDMS(double latitude, int decimalPlace){
        String direction = ((latitude > 0) ? " N" : " S");
        String strLatitude = Location.convert(latitude, Location.FORMAT_SECONDS);
        strLatitude = replaceDelimiters(strLatitude, decimalPlace);
        strLatitude += direction;
        return strLatitude;
    }

  /**
   * Converts decimal degrees to degrees, minutes, seconds.  Returns a coordinate as a String representation.
   *
   * @param longitude double
   * @param decimalPlace int
   * @return String
   */
    public static String longitudeAsDMS(double longitude, int decimalPlace){
        String direction = ((longitude > 0) ? " E" : " W");
        String strLongitude = Location.convert(abs(longitude), Location.FORMAT_SECONDS);
        strLongitude = replaceDelimiters(strLongitude, decimalPlace);
        strLongitude += direction;
        return strLongitude;
    }

  /**
   * Converts decimal degrees to degrees, decimal minutes.  Returns a coordinate as a String representation.
   *
   * @param latitude double
   * @param decimalPlace int
   * @return String
   */
    public static String latitudeAsDDM(double latitude, int decimalPlace){
        String direction = ((latitude > 0) ? " N" : " S");
        String strLatitude = Location.convert(latitude, Location.FORMAT_MINUTES);
        strLatitude = replaceDelimiters(strLatitude, decimalPlace);
        strLatitude += direction;
        return strLatitude;
    }

  /**
   * Converts decimal degrees to degrees, decimal minutes.  Returns a coordinate as a String representation.
   *
   * @param longitude double
   * @param decimalPlace int
   * @return String
   */
    public static String longitudeAsDDM(double longitude, int decimalPlace){
        String direction = ((longitude > 0) ? " E" : " W");
        String strLongitude = Location.convert(abs(longitude), Location.FORMAT_MINUTES);
        strLongitude = replaceDelimiters(strLongitude, decimalPlace);
        strLongitude += direction;
        return strLongitude;
    }

    @NonNull
    private static String replaceDelimiters(String str, int decimalPlace) {
        str = str.replaceFirst(":", "°");
        str = str.replaceFirst(":", "'");
        int pointIndex = str.indexOf(".");
        int endIndex = pointIndex + 1 + decimalPlace;
        if(endIndex < str.length()) {
          str = str.substring(0, endIndex);
        }
        str = str + "\"";
        return str;
    }

  /**
   * Below conversions taken from:
   * https://www.ibm.com/developerworks/java/library/j-coordconvert/
   */

  public double[] utm2LatLon(String UTM)
  {
    UTM2LatLon c = new UTM2LatLon();
    return c.convertUTMToLatLong(UTM);
  }

  public String latLon2UTM(double latitude, double longitude)
  {
    LatLon2UTM c = new LatLon2UTM();
    return c.convertLatLonToUTM(latitude, longitude);

  }

  private void validate(double latitude, double longitude)
  {
    if (latitude < -90.0 || latitude > 90.0 || longitude < -180.0
        || longitude >= 180.0)
    {
      throw new IllegalArgumentException(
          "Legal ranges: latitude [-90,90], longitude [-180,180).");
    }

  }

  public String latLon2MGRUTM(double latitude, double longitude)
  {
    LatLon2MGRUTM c = new LatLon2MGRUTM();
    return c.convertLatLonToMGRUTM(latitude, longitude);

  }

  public double[] mgrutm2LatLon(String MGRUTM)
  {
    MGRUTM2LatLon c = new MGRUTM2LatLon();
    return c.convertMGRUTMToLatLong(MGRUTM);
  }

  public double degreeToRadian(double degree)
  {
    return degree * Math.PI / 180;
  }

  public double radianToDegree(double radian)
  {
    return radian * 180 / Math.PI;
  }

  private double POW(double a, double b)
  {
    return Math.pow(a, b);
  }

  private double SIN(double value)
  {
    return Math.sin(value);
  }

  private double COS(double value)
  {
    return Math.cos(value);
  }

  private double TAN(double value)
  {
    return Math.tan(value);
  }

  private class LatLon2UTM
  {
    public String convertLatLonToUTM(double latitude, double longitude)
    {
      validate(latitude, longitude);
      String UTM;

      setVariables(latitude, longitude);

      String longZone = getLongZone(longitude);
      LatZones latZones = new LatZones();
      String latZone = latZones.getLatZone(latitude);

      double _easting = getEasting();
      double _northing = getNorthing(latitude);

      UTM = longZone + " " + latZone + " " + ((int) _easting) + " "
          + ((int) _northing);
      // UTM = longZone + " " + latZone + " " + decimalFormat.format(_easting) +
      // " "+ decimalFormat.format(_northing);

      return UTM;

    }

    protected void setVariables(double latitude, double longitude)
    {
      latitude = degreeToRadian(latitude);
      rho = equatorialRadius * (1 - e * e)
          / POW(1 - POW(e * SIN(latitude), 2), 3 / 2.0);

      nu = equatorialRadius / POW(1 - POW(e * SIN(latitude), 2), (1 / 2.0));

      double var1;
      if (longitude < 0.0)
      {
        var1 = ((int) ((180 + longitude) / 6.0)) + 1;
      }
      else
      {
        var1 = ((int) (longitude / 6)) + 31;
      }
      double var2 = (6 * var1) - 183;
      double var3 = longitude - var2;
      p = var3 * 3600 / 10000;

      S = A0 * latitude - B0 * SIN(2 * latitude) + C0 * SIN(4 * latitude) - D0
          * SIN(6 * latitude) + E0 * SIN(8 * latitude);

      K1 = S * k0;
      K2 = nu * SIN(latitude) * COS(latitude) * POW(sin1, 2) * k0 * (100000000)
          / 2;
      K3 = ((POW(sin1, 4) * nu * SIN(latitude) * Math.pow(COS(latitude), 3)) / 24)
          * (5 - POW(TAN(latitude), 2) + 9 * e1sq * POW(COS(latitude), 2) + 4
              * POW(e1sq, 2) * POW(COS(latitude), 4))
          * k0
          * (10000000000000000L);

      K4 = nu * COS(latitude) * sin1 * k0 * 10000;

      K5 = POW(sin1 * COS(latitude), 3) * (nu / 6)
          * (1 - POW(TAN(latitude), 2) + e1sq * POW(COS(latitude), 2)) * k0
          * 1000000000000L;

      A6 = (POW(p * sin1, 6) * nu * SIN(latitude) * POW(COS(latitude), 5) / 720)
          * (61 - 58 * POW(TAN(latitude), 2) + POW(TAN(latitude), 4) + 270
              * e1sq * POW(COS(latitude), 2) - 330 * e1sq
              * POW(SIN(latitude), 2)) * k0 * (1E+24);

    }

    protected String getLongZone(double longitude)
    {
      double longZone;
      if (longitude < 0.0)
      {
        longZone = ((180.0 + longitude) / 6) + 1;
      }
      else
      {
        longZone = (longitude / 6) + 31;
      }
      String val = String.valueOf((int) longZone);
      if (val.length() == 1)
      {
        val = "0" + val;
      }
      return val;
    }

    protected double getNorthing(double latitude)
    {
      double northing = K1 + K2 * p * p + K3 * POW(p, 4);
      if (latitude < 0.0)
      {
        northing = 10000000 + northing;
      }
      return northing;
    }

    protected double getEasting()
    {
      return 500000 + (K4 * p + K5 * POW(p, 3));
    }

    // Lat Lon to UTM variables

    // equatorial radius
    final double equatorialRadius = 6378137;

    // polar radius
    final double polarRadius = 6356752.314;

    // flattening
    double flattening = 0.00335281066474748;// (equatorialRadius-polarRadius)/equatorialRadius;

    // inverse flattening 1/flattening
    double inverseFlattening = 298.257223563;// 1/flattening;

    // Mean radius
    double rm = POW(equatorialRadius * polarRadius, 1 / 2.0);

    // scale factor
    final double k0 = 0.9996;

    // eccentricity
    final double e = Math.sqrt(1 - POW(polarRadius / equatorialRadius, 2));

    final double e1sq = e * e / (1 - e * e);

    double n = (equatorialRadius - polarRadius)
        / (equatorialRadius + polarRadius);

    // r curv 1
    double rho = 6368573.744;

    // r curv 2
    double nu = 6389236.914;

    // Calculate Meridional Arc Length
    // Meridional Arc
    double S = 5103266.421;

    final double A0 = 6367449.146;

    final double B0 = 16038.42955;

    final double C0 = 16.83261333;

    final double D0 = 0.021984404;

    final double E0 = 0.000312705;

    // Calculation Constants
    // Delta Long
    double p = -0.483084;

    final double sin1 = 4.84814E-06;

    // Coefficients for UTM Coordinates
    double K1 = 5101225.115;

    double K2 = 3750.291596;

    double K3 = 1.397608151;

    double K4 = 214839.3105;

    double K5 = -2.995382942;

    double A6 = -1.00541E-07;

  }

  private class LatLon2MGRUTM extends LatLon2UTM
  {
    public String convertLatLonToMGRUTM(double latitude, double longitude)
    {
      validate(latitude, longitude);
      String mgrUTM;

      setVariables(latitude, longitude);

      String longZone = getLongZone(longitude);
      LatZones latZones = new LatZones();
      String latZone = latZones.getLatZone(latitude);

      double _easting = getEasting();
      double _northing = getNorthing(latitude);
      Digraphs digraphs = new Digraphs();
      String digraph1 = digraphs.getDigraph1(Integer.parseInt(longZone),
          _easting);
      String digraph2 = digraphs.getDigraph2(Integer.parseInt(longZone),
          _northing);

      String easting = String.valueOf((int) _easting);
      if (easting.length() < 5)
      {
        easting = "00000" + easting;
      }
      easting = easting.substring(easting.length() - 5);

      String northing;
      northing = String.valueOf((int) _northing);
      if (northing.length() < 5)
      {
        northing = "0000" + northing;
      }
      northing = northing.substring(northing.length() - 5);

      mgrUTM = longZone + latZone + digraph1 + digraph2 + easting + northing;
      return mgrUTM;
    }
  }

  private class MGRUTM2LatLon extends UTM2LatLon
  {
    public double[] convertMGRUTMToLatLong(String mgrutm)
    {
      double[] latlon = { 0.0, 0.0 };
      // 02CNR0634657742
      int zone = Integer.parseInt(mgrutm.substring(0, 2));
      String latZone = mgrutm.substring(2, 3);

      String digraph1 = mgrutm.substring(3, 4);
      String digraph2 = mgrutm.substring(4, 5);
      easting = Double.parseDouble(mgrutm.substring(5, 10));
      northing = Double.parseDouble(mgrutm.substring(10, 15));

      LatZones lz = new LatZones();
      double latZoneDegree = lz.getLatZoneDegree(latZone);

      double a1 = latZoneDegree * 40000000 / 360.0;
      double a2 = 2000000 * Math.floor(a1 / 2000000.0);

      Digraphs digraphs = new Digraphs();

      double digraph2Index = digraphs.getDigraph2Index(digraph2);

      double startindexEquator = 1;
      if ((1 + zone % 2) == 1)
      {
        startindexEquator = 6;
      }

      double a3 = a2 + (digraph2Index - startindexEquator) * 100000;
      if (a3 <= 0)
      {
        a3 = 10000000 + a3;
      }
      northing = a3 + northing;

      zoneCM = -183 + 6 * zone;
      double digraph1Index = digraphs.getDigraph1Index(digraph1);
      int a5 = 1 + zone % 3;
      double[] a6 = { 16, 0, 8 };
      double a7 = 100000 * (digraph1Index - a6[a5 - 1]);
      easting = easting + a7;

      setVariables();

      double latitude;
      latitude = 180 * (phi1 - fact1 * (fact2 + fact3 + fact4)) / Math.PI;

      if (latZoneDegree < 0)
      {
        latitude = 90 - latitude;
      }

      double d = _a2 * 180 / Math.PI;
      double longitude = zoneCM - d;

      if (getHemisphere(latZone).equals("S"))
      {
        latitude = -latitude;
      }

      latlon[0] = latitude;
      latlon[1] = longitude;
      return latlon;
    }
  }

  private class UTM2LatLon
  {
    double easting;

    double northing;

    int zone;

    final String southernHemisphere = "ACDEFGHJKLM";

    protected String getHemisphere(String latZone)
    {
      String hemisphere = "N";
      if (southernHemisphere.contains(latZone))
      {
        hemisphere = "S";
      }
      return hemisphere;
    }

    public double[] convertUTMToLatLong(String UTM)
    {
      double[] latlon = { 0.0, 0.0 };
      String[] utm = UTM.split(" ");
      zone = Integer.parseInt(utm[0]);
      String latZone = utm[1];
      easting = Double.parseDouble(utm[2]);
      northing = Double.parseDouble(utm[3]);
      String hemisphere = getHemisphere(latZone);
      double latitude;
      double longitude;

      if (hemisphere.equals("S"))
      {
        northing = 10000000 - northing;
      }
      setVariables();
      latitude = 180 * (phi1 - fact1 * (fact2 + fact3 + fact4)) / Math.PI;

      if (zone > 0)
      {
        zoneCM = 6 * zone - 183.0;
      }
      else
      {
        zoneCM = 3.0;

      }

      longitude = zoneCM - _a3;
      if (hemisphere.equals("S"))
      {
        latitude = -latitude;
      }

      latlon[0] = latitude;
      latlon[1] = longitude;
      return latlon;

    }

    protected void setVariables()
    {
      arc = northing / k0;
      mu = arc
          / (a * (1 - POW(e, 2) / 4.0 - 3 * POW(e, 4) / 64.0 - 5 * POW(e, 6) / 256.0));

      ei = (1 - POW((1 - e * e), (1 / 2.0)))
          / (1 + POW((1 - e * e), (1 / 2.0)));

      ca = 3 * ei / 2 - 27 * POW(ei, 3) / 32.0;

      cb = 21 * POW(ei, 2) / 16 - 55 * POW(ei, 4) / 32;
      cc = 151 * POW(ei, 3) / 96;
      cd = 1097 * POW(ei, 4) / 512;
      phi1 = mu + ca * SIN(2 * mu) + cb * SIN(4 * mu) + cc * SIN(6 * mu) + cd
          * SIN(8 * mu);

      n0 = a / POW((1 - POW((e * SIN(phi1)), 2)), (1 / 2.0));

      r0 = a * (1 - e * e) / POW((1 - POW((e * SIN(phi1)), 2)), (3 / 2.0));
      fact1 = n0 * TAN(phi1) / r0;

      _a1 = 500000 - easting;
      dd0 = _a1 / (n0 * k0);
      fact2 = dd0 * dd0 / 2;

      t0 = POW(TAN(phi1), 2);
      Q0 = e1sq * POW(COS(phi1), 2);
      fact3 = (5 + 3 * t0 + 10 * Q0 - 4 * Q0 * Q0 - 9 * e1sq) * POW(dd0, 4)
          / 24;

      fact4 = (61 + 90 * t0 + 298 * Q0 + 45 * t0 * t0 - 252 * e1sq - 3 * Q0
          * Q0)
          * POW(dd0, 6) / 720;

      //
      lof1 = _a1 / (n0 * k0);
      lof2 = (1 + 2 * t0 + Q0) * POW(dd0, 3) / 6.0;
      lof3 = (5 - 2 * Q0 + 28 * t0 - 3 * POW(Q0, 2) + 8 * e1sq + 24 * POW(t0, 2))
          * POW(dd0, 5) / 120;
      _a2 = (lof1 - lof2 + lof3) / COS(phi1);
      _a3 = _a2 * 180 / Math.PI;

    }

    double arc;

    double mu;

    double ei;

    double ca;

    double cb;

    double cc;

    double cd;

    double n0;

    double r0;

    double _a1;

    double dd0;

    double t0;

    double Q0;

    double lof1;

    double lof2;

    double lof3;

    double _a2;

    double phi1;

    double fact1;

    double fact2;

    double fact3;

    double fact4;

    double zoneCM;

    double _a3;

    double b = 6356752.314;

    final double a = 6378137;

    final double e = 0.081819191;

    final double e1sq = 0.006739497;

    final double k0 = 0.9996;

  }

  @SuppressWarnings({"unchecked", "UnusedAssignment"})
  private class Digraphs
  {
    private final Map digraph1 = new Hashtable();

    private final Map digraph2 = new Hashtable();

    private final String[] digraph1Array = { "A", "B", "C", "D", "E", "F", "G", "H",
        "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
        "Y", "Z" };

    private final String[] digraph2Array = { "V", "A", "B", "C", "D", "E", "F", "G",
        "H", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V" };

    public Digraphs()
    {
      digraph1.put(1, "A");
      digraph1.put(2, "B");
      digraph1.put(3, "C");
      digraph1.put(4, "D");
      digraph1.put(5, "E");
      digraph1.put(6, "F");
      digraph1.put(7, "G");
      digraph1.put(8, "H");
      digraph1.put(9, "J");
      digraph1.put(10, "K");
      digraph1.put(11, "L");
      digraph1.put(12, "M");
      digraph1.put(13, "N");
      digraph1.put(14, "P");
      digraph1.put(15, "Q");
      digraph1.put(16, "R");
      digraph1.put(17, "S");
      digraph1.put(18, "T");
      digraph1.put(19, "U");
      digraph1.put(20, "V");
      digraph1.put(21, "W");
      digraph1.put(22, "X");
      digraph1.put(23, "Y");
      digraph1.put(24, "Z");

      digraph2.put(0, "V");
      digraph2.put(1, "A");
      digraph2.put(2, "B");
      digraph2.put(3, "C");
      digraph2.put(4, "D");
      digraph2.put(5, "E");
      digraph2.put(6, "F");
      digraph2.put(7, "G");
      digraph2.put(8, "H");
      digraph2.put(9, "J");
      digraph2.put(10, "K");
      digraph2.put(11, "L");
      digraph2.put(12, "M");
      digraph2.put(13, "N");
      digraph2.put(14, "P");
      digraph2.put(15, "Q");
      digraph2.put(16, "R");
      digraph2.put(17, "S");
      digraph2.put(18, "T");
      digraph2.put(19, "U");
      digraph2.put(20, "V");

    }

    public int getDigraph1Index(String letter)
    {
      for (int i = 0; i < digraph1Array.length; i++)
      {
        if (digraph1Array[i].equals(letter))
        {
          return i + 1;
        }
      }

      return -1;
    }

    public int getDigraph2Index(String letter)
    {
      for (int i = 0; i < digraph2Array.length; i++)
      {
        if (digraph2Array[i].equals(letter))
        {
          return i;
        }
      }

      return -1;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public String getDigraph1(int longZone, double easting)
    {
      int a1 = longZone;
      double a2 = 8 * ((a1 - 1) % 3) + 1;

      double a3 = easting;
      double a4 = a2 + ((int) (a3 / 100000)) - 1;
      return (String) digraph1.get((int) Math.floor(a4));
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public String getDigraph2(int longZone, double northing)
    {
      int a1 = longZone;
      double a2 = 1 + 5 * ((a1 - 1) % 2);
      double a3 = northing;
      double a4 = (a2 + ((int) (a3 / 100000)));
      a4 = (a2 + ((int) (a3 / 100000.0))) % 20;
      a4 = Math.floor(a4);
      if (a4 < 0)
      {
        a4 = a4 + 19;
      }
      return (String) digraph2.get((int) Math.floor(a4));

    }

  }

  private class LatZones
  {
    private final char[] letters = { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K',
        'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Z' };

    private final int[] degrees = { -90, -84, -72, -64, -56, -48, -40, -32, -24, -16,
        -8, 0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 84 };

    private final char[] negLetters = { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K',
        'L', 'M' };

    private final int[] negDegrees = { -90, -84, -72, -64, -56, -48, -40, -32, -24,
        -16, -8 };

    private final char[] posLetters = { 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
        'X', 'Z' };

    private final int[] posDegrees = { 0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 84 };

    @SuppressWarnings("FieldCanBeLocal")
    private final int arrayLength = 22;

    public LatZones()
    {
    }

    public int getLatZoneDegree(String letter)
    {
      char ltr = letter.charAt(0);
      for (int i = 0; i < arrayLength; i++)
      {
        if (letters[i] == ltr)
        {
          return degrees[i];
        }
      }
      return -100;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public String getLatZone(double latitude)
    {
      int latIndex = -2;
      int lat = (int) latitude;

      if (lat >= 0)
      {
        int len = posLetters.length;
        for (int i = 0; i < len; i++)
        {
          if (lat == posDegrees[i])
          {
            latIndex = i;
            break;
          }

          if (lat > posDegrees[i])
          {
            // Pass
//            continue;
          }
          else
          {
            latIndex = i - 1;
            break;
          }
        }
      }
      else
      {
        int len = negLetters.length;
        for (int i = 0; i < len; i++)
        {
          if (lat == negDegrees[i])
          {
            latIndex = i;
            break;
          }

          if (lat < negDegrees[i])
          {
            latIndex = i - 1;
            break;
          }
          else
          {
            // Pass
//            continue;
          }

        }

      }

      if (latIndex == -1)
      {
        latIndex = 0;
      }
      if (lat >= 0)
      {
        if (latIndex == -2)
        {
          latIndex = posLetters.length - 1;
        }
        return String.valueOf(posLetters[latIndex]);
      }
      else
      {
        if (latIndex == -2)
        {
          latIndex = negLetters.length - 1;
        }
        return String.valueOf(negLetters[latIndex]);

      }
    }

  }

}
