package shell;

import java.util.ArrayList;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.math3.util.Pair;

public class FixedCut implements FixedCutInterface {
    Knot knot;
    VirtualPoint external1;
    VirtualPoint external2;
    Segment cutSegment1;
    VirtualPoint kp1;
    VirtualPoint cp1;
    Knot superKnot;

    Segment kpSegment;
    ArrayList<Segment> innerNeighborSegments;

    MultiKeyMap<Integer, Segment> innerNeighborSegmentLookup;
    ArrayList<Segment> neighborSegments;

    Segment upperCutSegment;
    ArrayList<Pair<Segment, VirtualPoint>> neighborCutSegments;

    VirtualPoint topCutPoint;

    boolean needTwoNeighborMatches;
    boolean bothKnotPointsInside;
    boolean bothCutPointsOutside;

    VirtualPoint upperKnotPoint;

    Segment upperMatchSegment;
    VirtualPoint lowerKnotPoint;
    Segment lowerCutSegment;
    Shell shell;
    CutInfo c;
    CutEngine cutEngine;
    protected SegmentBalanceException sbe;

    public FixedCut(CutInfo c) {
        this.shell = c.shell;
        this.cutEngine = shell.cutEngine;
        this.knot = c.knot;
        this.external1 = c.external1;
        this.external2 = c.external2;
        this.cutSegment1 = c.cutSegment1;
        this.kp1 = c.kp1;
        this.cp1 = c.cp1;
        this.superKnot = c.superKnot;
        this.kpSegment = c.kpSegment;
        this.innerNeighborSegments = c.innerNeighborSegments;
        this.innerNeighborSegmentLookup = c.innerNeighborSegmentLookup;
        this.neighborSegments = c.neighborSegments;
        this.upperCutSegment = c.upperCutSegment;
        this.neighborCutSegments = c.neighborCutSegments;
        this.topCutPoint = c.topCutPoint;
        this.needTwoNeighborMatches = c.needTwoNeighborMatches;
        this.bothKnotPointsInside = c.bothKnotPointsInside;
        this.bothCutPointsOutside = c.bothCutPointsOutside;
        this.upperKnotPoint = c.upperKnotPoint;
        this.upperMatchSegment = c.upperMatchSegment;
        this.lowerKnotPoint = c.lowerKnotPoint;
        this.lowerCutSegment = c.lowerCutSegment;
        this.c = c;
        this.sbe = c.sbe;
    }

    @Override
    public String toString() {
        return " FixedCut : minKnot: " + knot + " | external " + external1 + " | neighbor: " + external2
                + " | Lower Cut: "
                + cutSegment1 + " | kp: " + kp1
                + " | vp: " + cp1 + " | superKnot: " + superKnot + " | kpSegment: " + kpSegment
                + " \ninnerNeighborSegments: " + innerNeighborSegments + " neighborSegments: "
                + neighborSegments + " upperCutSegment: " + upperCutSegment + " neighborCuts: "
                + Utils.pairsToString(neighborCutSegments) +
                " upperCutPointIsOutside: " + needTwoNeighborMatches + " bothKnotPOintsInside: "
                + bothKnotPointsInside + " upperKnotPoint: " + upperKnotPoint + " upperMatchSegment: "
                + upperMatchSegment
                + " ex2: " + upperMatchSegment.getOther(upperKnotPoint);
    }

