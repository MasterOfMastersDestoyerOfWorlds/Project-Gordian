package shell.cuts;

import java.util.ArrayList;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.math3.util.Pair;

import shell.BalanceMap;
import shell.exceptions.BalancerException;
import shell.exceptions.SegmentBalanceException;
import shell.knot.Knot;
import shell.knot.Segment;
import shell.knot.VirtualPoint;
import shell.shell.Shell;
import shell.utils.Utils;

public class CutInfo {
    public Knot knot;
    public VirtualPoint external1;
    public VirtualPoint external2;
    public Segment cutSegment1;
    public VirtualPoint kp1;
    public VirtualPoint cp1;
    public Knot superKnot;

    public Segment kpSegment;
    public ArrayList<Segment> innerNeighborSegments;

    public MultiKeyMap<Integer, Segment> innerNeighborSegmentLookup;
    public ArrayList<Segment> neighborSegments;

    public ArrayList<Pair<Segment, VirtualPoint>> neighborCutSegments;

    VirtualPoint upperCutPoint;

    boolean needTwoNeighborMatches;
    boolean bothKnotPointsInside;
    boolean bothCutPointsOutside;

    public VirtualPoint upperKnotPoint;
    public VirtualPoint upperExternal;
    public Segment upperCutSegment;
    public Segment upperMatchSegment;

    public VirtualPoint lowerKnotPoint;
    VirtualPoint lowerExternal;
    public Segment lowerCutSegment;
    public Segment lowerMatchSegment;
    Shell shell;
    SegmentBalanceException sbe;
    VirtualPoint lowerCutPoint;
    static int numCuts = 0;
    public int cutID;
    boolean bothKnotPointsOutside;
    public BalanceMap balanceMap;

    public boolean partialOverlaps;
    public boolean overlapOrientationCorrect;

    public CutInfo(Shell shell, Knot knot, VirtualPoint external1, VirtualPoint external2, Segment cutSegment1,
            VirtualPoint kp1, VirtualPoint cp1, Knot superKnot, Segment kpSegment,
            ArrayList<Segment> innerNeighborSegments, MultiKeyMap<Integer, Segment> innerNeighborSegmentLookup,
            ArrayList<Segment> neighborSegments, ArrayList<Pair<Segment, VirtualPoint>> neighborCutSegments,
            VirtualPoint topCutPoint, boolean needTwoNeighborMatches,
            boolean bothKnotPointsInside, boolean bothKnotPointsOutside, boolean bothCutPointsOutside,
            VirtualPoint upperKnotPoint, Segment upperMatchSegment, Segment upperCutSegment,
            VirtualPoint lowerKnotPoint, Segment lowerMatchSegment, Segment lowerCutSegment, BalanceMap balanceMap) {
        this.shell = shell;
        this.knot = knot;
        this.superKnot = superKnot;
        this.external1 = external1;
        this.external2 = external2;
        this.cutSegment1 = cutSegment1;
        this.kp1 = kp1;
        this.cp1 = cp1;
        this.kpSegment = kpSegment;

        numCuts++;
        cutID = numCuts;

        this.innerNeighborSegments = innerNeighborSegments;
        this.innerNeighborSegmentLookup = innerNeighborSegmentLookup;
        this.neighborSegments = neighborSegments;
        this.neighborCutSegments = neighborCutSegments;

        this.needTwoNeighborMatches = needTwoNeighborMatches;
        this.bothKnotPointsInside = bothKnotPointsInside;
        this.bothCutPointsOutside = bothCutPointsOutside;
        this.bothKnotPointsOutside = bothKnotPointsOutside;

        this.upperKnotPoint = upperKnotPoint;
        this.upperCutPoint = topCutPoint;
        this.upperCutSegment = upperCutSegment;
        this.upperMatchSegment = upperMatchSegment;
        this.upperExternal = upperMatchSegment.getOther(upperKnotPoint);

        this.lowerKnotPoint = lowerKnotPoint;
        this.lowerCutSegment = lowerCutSegment;
        this.lowerCutPoint = lowerCutSegment.getOther(lowerKnotPoint);
        this.lowerMatchSegment = lowerMatchSegment;
        this.lowerExternal = lowerMatchSegment.getOther(lowerKnotPoint);
        this.balanceMap = balanceMap;

        this.sbe = new SegmentBalanceException(shell, null, this);
    }

