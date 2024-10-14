/* $RCSfile$
 * $Author: egonw $
 * $Date: 2005-11-10 09:52:44 -0600 (Thu, 10 Nov 2005) $
 * $Revision: 4255 $
 *
 * Copyright (C) 2003-2005  Miguel, Jmol Development, www.jmol.org
 *
 * Contact: jmol-developers@lists.sf.net
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jmol.util;

import org.jmol.viewer.Viewer;

import javajs.util.AU;
import javajs.util.M4;
import javajs.util.P3;
import javajs.util.P3i;
import javajs.util.P4;
import javajs.util.PT;
import javajs.util.T3;
import javajs.util.T4;
import javajs.util.V3;



/**
 * general-purpose simple unit cell for calculations 
 * and as a super-class of unitcell, which is only part of Symmetry
 * 
 * allows one-dimensional (polymer) and two-dimensional (slab) 
 * periodicity
 * 
 */

public class SimpleUnitCell { 

  public static final int PARAM_STD       = 6;  // a b c alpha beta gamma [0-5]
  public static final int PARAM_VAX       = 6;  // ax ay az [6-8]
  public static final int PARAM_VBX       = 9;  // bx by bz [9-11]
  public static final int PARAM_VCX       = 12; // cx cy cz [12-14]
  public static final int PARAM_VCZ       = 14; // last vector value - test for vabc
  public static final int PARAM_OXYZ      = 15; // ox oy oz [15-17]
  
  public static final int PARAM_M4        = 6;  // m00 m01 ... m44 (16 entries, [6-21)
  public static final int PARAM_M33       = 21; // last matrix value - test for 4x4 matrix
  public static final int PARAM_SUPERCELL = 22; // na nb nc
  public static final int PARAM_SCALE = 25;     // scale
  public static final int PARAM_SLOP = 26;      // slop
  public static final int PARAM_COUNT = 27;

  public final static int INFO_IS_RHOMBOHEDRAL = 9;
  public final static int INFO_IS_HEXAGONAL = 8;  
  public final static int INFO_DIMENSION_TYPE = 7;
  public final static int INFO_DIMENSIONS = 6;
  public final static int INFO_GAMMA = 5;
  public final static int INFO_BETA = 4;
  public final static int INFO_ALPHA = 3;
  public final static int INFO_C = 2;
  public final static int INFO_B = 1;
  public final static int INFO_A = 0;

  public static final int DIMENSION_TYPE_ALL = 0x7;

  public static final String HEX_TO_RHOMB = "2/3a+1/3b+1/3c,-1/3a+1/3b+1/3c,-1/3a-2/3b+1/3c";
  public static final String RHOMB_TO_HEX = "a-b,b-c,a+b+c";

  protected final static double toRadians = Math.PI * 2 / 360;

 /**
   * allowance for rounding in [0,1)
   */
  public final static float SLOPSP = 0.0001f;
  public final static float SLOPDP = 0.000000000001f;
  public static final float SLOP_PARAMS = 0.001f; // generous

  protected float[] unitCellParams;
  
  /**
   * initial value is set in subclass UnitCell
   */
  protected float slop = (Viewer.isHighPrecision ? SLOPDP : SLOPSP);
  
  public float getPrecision() {
    return slop;
  }

  public void setPrecision(float slop) {
    unitCellParams[PARAM_SLOP] = this.slop = (!Float.isNaN(slop) ? slop 
        : !Float.isNaN(unitCellParams[PARAM_SLOP]) ? unitCellParams[PARAM_SLOP] 
            : Viewer.isHighPrecision ? SLOPDP : SLOPSP);
  }

  public M4 matrixCartesianToFractional;
  public M4 matrixFractionalToCartesian;
  protected M4 matrixCtoFNoOffset;
  protected M4 matrixFtoCNoOffset;  
  
  public double volume;
  
  protected int dimension = 3;
  /**
   * 0x07 : z,y,x periodic
   */
  public int dimensionType = DIMENSION_TYPE_ALL;
  
  private P3 fractionalOrigin;
  
  
  private int na, nb, nc;
  
  public boolean isSupercell() {
    return (na > 1 || nb > 1 || nc > 1);
  }

