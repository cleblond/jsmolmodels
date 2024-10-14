
/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2014-01-10 09:19:33 -0600 (Fri, 10 Jan 2014) $
 * $Revision: 19162 $
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
 *  Lesser General License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jmol.adapter.smarter;

import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import org.jmol.api.JmolModulationSet;
import org.jmol.symmetry.SpaceGroup;
import org.jmol.symmetry.Symmetry;
import org.jmol.symmetry.SymmetryOperation;
import org.jmol.symmetry.UnitCell;
import org.jmol.util.BSUtil;
import org.jmol.util.Logger;
import org.jmol.util.SimpleUnitCell;
import org.jmol.util.Tensor;
import org.jmol.util.Vibration;
import org.jmol.viewer.JC;
import org.jmol.viewer.Viewer;

import javajs.util.BS;
import javajs.util.Lst;
import javajs.util.M3;
import javajs.util.M4;
import javajs.util.Matrix;
import javajs.util.P3;
import javajs.util.P3i;
import javajs.util.PT;
import javajs.util.SB;
import javajs.util.T3;
import javajs.util.V3;

/**
 * 
 * A class used by AtomSetCollection for building the symmetry of a model and
 * generating new atoms based on that symmetry.
 * 
 */
public class XtalSymmetry {

  /**
   * A class only used by adapter.smarter.XtalSymmetry while building the
   * file-based model.
   */
  public static class FileSymmetry extends Symmetry {

    public FileSymmetry() {
      // for Class.forName()
    }

    //    public void setTimeReversal(int op, int val) {
    //      spaceGroup.operations[op].setTimeReversal(val);
    //    }
    //  
    public boolean addLatticeVectors(Lst<float[]> lattvecs) {
      return spaceGroup.addLatticeVectors(lattvecs);
    }

    /**
     * @param code
     * @param rs
     *        is a full (3+d)x(3+d) array of epsilons
     * @param vs
     *        is a (3+d)x(1) array of translations
     * @param sigma
     * @return Jones-Faithful representation
     */
    public String addSubSystemOp(String code, Matrix rs, Matrix vs,
                                 Matrix sigma) {
      spaceGroup.isSSG = true;
      String s = SymmetryOperation.getXYZFromRsVs(rs, vs, false);
      int i = spaceGroup.addSymmetry(s, -1, true);
      spaceGroup.matrixOperations[i].setSigma(code, sigma);
      return s;
    }

    public boolean checkDistance(P3 f1, P3 f2, float distance, float dx,
                                 int iRange, int jRange, int kRange,
                                 P3 ptOffset) {
      return unitCell.checkDistance(f1, f2, distance, dx, iRange, jRange,
          kRange, ptOffset);
    }

    /**
     * 
     * @param desiredSpaceGroupIndex
     * @param name
     * @param data
     *        a Lst<SymmetryOperation> or Lst<M4>
     * @param modDim
     *        in [3+d] modulation dimension
     * @return true if a known space group
     */
    public boolean createSpaceGroup(int desiredSpaceGroupIndex, String name,
                                    Object data, int modDim) {
      spaceGroup = SpaceGroup.createSpaceGroup(desiredSpaceGroupIndex, name,
          data, modDim);
      if (spaceGroup != null && Logger.debugging)
        Logger.debug("using generated space group " + spaceGroup.dumpInfo());
      return spaceGroup != null;
    }

    public String fcoord(T3 p) {
      return SymmetryOperation.fcoord(p, " ");
    }

    /**
     * MMCifReader only
     * 
     * @param xyz
     * @param rotTransMatrix
     */
    public void getMatrixFromString(String xyz, float[] rotTransMatrix) {
      SymmetryOperation.getMatrixFromString(null, xyz, rotTransMatrix, false,
          true, false);
    }

    public String getSpaceGroupOperationCode(int iOp) {
      return spaceGroup.matrixOperations[iOp].subsystemCode;
    }

    public Tensor getTensor(Viewer vwr, float[] parBorU) {
      if (parBorU == null)
        return null;
      if (unitCell == null)
        unitCell = UnitCell.fromParams(new float[] { 1, 1, 1, 90, 90, 90 },
            true, SimpleUnitCell.SLOPSP);
      return unitCell.getTensor(vwr, parBorU);
    }

    public String getSpaceGroupTitle() {
      String s = getSpaceGroupName();
      return (s.startsWith("cell=") ? s
          : spaceGroup != null ? spaceGroup.asString()
              : unitCell != null && unitCell.name.length() > 0
                  ? "cell=" + unitCell.name
                  : "");
    }

    public void setPrecision(float prec) {
      unitCell.setPrecision(prec);
    }

    public void toFractionalM(M4 m) {
      if (!isBio)
        unitCell.toFractionalM(m);
    }

    public void toUnitCellRnd(T3 pt, T3 offset) {
      unitCell.toUnitCellRnd(pt, offset);
    }

    public void twelfthify(P3 pt) {
      unitCell.twelfthify(pt);
    }

    /**
     * return a conventional lattice from a primitive
     * 
     * @param latticeType
     *        "A" "B" "C" "R" etc.
     * @param primitiveToCrystal
     * @return [origin va vb vc]
     */
    T3[] getConventionalUnitCell(String latticeType, M3 primitiveToCrystal) {
      return (unitCell == null || latticeType == null ? null
          : unitCell.getConventionalUnitCell(latticeType, primitiveToCrystal));
    }

    int getSpaceGroupIndex() {
      return spaceGroup.getIndex();
    }

    /**
     * Get the actual fractional-to-Cartesian array;
     * 
     * @return first three rows of this matrix
     */
    float[][] getUnitCellF2C() {
      return unitCell.getF2C();
    }

    public void addInversion() {
      SymmetryOperation[] ops = spaceGroup.matrixOperations;
      M4 inv = new M4();
      inv.m00 = inv.m11 = inv.m22 = -1;
      inv.m33 = 1;
      int n = getSpaceGroupOperationCount();
      M4 m = new M4();
      for (int i = 0; i < n; i++) {
        m.mul2(inv, ops[i]);
        String s = SymmetryOperation.getXYZFromMatrix(m, true, true, false);
        addSpaceGroupOperation(s, 0);
      }
    }

  }

  private static final float MAX_INTERCHAIN_BOND_2 = 25; // allowing for hydrogen bonds
  private final static float MINIMUM_FRACTIONAL_ATOM_DISTANCE = 0.0001f;
  private final static int PARTICLE_CHAIN = 1;

  private final static int PARTICLE_NONE = 0;
  private final static int PARTICLE_SYMOP = 2;
  private static final float SQUARED_CARTESIAN_DISTANCE_CHECK_NOOPS = 0.0001f;

  private static final float SQUARED_CARTESIAN_DISTANCE_CHECK_OPS = 0.01f;

  private AtomSetCollectionReader acr;
  private boolean applySymmetryToBonds;

  private AtomSetCollection asc;

  /**
   * the Symmetry that was in place prior to any supercell or subgroup business;
   * used by CIF and Jana readers to reset after subgroup processing
   * 
   */
  private FileSymmetry baseSymmetry;
  private int bondCount0;
  private SB bondsFound = new SB();
  private boolean centroidPacked;

  private boolean checkAll;

  private boolean checkNearAtoms;
  /**
   * CrystalReader only; indicates that this is not just an input file. The
   * issue here is that although the space group is full and has many
   * operations, only the lattice operations are of importance here. These come
   * in blocks, so we just check for one of these using modulus.
   */
  private boolean crystalReaderLatticeOpsOnly;
  private Map<Integer, Character> disorderMap;

  private int disorderMapMax;
  private boolean doCentroidUnitCell;
  private boolean doNormalize = true;
  private boolean doPackUnitCell;

  private String filterSymop;
  private int firstAtom;

  private int[] latticeCells;

  /**
   * the first centering op (such as (x+1/2, y+1/2, z+1/2), assuming these come
   * in blocks
   */
  private int latticeOp;
  private M4 mident;

  private P3i minXYZ, maxXYZ;
  private P3 minXYZ0, maxXYZ0;

  private M3 mTemp;
  private int ndims = 3;

  private int noSymmetryCount;

  private int nVib;

  //private P3 unitCellOffset;

  private float packingRange;
  private final P3 ptOffset = new P3();

  private P3 ptTemp;
  /**
   * range minima and maxima -- also usedf for cartesians comparisons
   * 
   */
  private float rminx, rminy, rminz, rmaxx, rmaxy, rmaxz;
  /**
   * the initial and main Symmetry object
   */
  private FileSymmetry symmetry;

  private float symmetryRange;
  private Lst<float[]> trajectoryUnitCells;

  private float[] unitCellParams = null;
  private V3[] unitCellTranslations;

  public XtalSymmetry() {
    // for reflection
  }

  public Tensor addRotatedTensor(Atom a, Tensor t, int iSym, boolean reset,
                                 FileSymmetry symmetry) {
    if (ptTemp == null) {
      ptTemp = new P3();
      mTemp = new M3();
    }
    return a.addTensor(
        ((Tensor) acr.getInterface("org.jmol.util.Tensor")).setFromEigenVectors(
            symmetry.rotateAxes(iSym, t.eigenVectors, ptTemp, mTemp),
            t.eigenValues, t.isIsotropic ? "iso" : t.type, t.id, t),
        null, reset);
  }