    public CutMatchList findCutMatchListFixedCut()
            throws SegmentBalanceException {

        if (needTwoNeighborMatches && !bothCutPointsOutside) {
            shell.buff.add("findCutMatchListFixedCutNeedTwoMatches");

            return new FixedCutTwoMatches(c).findCutMatchListFixedCut();
        } else if (bothKnotPointsInside && !bothCutPointsOutside) {
            shell.buff.add("findCutMatchListBothCutsInside");
            return new FixedCutBothCutsInside(c).findCutMatchListFixedCut();
        } else if (bothCutPointsOutside) {
            shell.buff.add("findCutMatchListBothCutsOutside");
            return new FixedCutBothCutsOutside(c).findCutMatchListFixedCut();

        }
        cutEngine.totalCalls++;
        if (cutEngine.cutLookup.containsKey(knot.id, external2.id, kp1.id, cp1.id, superKnot.id)) {
            cutEngine.resolved++;
            // return cutLookup.get(knot.id, external2.id, kp1.id, cp1.id,
            // superKnot.id).copy();
        }

        if (!(knot.contains(cutSegment1.first) && knot.contains(cutSegment1.last))) {
            shell.buff.add(knot);
            shell.buff.add(cutSegment1);
            new CutMatchList(shell, sbe);
            throw new SegmentBalanceException(sbe);
        }
        if ((knot.contains(external1) || knot.contains(external2))) {
            float z = 1 / 0;
        }

        ArrayList<VirtualPoint> innerNeighborSegmentsFlattened = new ArrayList<>();
        for (Segment s : innerNeighborSegments) {
            innerNeighborSegmentsFlattened.add(s.first);
            innerNeighborSegmentsFlattened.add(s.last);
        }
        double minDelta = Double.MAX_VALUE;
        int overlapping = -1;
        CutMatchList result = null;
        for (int a = 0; a < knot.knotPoints.size(); a++) {

            VirtualPoint knotPoint21 = knot.knotPoints.get(a);
            VirtualPoint knotPoint22 = knot.knotPoints.get(a + 1 >= knot.knotPoints.size() ? 0 : a + 1);
            Segment cutSegment2 = knot.getSegment(knotPoint21, knotPoint22);
            // I think that the continue state instead of being do the ct sgments partially
            // overlapp each other, should instead
            // be like
            if (cutSegment1.partialOverlaps(cutSegment2) && !cutSegment2.equals(kpSegment)) {
                shell.buff.add("Checking: " + cutSegment2);

                boolean leftHasOneOut = CutEngine.marchUntilHasOneKnotPoint(cutSegment1.first, cutSegment1,
                        cutSegment2, kp1, upperKnotPoint, knot);
                boolean rightHasOneOut = CutEngine.marchUntilHasOneKnotPoint(cutSegment1.last, cutSegment1,
                        cutSegment2, kp1, upperKnotPoint, knot);
                shell.buff.add(leftHasOneOut + " " + rightHasOneOut);
                shell.buff.add("!(leftHasOneOut || rightHasOneOut)" + !(leftHasOneOut || rightHasOneOut));
                shell.buff
                        .add("(cutSegment1.contains(kp1) XOR cutSegment2.contains(kp1)" + kp1 + " " + cutSegment2 + " "
                                + ((cutSegment2.contains(kp1) || cutSegment2.contains(c.upperKnotPoint))));
                boolean skipFlag = true;
                if (skipFlag || !(leftHasOneOut || rightHasOneOut)
                        || ((cutSegment2.contains(kp1) || cutSegment2.contains(c.upperKnotPoint)))) {
                    shell.buff.add("ONE SIDE WOULD BE UNBALANCED " + cutSegment2);

                    continue;
                }

            }
            if (cutSegment1.equals(cutSegment2)) {
                if (needTwoNeighborMatches) {
                    shell.buff.add("Skipping: " + cutSegment2);
                    continue;
                }
                shell.buff.add("ONLY YOUUUUUUUUU :" + cutSegment2);
                Segment s11 = kp1.getClosestSegment(external1, null);
                Segment s12 = cp1.getClosestSegment(external2, s11);
                CutMatchList cutMatch = new CutMatchList(shell, sbe);
                cutMatch.addCut(cutSegment1, s11, s12,
                        kp1, cp1, c, false, false);
                boolean cutPointsAcross = false;
                for (Segment s : innerNeighborSegments) {
                    if (s.contains(cp1) && s.contains(kp1)) {
                        cutPointsAcross = true;
                    }
                }

                boolean hasSegment = cutPointsAcross;

                if (cutMatch.delta < minDelta && !hasSegment) {
                    result = cutMatch;
                    minDelta = cutMatch.delta;
                    overlapping = 1;
                    shell.buff.add("UPDATING MINDELTA " + minDelta);

                }
            } else {
                double delta = Double.MAX_VALUE;
                VirtualPoint cp2 = knotPoint22;
                VirtualPoint kp2 = knotPoint21;

                // boolean orphanFlag = wouldOrphan(cp1, kp1, cp2, kp2,
                // knot.knotPointsFlattened);

                Segment s11 = kp1.getClosestSegment(external1, null);
                Segment s12 = kp2.getClosestSegment(external2, s11);
                
                boolean hasSegment = canCutSegment(kp2, s12, cp2, cutSegment2, innerNeighborSegmentsFlattened);
               
                CutMatchList internalCuts1 = null;
                CutMatchList cutMatch1 = null;
                double d1 = Double.MAX_VALUE;
                if (!hasSegment) {
                    shell.buff.currentDepth++;
                    internalCuts1 = cutEngine.internalPathEngine.calculateInternalPathLength(kp1, cp1, external1, kp2,
                            cp2,
                            external2, knot);
                    shell.buff.currentDepth--;
                    
                    cutMatch1 = new CutMatchList(shell, sbe);
                    cutMatch1.addTwoCut(cutSegment1, cutSegment2, s11,
                            s12, kp1,
                            kp2, internalCuts1, c, false);

                    d1 = cutMatch1.delta;

                    delta = d1 < delta ? d1 : delta;
                }

                // boolean orphanFlag2 = wouldOrphan(cp1, kp1, kp2, cp2,
                // knot.knotPointsFlattened);

                Segment s21 = kp1.getClosestSegment(external1, null);
                Segment s22 = cp2.getClosestSegment(external2, s21);


                CutMatchList internalCuts2 = null;
                CutMatchList cutMatch2 = null;
                double d2 = Double.MAX_VALUE;
                boolean hasSegment2 = canCutSegment(cp2, s22, kp2, cutSegment2, innerNeighborSegmentsFlattened);
                if (!hasSegment2) {
                    shell.buff.currentDepth++;
                    internalCuts2 = cutEngine.internalPathEngine.calculateInternalPathLength(kp1, cp1, external1, cp2,
                            kp2,
                            external2, knot);
                    shell.buff.currentDepth--;

                    cutMatch2 = new CutMatchList(shell, sbe);
                    cutMatch2.addTwoCut(cutSegment1, cutSegment2, s21,
                            s22, kp1,
                            cp2, internalCuts2, c, false);

                    d2 = cutMatch2.delta;

                    delta = d2 < delta ? d2 : delta;
                }

                if (delta < minDelta) {
                    if (!hasSegment && delta == d1) {
                        result = cutMatch1;
                    } else {
                        result = cutMatch2;

                    }

                    minDelta = delta;
                    overlapping = 2;
                }

            }
        }
        if (result!= null && overlapping == 1) {
            return result;
        } else if (result!= null && overlapping == 2) {
            return result;

        } else {
            shell.buff.add("No Available CUTS!");
            CutMatchList cml = new CutMatchList(shell, sbe);
            cml.addDumbCutMatch(knot, superKnot);
            throw new SegmentBalanceException(sbe);
        }
    }