  protected float a, b, c, alpha, beta, gamma;
  protected double cosAlpha, sinAlpha;
  protected double cosBeta, sinBeta;
  protected double cosGamma, sinGamma;
  protected double cA_, cB_;
  protected double a_;
  protected double b_, c_;

  
  public static boolean isValid(float[] parameters) {
    return (parameters != null && (parameters[0] > 0 || parameters.length > PARAM_VCZ
        && !Float.isNaN(parameters[PARAM_VCZ])));
  }

  protected SimpleUnitCell() {
    fractionalOrigin = new P3();
  }

  /**
   * 
   * @param params
   * 
   *        len = 6 [a b c alpha beta gamma]
   * 
   *        len = 6 [a b -1 alpha beta gamma] // slab
   * 
   *        len = 6 [a -1 -1 alpha beta gamma] // polymer
   * 
   *        or len = 15 [-1 -1 -1 -1 -1 -1 va[3] vb[3] vc[3]] // vectors only
   * 
   *        or len = 15 [a -1 -1 -1 -1 -1 va[3] vb[3] vc[3]] // polymer, vectors only
   * 
   *        or len = 15 [a b -1 -1 -1 -1 va[3] vb[3] vc[3]] // slab, vectors only
   * 
   *        or len = 22 [a b c alpha beta gamma m00 m01 .. m33] // matrix included
   * 
   *        and/or len = 25 [...................... na nb nc] // supercell
   * 
   *        and/or len = 26 [...................... na nb nc scale] // scaled supercell
   * 
   *        and/or len = 27 [..................................... slop] // precision
   * @return a simple unit cell
   */
  public static SimpleUnitCell newA(float[] params) {
    SimpleUnitCell c = new SimpleUnitCell();
    c.init(params);
    return c;
  }
  
  /**
   * Allows for abc, ab, or a
   * 
   * @param params
   * @return 1, 2, or 3
   */
  public static int getDimensionFromParams(float[] params) {
    return (params[0] <= 0 ? 3 // given elsewhere
           : params[1] < 0 ? 1 // no b or c
           : params[2] < 0 ? 2 // no c
           : 3);
  }