  @SuppressWarnings("unchecked")
  public void applySymmetryBio(Map<String, Object> thisBiomolecule,
                               boolean applySymmetryToBonds, String filter) {
    Lst<M4> biomts = (Lst<M4>) thisBiomolecule.get("biomts");
    int len = biomts.size();
    // this was a bad idea - biomt can be just the identity matrix, in which case this is just a selection set -- 7OJP
    //    if (len < 2)
    //      return;
    if (mident == null) {
      mident = new M4();
      mident.setIdentity();
    }
    acr.lstNCS = null; // disable NCS
    setLatticeCells();
    int[] lc = (latticeCells != null && latticeCells[0] != 0 ? new int[3]
        : null);
    if (lc != null)
      for (int i = 0; i < 3; i++)
        lc[i] = latticeCells[i];
    latticeCells = null;

    String bmChains = acr.getFilterWithCase("BMCHAINS");
    int fixBMChains = (bmChains == null ? -1
        : bmChains.length() < 2 ? 0 : PT.parseInt(bmChains.substring(1)));
    if (fixBMChains == Integer.MIN_VALUE) {
      fixBMChains = -(int) bmChains.charAt(1);
    }
    int particleMode = (filter.indexOf("BYCHAIN") >= 0 ? PARTICLE_CHAIN
        : filter.indexOf("BYSYMOP") >= 0 ? PARTICLE_SYMOP : PARTICLE_NONE);
    doNormalize = false;
    Lst<String> biomtchains = (Lst<String>) thisBiomolecule.get("chains");
    (symmetry = new FileSymmetry()).setSpaceGroup(doNormalize);
    addSpaceGroupOperation("x,y,z", false);
    String name = (String) thisBiomolecule.get("name");
    setAtomSetSpaceGroupName(acr.sgName = name);
    this.applySymmetryToBonds = applySymmetryToBonds;
    bondCount0 = asc.bondCount;
    firstAtom = asc.getLastAtomSetAtomIndex();
    int atomMax = asc.ac;
    Map<Integer, BS> ht = new Hashtable<Integer, BS>();
    int nChain = 0;
    Atom[] atoms = asc.atoms;
    boolean addBonds = (bondCount0 > asc.bondIndex0 && applySymmetryToBonds);
    switch (particleMode) {
    case PARTICLE_CHAIN:
      for (int i = atomMax; --i >= firstAtom;) {
        Integer id = Integer.valueOf(atoms[i].chainID);
        BS bs = ht.get(id);
        if (bs == null) {
          nChain++;
          ht.put(id, bs = new BS());
        }
        bs.set(i);
      }
      asc.bsAtoms = new BS();
      for (int i = 0; i < nChain; i++) {
        asc.bsAtoms.set(atomMax + i);
        Atom a = new Atom();
        a.set(0, 0, 0);
        a.radius = 16;
        asc.addAtom(a);
      }
      int ichain = 0;
      for (Entry<Integer, BS> e : ht.entrySet()) {
        Atom a = atoms[atomMax + ichain++];
        BS bs = e.getValue();
        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1))
          a.add(atoms[i]);
        a.scale(1f / bs.cardinality());
        a.atomName = "Pt" + ichain;
        a.chainID = e.getKey().intValue();
      }
      firstAtom = atomMax;
      atomMax += nChain;
      addBonds = false;
      break;
    case PARTICLE_SYMOP:
      asc.bsAtoms = new BS();
      asc.bsAtoms.set(atomMax);
      Atom a = atoms[atomMax] = new Atom();
      a.set(0, 0, 0);
      for (int i = atomMax; --i >= firstAtom;)
        a.add(atoms[i]);
      a.scale(1f / (atomMax - firstAtom));
      a.atomName = "Pt";
      a.radius = 16;
      asc.addAtom(a);
      firstAtom = atomMax++;
      addBonds = false;
      break;
    }
    Map<String, BS> assemblyIdAtoms = (Map<String, BS>) thisBiomolecule
        .get("asemblyIdAtoms");

    if (filter.indexOf("#<") >= 0) {
      // ??
      len = Math.min(len,
          PT.parseInt(filter.substring(filter.indexOf("#<") + 2)) - 1);
      filter = PT.rep(filter, "#<", "_<");
    }

    int maxChain = 0;
    for (int iAtom = firstAtom; iAtom < atomMax; iAtom++) {
      atoms[iAtom].bsSymmetry = new BS(); //TODO was set 0, but this is not necessarily true now
      int chainID = atoms[iAtom].chainID;
      if (chainID > maxChain)
        maxChain = chainID;
    }
    BS bsAtoms = asc.bsAtoms;
    int[] atomMap = (addBonds ? new int[asc.ac] : null);
    // allow for filtering BIOMT number
    // len >= 2, so I don't know what is going on here -- no chains? 
    for (int imt = (biomtchains == null ? 1 : 0); imt < len; imt++) {
      if (filter.indexOf("!#") >= 0) {
        if (filter.indexOf("!#" + (imt + 1) + ";") >= 0)
          continue;
      } else if (filter.indexOf("#") >= 0
          && filter.indexOf("#" + (imt + 1) + ";") < 0) {
        continue;
      }
      M4 mat = biomts.get(imt);
      boolean notIdentity = !mat.equals(mident);

      // if asym_id is given, that is what is being referred to, not author chains
      // we just set bsAtoms to match
      String chains = (biomtchains == null ? null : biomtchains.get(imt));
      if (chains != null && assemblyIdAtoms != null) {
        // must use label_asym_id, not auth_asym_id // bug fix 11/18/2015 
        bsAtoms = new BS();
        for (Entry<String, BS> e : assemblyIdAtoms.entrySet())
          if (chains.indexOf(":" + e.getKey() + ";") >= 0)
            bsAtoms.or(e.getValue());
        if (asc.bsAtoms != null)
          bsAtoms.and(asc.bsAtoms);
        chains = null;
      }

      int lastID = -1, id;
      boolean skipping = false;
      for (int iAtom = firstAtom; iAtom < atomMax; iAtom++) {
        if (bsAtoms != null) {
          skipping = !bsAtoms.get(iAtom);
        } else if (chains != null && (id = atoms[iAtom].chainID) != lastID) {
          skipping = (chains
              .indexOf(":" + acr.vwr.getChainIDStr(lastID = id) + ";") < 0);
        }
        if (skipping)
          continue;
        try {
          int atomSite = atoms[iAtom].atomSite;
          Atom atom1;
          if (addBonds)
            atomMap[atomSite] = asc.ac;
          atom1 = asc.newCloneAtom(atoms[iAtom]);
          atom1.bondingRadius = imt; // temporary only -- to distinguish transforms
          asc.atomSymbolicMap.put("" + atom1.atomSerial, atom1);
          if (asc.bsAtoms != null)
            asc.bsAtoms.set(atom1.index);
          atom1.atomSite = atomSite;
          if (notIdentity)
            mat.rotTrans(atom1);
          atom1.bsSymmetry = BSUtil.newAndSetBit(imt);
        } catch (Exception e) {
          asc.errorMessage = "appendAtomCollection error: " + e;
        }
      }
      // not the identity, which is always the first entry (set by Jmol in reader)
      // symmetry always has first operation x,y,z
      if (notIdentity)
        symmetry.addBioMoleculeOperation(mat, false);
      if (addBonds) {
        // Clone bonds
        for (int bondNum = asc.bondIndex0; bondNum < bondCount0; bondNum++) {
          Bond bond = asc.bonds[bondNum];
          int iAtom1 = atomMap[atoms[bond.atomIndex1].atomSite];
          int iAtom2 = atomMap[atoms[bond.atomIndex2].atomSite];
          //            if (iAtom1 >= atomMax || iAtom2 >= atomMax)
          asc.addNewBondWithOrder(iAtom1, iAtom2, bond.order);
        }
      }
    }
    if (biomtchains != null) {
      if (asc.bsAtoms == null)
        asc.bsAtoms = BSUtil.newBitSet2(0, asc.ac);
      asc.bsAtoms.clearBits(firstAtom, atomMax);
      if (particleMode == PARTICLE_NONE) {
        // check for BMCHAINS filter 
        if (fixBMChains != -1) {
          boolean assignABC = (fixBMChains != 0);
          BS bsChains = (assignABC ? new BS() : null);
          atoms = asc.atoms;
          int firstNew = 0;
          if (assignABC) {
            // tested for 1k28 and 1auy
            //For PDB and mmCIF, convert chains for symmetry-generated atoms to unique
            // ids. Three options:
            // bmChains or bmChains=0: append symmetry operator number to chain ID
            // bmChains=q: start with 'q'
            // bmChains= 3 : start with last chain ID + 3
            firstNew = (fixBMChains < 0 ? Math.max(-fixBMChains, maxChain + 1)
                : Math.max(maxChain + fixBMChains, 0 + 'A'));
            bsChains.setBits(0, firstNew - 1);
            bsChains.setBits(1 + 'Z', 0 + 'a');
            bsChains.setBits(1 + 'z', 200);
          }
          BS[] bsAll = (asc.structureCount == 1 ? asc.structures[0].bsAll
              : null);
          Map<String, Integer> chainMap = new Hashtable<String, Integer>();
          Map<Integer, Integer> knownMap = new Hashtable<Integer, Integer>();
          Map<Integer, int[]> knownAtomMap = (bsAll == null ? null
              : new Hashtable<Integer, int[]>());
          int[] lastKnownAtom = null;
          for (int i = atomMax, n = asc.ac; i < n; i++) {
            int ic = atoms[i].chainID;
            int isym = atoms[i].bsSymmetry.nextSetBit(0);
            String ch0 = acr.vwr.getChainIDStr(ic);
            String ch = (isym == 0 ? ch0 : ch0 + isym);
            Integer known = chainMap.get(ch);
            if (known == null) {
              if (assignABC && isym != 0) {
                int pt = (firstNew < 200 ? bsChains.nextClearBit(firstNew)
                    : 200);
                if (pt < 200) {
                  bsChains.set(pt);
                  // have A-Z or a-z
                  known = Integer
                      .valueOf(acr.vwr.getChainID("" + (char) pt, true));
                  firstNew = pt;
                } else {
                  // 1auy will do this
                }
              }
              if (known == null)
                known = Integer.valueOf(acr.vwr.getChainID(ch, true));
              if (ch != ch0) {
                knownMap.put(known, Integer.valueOf(ic));
                if (bsAll != null) {
                  if (lastKnownAtom != null)
                    lastKnownAtom[1] = i;
                  knownAtomMap.put(known, lastKnownAtom = new int[] { i, n });
                }
              }
              chainMap.put(ch, known);
            }
            atoms[i].chainID = known.intValue();
          }
          if (asc.structureCount > 0) {
            // update structures
            Structure[] strucs = asc.structures;
            int nStruc = asc.structureCount;
            for (Entry<Integer, Integer> e : knownMap.entrySet()) {
              Integer known = e.getKey();
              int ch1 = known.intValue();
              int ch0 = e.getValue().intValue();
              for (int i = 0; i < nStruc; i++) {
                Structure s = strucs[i];
                if (s.bsAll != null) {
                  // MMTFReader processes bsAll[] in addStructureSymmetry()
                } else if (s.startChainID == s.endChainID) {
                  if (s.startChainID == ch0) {
                    Structure s1 = s.clone();
                    s1.startChainID = s1.endChainID = ch1;
                    asc.addStructure(s1);
                  }
                } else {
                  System.err.println(
                      "XtalSymmetry not processing biomt chain structure "
                          + acr.vwr.getChainIDStr(ch0) + " to "
                          + acr.vwr.getChainIDStr(ch1));
                  // TODO now what??
                }
              }

            }
          }
        }

        // clean interchain CONECT 
        Lst<int[]> vConnect = (Lst<int[]>) asc.getAtomSetAuxiliaryInfoValue(-1,
            "PDB_CONECT_bonds");
        if (!addBonds && vConnect != null) {
          for (int i = vConnect.size(); --i >= 0;) {
            int[] bond = vConnect.get(i);
            Atom a = asc.getAtomFromName("" + bond[0]);
            Atom b = asc.getAtomFromName("" + bond[1]);
            // bondingRadius here just being used for BIOMT 
            if (a != null && b != null && a.bondingRadius != b.bondingRadius
                && (bsAtoms == null
                    || bsAtoms.get(a.index) && bsAtoms.get(b.index))
                && a.distanceSquared(b) > MAX_INTERCHAIN_BOND_2) {
              vConnect.removeItemAt(i);
              System.out.println("long interchain bond removed for @"
                  + a.atomSerial + "-@" + b.atomSerial);
            }
          }
        }
      }
      // reset bondingRadius to NaN
      for (int i = atomMax, n = asc.ac; i < n; i++) {
        asc.atoms[i].bondingRadius = Float.NaN;
      }

    }
    noSymmetryCount = atomMax - firstAtom;
    finalizeSymmetry(symmetry);
    setCurrentModelInfo(len, null, null);
    reset();

    //TODO: need to clone bonds
  }

  /**
   * Get the symmetry that was in place prior to any supercell business.
   * 
   * @return base symmetry
   */
  public FileSymmetry getBaseSymmetry() {
    return (baseSymmetry == null ? symmetry : baseSymmetry);
  }

  public T3 getOverallSpan() {
    return (maxXYZ0 == null
        ? V3.new3(maxXYZ.x - minXYZ.x, maxXYZ.y - minXYZ.y, maxXYZ.z - minXYZ.z)
        : V3.newVsub(maxXYZ0, minXYZ0));
  }

  public FileSymmetry getSymmetry() {
    return (symmetry == null ? (symmetry = new FileSymmetry()) : symmetry);
  }

  public boolean isWithinCell(int ndims, P3 pt, float minX, float maxX,
                              float minY, float maxY, float minZ, float maxZ,
                              float slop) {
    return (pt.x > minX - slop && pt.x < maxX + slop
        && (ndims < 2 || pt.y > minY - slop && pt.y < maxY + slop)
        && (ndims < 3 || pt.z > minZ - slop && pt.z < maxZ + slop));
  }

  /**
   * magCIF files have moments expressed as Bohr magnetons along the
   * cryrstallographic axes. These have to be "fractionalized" in order to be
   * properly handled by symmetry operations, then, in the end, turned into
   * Cartesians.
   * 
   * It is not clear to me at all how this would be handled if there are
   * subsystems. This method must be run PRIOR to applying symmetry and thus
   * prior to creation of modulation sets.
   * 
   */
  public void scaleFractionalVibs() {
    float[] params = acr.unitCellParams;
    P3 ptScale = P3.new3(1 / params[0], 1 / params[1], 1 / params[2]);
    int i0 = asc.getAtomSetAtomIndex(asc.iSet);
    for (int i = asc.ac; --i >= i0;) {
      V3 v = asc.atoms[i].vib;
      if (v != null) {
        v.scaleT(ptScale);
      }
    }
  }

  public XtalSymmetry set(AtomSetCollectionReader reader) {
    this.acr = reader;
    this.asc = reader.asc;
    getSymmetry();
    return this;
  }

  /**
   * Shelx and Wien2k readers
   * 
   * @param latt
   */
  public void setLatticeParameter(int latt) {
    symmetry.setSpaceGroup(doNormalize);
    symmetry.setLattice(latt);
  }

  public int setSpinVectors() {
    // return spin vectors to cartesians
    if (nVib > 0 || asc.iSet < 0 || !acr.vibsFractional)
      return nVib; // already done
    int i0 = asc.getAtomSetAtomIndex(asc.iSet);
    FileSymmetry sym = getBaseSymmetry();
    for (int i = asc.ac; --i >= i0;) {
      Vibration v = (Vibration) asc.atoms[i].vib;
      if (v != null) {
        if (v.modDim > 0) {
          ((JmolModulationSet) v).setMoment();
        } else {
          v = (Vibration) v.clone(); // this could be a modulation set
          sym.toCartesian(v, true);
          asc.atoms[i].vib = v;
        }
        nVib++;
      }
    }
    return nVib;
  }

  int addSpaceGroupOperation(String xyz, boolean andSetLattice) {
    symmetry.setSpaceGroup(doNormalize);
    if (andSetLattice && symmetry.getSpaceGroupOperationCount() == 1)
      setLatticeCells();
    return symmetry.addSpaceGroupOperation(xyz, 0);
  }

  /**
   * General entry point.
   * 
   * @param readerSymmetry
   * @return FileSymmetry
   * @throws Exception
   */
  FileSymmetry applySymmetryFromReader(FileSymmetry readerSymmetry)
      throws Exception {
    asc.setCoordinatesAreFractional(acr.iHaveFractionalCoordinates);
    setAtomSetSpaceGroupName(acr.sgName);
    symmetryRange = acr.symmetryRange;
    asc.setInfo("symmetryRange", Float.valueOf(symmetryRange));
    if (acr.doConvertToFractional || acr.fileCoordinatesAreFractional) {
      setLatticeCells();
      boolean doApplySymmetry = true;
      if (acr.ignoreFileSpaceGroupName || !acr.iHaveSymmetryOperators) {
        if (!acr.merging || readerSymmetry == null)
          readerSymmetry = new FileSymmetry();
        if (acr.unitCellParams[0] == 0 && acr.unitCellParams[2] == 0) {
          SimpleUnitCell.fillParams(null, null, null, acr.unitCellParams);
        }
        doApplySymmetry = readerSymmetry.createSpaceGroup(
            acr.desiredSpaceGroupIndex,
            (acr.sgName.indexOf("!") >= 0 ? "P1" : acr.sgName),
            acr.unitCellParams, acr.modDim);
      } else {
        acr.doPreSymmetry();
        readerSymmetry = null;
      }
      // this will be a float-precision value in JavaScript
      packingRange = acr.getPackingRangeValue(0);
      if (doApplySymmetry) {
        if (readerSymmetry != null)
          getSymmetry().setSpaceGroupTo(readerSymmetry.getSpaceGroup());
        //parameters are counts of unit cells as [a b c]

        applySymmetryLattice();
        if (readerSymmetry != null && filterSymop == null)
          setAtomSetSpaceGroupName(readerSymmetry.getSpaceGroupName());
      } else {
        setUnitCellSafely();
      }
    }
    if (acr.iHaveFractionalCoordinates && acr.merging
        && readerSymmetry != null) {

      // when merging (with appendNew false), we must return cartesians
      Atom[] atoms = asc.atoms;
      for (int i = asc.getLastAtomSetAtomIndex(), n = asc.ac; i < n; i++)
        readerSymmetry.toCartesian(atoms[i], true);
      asc.setCoordinatesAreFractional(false);

      // We no longer allow merging of multiple-model files
      // when the file to be appended has fractional coordinates and vibrations
      acr.addVibrations = false;
    }
    return symmetry;
  }

  FileSymmetry setSymmetry(FileSymmetry symmetry) {
    return (this.symmetry = symmetry);
  }

  void setTensors() {
    int n = asc.ac;
    for (int i = asc.getLastAtomSetAtomIndex(); i < n; i++) {
      Atom a = asc.atoms[i];
      if (a.anisoBorU == null)
        continue;
      // getTensor will return correct type
      a.addTensor(symmetry.getTensor(acr.vwr, a.anisoBorU), null, false);
      if (Float.isNaN(a.bfactor))
        a.bfactor = a.anisoBorU[7] * 100f;
      // prevent multiple additions
      a.anisoBorU = null;
    }
  }

  private void adjustRangeMinMax(T3[] oabc) {

    // be sure to add in packing error adjustments 
    // so that we include all needed atoms
    // load "" packed x.x supercell...
    P3 pa = new P3();
    P3 pb = new P3();
    P3 pc = new P3();
    if (acr.forcePacked) {
      pa.setT(oabc[1]);
      pb.setT(oabc[2]);
      pc.setT(oabc[3]);
      pa.scale(packingRange);
      pb.scale(packingRange);
      pc.scale(packingRange);
    }

    // account for lattice specification
    // load "" {x y z} supercell...
    oabc[0].scaleAdd2(minXYZ.x, oabc[1], oabc[0]);
    oabc[0].scaleAdd2(minXYZ.y, oabc[2], oabc[0]);
    oabc[0].scaleAdd2(minXYZ.z, oabc[3], oabc[0]);
    // add in packing adjustment
    oabc[0].sub(pa);
    oabc[0].sub(pb);
    oabc[0].sub(pc);
    // fractionalize and adjust min/max
    P3 pt = P3.newP(oabc[0]);
    symmetry.toFractional(pt, true);
    setSymmetryMinMax(pt);

    // account for lattice specification
    oabc[1].scale(maxXYZ.x - minXYZ.x);
    oabc[2].scale(maxXYZ.y - minXYZ.y);
    oabc[3].scale(maxXYZ.z - minXYZ.z);
    // add in packing adjustment
    oabc[1].scaleAdd2(2, pa, oabc[1]);
    oabc[2].scaleAdd2(2, pb, oabc[2]);
    oabc[3].scaleAdd2(2, pc, oabc[3]);
    // run through six of the corners -- a, b, c, ab, ac, bc
    for (int i = 0; i < 3; i++) {
      for (int j = i + 1; j < 4; j++) {
        pt.add2(oabc[i], oabc[j]);
        if (i != 0)
          pt.add(oabc[0]);
        symmetry.toFractional(pt, false);
        setSymmetryMinMax(pt);
      }
    }
    // bc in the end, so we need abc
    symmetry.toCartesian(pt, false);
    pt.add(oabc[1]);
    symmetry.toFractional(pt, false);
    setSymmetryMinMax(pt);
    // allow for some imprecision
    minXYZ = P3i.new3((int) Math.min(0, Math.floor(rminx + 0.001f)),
        (int) Math.min(0, Math.floor(rminy + 0.001f)),
        (int) Math.min(0, Math.floor(rminz + 0.001f)));
    maxXYZ = P3i.new3((int) Math.max(1, Math.ceil(rmaxx - 0.001f)),
        (int) Math.max(1, Math.ceil(rmaxy - 0.001f)),
        (int) Math.max(1, Math.ceil(rmaxz - 0.001f)));
  }

  /**
   * @param ms
   *        modulated structure interface
   * @param bsAtoms
   *        relating to supercells
   * @throws Exception
   */
  private void applyAllSymmetry(MSInterface ms, BS bsAtoms) throws Exception {
    if (asc.ac == 0 || bsAtoms != null && bsAtoms.isEmpty())
      return;
    int n = noSymmetryCount = asc.baseSymmetryAtomCount > 0
        ? asc.baseSymmetryAtomCount
        : bsAtoms == null ? asc.getLastAtomSetAtomCount()
            : asc.ac - bsAtoms.nextSetBit(asc.getLastAtomSetAtomIndex());
    asc.setTensors();
    applySymmetryToBonds = acr.applySymmetryToBonds;
    doPackUnitCell = acr.doPackUnitCell && !applySymmetryToBonds;
    bondCount0 = asc.bondCount;
    ndims = SimpleUnitCell.getDimensionFromParams(acr.unitCellParams);
    finalizeSymmetry(symmetry);
    int operationCount = symmetry.getSpaceGroupOperationCount();
    BS excludedOps = (acr.thisBiomolecule == null ? null : new BS());
    checkNearAtoms = acr.checkNearAtoms || excludedOps != null;
    SimpleUnitCell.setMinMaxLatticeParameters(ndims, minXYZ, maxXYZ, 0);
    latticeOp = symmetry.getLatticeOp();
    crystalReaderLatticeOpsOnly = (asc.crystalReaderLatticeOpsOnly
        && latticeOp >= 0); // CrystalReader
    if (doCentroidUnitCell)
      asc.setInfo("centroidMinMax", new int[] { minXYZ.x, minXYZ.y, minXYZ.z,
          maxXYZ.x, maxXYZ.y, maxXYZ.z, (centroidPacked ? (int) (packingRange * 100) : 0) });
    if (doCentroidUnitCell || acr.doPackUnitCell
        || symmetryRange != 0 && maxXYZ.x - minXYZ.x == 1
            && maxXYZ.y - minXYZ.y == 1 && maxXYZ.z - minXYZ.z == 1) {
      // weird Mac bug does not allow   Point3i.new3(minXYZ) !!
      minXYZ0 = P3.new3(minXYZ.x, minXYZ.y, minXYZ.z);
      maxXYZ0 = P3.new3(maxXYZ.x, maxXYZ.y, maxXYZ.z);
      if (ms != null) {
        ms.setMinMax0(minXYZ0, maxXYZ0);
        minXYZ.set((int) minXYZ0.x, (int) minXYZ0.y, (int) minXYZ0.z);
        maxXYZ.set((int) maxXYZ0.x, (int) maxXYZ0.y, (int) maxXYZ0.z);
      }
      switch (ndims) {
      case 3:
        // standard
        minXYZ.z--;
        maxXYZ.z++;
        //$FALL-THROUGH$;
      case 2:
        // slab or standard
        minXYZ.y--;
        maxXYZ.y++;
        //$FALL-THROUGH$;
      case 1:
        // slab, polymer, or standard
        minXYZ.x--;
        maxXYZ.x++;
      }
    }
    int nCells = (maxXYZ.x - minXYZ.x) * (maxXYZ.y - minXYZ.y)
        * (maxXYZ.z - minXYZ.z);
    int nsym = n * (crystalReaderLatticeOpsOnly ? 4 : operationCount);
    // we track cartesian coordinates of all atoms when there is biomolecular
    // symmetry or we need to check near atoms (d2 < d0); 
    // we track only cell=555 when there are multiple unit cells;
    // otherwise we don't check (though the first atom is always checked???)
    int cartesianCount = (checkNearAtoms || acr.thisBiomolecule != null
        ? nsym * nCells
        : symmetryRange > 0 ? nsym // checking against {1 1 1}
            : 1 // not checking (Q: Why not zero here?)
    );
    P3[] cartesians = new P3[cartesianCount];
    Atom[] atoms = asc.atoms;
    for (int i = 0; i < n; i++)
      atoms[firstAtom + i].bsSymmetry = BS.newN(operationCount * (nCells + 1));
    int pt = 0;
    unitCellTranslations = new V3[nCells];
    int iCell = 0;
    int cell555Count = 0;
    float absRange = Math.abs(symmetryRange);
    boolean checkCartesianRange = (symmetryRange != 0);
    boolean checkRangeNoSymmetry = (symmetryRange < 0);
    boolean checkRange111 = (symmetryRange > 0);
    if (checkCartesianRange) {
      rminx = rminy = rminz = Float.MAX_VALUE;
      rmaxx = rmaxy = rmaxz = -Float.MAX_VALUE;
    }
    // incommensurate symmetry can have lattice centering, resulting in 
    // duplication of operators. There's a bug later on that requires we 
    // only do this with the first atom set for now, at least.
    FileSymmetry sym = symmetry;
    FileSymmetry lastSymmetry = sym;
    checkAll = (crystalReaderLatticeOpsOnly
        || asc.atomSetCount == 1 && checkNearAtoms && latticeOp >= 0);
    Lst<M4> lstNCS = acr.lstNCS;
    if (lstNCS != null && lstNCS.get(0).m33 == 0) {
      int nOp = sym.getSpaceGroupOperationCount();
      int nn = lstNCS.size();
      for (int i = nn; --i >= 0;) {
        M4 m = lstNCS.get(i);
        m.m33 = 1;
        sym.toFractionalM(m);
      }
      for (int i = 1; i < nOp; i++) {
        M4 m1 = sym.getSpaceGroupOperation(i);
        for (int j = 0; j < nn; j++) {
          M4 m = M4.newM4(lstNCS.get(j));
          m.mul2(m1, m);
          if (doNormalize && noSymmetryCount > 0)
            SymmetryOperation.normalizeOperationToCentroid(3, m, atoms,
                firstAtom, noSymmetryCount);
          lstNCS.addLast(m);
        }
      }
    }

    // always do the 555 cell first, just operation 0 -- but for incommensurate systems this may not be x,y,z

    P3 pttemp = null;
    M4 op = sym.getSpaceGroupOperation(0);
    if (doPackUnitCell) {
      pttemp = new P3();
      ptOffset.set(0, 0, 0);
    }
    int[] atomMap = (bondCount0 > asc.bondIndex0 && applySymmetryToBonds
        ? new int[n]
        : null);
    int[] unitCells = new int[nCells];
    for (int tx = minXYZ.x; tx < maxXYZ.x; tx++) {
      for (int ty = minXYZ.y; ty < maxXYZ.y; ty++) {
        for (int tz = minXYZ.z; tz < maxXYZ.z; tz++) {
          unitCellTranslations[iCell] = V3.new3(tx, ty, tz);
          unitCells[iCell++] = 555 + tx * 100 + ty * 10 + tz;
          if (tx != 0 || ty != 0 || tz != 0 || cartesians.length == 0)
            continue;

          // base cell only
          for (pt = 0; pt < n; pt++) {
            Atom atom = atoms[firstAtom + pt];
            if (ms != null) {
              // incommensurately modulated structure
              sym = ms.getAtomSymmetry(atom, this.symmetry);
              if (sym != lastSymmetry) {
                if (sym.getSpaceGroupOperationCount() == 0)
                  finalizeSymmetry(lastSymmetry = sym);
                op = sym.getSpaceGroupOperation(0);
              }
            }
            P3 c = P3.newP(atom);
            op.rotTrans(c);
            sym.toCartesian(c, false);
            if (doPackUnitCell) {

              // 555 cell 
              sym.toUnitCellRnd(c, ptOffset);
              pttemp.setT(c);
              sym.toFractional(pttemp, false);
              acr.fixFloatPt(pttemp, PT.FRACTIONAL_PRECISION); // LEGACY ONLY
              // when bsAtoms != null, we are
              // setting it to be correct for a 
              // second unit cell -- the supercell
              if (bsAtoms == null)
                atom.setT(pttemp);
              else if (atom.distance(pttemp) < MINIMUM_FRACTIONAL_ATOM_DISTANCE)
                bsAtoms.set(atom.index);
              else {// not in THIS unit cell
                bsAtoms.clear(atom.index);
                continue;
              }
            }
            if (bsAtoms != null)
              atom.bsSymmetry.clearAll();
            atom.bsSymmetry.set(iCell * operationCount);
            atom.bsSymmetry.set(0);
            if (checkCartesianRange)
              setSymmetryMinMax(c);
            if (pt < cartesianCount)
              cartesians[pt] = c;
          }
          if (checkRangeNoSymmetry) {
            rminx -= absRange;
            rminy -= absRange;
            rminz -= absRange;
            rmaxx += absRange;
            rmaxy += absRange;
            rmaxz += absRange;
          }
          cell555Count = pt = symmetryAddAtoms(0, 0, 0, 0, pt,
              iCell * operationCount, cartesians, ms, excludedOps, atomMap);
        }
      }
    }
    if (checkRange111) {
      rminx -= absRange;
      rminy -= absRange;
      rminz -= absRange;
      rmaxx += absRange;
      rmaxy += absRange;
      rmaxz += absRange;
    }

    // now apply all the translations
    iCell = 0;
    for (int tx = minXYZ.x; tx < maxXYZ.x; tx++) {
      for (int ty = minXYZ.y; ty < maxXYZ.y; ty++) {
        for (int tz = minXYZ.z; tz < maxXYZ.z; tz++) {
          iCell++;
          if (tx != 0 || ty != 0 || tz != 0)
            pt = symmetryAddAtoms(tx, ty, tz, cell555Count, pt,
                iCell * operationCount, cartesians, ms, excludedOps, atomMap);
        }
      }
    }
    if (iCell * n == asc.ac - firstAtom)
      duplicateAtomProperties(iCell);
    setCurrentModelInfo(n, sym, unitCells);
    unitCellParams = null;
    reset();
  }

  /**
   * LOAD FILL ...
   * 
   * @param bsAtoms
   * @throws Exception
   */
  private void applyRangeSymmetry(BS bsAtoms) throws Exception {
    T3[] range = (T3[]) acr.fillRange;
    bsAtoms = updateBSAtoms();
    acr.forcePacked = true;
    doPackUnitCell = false;
    P3i minXYZ2 = P3i.new3(minXYZ.x, minXYZ.y, minXYZ.z);
    P3i maxXYZ2 = P3i.new3(maxXYZ.x, maxXYZ.y, maxXYZ.z);
    P3[] oabc = new P3[4];
    for (int i = 0; i < 4; i++)
      oabc[i] = P3.newP(range[i]);
    setUnitCellSafely();
    adjustRangeMinMax(oabc);
    //Logger.info("setting min/max for original lattice to " + minXYZ + " and "
    //  + maxXYZ);
    FileSymmetry superSymmetry = new FileSymmetry();
    superSymmetry.getUnitCell((T3[]) acr.fillRange, false, null);
    applyAllSymmetry(acr.ms, bsAtoms);
    P3 pt0 = new P3();
    Atom[] atoms = asc.atoms;
    for (int i = asc.ac; --i >= firstAtom;) {
      pt0.setT(atoms[i]);
      symmetry.toCartesian(pt0, false);
      superSymmetry.toFractional(pt0, false);
      //acr.fixFloatPt(pt0, PT.FRACTIONAL_PRECISION); // LEGACY ONLY
      if (acr.noPack
          ? !removePacking(ndims, pt0, minXYZ2.x, maxXYZ2.x, minXYZ2.y,
              maxXYZ2.y, minXYZ2.z, maxXYZ2.z, packingRange)
          : !isWithinCell(ndims, pt0, minXYZ2.x, maxXYZ2.x, minXYZ2.y,
              maxXYZ2.y, minXYZ2.z, maxXYZ2.z, packingRange))
        bsAtoms.clear(i);
    }
  }

  private void applySuperSymmetry(String supercell, BS bsAtoms, int iAtomFirst,
                                  T3[] oabc, P3 pt0, V3[] vabc, float slop)
      throws Exception {
    asc.setGlobalBoolean(JC.GLOBAL_SUPERCELL);
    boolean doPack0 = doPackUnitCell;
    doPackUnitCell = doPack0;//(doPack0 || oabc != null && acr.forcePacked);
    // this call will set unitCellParams to null
    applyAllSymmetry(acr.ms, null);
    doPackUnitCell = doPack0;

    // 2) set all atom coordinates to Cartesians

    Atom[] atoms = asc.atoms;
    int atomCount = asc.ac;
    for (int i = iAtomFirst; i < atomCount; i++) {
      symmetry.toCartesian(atoms[i], true);
      bsAtoms.set(i);
    }

    // 3) create the supercell unit cell

    //      oldParams = symmetry.getUnitCellParams();

    asc.setCurrentModelInfo("unitcell_conventional",
        symmetry.getV0abc("a,b,c", null));
    V3 va = vabc[0];
    V3 vb = vabc[1];
    V3 vc = vabc[2];
    symmetry = new FileSymmetry();
    setUnitCell(new float[] { // 27 values
        0, 0, 0, 0, 0, 0, // abc al be ga
        va.x, va.y, va.z, vb.x, vb.y, vb.z, vc.x, vc.y, vc.z, 0, 0, 0, 0, 0, 0, // m21 22 23 30 31 32 33
        Float.NaN, // not a matrix
        Float.NaN, Float.NaN, Float.NaN, // no supercell items 
        Float.NaN, // no scaling
        slop }, null, null);
    String name = oabc == null || supercell == null ? "P1"
        : "cell=" + supercell;
    setAtomSetSpaceGroupName(name);
    symmetry.setSpaceGroupName(name);
    symmetry.setSpaceGroup(doNormalize);
    symmetry.addSpaceGroupOperation("x,y,z", 0);

    // 4) reset atoms to fractional values in this new system

    if (pt0 != null && pt0.length() == 0)
      pt0 = null;
    if (pt0 != null)
      symmetry.toFractional(pt0, true);
    for (int i = iAtomFirst; i < atomCount; i++) {
      symmetry.toFractional(atoms[i], true);
      if (pt0 != null)
        atoms[i].sub(pt0);
    }

    // 5) apply the full lattice symmetry now

    asc.haveAnisou = false;
    asc.setCurrentModelInfo("matUnitCellOrientation", null);
  }

  /**
   * 
   * Apply all the lattice-based aspects of symmetry, creating supercells,
   * filling regions such as primitive cells, etc.
   * 
   * Called once, only by applySymmetryFromReader
   * 
   * @throws Exception
   */
  private void applySymmetryLattice() throws Exception {
    if (!asc.coordinatesAreFractional || symmetry.getSpaceGroup() == null)
      return;
    int maxX = latticeCells[0];
    int maxY = latticeCells[1];
    int maxZ = Math.abs(latticeCells[2]);
    /**
     * kcode: Generally the multiplier is just {ijk ijk scale}, but when we have
     * 1iiijjjkkk 1iiijjjkkk scale, floats lose kkk due to Java float precision
     * issues so we use P4 {1iiijjjkkk 1iiijjjkkk scale, 1kkkkkk}. Here, our
     * offset -- initially 0 or 1 from the uccage renderer, but later -500 or
     * -499 -- tells us which code we are looking at, the first one or the
     * second one.
     */
    int kcode = latticeCells[3];
    firstAtom = asc.getLastAtomSetAtomIndex();
    BS bsAtoms = asc.bsAtoms;
    if (bsAtoms != null) {
      updateBSAtoms();
      firstAtom = bsAtoms.nextSetBit(firstAtom);
    }
    rminx = rminy = rminz = Float.MAX_VALUE;
    rmaxx = rmaxy = rmaxz = -Float.MAX_VALUE;
    if (acr.latticeType == null)
      acr.latticeType = "" + symmetry.getLatticeType();
    if (acr.isPrimitive) {
      asc.setCurrentModelInfo("isprimitive", Boolean.TRUE);
      if (!"P".equals(acr.latticeType) || acr.primitiveToCrystal != null) {
        asc.setCurrentModelInfo(JC.INFO_UNIT_CELL_CONVENTIONAL, symmetry
            .getConventionalUnitCell(acr.latticeType, acr.primitiveToCrystal));
      }
    }
    setUnitCellSafely();
    asc.setCurrentModelInfo("f2c", symmetry.getUnitCellF2C());
    String s = symmetry.getSpaceGroupTitle();
    if (s.indexOf("--") < 0)
      asc.setCurrentModelInfo(JC.INFO_SPACE_GROUP_F2C_TITLE, s);
    asc.setCurrentModelInfo("f2cParams", symmetry.getUnitCellParams());
    if (acr.latticeType != null) {
      asc.setCurrentModelInfo("latticeType", acr.latticeType);
      if (acr.fillRange instanceof String) {
        T3[] range = setLatticeRange(acr.latticeType, (String) acr.fillRange);
        if (range == null) {
          acr.appendLoadNote(
              acr.fillRange + " symmetry could not be implemented");
          acr.fillRange = null;
        } else {
          acr.fillRange = range;
        }
      }
    }
    baseSymmetry = symmetry;
    if (acr.fillRange != null) {
      setMinMax(ndims, kcode, maxX, maxY, maxZ);
      applyRangeSymmetry(bsAtoms);
      return;
    }
    T3[] oabc = null;
    float slop = 1e-6f;
    nVib = 0;
    String supercell = acr.strSupercell;
    boolean isSuper = (supercell != null && supercell.indexOf(",") >= 0);
    M4 matSuper = null;
    P3 pt0 = null;
    V3[] vabc = new V3[3];
    if (isSuper) {
      // expand range to accommodate this alternative cell
      // oabc will be cartesian
      matSuper = new M4();
      if (mident == null)
        mident = new M4();
      setUnitCellSafely();
      oabc = symmetry.getV0abc(supercell, matSuper);
      matSuper.transpose33();
      if (oabc != null && !matSuper.equals(mident)) {
        // flag this to set symmetry to P1 in the end
        // set the bounds for atoms in the new unit cell
        // in terms of the old unit cell
        setMinMax(ndims, kcode, maxX, maxY, maxZ);
        // base origin for new unit cell
        pt0 = P3.newP(oabc[0]);
        // base vectors for new unit cell
        vabc[0] = V3.newV(oabc[1]);
        vabc[1] = V3.newV(oabc[2]);
        vabc[2] = V3.newV(oabc[3]);
        adjustRangeMinMax(oabc);
      }
    }
    int iAtomFirst = asc.getLastAtomSetAtomIndex();
    if (bsAtoms != null)
      iAtomFirst = bsAtoms.nextSetBit(iAtomFirst);
    if (rminx == Float.MAX_VALUE) {
      // just a standard load
      supercell = null;
      oabc = null;
    } else {
      bsAtoms = updateBSAtoms();
      slop = symmetry.getPrecision();
      applySuperSymmetry(supercell, bsAtoms, iAtomFirst, oabc, pt0, vabc, slop);
    }
    setMinMax(ndims, kcode, maxX, maxY, maxZ);
    if (oabc == null) {
      // finally operate on the simple cell
      applyAllSymmetry(acr.ms, bsAtoms);
      if (!acr.noPack && (!applySymmetryToBonds || !acr.doPackUnitCell)) {
        // normal return for simple structure load
        return;
      }
      // fall through if packed and there are bonds, that is, when we did not shift atoms
      // so we reset to the original min and max and then trim.
      setMinMax(ndims, kcode, maxX, maxY, maxZ);
    }
    if (acr.forcePacked || acr.doPackUnitCell || acr.noPack) {
      trimToUnitCell(iAtomFirst);
    }

    // we leave matSuper, because we might need it for vibrations in CASTEP
    // this is now effectively P1. We need to set all the sites 

    updateSupercellAtomSites(matSuper, bsAtoms, slop);

    //    if (oldParams != null)
    //      setUnitCell(oldParams, null, offset);
  }

  @SuppressWarnings("unchecked")
  private void duplicateAtomProperties(int nTimes) {
    Map<String, Object> p = (Map<String, Object>) asc
        .getAtomSetAuxiliaryInfoValue(-1, "atomProperties");
    if (p != null)
      for (Map.Entry<String, Object> entry : p.entrySet()) {
        String key = entry.getKey();
        Object val = entry.getValue();
        if (val instanceof String) {
          String data = (String) val;
          SB s = new SB();
          for (int i = nTimes; --i >= 0;)
            s.append(data);
          p.put(key, s.toString());
        } else {
          float[] f = (float[]) val;
          float[] fnew = new float[f.length * nTimes];
          for (int i = nTimes; --i >= 0;)
            System.arraycopy(f, 0, fnew, i * f.length, f.length);
        }
      }
  }

  private void finalizeSymmetry(FileSymmetry symmetry) {
    String name = (String) asc.getAtomSetAuxiliaryInfoValue(-1, JC.INFO_SPACE_GROUP);
    symmetry.setFinalOperations(ndims, name, asc.atoms, firstAtom,
        noSymmetryCount, doNormalize, filterSymop);
    if (filterSymop != null || name == null || name.equals("unspecified!")) {
      setAtomSetSpaceGroupName(symmetry.getUnitCellDisplayName());
    }
    if (unitCellParams != null
        || Float.isNaN(acr.unitCellParams[SimpleUnitCell.INFO_A]))
      return;
    if (symmetry.fixUnitCell(acr.unitCellParams)) {
      acr.appendLoadNote(
          "Unit cell parameters were adjusted to match space group!");
    }
    setUnitCellSafely();
  }

  public boolean removePacking(int ndims, P3 pt, float minX, float maxX,
                               float minY, float maxY, float minZ, float maxZ,
                               float slop) {
    return (pt.x > minX - slop && pt.x < maxX - slop
        && (ndims < 2 || pt.y > minY - slop && pt.y < maxY - slop)
        && (ndims < 3 || pt.z > minZ - slop && pt.z < maxZ - slop));
  }

  private void reset() {
    asc.coordinatesAreFractional = false;
    asc.setCurrentModelInfo("hasSymmetry", Boolean.TRUE);
    asc.setGlobalBoolean(JC.GLOBAL_SYMMETRY);
  }

  private void setAtomSetSpaceGroupName(String spaceGroupName) {
    symmetry.setSpaceGroupName(spaceGroupName);
    String s = spaceGroupName + "";
    asc.setCurrentModelInfo(JC.INFO_SPACE_GROUP_F2C_TITLE, s);
    asc.setCurrentModelInfo(JC.INFO_SPACE_GROUP, s);
  }

  private void setCurrentModelInfo(int n, FileSymmetry sym, int[] unitCells) {
    if (sym == null) {
      // biomodel symmetry
      asc.setCurrentModelInfo("presymmetryAtomCount",
          Integer.valueOf(noSymmetryCount));
      asc.setCurrentModelInfo("biosymmetryCount", Integer.valueOf(n));
      asc.setCurrentModelInfo("biosymmetry", symmetry);
    } else {
      asc.setCurrentModelInfo("presymmetryAtomCount", Integer.valueOf(n));
      asc.setCurrentModelInfo("latticeDesignation",
          sym.getLatticeDesignation());
      asc.setCurrentModelInfo(JC.INFO_UNIT_CELL_RANGE, unitCells);
      //      asc.setCurrentModelInfo(JC.INFO_UNIT_CELL_TRANSLATIONS, unitCellTranslations);
      if (acr.isSUPERCELL)
        asc.setCurrentModelInfo("supercell", acr.strSupercell);
    }
    asc.setCurrentModelInfo("presymmetryAtomIndex", Integer.valueOf(firstAtom));
    int operationCount = symmetry.getSpaceGroupOperationCount();
    if (operationCount > 0) {
      asc.setCurrentModelInfo(JC.INFO_SYMMETRY_OPERATIONS, symmetry.getSymopList(doNormalize));
      asc.setCurrentModelInfo(JC.INFO_SYMOPS_TEMP, symmetry.getSymmetryOperations());
         }
    asc.setCurrentModelInfo("symmetryCount", Integer.valueOf(operationCount));
    asc.setCurrentModelInfo("latticeType",
        acr.latticeType == null ? "P" : acr.latticeType);
    asc.setCurrentModelInfo("intlTableNo", symmetry.getIntTableNumber());
    asc.setCurrentModelInfo("intlTableIndex", symmetry.getSpaceGroupInfoObj("itaIndex", null, false, false));
    asc.setCurrentModelInfo("intlTableTransform", symmetry.getSpaceGroupInfoObj("itaTransform", null, false, false));
    asc.setCurrentModelInfo("intlTableJmolId",
        symmetry.getSpaceGroupJmolId());
    asc.setCurrentModelInfo(JC.INFO_SPACE_GROUP_INDEX,
        Integer.valueOf(symmetry.getSpaceGroupIndex()));
    asc.setCurrentModelInfo(JC.INFO_SPACE_GROUP_TITLE, symmetry.getSpaceGroupTitle());
    if (acr.sgName == null || acr.sgName.indexOf("?") >= 0
        || acr.sgName.indexOf("!") >= 0)
      setAtomSetSpaceGroupName(acr.sgName = symmetry.getSpaceGroupName());
  }

  private void setCurrentModelUCInfo(float[] unitCellParams, P3 unitCellOffset,
                                     M3 matUnitCellOrientation) {
    if (unitCellParams != null)
      asc.setCurrentModelInfo(JC.INFO_UNIT_CELL_PARAMS, unitCellParams);
    if (unitCellOffset != null)
      asc.setCurrentModelInfo(JC.INFO_UNIT_CELL_OFFSET, unitCellOffset);
    if (matUnitCellOrientation != null)
      asc.setCurrentModelInfo("matUnitCellOrientation", matUnitCellOrientation);
  }

  private void setLatticeCells() {

    //set when unit cell is determined
    // x <= 555 and y >= 555 indicate a range of cells to load
    // AROUND the central cell 555 and that
    // we should normalize (z = 1) or pack unit cells (z = -1) or not (z = 0)
    // in addition (Jmol 11.7.36) z = -2 does a full 3x3x3 around the designated cells
    // but then only delivers the atoms that are within the designated cells. 
    // Normalization is the moving of the center of mass into the unit cell.
    // Starting with Jmol 12.0.RC23 we do not normalize a CIF file that 
    // is being loaded without {i j k} indicated.

    latticeCells = acr.latticeCells;
    boolean isLatticeRange = (latticeCells[0] <= 555 && latticeCells[1] >= 555
        && (latticeCells[2] == 0 || latticeCells[2] == 1
            || latticeCells[2] == -1));
    doNormalize = latticeCells[0] != 0
        && (!isLatticeRange || latticeCells[2] == 1);
    applySymmetryToBonds = acr.applySymmetryToBonds;
    doPackUnitCell = acr.doPackUnitCell && !applySymmetryToBonds;
    doCentroidUnitCell = acr.doCentroidUnitCell;
    centroidPacked = acr.centroidPacked;
    filterSymop = acr.filterSymop;
  }

  /**
   * Establish a range of cells that cover the desired range.
   * 
   * @param latticetype
   *        "conventional", "primitive", "R",
   * @param rangeType
   * @return range as T3d[] or null
   */
  private T3[] setLatticeRange(String latticetype, String rangeType) {
    T3[] range = null;
    boolean isRhombohedral = "R".equals(latticetype);
    if (rangeType.equals(AtomSetCollectionReader.CELL_TYPE_CONVENTIONAL)) {
      range = symmetry.getConventionalUnitCell(latticetype,
          acr.primitiveToCrystal);
    } else if (rangeType.equals(AtomSetCollectionReader.CELL_TYPE_PRIMITIVE)) {
      range = symmetry.getUnitCellVectors();
      symmetry.toFromPrimitive(true, latticetype.charAt(0), range,
          acr.primitiveToCrystal);
    } else if (isRhombohedral && rangeType.equals("rhombohedral")) {
      if (symmetry.getUnitCellInfoType(SimpleUnitCell.INFO_IS_HEXAGONAL) == 1) {
        rangeType = SimpleUnitCell.HEX_TO_RHOMB;
      } else {
        rangeType = null;
      }
    } else if (isRhombohedral && rangeType.equals("trigonal")) {
      if (symmetry
          .getUnitCellInfoType(SimpleUnitCell.INFO_IS_RHOMBOHEDRAL) == 1) {
        rangeType = SimpleUnitCell.RHOMB_TO_HEX;
      } else {
        rangeType = null;
      }
    } else if (rangeType.indexOf(",") < 0 || rangeType.indexOf("a") < 0
        || rangeType.indexOf("b") < 0 || rangeType.indexOf("c") < 0) {
      // was not "a,b,c..."
      rangeType = null;
    } else {
      rangeType = null;
    }
    if (rangeType != null && range == null
        && (range = symmetry.getV0abc(rangeType, null)) == null) {
      rangeType = null;
    }
    if (rangeType == null)
      return null;
    acr.addJmolScript("unitcell " + PT.esc(rangeType));
    return range;
  }

  private void setMinMax(int dim, int kcode, int maxX, int maxY, int maxZ) {
    minXYZ = new P3i();
    maxXYZ = P3i.new3(maxX, maxY, maxZ);
    SimpleUnitCell.setMinMaxLatticeParameters(dim, minXYZ, maxXYZ, kcode);
  }

  private void setSymmetryMinMax(P3 c) {
    if (rminx > c.x)
      rminx = c.x;
    if (rminy > c.y)
      rminy = c.y;
    if (rminz > c.z)
      rminz = c.z;
    if (rmaxx < c.x)
      rmaxx = c.x;
    if (rmaxy < c.y)
      rmaxy = c.y;
    if (rmaxz < c.z)
      rmaxz = c.z;
  }

  private void setUnitCell(float[] info, M3 matUnitCellOrientation,
                           P3 unitCellOffset) {
    unitCellParams = new float[info.length];
    //this.unitCellOffset = unitCellOffset;
    for (int i = 0; i < info.length; i++)
      unitCellParams[i] = info[i];
    asc.haveUnitCell = true;
    if (asc.isTrajectory) {
      if (trajectoryUnitCells == null) {
        trajectoryUnitCells = new Lst<float[]>();
        asc.setInfo(JC.INFO_UNIT_CELLS, trajectoryUnitCells);
      }
      trajectoryUnitCells.addLast(unitCellParams);
    }
    asc.setGlobalBoolean(JC.GLOBAL_UNITCELLS);
    getSymmetry().setUnitCellFromParams(unitCellParams, false,
        unitCellParams[SimpleUnitCell.PARAM_SLOP]);
    // we need to set the auxiliary info as well, because 
    // ModelLoader creates a new symmetry object.
    if (unitCellOffset != null)
      symmetry.setOffsetPt(unitCellOffset);
    if (matUnitCellOrientation != null)
      symmetry.initializeOrientation(matUnitCellOrientation);
    setCurrentModelUCInfo(unitCellParams, unitCellOffset,
        matUnitCellOrientation);
  }

  private void setUnitCellSafely() {
    if (unitCellParams == null)
      setUnitCell(acr.unitCellParams, acr.matUnitCellOrientation,
          acr.unitCellOffset);
  }

  @SuppressWarnings("cast") // DO NOT REMOVE
  private int symmetryAddAtoms(int transX, int transY, int transZ,
                               int baseCount, int pt, int iCellOpPt,
                               P3[] cartesians, MSInterface ms, BS excludedOps,
                               int[] atomMap)
      throws Exception {
    boolean isBaseCell = (baseCount == 0);
    boolean addBonds = (atomMap != null);
    if (doPackUnitCell)
      ptOffset.set(transX, transY, transZ);

    //symmetryRange < 0 : just check symop=1 set
    //symmetryRange > 0 : check against {1 1 1}

    // if we are not checking special atoms, then this is a PDB file
    // and we return all atoms within a cubical volume around the 
    // target set. The user can later use select within() to narrow that down
    // This saves immensely on time.

    float range2 = symmetryRange * symmetryRange;
    boolean checkRangeNoSymmetry = (symmetryRange < 0);
    boolean checkRange111 = (symmetryRange > 0);
    boolean checkSymmetryMinMax = (isBaseCell && checkRange111);
    checkRange111 &= !isBaseCell;
    int nOp = symmetry.getSpaceGroupOperationCount();
    Lst<M4> lstNCS = acr.lstNCS;
    int nNCS = (lstNCS == null ? 0 : lstNCS.size());
    int nOperations = nOp + nNCS;
    nNCS = nNCS / nOp;
    /**
     * checkNearAtoms indicates that we are looking for atoms with (nearly) the
     * same cartesian position. This is important when packing and there are
     * multiple operations.
     */
    boolean checkNearAtoms = (this.checkNearAtoms
        && (nOperations > 1 || doPackUnitCell));
    boolean checkSymmetryRange = (checkRangeNoSymmetry || checkRange111);
    boolean checkDistances = (checkNearAtoms || checkSymmetryRange);
    boolean checkOps = (excludedOps != null);
    boolean addCartesian = (checkNearAtoms || checkSymmetryMinMax);
    BS bsAtoms = (acr.isMolecular ? null : asc.bsAtoms);
    FileSymmetry sym = symmetry;
    if (checkRangeNoSymmetry)
      baseCount = noSymmetryCount;
    int atomMax = firstAtom + noSymmetryCount;
    int bondAtomMin = (asc.firstAtomToBond < 0 ? atomMax : asc.firstAtomToBond);
    P3 pttemp = new P3();
    String code = null;
    float minCartDist2 = (checkOps ? SQUARED_CARTESIAN_DISTANCE_CHECK_OPS
        : SQUARED_CARTESIAN_DISTANCE_CHECK_NOOPS);
    char subSystemId = '\0';
    int j00 = (bsAtoms == null ? firstAtom : bsAtoms.nextSetBit(firstAtom));

    // loop over all symmetry operations...
    out: for (int iSym = 0; iSym < nOperations; iSym++) {

      // ignore if very first operation 
      // or this is CrystalReader and we are just checking the lattice operations
      //    and this is not a lattice operation
      // or this is an operation we have excluded
      if (isBaseCell && iSym == 0
          || crystalReaderLatticeOpsOnly && iSym > 0 && (iSym % latticeOp) != 0
          || excludedOps != null && excludedOps.get(iSym))
        continue;

      /* pt0 sets the range of points cross-checked. 
       * If we are checking special positions, then we have to check
       *   all previous atoms. 
       * If we are doing a symmetry range check relative to {1 1 1}, then
       *   we have to check only the base set. (checkRange111 true)
       * If we are doing a symmetry range check on symop=1555 (checkRangeNoSymmetry true), 
       *   then we don't check any atoms and just use the box.
       *    
       */

      int pt0 = firstAtom
          + (checkNearAtoms ? pt : checkRange111 ? baseCount : 0);
      int spinOp = (iSym >= nOp ? 0
          : asc.vibScale == 0 ? sym.getSpinOp(iSym) : asc.vibScale);
      int i0 = Math.max(firstAtom,
          (bsAtoms == null ? 0 : bsAtoms.nextSetBit(0)));
      boolean checkDistance = checkDistances;
      int spt = (iSym >= nOp ? (iSym - nOp) / nNCS : iSym);
      int cpt = spt + iCellOpPt;

      for (int i = i0; i < atomMax; i++) {
        Atom a = asc.atoms[i];
        if (bsAtoms != null && !bsAtoms.get(i))
          continue;

        if (ms == null) {
          sym.newSpaceGroupPoint(a, iSym,
              (iSym >= nOp ? lstNCS.get(iSym - nOp) : null), transX, transY,
              transZ, pttemp);
        } else {
          sym = ms.getAtomSymmetry(a, this.symmetry);
          sym.newSpaceGroupPoint(a, iSym, null, transX, transY, transZ, pttemp);
          // COmmensurate structures may use a symmetry operator
          // to changes space groups.
          code = sym.getSpaceGroupOperationCode(iSym);
          if (code != null) {
            subSystemId = code.charAt(0);
            sym = ms.getSymmetryFromCode(code);
            if (sym.getSpaceGroupOperationCount() == 0)
              finalizeSymmetry(sym);
          }
        }
        acr.fixFloatPt(pttemp, PT.FRACTIONAL_PRECISION); // LEGACY ONLY
        P3 c = P3.newP(pttemp); // cartesian position
        sym.toCartesian(c, false);
        if (doPackUnitCell) {
          sym.toUnitCellRnd(c, ptOffset);
          pttemp.setT(c);
          sym.toFractional(pttemp, false);
          acr.fixFloatPt(pttemp, PT.FRACTIONAL_PRECISION); // LEGACY ONLY
          if (!isWithinCell(ndims, pttemp, minXYZ0.x, maxXYZ0.x, minXYZ0.y,
              maxXYZ0.y, minXYZ0.z, maxXYZ0.z, packingRange)) {
            continue;
          }
        }
        if (checkSymmetryMinMax)
          setSymmetryMinMax(c);
        Atom special = null;
        if (checkDistance) {
          // for range checking, we first make sure we are not out of range
          // for the cartesian
          if (checkSymmetryRange && (c.x < rminx || c.y < rminy || c.z < rminz
              || c.x > rmaxx || c.y > rmaxy || c.z > rmaxz))
            continue;
          float minDist2 = Float.MAX_VALUE;
          // checkAll means we have to check against operations that have
          // just been done; otherwise we can check only to the base set
          int j0 = (checkAll ? asc.ac : pt0);
          String name = a.atomName;
          char id = (code == null ? a.altLoc : subSystemId);
          for (int j = j00; j < j0; j++) {
            if (bsAtoms != null && !bsAtoms.get(j))
              continue;
            P3 pc = cartesians[j - firstAtom];
            if (pc == null)
              continue;
            float d2 = c.distanceSquared(pc);
            if (checkNearAtoms && d2 < minCartDist2) {
              if (checkOps) {
                // if a matching atom is found for a model built
                // from a mix of crystallographic and noncrystallographic 
                // operators, we throw out the entire operation, not just this atom
                excludedOps.set(iSym);
                continue out;
              }
              special = asc.atoms[j];
              if ((special.atomName == null || special.atomName.equals(name))
                  && special.altLoc == id)
                break;
              special = null;
            }
            if (checkRange111 && j < baseCount && d2 < minDist2)
              minDist2 = d2;
          }
          if (checkRange111 && minDist2 > range2)
            continue;
        }
        if (checkOps) {
          // if we did not find a common atom for the first atom when checking operators,
          // we do not have to check again for any other atom.
          checkDistance = false;
        }
        int atomSite = a.atomSite;
        if (special != null) {
          if (addBonds)
            atomMap[atomSite] = special.index;
          special.bsSymmetry.set(cpt);
          special.bsSymmetry.set(spt);
        } else {
          if (addBonds)
            atomMap[atomSite] = asc.ac;
          Atom atom1 = a.copyTo(pttemp, asc);
          if (asc.bsAtoms != null)
            asc.bsAtoms.set(atom1.index);
          if (spinOp != 0 && atom1.vib != null) {
            // spinOp is making the correction for spin being a pseudoVector, not a standard vector
            sym.getSpaceGroupOperation(iSym).rotate(atom1.vib);
            atom1.vib.scale(spinOp);
          }
          if (atom1.part < 0) {
            // special negative disorder group in CifReader
            Integer key = Integer.valueOf(iSym * 1000 + 500 + atom1.part);
            if (disorderMap == null)
              disorderMap = new Hashtable<Integer, Character>();
            Character ch = disorderMap.get(key);
            if (ch == null) {
              Integer ia = Integer.valueOf(atom1.part);
              boolean isNew = (disorderMap.get(ia) == null);
              if (disorderMapMax == 0 || disorderMapMax == 'z') {
                // back to "A"
                disorderMapMax = '@';
              } else if (disorderMapMax == 'Z') {
                // allow a-z as well; use select ALTLOC like 'a' to distinguish 'a' from 'A'
                disorderMapMax = '`';
              }
              // first time will be altloc, then start incrementing altLoc
              ch = new Character(
                  isNew ? atom1.altLoc : (char) ++disorderMapMax);
              disorderMap.put(key, ch);
              if (isNew)
                disorderMap.put(ia, ch);
            }
            atom1.altLoc = ch.charValue();
          }
          atom1.atomSite = atomSite;
          if (code != null)
            atom1.altLoc = subSystemId;
          atom1.bsSymmetry = BSUtil.newAndSetBit(cpt);
          atom1.bsSymmetry.set(spt);
          if (addCartesian)
            cartesians[pt++] = c;
          Lst<Object> tensors = a.tensors;
          if (tensors != null) {
            atom1.tensors = null;
            for (int j = tensors.size(); --j >= 0;) {
              Tensor t = (Tensor) tensors.get(j);
              if (t == null)
                continue;
              if (nOp == 1)
                atom1.addTensor(t.copyTensor(), null, false);
              else
                addRotatedTensor(atom1, t, iSym, false, sym);
            }
          }
        }
      }
      if (addBonds) {
        // Clone bonds
        Bond[] bonds = asc.bonds;
        Atom[] atoms = asc.atoms;
        String key;
        for (int bondNum = asc.bondIndex0; bondNum < bondCount0; bondNum++) {
          Bond bond = bonds[bondNum];
          Atom atom1 = atoms[bond.atomIndex1];
          Atom atom2 = atoms[bond.atomIndex2];
          if (atom1 == null || atom2 == null
              || atom2.atomSetIndex < atom1.atomSetIndex)
            continue;

          int ia1 = atomMap[atom1.atomSite];
          int ia2 = atomMap[atom2.atomSite];
          if (ia1 > ia2) {
            int i = ia1;
            ia1 = ia2;
            ia2 = i;
          }
          if ((ia1 != ia2 && (ia1 >= bondAtomMin || ia2 >= bondAtomMin))
              && bondsFound.indexOf(key = "-" + ia1 + "," + ia2) < 0) {
            bondsFound.append(key);
            asc.addNewBondWithOrder(ia1, ia2, bond.order);//.distance = d;
          }
        }
      }
    }
    return pt;
  }

  /**
   * 
   * create property_part for SHELX and CIF loaders
   * 
   */
  public void setPartProperty() {
    for (int iset = asc.atomSetCount; --iset >= 0;) {
      float[] parts = new float[asc.getAtomSetAtomCount(iset)];
      for (int i = 0, ia = asc
          .getAtomSetAtomIndex(iset), n = parts.length; i < n; i++) {
        Atom a = asc.atoms[ia++];
        parts[i] = a.part;
      }
      asc.setAtomProperties("part", parts, iset, false);
    }
  }

  private void trimToUnitCell(int iAtomFirst) {
    // trim atom set based on current min/max
    Atom[] atoms = asc.atoms;
    BS bs = updateBSAtoms();
    if (acr.noPack) {
      for (int i = bs.nextSetBit(iAtomFirst); i >= 0; i = bs
          .nextSetBit(i + 1)) {
        if (!removePacking(ndims, atoms[i], minXYZ.x, maxXYZ.x, minXYZ.y,
            maxXYZ.y, minXYZ.z, maxXYZ.z, packingRange))
          bs.clear(i);
      }
    } else {
      for (int i = bs.nextSetBit(iAtomFirst); i >= 0; i = bs
          .nextSetBit(i + 1)) {
        if (!isWithinCell(ndims, atoms[i], minXYZ.x, maxXYZ.x, minXYZ.y,
            maxXYZ.y, minXYZ.z, maxXYZ.z, packingRange))
          bs.clear(i);
      }
    }
  }

  /**
   * Update asc.bsAtoms to include all atoms, or at least all atoms that are
   * still viable from the reader.
   * 
   * @return updated BS
   */
  private BS updateBSAtoms() {
    BS bs = asc.bsAtoms;
    if (bs == null)
      bs = asc.bsAtoms = BSUtil.newBitSet2(0, asc.ac);
    if (bs.nextSetBit(firstAtom) < 0)
      bs.setBits(firstAtom, asc.ac);
    return bs;
  }

  private void updateSupercellAtomSites(M4 matSuper, BS bsAtoms, float slop) {
    int n = bsAtoms.cardinality();
    Atom[] baseAtoms = new Atom[n];
    int nbase = 0;
    float slop2 = slop * slop;
    for (int i = bsAtoms.nextSetBit(0); i >= 0; i = bsAtoms.nextSetBit(i + 1)) {
      Atom a = asc.atoms[i];
      Atom p = new Atom();
      p.setT(a);
      if (matSuper != null) {
        matSuper.rotTrans(p);
        SimpleUnitCell.unitizeDimRnd(3, p, slop);
      }
      p.atomSerial = a.atomSite;// ORIGINAL atomSite
      p.atomSite = a.atomSite;
      symmetry.unitize(p);
      boolean found = false;
      for (int ib = 0; ib < nbase; ib++) {
        Atom b = baseAtoms[ib];
        if (p.atomSerial == b.atomSerial && p.distanceSquared(b) < slop2) {
          found = true;
          a.atomSite = b.atomSite;
          break;
        }
      }
      if (!found) {
        a.atomSite = p.atomSite = nbase;
        baseAtoms[nbase++] = p;
      }
    }
  }

  public FileSymmetry newFileSymmetry() {
    return new FileSymmetry();
  }

}