    public boolean canCutSegment(VirtualPoint cp2, Segment s22, VirtualPoint kp2, Segment cutSegment2, ArrayList<VirtualPoint>innerNeighborSegmentsFlattened){
        
        boolean innerNeighbor2 = false;
        for (Segment s : innerNeighborSegments) {
            if (s.contains(cp2)) {
                innerNeighbor2 = true;
            }
        }

        boolean replicatesNeighbor2 = false;
        for (Segment s : neighborSegments) {
            if (s.equals(s22)) {
                replicatesNeighbor2 = false;
            }
        }

        boolean outerNeighbor2 = false;
        for (Segment s : neighborSegments) {
            if (s.contains(cp2)) {
                outerNeighbor2 = true;
            }
        }

        boolean cutPointsAcross2 = false;
        for (Segment s : innerNeighborSegments) {
            if (s.contains(cp1) && s.contains(kp2)) {
                cutPointsAcross2 = true;
            }
        }
        boolean neighborIntersect2 = false;
        if (innerNeighborSegmentsFlattened.contains(cp1) && innerNeighborSegmentsFlattened.contains(cp2)) {
            neighborIntersect2 = true;
        }
        boolean hasSegment2 = replicatesNeighbor2
                || (innerNeighbor2 && outerNeighbor2) || neighborIntersect2 || s22.equals(upperCutSegment);
        // false;//
        // superKnot.hasSegment(s22)
        // ||
        // kpSegment.contains(cp2);

        if (hasSegment2) {
            shell.buff.add("REEE cutSeg1: " + cutSegment1 + " cutSeg2: " + cutSegment2 + " s22: " + s22
                    + " cp2 :" + cp2 + " kpSegment " + kpSegment);

            shell.buff.add("hasSegment2: " + hasSegment2 + " " + replicatesNeighbor2 + " " + innerNeighbor2
                    + " " + outerNeighbor2 + " " + " " + neighborIntersect2 + " "
                    + s22.equals(upperCutSegment));
        }
        return hasSegment2;
    }

}