  protected void init(float[] params) {
    if (params == null)
      params = new float[] { 1, 1, 1, 90, 90, 90 };
    if (!isValid(params))
      return;
    unitCellParams = newParams(params, Float.NaN);
    boolean rotateHex = false; // special gamma = -1 indicates hex rotation for AFLOW

    dimension = getDimensionFromParams(params);
    switch (dimension) {
    case 1:
      dimensionType = 1; // a only
      break;
    case 2:
      dimensionType = 3; // a and b
      break;
    case 3:
      dimensionType = 7; // a, b, and c
      break;
    }
    a = params[0];
    b = params[1];
    c = params[2];
    alpha = params[3];
    beta = params[4];
    gamma = params[5];
    if (gamma == -1 && c > 0) {
      rotateHex = true;
      gamma = 120;
    }
    if (params.length > PARAM_SLOP) {
      if (Float.isNaN(params[PARAM_SLOP])) {
        params[PARAM_SLOP] = slop;
      } else {
        slop = params[PARAM_SLOP];
      }
    }

    // (int) double.NaN == 0 (but not in JavaScript!)
    // supercell
    float fa = na = Math.max(1,
        params.length > PARAM_SUPERCELL + 2
            && !Float.isNaN(params[PARAM_SUPERCELL])
                ? (int) params[PARAM_SUPERCELL]
                : 1);
    float fb = nb = Math.max(1,
        params.length > PARAM_SUPERCELL + 2
            && !Float.isNaN(params[PARAM_SUPERCELL + 1])
                ? (int) params[PARAM_SUPERCELL + 1]
                : 1);
    float fc = nc = Math.max(1,
        params.length > PARAM_SUPERCELL + 2
            && !Float.isNaN(params[PARAM_SUPERCELL + 2])
                ? (int) params[PARAM_SUPERCELL + 2]
                : 1);
    if (params.length > PARAM_SCALE && !Float.isNaN(params[PARAM_SCALE])) {
      float fScale = params[PARAM_SCALE];
      if (fScale > 0) {
        fa *= fScale;
        fb *= fScale;
        fc *= fScale;
      }
    } else {
      fa = fb = fc = 1;
    }

    if (a <= 0 && c <= 0) {
      // must calculate a, b, c alpha beta gamma from Cartesian vectors;
      V3 va = newV(params, PARAM_VAX);
      V3 vb = newV(params, PARAM_VBX);
      V3 vc = newV(params, PARAM_VCX);
      setABC(va, vb, vc);
      if (c < 0) {
        float[] n = AU.arrayCopyF(params, -1);
        if (b < 0) {
          vb.set(0, 0, 1);
          vb.cross(vb, va);
          if (vb.length() < 0.001f)
            vb.set(0, 1, 0);
          vb.normalize();
          n[9] = vb.x;
          n[10] = vb.y;
          n[11] = vb.z;
        }
        if (c < 0) {
          vc.cross(va, vb);
          vc.normalize();
          n[12] = vc.x;
          n[13] = vc.y;
          n[PARAM_VCZ] = vc.z;
        }
        params = n;
      }
    }

    // checking here for still a dimension issue with b or c
    // was < 0 above; here <= 0
    a *= fa;
    if (b <= 0) {
      b = c = 1;
    } else if (c <= 0) {
      c = 1;
      b *= fb;
    } else {
      b *= fb;
      c *= fc;
    }
    setCellParams();

    if (params.length > PARAM_M33 && !Float.isNaN(params[PARAM_M33])) {
      // parameters with a 4x4 matrix
      // [a b c alpha beta gamma m00 m01 m02 m03 m10 m11.... m20...]
      // this is for PDB and CIF reader
      float[] scaleMatrix = new float[16];
      for (int i = 0; i < 16; i++) {
        float f;
        switch (i % 4) {
        case 0:
          f = fa;
          break;
        case 1:
          f = fb;
          break;
        case 2:
          f = fc;
          break;
        default:
          f = 1;
          break;
        }
        scaleMatrix[i] = params[PARAM_M4 + i] * f;
      }
      matrixCartesianToFractional = M4.newA16(scaleMatrix);
      matrixCartesianToFractional.getTranslation(fractionalOrigin);
      matrixFractionalToCartesian = M4.newM4(matrixCartesianToFractional)
          .invert();
      if (params[0] == 1)
        setParamsFromMatrix();
    } else if (params.length > PARAM_VCZ && !Float.isNaN(params[PARAM_VCZ])) {
      // parameters with a 3 vectors
      // [a b c alpha beta gamma ax ay az bx by bz cx cy cz...]
      M4 m = matrixFractionalToCartesian = new M4();
      m.setColumn4(0, params[PARAM_VAX] * fa, params[PARAM_VAX + 1] * fa,
          params[PARAM_VAX + 2] * fa, 0);
      m.setColumn4(1, params[PARAM_VBX] * fb, params[PARAM_VBX + 1] * fb,
          params[PARAM_VBX + 2] * fb, 0);
      m.setColumn4(2, params[PARAM_VCX] * fc, params[PARAM_VCX + 1] * fc,
          params[PARAM_VCX + 2] * fc, 0);
      if (params.length > PARAM_OXYZ + 2 && !Float.isNaN(params[PARAM_OXYZ + 2])) {
        // cartesian offset
        m.setColumn4(3, params[PARAM_OXYZ], params[PARAM_OXYZ + 1],
            params[PARAM_OXYZ + 2], 1);
      } else {
        m.setColumn4(3, 0, 0, 0, 1);
      }
      matrixCartesianToFractional = M4.newM4(matrixFractionalToCartesian)
          .invert();
    } else {
      M4 m = matrixFractionalToCartesian = new M4();

      if (rotateHex) {
        // 1, 2. align a and b symmetrically about the x axis (AFLOW)
        m.setColumn4(0, (float) (-b * cosGamma), (float) (-b * sinGamma), 0, 0);
        // 2. place the b is in xy plane making a angle gamma with a
        m.setColumn4(1, (float) (-b * cosGamma), (float) (b * sinGamma), 0, 0);
      } else {
        // 1. align the a axis with x axis
        m.setColumn4(0, a, 0, 0, 0);
        // 2. place the b is in xy plane making a angle gamma with a
        m.setColumn4(1, (float) (b * cosGamma), (float) (b * sinGamma), 0, 0);
      }
      // 3. now the c axis,
      // http://server.ccl.net/cca/documents/molecular-modeling/node4.html
      m.setColumn4(2, (float) (c * cosBeta),
          (float) (c * (cosAlpha - cosBeta * cosGamma) / sinGamma),
          (float) (volume / (a * b * sinGamma)), 0);
      m.setColumn4(3, 0, 0, 0, 1);
      matrixCartesianToFractional = M4.newM4(matrixFractionalToCartesian)
          .invert();
    }
    matrixCtoFNoOffset = matrixCartesianToFractional;
    matrixFtoCNoOffset = matrixFractionalToCartesian;
  }