    public CutInfo(Shell shell, VirtualPoint lowerKnotPoint, VirtualPoint lowerCutPoint, Segment lowerCutSegment,
            VirtualPoint lowerExternal,
            VirtualPoint upperKnotPoint, VirtualPoint upperCutPoint, Segment upperCutSegment,
            VirtualPoint upperExternal,
            Knot superKnot, BalanceMap balanceMap) throws BalancerException {

        this.external1 = lowerExternal;
        this.external2 = upperExternal;
        Segment s51 = lowerKnotPoint.getClosestSegment(lowerExternal, null);
        Segment s52 = upperKnotPoint.getClosestSegment(upperExternal, s51);
        VirtualPoint externalPoint51 = s51.getOther(lowerKnotPoint);
        VirtualPoint externalPoint52 = s52.getOther(upperKnotPoint);
        numCuts++;
        cutID = numCuts;
        this.shell = shell;
        this.knot = superKnot;
        this.superKnot = superKnot;

        this.cutSegment1 = lowerCutSegment;
        this.innerNeighborSegments = new ArrayList<>();
        this.neighborCutSegments = new ArrayList<>();

        this.lowerExternal = externalPoint51;
        this.lowerKnotPoint = lowerKnotPoint;
        this.lowerCutSegment = lowerCutSegment;
        this.lowerCutPoint = lowerCutSegment.getOther(lowerKnotPoint);
        this.lowerMatchSegment = s51;

        this.neighborSegments = new ArrayList<>();

        this.upperKnotPoint = upperKnotPoint;
        this.upperMatchSegment = s52;
        this.upperCutPoint = upperCutPoint;
        this.upperExternal = externalPoint52;
        this.upperCutSegment = upperCutSegment;

        if (this.upperCutSegment.partialOverlaps(this.lowerCutSegment)) {
            this.partialOverlaps = true;
            if (lowerKnotPoint.equals(upperCutPoint) || lowerCutPoint.equals(upperKnotPoint)
                    || lowerKnotPoint.equals(upperKnotPoint)) {
                this.overlapOrientationCorrect = false;
            } else if (lowerCutPoint.equals(upperCutPoint)) {
                this.overlapOrientationCorrect = true;
            }
        } else {
            this.partialOverlaps = false;
            this.overlapOrientationCorrect = true;
        }

        this.sbe = new SegmentBalanceException(shell, null, this);
        if (balanceMap == null && this.overlapOrientationCorrect) {
            this.balanceMap = new BalanceMap(knot, this.sbe);
            this.balanceMap.addCut(lowerKnotPoint, lowerCutPoint);
            this.balanceMap.addCut(upperKnotPoint, upperCutPoint);
            this.balanceMap.addExternalMatch(lowerKnotPoint, externalPoint51, null);
            this.balanceMap.addExternalMatch(upperKnotPoint, externalPoint52, null);
        } else if (balanceMap != null) {
            this.balanceMap = balanceMap;
            balanceMap.sbe = this.sbe;
        }

    }

    public CutInfo(Shell shell, Segment cutSegmentFinal, Segment matchSegment1Final, Segment cutSegment2Final,
            Segment matchSegment2Final, Knot knot) {
        numCuts++;
        cutID = numCuts;
        this.shell = shell;
        this.knot = knot;
        this.superKnot = knot;
        this.cutSegment1 = cutSegmentFinal;
        this.lowerCutSegment = cutSegmentFinal;
        this.lowerMatchSegment = matchSegment1Final;
        this.upperCutSegment = cutSegment2Final;
        this.upperMatchSegment = matchSegment2Final;
        this.superKnot = knot;
    }

    public CutInfo(CutInfo c) {
        this.shell = c.shell;
        this.knot = c.knot;
        this.superKnot = c.superKnot;
        this.external1 = c.external1;
        this.external2 = c.external2;
        this.cutSegment1 = c.cutSegment1;
        this.kp1 = c.kp1;
        this.cp1 = c.cp1;
        this.kpSegment = c.kpSegment;

        this.cutID = c.cutID;

        this.innerNeighborSegments = c.innerNeighborSegments;
        this.innerNeighborSegmentLookup = c.innerNeighborSegmentLookup;
        this.neighborSegments = c.neighborSegments;
        this.neighborCutSegments = c.neighborCutSegments;

        this.needTwoNeighborMatches = c.needTwoNeighborMatches;
        this.bothKnotPointsInside = c.bothKnotPointsInside;
        this.bothCutPointsOutside = c.bothCutPointsOutside;
        this.bothKnotPointsOutside = c.bothKnotPointsOutside;

        this.upperKnotPoint = c.upperKnotPoint;
        this.upperCutPoint = c.upperCutPoint;
        this.upperCutSegment = c.upperCutSegment;
        this.upperMatchSegment = c.upperMatchSegment;
        this.upperExternal = c.upperExternal;

        this.lowerKnotPoint = c.lowerKnotPoint;
        this.lowerCutSegment = c.lowerCutSegment;
        this.lowerCutPoint = c.lowerCutPoint;
        this.lowerMatchSegment = c.lowerMatchSegment;
        this.lowerExternal = c.lowerExternal;
        this.balanceMap = c.balanceMap;

        this.partialOverlaps = c.partialOverlaps;
        this.overlapOrientationCorrect = c.overlapOrientationCorrect;

        this.sbe = c.sbe;

    }

