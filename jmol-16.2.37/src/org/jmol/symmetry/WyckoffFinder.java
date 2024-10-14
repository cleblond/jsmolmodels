package org.jmol.symmetry;

import java.util.Hashtable;
import java.util.Map;

import org.jmol.viewer.Viewer;

import javajs.util.Lst;
import javajs.util.M4;
import javajs.util.Measure;
import javajs.util.P3;
import javajs.util.P4;
import javajs.util.PT;
import javajs.util.SB;
import javajs.util.V3;

/**
 * A class to calculate, store, and retrieve Wyckoff information as per
 * BCS nph-trgen at
 * 
 * https://www.cryst.ehu.es/cgi-bin/cryst/programs//nph-trgen?gnum=146&what=wpos&trmat=2/3a+1/3b+1/3c,-1/3a+1/3b+1/3c,-1/3a-2/3b+1/3c&unconv=R%203%20:R&from=ita
 * 
 * For the 611 standard setting in ITA GENPOS, we are just reading the json file and 
 * loading its information.
 * 
 * For setting not found through GENPOS, we calculate the Wyckoff positions. 
 *  
 *   
 * 
 */
public class WyckoffFinder {

  private static WyckoffFinder nullHelper;
  private final static Map<String, WyckoffFinder> helpers = new Hashtable<String, WyckoffFinder>();

  /**
   * positive numbers will be label characters or '*'
   */
  public final static int WYCKOFF_RET_LABEL = -1;
  public final static int WYCKOFF_RET_COORD = -2;
  public final static int WYCKOFF_RET_COORDS = -3;
  public final static int WYCKOFF_RET_ALL_ARRAY = -4;
  public final static int WYCKOFF_RET_COORDS_ALL = '*';
  public final static int WYCKOFF_RET_GENERAL = 'G';
  public final static int WYCKOFF_RET_CENTERING = 'C';
  public final static int WYCKOFF_RET_CENTERING_STRING = 'S';
  public final static int WYCKOFF_RET_WITH_MULT = 'M';

  private Lst<Object> positions;
  private int npos, ncent;
  protected P3[] centerings;
  protected String[] centeringStr;
  private Lst<Object> gpos;

  public WyckoffFinder() {
    // only used for dynamic instantiation from Symmetry.java
  }

  /**
   * Retrieve the JSON data for this space group and extract its Wyckoff
   * information.
   * 
   * Effectively static, as this is only accessed from the singleton static
   * helper instance. But we leave it not static so as not to generate a "static
   * access" warning.
   * 
   * @param vwr
   * @param sg
   * @return helper
   */
  WyckoffFinder getWyckoffFinder(Viewer vwr, SpaceGroup sg) {
    String cleg = sg.getClegId();
    String key = sg.specialPrefix + cleg;
    WyckoffFinder helper = helpers.get(key);
    if (helper != null) 
      return helper;
    helper = createHelper(vwr, cleg, sg.groupType);
    if (helper == null) {
      if (nullHelper == null)
        nullHelper = new WyckoffFinder(null);
      helpers.put(key, nullHelper);
    } else {
      helpers.put(key, helper);
    }
    return helper;
  }