  private static V3 newV(float[] p, int i) {
    return V3.new3(p[i++], p[i++],p[i]);
  }

  public static float[] newParams(float[] params, float slop) {
    float[] p = new float[PARAM_COUNT];
    int n = params.length;
    for (int i = 0; i < PARAM_COUNT; i++)
      p[i] = (i < n ? params[i] : Float.NaN);
    if (n < PARAM_COUNT)
      p[PARAM_SLOP] = slop;
    return p;
  }

  public static void addVectors(float[] params) {
    SimpleUnitCell c = SimpleUnitCell.newA(params);
    M4 m = c.matrixFractionalToCartesian;
    for (int i = 0; i < 9; i++)
    params[PARAM_M4 + i] = m.getElement(i%3, i/3);
  }


  private void setParamsFromMatrix() {
    V3 va = V3.new3(1,  0,  0);
    V3 vb = V3.new3(0,  1,  0);
    V3 vc = V3.new3(0,  0,  1);
    matrixFractionalToCartesian.rotate(va);
    matrixFractionalToCartesian.rotate(vb);
    matrixFractionalToCartesian.rotate(vc);
    setABC(va, vb, vc);
    setCellParams();
  }

  private void setABC(V3 va, V3 vb, V3 vc) {
    fillParams(va, vb, vc, unitCellParams);
    float[] p = unitCellParams;
    a = p[0];
    b = p[1];
    c = p[2];
    alpha = p[3];
    beta = p[4];
    gamma = p[5];
  }

  public static void fillParams(V3 va, V3 vb, V3 vc, float[] p) {
    if (va == null) {
      va = newV(p, PARAM_VAX);
      vb = newV(p, PARAM_VBX);
      vc = newV(p, PARAM_VCX);
    }
    float a = va.length();
    float b = vb.length();
    float c = vc.length();
    if (a == 0)
      return;
    if (b == 0)
      b = c = -1; //polymer
    else if (c == 0)
      c = -1; //slab
    p[0] = a;
    p[1] = b;
    p[2] = c;
    p[3] = (float) (b < 0 || c < 0 ? 90 : vb.angle(vc) / toRadians);
    p[4] = (float) (c < 0 ? 90 : va.angle(vc) / toRadians);
    p[5] = (float) (b < 0 ? 90 : va.angle(vb) / toRadians);
  }

  private void setCellParams() {
    cosAlpha = Math.cos(toRadians * alpha);
    sinAlpha = Math.sin(toRadians * alpha);
    cosBeta = Math.cos(toRadians * beta);
    sinBeta = Math.sin(toRadians * beta);
    cosGamma = Math.cos(toRadians * gamma);
    sinGamma = Math.sin(toRadians * gamma);
    double unitVolume = Math.sqrt(sinAlpha * sinAlpha + sinBeta * sinBeta
        + sinGamma * sinGamma + 2.0 * cosAlpha * cosBeta * cosGamma - 2);
    volume = a * b * c * unitVolume;
    // these next few are for the B' calculation
    cA_ = (cosAlpha - cosBeta * cosGamma) / sinGamma;
    cB_ = unitVolume / sinGamma;
    a_ = b * c * sinAlpha / volume;
    b_ = a * c * sinBeta / volume;
    c_ = a * b * sinGamma / volume;
  }