    @Override
    public String toString() {
        return "ID: " + cutID + " minKnot: " + knot + " | external " + external1 + " | neighbor: " + external2
                + " | cutSegment1: "
                + cutSegment1 + " | kp1: " + kp1 + " | cp1: " + cp1 + " | superKnot: " + superKnot + " | kpSegment: "
                + kpSegment +

                " \ninnerNeighborSegments: " + innerNeighborSegments + " neighborSegments: "
                + neighborSegments + " innerNeighborSegmentLookup " + innerNeighborSegmentLookup + " neighborCuts: "
                + Utils.pairsToString(neighborCutSegments) + "\n" +

                " upperCutPointIsOutside: " + needTwoNeighborMatches + " bothKnotPOintsInside: "
                + bothKnotPointsInside + "\n" +

                " lowerCutSegment: " + lowerCutSegment + " lowerKnotPoint: " + lowerKnotPoint + " lowerCutPoint"
                + lowerCutPoint + " lowerMatchSegment: "
                + lowerMatchSegment + " lowerExternal: " + lowerExternal + "\n" +

                " upperCutSegment: " + upperCutSegment + " upperKnotPoint: " + upperKnotPoint + " upperCutPoint"
                + upperCutPoint + " upperMatchSegment: "
                + upperMatchSegment + " upperExternal: " + upperExternal;
    }

    public SegmentBalanceException genNewSegmentBalanceException() {

        return new SegmentBalanceException(shell, null, this);
    }

    public CutInfo copyAndSwapExternals() throws SegmentBalanceException {
        CutInfo c = new CutInfo(this);
        Segment s41 = this.upperKnotPoint.getClosestSegment(this.external1, null);
        Segment s42 = this.lowerKnotPoint.getClosestSegment(this.external2, s41);
        VirtualPoint externalPoint41 = s41.getOther(this.upperKnotPoint);
        VirtualPoint externalPoint42 = s42.getOther(this.lowerKnotPoint);
        c.lowerMatchSegment = s41;
        c.upperMatchSegment = s42;
        c.external1 = this.external2;
        c.external2 = this.external1;
        c.lowerExternal = externalPoint41;
        c.upperExternal = externalPoint42;

        c.sbe = new SegmentBalanceException(shell, null, c);
        if (this.overlapOrientationCorrect) {
            c.balanceMap = new BalanceMap(knot, c.sbe);
            c.balanceMap.addCut(lowerKnotPoint, lowerCutPoint);
            c.balanceMap.addCut(upperKnotPoint, upperCutPoint);
            c.balanceMap.addExternalMatch(lowerKnotPoint, externalPoint42, null);
            c.balanceMap.addExternalMatch(upperKnotPoint, externalPoint41, null);
        }
        if (!c.lowerMatchSegment.contains(c.lowerExternal)) {
            throw new SegmentBalanceException(shell, null, this);
        }
        return c;
    }

    public VirtualPoint getExternalMatchPointFromCutSegment(VirtualPoint externalKnot, Segment cutSegment,
            VirtualPoint exclude) {
        if (cutSegment.id == lowerCutSegment.id && externalKnot.contains(lowerExternal)
                && (exclude == null || exclude.id != lowerExternal.id)) {
            return lowerExternal;
        } else if (cutSegment.id == upperCutSegment.id && externalKnot.contains(upperExternal)
                && (exclude == null || exclude.id != upperExternal.id)) {
            return upperExternal;
        }
        return null;
    }

    public VirtualPoint getKnotPointFromCutSegment(VirtualPoint externalKnot, Segment cutSegment,
            VirtualPoint exclude) {
        if (cutSegment.id == lowerCutSegment.id && externalKnot.contains(lowerExternal)
                && (exclude == null || exclude.id != lowerKnotPoint.id)) {
            return lowerKnotPoint;
        } else if (cutSegment.id == upperCutSegment.id && externalKnot.contains(upperExternal)
                && (exclude == null || exclude.id != upperKnotPoint.id)) {
            return upperKnotPoint;
        }
        return null;
    }

    public Segment getCutSegmentFromKnotPoint(VirtualPoint prevMatchPoint) {
        if (prevMatchPoint.id == lowerKnotPoint.id) {
            return lowerCutSegment;
        } else if (prevMatchPoint.id == upperKnotPoint.id) {
            return upperCutSegment;
        }
        return null;
    }

    public boolean hasKnotPoint(VirtualPoint nextMatchPoint) {
        if (lowerKnotPoint.id == nextMatchPoint.id || upperKnotPoint.id == nextMatchPoint.id) {
            return true;
        }
        return false;
    }
}