  /**
   * Given a fractional coordinate and a Wyckoff position letter such as "b",
   * determine the matching orbit symmetry element and project p onto it.
   * 
   * @param p
   * @param letter
   * @return p as its projection
   */
  P3 findPositionFor(P3 p, String letter) {
    if (positions != null) {
      boolean isGeneral = (letter.equals("G"));
      for (int i = isGeneral ? 1 : npos; --i >= 0;) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) positions.get(i);
        String l = (String) map.get("label");
        if (isGeneral || l.equals(letter)) {
          @SuppressWarnings("unchecked")
          Lst<Object> coords = (Lst<Object>) map.get("coord");
          if (coords != null)
            getWyckoffCoord(coords, 0, l).project(p);
          return p;
        }
      }
    }
    return null;
  }

  /**
   * Get string information about this position or space group
   * 
   * @param uc
   * @param p
   * @param returnType
   *        label, coord, label*, or
   * @param withMult
   * @param is2d 
   * @return label or coordinate or label with centerings and coordinates or
   *         full list for space group
   */
  Object getInfo(UnitCell uc, P3 p, int returnType, boolean withMult, boolean is2d) {
    Object info = createInfo(uc, p, returnType, withMult, is2d);
    return (info == null ? "?" : info);
  }

  /**
   * Just wrap the coordinate with parentheses.
   * 
   * @param xyz
   * @param sb
   * @return sb for continued appending
   */
  protected static SB wrap(String xyz, SB sb) {
    return sb.appendC('(').append(xyz).appendC(')');
  }

  private final static String elementList = "AlB C D FeF "
      + "GaHeI GeK LiMgN Os"
      + "P CaRhS T U V W XeYbZnAm";

  /**
   * Generate information for the symop("wyckoff") script function
   * 
   * @param uc
   * @param p
   * @param returnType
   *        '*', -1, -2, -3, or a character label 'a'-'A' or 'G' for general or
   *        'C' for centerings
   * @param withMult
   *        from "wyckoffm"
   * @param is2d 
   * @return an informational string
   */
  @SuppressWarnings("unchecked")
  private Object createInfo(UnitCell uc, P3 p, int returnType,
                            boolean withMult, boolean is2d) {
    switch (returnType) {
    case WYCKOFF_RET_CENTERING_STRING:
      return getCenteringStr(-1, ' ', null).toString().trim();
    case WYCKOFF_RET_CENTERING:
      P3[] ret = new P3[centerings.length];
      for (int i = ret.length; --i >= 0;)
        ret[i] = centerings[i];
      return ret;
    case WYCKOFF_RET_COORDS_ALL:
      SB sb = new SB();
      getCenteringStr(-1, '+', sb);
      for (int i = npos; --i >= 0;) {
        Map<String, Object> map = (Map<String, Object>) positions.get(i);
        String label = (withMult ? "" + map.get("mult") : "")
            + map.get("label");
        sb.appendC('\n').append(label);
        getList(i == 0 ? gpos : (Lst<Object>) map.get("coord"), label, sb,
            (i == 0 ? ncent : 0));
      }
      return sb.toString();
    case WYCKOFF_RET_ALL_ARRAY:
      P3[] pts = new P3[npos];
      for (int i = 0; i < npos; i++) {
          Map<String, Object> map = (Map<String, Object>) positions.get(i);
          pts[i] = findPositionFor(P3.newP(p), (String) map.get("label"));
          uc.toCartesian(pts[i], false);
      }
      return new Object[] { pts, elementList };
    case WYCKOFF_RET_LABEL:
    case WYCKOFF_RET_COORD:
    case WYCKOFF_RET_COORDS:
      for (int i = npos; --i >= 0;) {
        Map<String, Object> map = (Map<String, Object>) positions.get(i);
        String label = (withMult ? "" + map.get("mult") : "")
            + map.get("label");
        if (i == 0) {
          // general
          switch (returnType) {
          case WYCKOFF_RET_LABEL:
            return label;
          case WYCKOFF_RET_COORD:
            return "(x,y,z)";
          case WYCKOFF_RET_COORDS:
            SB sbc = new SB();
            sbc.append(label).appendC(' ');
            getCenteringStr(-1, '+', sbc).appendC(' ');
            getList(gpos, label, sbc, ncent);
            return sbc.toString();
          }
        }
        Lst<Object> coords = (Lst<Object>) map.get("coord");
        for (int c = 0, n = coords.size(); c < n; c++) {
          WyckoffCoord coord = getWyckoffCoord(coords, c, label);
          //          System.out.println(label+ " " + coord + " " +  c + " " + p);
          if (coord.contains(this, uc, p)) {
            switch (returnType) {
            case WYCKOFF_RET_LABEL:
              return label;
            case WYCKOFF_RET_COORD:
              return coord.asString(null, true).toString();
            case WYCKOFF_RET_COORDS:
              SB sbc = new SB();
              sbc.append(label).appendC(' ');
              getCenteringStr(-1, '+', sbc).appendC(' ');
              getList(coords, label, sbc, 0);
              return sbc.toString();
            }
          }
        }
      }
      break;
    case WYCKOFF_RET_GENERAL:
    default:
      // specific letter
      String letter = "" + (char) returnType;
      boolean isGeneral = (returnType == WYCKOFF_RET_GENERAL);
      P3 tempP = new P3();
      for (int i = isGeneral ? 1 : npos; --i >= 0;) {
        Map<String, Object> map = (Map<String, Object>) positions.get(i);
        String label = (String) map.get("label");
        if (isGeneral || label.equals(letter)) {
          SB sbc = new SB();
          if (isGeneral)
            sbc.append(label).appendC(' ');
          Lst<Object> coords = (i == 0 ? gpos : (Lst<Object>) map.get("coord"));
          getList(coords, (withMult ? map.get("mult") : "") + letter, sbc, 0);
          if (i > 0 && ncent > 0) {
            M4 tempOp = new M4();
            for (int j = 0; j < ncent; j++) {
              addCentering(coords, centerings[j], tempOp, tempP, sbc);
            }
          }
          return sbc.toString();
        }
      }
      break;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private static WyckoffFinder createHelper(Viewer vwr, String clegId,
                                            int groupType) {
    String key = SpaceGroup.getGroupTypePrefix(groupType) + clegId;
    String sgname = clegId;
    int pt = sgname.indexOf(":");
    int itno = PT.parseInt(pt < 0 ? sgname : sgname.substring(0, pt));
    if (!SpaceGroup.isInRange(itno, groupType, false, false))
      return null;
    Map<String, Object> resource = Symmetry.getITJSONResource(vwr, groupType, itno, null);
    if (resource == null)
      return null;
    Lst<Object> its = (Lst<Object>) resource.get("its");
    Map<String, Object> map = null;
    boolean haveMap = false;
    for (int i = 0, c = its.size(); i < c; i++) {
      map = (Map<String, Object>) its.get(i);
      if (key.equals(map.get("clegId"))) {
        haveMap = true;
        break;
      }
    }
    
    // "more" type, from wp-list, does not contain gp or wpos
    if (!haveMap || map.containsKey("more"))
      map = SpaceGroup.fillMoreData(vwr, haveMap ? map : null, clegId, itno,
          (Map<String, Object>) its.get(0));
    WyckoffFinder helper = new WyckoffFinder(map);
    return helper;
  }

  /**
   * Load data from the JSON map.
   * 
   * @param map
   */
  @SuppressWarnings("unchecked")
  private WyckoffFinder(Map<String, Object> map) {
    if (map != null) {
      // These will be turned into WyckoffCoord as needed
      Lst<Object> gp = (Lst<Object>) map.get("gp");
      gpos = new Lst<Object>();
      gpos.addAll(gp);
      Map<String, Object> wpos = (Map<String, Object>) map.get("wpos");
      positions = (Lst<Object>) wpos.get("pos");
      npos = positions.size();
      Lst<Object> cent = (Lst<Object>) wpos.get("cent");
      if (cent != null) {
        ncent = cent.size();
        centeringStr = new String[ncent];
        centerings = new P3[ncent];
        for (int i = ncent; --i >= 0;) {
          String s = (String) cent.get(i);
          centeringStr[i] = s;
          centerings[i] = SymmetryOperation.toPoint(s, null);
        }
      }
    }
  }

  private SB getCenteringStr(int index, char sep, SB sb) {
    if (sb == null)
      sb = new SB();
    if (ncent == 0)
      return sb;
    if (index >= 0) {
      sb.appendC(sep);
      return wrap(centeringStr[index], sb);
    }
    for (int i = 0; i < ncent; i++) {
      sb.appendC(sep);
      wrap(centeringStr[i], sb);
    }
    return sb;
  }

  private static SB getList(Lst<Object> coords, String letter, SB sb, int n) {
    if (sb == null)
      sb = new SB();
    n = (n == 0 ? coords.size() : coords.size() / (n + 1));
    for (int c = 0; c < n; c++) {
      WyckoffCoord coord = getWyckoffCoord(coords, c, letter);
      sb.append(" ");
      coord.asString(sb, false);
    }
    return sb;
  }

  private void addCentering(Lst<Object> coords, P3 centering, M4 tempOp,
                            P3 tempP, SB sb) {
    for (int n = coords.size(), c = 0; c < n; c++) {
      WyckoffCoord coord = (WyckoffCoord) coords.get(c);
      sb.append(" ");
      coord.asStringCentered(centering, tempOp, tempP, sb);
    }
  }

  private static WyckoffCoord getWyckoffCoord(Lst<Object> coords, int c,
                                              String label) {
    Object coord = coords.get(c);
    if (coord instanceof String) {
      coords.set(c, coord = new WyckoffCoord((String) coord, label));
    }
    return (WyckoffCoord) coord;
  }

  /**
   * The WyckoffCoord class.
   * 
   * A coordinate in the orbit of a Wyckoff position.
   *
   * Defined by a symmetry element (point, line, or plane), and an associated
   * 4x4 matrix representation.
   * 
   * The matrix operation is used to bring general positions into its orbit.
   * This matrix is used for lines and planes to define the symmetry element
   * associated with the position.
   * 
   * A point if of this position coordinate if it not claimed by a Wyckoff
   * position of higher priority (points before lines, for example) by checking
   * its distance to the symmetry element -- whether it is on the line or in the
   * plane.
   * 
   * 
   * This static class has only three relatively "public" methods (accessed only
   * by WyckoffFinder):
   *
   * 
   * protected boolean contains(WyckoffFinder w, UnitCell uc, P3 p)
   * 
   * protected SB asString(SB sb, boolean withCentering)
   *
   * protected void project(P3 p)
   *
   *
   * The first of these checks to see if a given point is of this coordinate
   * type. It does this by checking a 5x5 range of unit cell offsets around the
   * basic unit cell. This was found to be necessary for some of the hexagonal
   * space groups.
   * 
   * The second returns a parentheses-wrapped string, with added centering if
   * appropriate. The third projects a general position onto this particular
   * orbit coordinate. Jmol uses this to generate a starting position for
   * 
   * MODELKIT ADD C WYCKOFF x
   * 
   * 
   */
  static class WyckoffCoord {

    private final static int TYPE_POINT = 1;
    private final static int TYPE_LINE = 2;
    private final static int TYPE_PLANE = 3;

    /**
     * type of this position; point, line or plane
     */
    private int type;

    /**
     * string representation, for example: "2x,x,11/12"
     */
    private String xyz;

    private String label;

    /**
     * centering discovered to match a point to this position. Only temporary;
     * cleared after first retrieval.
     */
    private transient String thisCentering = "";

    /**
     * 4x4 matrix representation of this position coordinate
     */
    private M4 op;

    /**
     * symmetry element associated with this coordinate.
     * 
     */
    private P3 point;
    private V3 line;
    private P4 plane;

    /**
     * static temporary variables
     */
    private final static P3 p1 = new P3(), p2 = new P3(), p3 = new P3(),
        pc = new P3();
    private final static V3 vt = new V3();

    /**
     * Create and initialize a position coordinate.
     * 
     * @param xyz
     *        "1/8,-y+1/8,y+1/8"
     * @param label
     *        "a", "b", etc.
     */
    WyckoffCoord(String xyz, String label) {
      this.xyz = xyz;
      this.label = label;
      create(xyz);
    }

    public void asStringCentered(P3 centering, M4 tempOp, P3 tempP, SB sb) {
      tempOp.setM4(op);
      tempOp.add(centering);
      tempOp.getTranslation(tempP);
      tempP.x = tempP.x % 1;
      tempP.y = tempP.y % 1;
      tempP.z = tempP.z % 1;
      tempOp.setTranslation(tempP);
      sb.appendC(' ');
      String s = "," + SymmetryOperation.getXYZFromMatrixFrac(tempOp, false,
          true, false, true) + ",";
      s = PT.rep(s, ",,", ",0,");
      s = PT.rep(s, ",+", ",");
      sb.appendC('(').append(s.substring(1, s.length() - 1)).appendC(')');
    }

    /**
     * Check to see if the given point is associated with this Wyckoff position.
     * 
     * @param w
     * @param uc
     * @param p
     * 
     * @return true if claimed
     */
    protected boolean contains(WyckoffFinder w, UnitCell uc, P3 p) {
      float slop = uc.getPrecision();
      thisCentering = null;
      // do a preliminary check
      if (checkLatticePt(p, slop))
        return true;
      if (w.centerings == null)
        return false;
      for (int i = w.centerings.length; --i >= 0;) {
        pc.add2(p, w.centerings[i]);
        uc.unitize(pc);
        if (checkLatticePt(pc, slop)) {
          thisCentering = w.centeringStr[i];
          return true;
        }
      }
      return false;
    }

    /**
     * Project a general position onto this Wyckoff position.
     * 
     * @param p
     */
    protected void project(P3 p) {
      switch (type) {
      case TYPE_POINT:
        p.setT(point);
        break;
      case TYPE_LINE:
        Measure.projectOntoAxis(p, point, line, vt);
        break;
      case TYPE_PLANE:
        Measure.getPlaneProjection(p, plane, vt, vt);
        p.setT(vt);
        break;
      }
    }

    /**
     * Return the parentheses-wrapped string form of this position, possibly
     * with added centering.
     * 
     * @param sb
     * @param withCentering
     * @return sb for continued appending
     */
    protected SB asString(SB sb, boolean withCentering) {
      if (sb == null)
        sb = new SB();
      wrap(xyz, sb);
      if (withCentering && thisCentering != null) {
        sb.appendC('+');
        wrap(thisCentering, sb);
      }
      return sb;
    }

    /**
     * Thoroughly check a 3x3 range of lattice offsets. This is important for
     * groups such as 178 that have oddly directed angles.
     * 
     * Checks 000 first.
     * 
     * @param p
     * @param slop
     *        set false if 000 has been done first
     * @return true if found
     */
    private boolean checkLatticePt(P3 p, float slop) {
      // problem here was with #178 
      // where O1 #1 0.183 4.823 0.915 (0.363 0.682 0.083)
      // needs to match 2x,x,1/12, since 
      // 2*0.682 == 1.364
      if (checkPoint(p, slop))
        return true;
      for (int z = 125 / 2, i = -2; i < 3; i++) {
        for (int j = -2; j < 3; j++) {
          for (int k = -2; k < 3; k++, z--) {
            if (z == 0)
              continue;
            p3.set(i, j, k);
            p3.add(p);
            if (checkPoint(p3, slop)) {
              System.out.println(
                  label + " " + xyz + " found for " + i + " " + j + " " + k);
              return true;
            }
          }
        }
      }
      return false;
    }

    /**
     * Checks a point by measuring its distance to this coordinate's symmetry
     * element.
     * 
     * @param p
     * @param slop
     * @return true if the point is within slop fractional distance to the
     *         element.
     */
    private boolean checkPoint(P3 p, float slop) {
      float d = 1;
      switch (type) {
      case TYPE_POINT:
        // will be unitized
        d = point.distance(p);
        break;
      case TYPE_LINE:
        p1.setT(p);
        Measure.projectOntoAxis(p1, point, line, vt);
        d = p1.distance(p);
        break;
      case TYPE_PLANE:
        d = Math.abs(Measure.getPlaneProjection(p, plane, vt, vt));
        break;
      }
      return d < slop;
    }

    /**
     * Generate the operator matrix and the symmetry elements associated with
     * this coordinate.
     * 
     * @param p
     */
    private void create(String p) {
      int nxyz = (p.indexOf('x') >= 0 ? 1 : 0) + (p.indexOf('y') >= 0 ? 1 : 0)
          + (p.indexOf('z') >= 0 ? 1 : 0);
      float[] a = new float[16];
      String[] v = PT.split(xyz, ",");
      getRow(v[0], a, 0);
      getRow(v[1], a, 4);
      getRow(v[2], a, 8);
      a[15] = 1;
      op = M4.newA16(a);
      switch (nxyz) {
      case 0:
        type = TYPE_POINT;
        point = SymmetryOperation.toPoint(p, null);
        break;
      case 1:
        // just one 
        type = TYPE_LINE;
        p1.set(0.19f, 0.53f, 0.71f);
        op.rotTrans(p1);
        p2.set(0.51f, 0.27f, 0.64f);
        op.rotTrans(p2);
        p2.sub2(p2, p1);
        p2.normalize();
        point = P3.newP(p1);
        line = V3.newV(p2);
        break;
      case 2:
        type = TYPE_PLANE;
        p1.set(0.19f, 0.51f, 0.73f);
        op.rotTrans(p1);
        p2.set(0.23f, 0.47f, 0.86f);
        op.rotTrans(p2);
        p3.set(0.1f, 0.2f, 0.3f);
        op.rotTrans(p3);
        plane = Measure.getPlaneThroughPoints(p1, p2, p3, null, null,
            new P4());
        break;
      case 3:
        // general position
        break;
      }
    }

    /**
     * Fill out a row in the op matrix based on a part of the coordiante string
     * 
     * @param s
     *        "2x-y,y+x,11/12"
     * @param a
     * @param rowpt
     */
    private static void getRow(String s, float[] a, int rowpt) {
      // -x+1/2 => +-x+1/2 => ["","-x","1/2"] 
      // 2x+-3  =>  ["2*x","-3"]
      // 2/3x => ["2/3x"]
      // x-y =>  ["x","-y"]
      s = PT.rep(s, "-", "+-");
      s = PT.rep(s, "x", "*x");
      s = PT.rep(s, "y", "*y");
      s = PT.rep(s, "z", "*z");
      s = PT.rep(s, "-*", "-");
      s = PT.rep(s, "+*", "+");
      String[] part = PT.split(s, "+");
      for (int p = part.length; --p >= 0;) {
        s = part[p];
        if (s.length() == 0)
          continue;
        int pt = 3;
        if (s.indexOf('.') >= 0) {
          float d = PT.parseFloat(s);
          a[rowpt + pt] = d;
          continue;
        }
        int i0 = 0;
        float sgn = 1;
        switch (s.charAt(0)) {
        case '-':
          sgn = -1;
          //$FALL-THROUGH$
        case '*':
          i0++;
          break;
        }
        float v = 0;
        // reverse-scanning wins the day
        for (int i = s.length(), f2 = 0; --i >= i0;) {
          char c = s.charAt(i);
          switch (c) {
          case 'x':
            pt = 0;
            v = 1;
            break;
          case 'y':
            pt = 1;
            v = 1;
            break;
          case 'z':
            pt = 2;
            v = 1;
            break;
          case '/':
            f2 = 1;
            v = 1 / v;
            //$FALL-THROUGH$
          case '*':
            sgn *= v;
            v = 0;
            break;
          default:
            int u = "0123456789".indexOf(c);
            if (u < 0)
              System.err.println("WH ????");
            if (v == 0) {
              v = u;
            } else {
              f2 = (f2 == 0 ? 10 : f2 * 10);
              v += f2 * u;
            }
            break;
          }
        }
        a[rowpt + pt] = sgn * v;
      }
    }

    @Override
    public String toString() {
      return asString(null, false).toString();
    }
  } // end of WyckoffCoord

  /*  Jmol script Test:
  
      function testw(aflowid) {
        if (!aflowid)
          aflowid = "225.1";
        var f = "=aflowlib/" + aflowid; 
  
        // aflow file data
        load @f packed
        print {*}.wyckoff.pivot;
  
        // just by site in the CIF file
        load "" packed filter "nowyckoff" 
        print {*}.wyckoff.pivot; 
  
        // all atoms
        ar = [];
        for (a in all){ ar.push(a.symop("wyckoff")); }
        print ar.pivot;
      }
  
      testw("225.1")
      testw("178.1")
    
  */

}