  /**
   * Get the fractional origin for the UccageRenderer.
   * 
   * NOTE: This is NOT a copy
   * 
   * @return fractionalOrigin
   */
  public P3 getFractionalOrigin() {
    return fractionalOrigin;
  }

  /**
   * convenience return only after changing fpt
   * 
   * @param fpt
   * @return adjusted fpt
   */
  public P3 toSupercell(P3 fpt) {
    fpt.x /= na;
    fpt.y /= nb;
    fpt.z /= nc;
    return fpt;
  }

  public final void toCartesian(T3 pt, boolean ignoreOffset) {
    if (matrixFractionalToCartesian != null)
      (ignoreOffset ? matrixFtoCNoOffset : matrixFractionalToCartesian)
          .rotTrans(pt);
  }

  public void toFractionalM(M4 m) {
    if (matrixCartesianToFractional == null)
      return;
    m.mul(matrixFractionalToCartesian);
    m.mul2(matrixCartesianToFractional, m);
  }
  
  public final void toFractional(T3 pt, boolean ignoreOffset) {
    if (matrixCartesianToFractional == null)
      return;
    (ignoreOffset ? matrixCtoFNoOffset : matrixCartesianToFractional)
        .rotTrans(pt);
  }

  public boolean isPolymer() {
    return (dimension == 1);
  }

  public boolean isSlab() {
    return (dimension == 2);
  }

  public final float[] getUnitCellParams() {
    return unitCellParams;
  }

  public final float[] getUnitCellAsArray(boolean vectorsOnly) {
    M4 m = matrixFractionalToCartesian;
    return (vectorsOnly ? new float[] { 
        m.m00, m.m10, m.m20, // Va
        m.m01, m.m11, m.m21, // Vb
        m.m02, m.m12, m.m22, // Vc
      } 
      : new float[] { 
        a, b, c, alpha, beta, gamma, 
        m.m00, m.m10, m.m20, // Va
        m.m01, m.m11, m.m21, // Vb
        m.m02, m.m12, m.m22, // Vc
        dimension, (float) volume,
      } 
    );
  }

  public final float getInfo(int infoType) {
    switch (infoType) {
    case INFO_A:
      return a;
    case INFO_B:
      return b;
    case INFO_C:
      return c;
    case INFO_ALPHA:
      return alpha;
    case INFO_BETA:
      return beta;
    case INFO_GAMMA:
      return gamma;
    case INFO_DIMENSIONS:
      return dimension;
    case INFO_DIMENSION_TYPE:
      return dimensionType;
    case INFO_IS_HEXAGONAL:
      return (isHexagonal(unitCellParams) ? 1 : 0);
    case INFO_IS_RHOMBOHEDRAL:
      return (isRhombohedral(unitCellParams) ? 1 : 0);      
    }
    return Float.NaN;
  }

  /**
   * Generate the reciprocal unit cell, scaled as desired
   * 
   * @param abc [a,b,c] or [o,a,b,c]
   * @param ret
   * @param scale 0 for 2pi, teneral reciprocal lattice
   * @return oabc
   */
  public static T3[] getReciprocal(T3[] abc, T3[] ret, float scale) {
    int off = (abc.length == 4 ? 1 : 0);
    P3[] rabc = new P3[4];
    rabc[0] = (off == 1 ? P3.newP(abc[0]) : new P3()); // origin
    if (scale == 0)
      scale = (float) (2 * Math.PI);
    // a' = 2pi/V * b x c  = 2pi * (b x c) / (a . (b x c))
    // b' = 2pi/V * c x a 
    // c' = 2pi/V * a x b 
    for (int i = 0; i < 3; i++) {
      P3 v = rabc[i + 1] = new P3();
      v.cross(abc[((i + 1) % 3) + off], abc[((i + 2) % 3) + off]);
      float vol = abc[i + off].dot(v);
      if (scale == -1)
        scale = (float) Math.sqrt(vol);
      v.scale(scale / vol);
    }
    if (ret == null)
      return rabc;
    for (int i = 0; i < 4; i++) {
      ret[i] = rabc[i];
    }
    return ret;
  }

  /**
   * set cell vectors by string. Does not set origin.
   * 
   * @param abcabg "a=...,b=...,c=...,alpha=...,beta=..., gamma=..." or null  
   * @param params to use if not null 
   * @param ucnew  to create and return; null if only to set params
   * @return T3[4] ucnew as [origin, a, b, c], with origin unchanged or {0 0 0}
   */
  public static T3[] setAbc(String abcabg, float[] params, T3[] ucnew) {
    if (abcabg != null) {
      if (params == null)
        params = new float[6];
      String[] tokens = PT.split(abcabg.replace(',', '='), "=");
      if (tokens.length >= 12)
        for (int i = 0; i < 6; i++)
          params[i] = PT.parseFloat(tokens[i * 2 + 1]);
    }
    if (ucnew == null)
      return null;
    return setAbcFromParams(params, ucnew);
  }

  public static T3[] setAbcFromParams(float[] params, T3[] ucnew) {
    float[] f = newA(params).getUnitCellAsArray(true);
    ucnew[1].set(f[0], f[1], f[2]);
    ucnew[2].set(f[3], f[4], f[5]);
    ucnew[3].set(f[6], f[7], f[8]);
    return ucnew;
  }

  /**
   * Used for just about everything; via UnitCell
   * including SpaceGroup.getSiteMultiplicity, Symmetry.getInvariantSymops, 
   * Symmetry.removeDuplicates, Symmetry.unitize, Symmetry.toUnitCell, SymmetryDesc.getTransform.
   * 
   * Low precision here unless SET HIGHPRECISION TRUE has been issued.
   * 
   * @param dimension
   * @param pt
   */
  public void unitizeDim(int dimension, T3 pt) {
    switch (dimension) {
    case 3:
      pt.z = unitizeX(pt.z, slop);  
      //$FALL-THROUGH$
    case 2:
      pt.y = unitizeX(pt.y, slop);
      //$FALL-THROUGH$
    case 1: 
      pt.x = unitizeX(pt.x, slop);
	    }
	  }

  /**
   * Only used for getting equivalent points and checking for duplicates. 
   * Presumption here is that two points should not be close together
   * regardless of lattice distances.
   * 
   * @param dimension
   * @param pt
   * @param slop 
   */
  public static void unitizeDimRnd(int dimension, T3 pt, float slop) {
    switch (dimension) {
    case 3:
      pt.z = unitizeXRnd(pt.z, slop);  
      //$FALL-THROUGH$
    case 2:
      pt.y = unitizeXRnd(pt.y, slop);
      //$FALL-THROUGH$
    case 1:
      pt.x = unitizeXRnd(pt.x, slop);
    }
  }

  public static float unitizeX(float x, float slop) {
	    // introduced in Jmol 11.7.36
    x = (float) (x - Math.floor(x));
    // question - does this cause problems with dragatom?
    if (x > 1 - slop || x < slop)  // 0.9999, 0.0001 was just too tight ams/jolliffeite
      x = 0;
    return x;
  }

  /**
   * Slightly higher precision -- only used for cmdAssignSpaceGroup checking for duplicates
   * @param x
   * @param slop 
   * @return rounded value +/-slop
   */
  public static float unitizeXRnd(float x, float slop) {
    // introduced in Jmol 11.7.36
    x = (x - (float) Math.floor(x));
    if (x > 1 - slop || x < slop) 
      x = 0;
    return x;
  }
  
  public int twelfthsOf(float f) {
    if (f == 0)
      return 0;
    f = Math.abs(f * 12);
    int i = Math.round(f);
    return (i <= 12 && Math.abs(f - i) < slop * 12 ? i : -1);
  }
  
  public void twelfthify(P3 pt) {
    switch (dimension) {
    case 3:
      pt.z = setTwelfths(pt.z);
      //$FALL-THROUGH$
    case 2:
      pt.y = setTwelfths(pt.y);
      //$FALL-THROUGH$
    case 1:
      pt.x = setTwelfths(pt.x);
      break;
    }
  }

  private float setTwelfths(float x) {
    int i = twelfthsOf(x);
    return (i >= 0 ? i / 12f * (x < 0 ? -1 : 1) : x);
  }




  ////// lattice methods //////
  
  /**
   * Expanded cell notation:
   * 
   * 111 - 1000 --> center 5,5,5; range 0 to 9 or -5 to +4
   * 
   * 1000000 - 1999999 --> center 50,50,50; range 0 to 99 or -50 to +49
   * 1000000000 - 1999999999 --> center 500, 500, 500; range 0 to 999 or -500 to
   * +499
   * 
   * for example, a 3x3x3 block of 27 cells:
   * 
   * {444 666 1} or {1494949 1515151 1} or {1499499499 1501501501 1}
   * 
   * @param nnn
   * @param cell
   * @param offset
   *        0 or 1 typically; < 0 means "apply no offset"
   * @param kcode
   *        Generally the multiplier is just {ijk ijk scale}, but when we have
   *        1iiijjjkkk 1iiijjjkkk scale, floats lose kkk due to Java float
   *        precision issues so we use P4 {1iiijjjkkk 1iiijjjkkk scale,
   *        1kkkkkk}. Here, our offset -- initially 0 or 1 from the uccage
   *        renderer, but later -500 or -499 -- tells us which code we are
   * 
   */
  public static void ijkToPoint3f(int nnn, P3 cell, int offset, int kcode) {
    int f = (nnn > 1000000000 ? 1000 : nnn > 1000000 ? 100 : 10);
    int f2 = f * f;
    offset -= (offset >= 0 ? 5 * f / 10 : offset);
    cell.x = ((nnn / f2) % f) + offset;
    cell.y = (nnn % f2) / f + offset;
    cell.z = (kcode == 0 ? nnn % f 
        : (offset == -500 ? kcode / f : kcode) % f) + offset;
  }
  

  /**
   * Convert user's {3 2 1} to {1500500500, 1503502501, 0 or 1, 1500501}
   * @param pt
   * @param scale 1 for block of unit cells; 0 for one large supercell
   * @return converted P4
   */
  public static P4 ptToIJK(T3 pt, int scale) {
    if (pt.x <= 5 && pt.y <= 5 && pt.z <= 5) {
      return P4.new4(555, (pt.x + 4) * 100 + (pt.y + 4) * 10 + pt.z + 4, scale, 0);
    } 
    int i555 = 1500500500;
    return P4.new4(i555, i555 + pt.x*1000000 + pt.y * 1000 + pt.z, scale, 1500500 + pt.z);
  }

  /**
   * Generally the multiplier is just {ijk ijk scale}, but when we have
   * 1iiijjjkkk 1iiijjjkkk scale, floats lose kkk due to Java float precision
   * issues so we use P4 {1iiijjjkkk 1iiijjjkkk scale, 1kkkkkk}
   * 
   * @param pt
   * @return String representation for state
   */
  public static String escapeMultiplier(T3 pt) {
    if (pt instanceof P4) {
      P4 pt4 = (P4) pt;
      int x = (int) Math.floor(pt4.x / 1000)*1000 
                  + (int) Math.floor(pt4.w / 1000) - 1000;
      int y = (int) Math.floor(pt4.y / 1000)*1000 
          + (int) Math.floor(pt4.w) % 1000;
      return "{" + x + " " + y + " " + pt.z + "}"; 
    }
    return Escape.eP(pt);
  }

  /**
   * 
   * @param dimension
   * @param minXYZ
   * @param maxXYZ
   * @param kcode
   *        Generally the multiplier is just {ijk ijk scale}, but when we have
   *        1iiijjjkkk 1iiijjjkkk scale, floats lose kkk due to Java float
   *        precision issues so we use P4 {1iiijjjkkk 1iiijjjkkk scale,
   *        1kkkkkk}. Here, our offset -- initially 0 or 1 from the uccage
   *        renderer, but later -500 or -499 -- tells us which code we are
   *        looking at, the first one or the second one.
   */
  public static void setMinMaxLatticeParameters(int dimension, P3i minXYZ, P3i maxXYZ, int kcode) {
    
    if (maxXYZ.x <= maxXYZ.y && maxXYZ.y >= 555) {
      //alternative format for indicating a range of cells:
      //{111 666}
      //555 --> {0 0 0}
      P3 pt = new P3();
      ijkToPoint3f(maxXYZ.x, pt, 0, kcode);
      minXYZ.x = (int) pt.x;
      minXYZ.y = (int) pt.y;
      minXYZ.z = (int) pt.z;
      ijkToPoint3f(maxXYZ.y, pt, 1, kcode);
      //555 --> {1 1 1}
      maxXYZ.x = (int) pt.x;
      maxXYZ.y = (int) pt.y;
      maxXYZ.z = (int) pt.z;
    }
    switch (dimension) {
    case 1: // polymer
      minXYZ.y = 0;
      maxXYZ.y = 1;
      //$FALL-THROUGH$
    case 2: // slab
      minXYZ.z = 0;
      maxXYZ.z = 1;
    }
  }
  
  /**
   * @param params
   * @return true if approximately hexagonal
   */
  public static boolean isHexagonal(float[] params) {
    // a == b && alpha = beta = 90 && gamma = 120 (gamma -1 is a "rotateHexCell" indicator for AFLOW
    return (approx0(params[0] - params[1]) 
        && approx0(params[3] - 90) && approx0(params[4] - 90) && (approx0(params[5] - 120) 
            || params[5] == -1));
  }

  /**
   * @param params
   * @return true if approximately rhombohedral
   */
  public static boolean isRhombohedral(float[] params) {
    // a = b = c and alpha = beta = gamma and alpha != 90
    return (approx0(params[0] - params[1]) && approx0(params[1] - params[2])
        && !approx0(params[3] - 90) && approx0(params[3] - params[4])
        && approx0(params[4] - params[5]));
  }

  /**
   * 
   * @param f
   * @return true or false
   */
  public static boolean approx0(float f) {
    return (Math.abs(f) < SLOP_PARAMS);
  }
  
  public static int getCellRange(T3 fset, P3[] cellRange) {
	    int t3w = (fset instanceof T4 ? (int) ((T4) fset).w : 0);
	    SimpleUnitCell.ijkToPoint3f((int) fset.x, cellRange[0], 0, t3w);
	    SimpleUnitCell.ijkToPoint3f((int) fset.y, cellRange[1], 1, t3w);
	    if (fset.z < 0) {
	      cellRange[0].scale(-1 / fset.z);
	      cellRange[1].scale(-1 / fset.z);
	    }
	    return t3w;
	  }

  public static float parseCalc(Viewer vwr, String[] functions, String s) {
    String[] parts;
    float d = PT.parseFloatStrict(s);
    if (!Double.isNaN(d))
      return d;
    s = s.toLowerCase();
    if (functions != null && s.indexOf('(') >= 0) {
      parts = PT.split(s, "(");
      for (int i = parts.length - 1; --i >= 0;) {
        String p = parts[i];
        String f = null;
        for (int j = functions.length; --j >= 0;) {
          if (p.endsWith(functions[j])) {
            f = functions[j];
            break;
          }
        }
        if (f == null) {
          System.err.println("Unrecognized function " + s);
          parts[i] += "?";
        }
      }
      s = PT.join(parts, '(', 0);
    }
    // make sure all / are decimal 1/2 -> 1./2
    if (s.indexOf('/') >= 0) {
      parts = PT.split(s, "/");
      for (int i = parts.length - 1; --i >= 0;) {
        String p = parts[i];
        boolean haveDecimal = false;
        boolean haveDigit = false;
        for (int j = p.length(); --j >= 0;) {
          char c = p.charAt(j);
          if (c == '.') {
            haveDecimal = true;
            break;
          } else if (c == ')') {
            if (haveDigit) {
              return Float.NaN;              
            }
            parts[i] += "*1.0";
            break;
          }
          if (!PT.isDigit(c)) {
            break;
          }
          haveDigit = true;
        }
        if (haveDigit && !haveDecimal)
          parts[i] += ".";
      }
      s = PT.join(parts, '/', 0);
    }
    return vwr.evaluateExpressionAsVariable(s).asFloat();
  }

  
  @Override
  public String toString() {
    return "[" + a + " " + b + " " + c + " " + alpha + " " + beta + " " + gamma + "]";
  }

}
