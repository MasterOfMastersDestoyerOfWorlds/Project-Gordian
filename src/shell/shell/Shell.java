package shell.shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.math3.util.Pair;

import shell.DistanceMatrix;
import shell.PointSet;
import shell.cuts.CutEngine;
import shell.exceptions.BalancerException;
import shell.exceptions.IdDoesNotExistException;
import shell.exceptions.IdsNotConcurrentException;
import shell.exceptions.SegmentBalanceException;
import shell.knot.Knot;
import shell.knot.Point;
import shell.knot.Run;
import shell.knot.Segment;
import shell.knot.VirtualPoint;
import shell.objects.PointND;
import shell.utils.RunListUtils;
import shell.utils.StringBuff;

/**
 * This class represents a list of some points in the point set. Initially each
 * shell is a convex hull, but they are eventually combined together to form the
 * optimal tsp path and they lose their convex property
 */

public class Shell extends LinkedList<PointND> {
    private static final long serialVersionUID = -5904334592585016845L;
    public static int failed = 0;
    private Shell child;
    ArrayList<VirtualPoint> visited;
    public ArrayList<VirtualPoint> unvisited;
    public HashMap<Integer, VirtualPoint> pointMap = new HashMap<Integer, VirtualPoint>();
    public DistanceMatrix distanceMatrix;
    public String knotName;

    public CutEngine cutEngine = new CutEngine(this);

    public StringBuff buff = new StringBuff();

    int breakCount = 0;
    int runCount = 0;

    boolean skipHalfKnotFlag = true;

    public Shell() {
        pointMap = new HashMap<>();
        unvisited = new ArrayList<VirtualPoint>();
        visited = new ArrayList<VirtualPoint>();
    }

    public ArrayList<VirtualPoint> createKnots() {
        ArrayList<VirtualPoint> knots = new ArrayList<>();
        @SuppressWarnings("unchecked")
        ArrayList<VirtualPoint> toVisit = (ArrayList<VirtualPoint>) unvisited.clone();
        ArrayList<VirtualPoint> runList = new ArrayList<>();
        VirtualPoint mainPoint = toVisit.get(0);

        boolean endpointReached = false;
        VirtualPoint endPoint1 = null;
        VirtualPoint endPoint2 = null;
        while (toVisit.size() > 0 || runList.size() > 0) {
            toVisit.remove(mainPoint);
            Segment potentialSegment1 = mainPoint.getPointer(1);
            Point pointer1 = (Point) potentialSegment1.getOtherKnot(mainPoint.topGroup);

            Segment potentialSegment11 = pointer1.topGroup.getPointer(1);
            Point pointer11 = (Point) potentialSegment11.getOtherKnot(pointer1.topGroup);

            Segment potentialSegment12 = pointer1.topGroup.getPointer(2);
            Point pointer12 = (Point) potentialSegment12.getOtherKnot(pointer1.topGroup);

            Segment potentialSegment2 = mainPoint.getPointer(2);
            Point pointer2 = (Point) potentialSegment2.getOtherKnot(mainPoint.topGroup);

            Segment potentialSegment21 = pointer2.topGroup.getPointer(1);
            Point pointer21 = (Point) potentialSegment21.getOtherKnot(pointer2.topGroup);

            Segment potentialSegment22 = pointer2.topGroup.getPointer(2);
            Point pointer22 = (Point) potentialSegment22.getOtherKnot(pointer2.topGroup);

            VirtualPoint vp1 = pointer1.topGroup;
            VirtualPoint vp11 = pointer11.topGroup;
            VirtualPoint vp12 = pointer12.topGroup;
            VirtualPoint vp2 = pointer2.topGroup;
            VirtualPoint vp21 = pointer21.topGroup;
            VirtualPoint vp22 = pointer22.topGroup;
            Point matchEndPoint = null;
            Point matchBasePoint = null;
            VirtualPoint matchPoint = null;

            Segment matchSegment = null;
            boolean inKnots1 = knots.contains(vp1);
            boolean inKnots2 = knots.contains(vp2);
            // need to check that we haven't already match the end of the run in run case
            if (mainPoint.equals(vp11) && potentialSegment1.equals(potentialSegment11) && !inKnots1
                    && vp1.numMatches < 2 && (mainPoint.numMatches == 0 || !mainPoint.s1.contains(pointer1))) {
                matchPoint = vp1;
                matchEndPoint = pointer1;
                matchBasePoint = pointer11;
                matchSegment = potentialSegment11;
            } else if (mainPoint.equals(vp12) && potentialSegment1.equals(potentialSegment12) && !inKnots1
                    && vp1.numMatches < 2 && (mainPoint.numMatches == 0 || !mainPoint.s1.contains(pointer1))) {
                matchPoint = vp1;
                matchEndPoint = pointer1;
                matchBasePoint = pointer12;
                matchSegment = potentialSegment12;
            } else if ((mainPoint.equals(vp21)) && potentialSegment2.equals(potentialSegment21) && !inKnots2
                    && vp2.numMatches < 2 && (mainPoint.numMatches == 0 || !mainPoint.s1.contains(pointer2))) {
                matchPoint = vp2;
                matchEndPoint = pointer2;
                matchBasePoint = pointer21;
                matchSegment = potentialSegment21;
            } else if (mainPoint.equals(vp22) && potentialSegment2.equals(potentialSegment22) && !inKnots2
                    && vp2.numMatches < 2 && (mainPoint.numMatches == 0 || !mainPoint.s1.contains(pointer2))) {
                matchPoint = vp2;
                matchEndPoint = pointer2;
                matchBasePoint = pointer22;
                matchSegment = potentialSegment22;
            }
            if (matchPoint != null) {
                if (mainPoint.numMatches == 0) {
                    mainPoint.setMatch1(matchPoint, matchEndPoint, matchBasePoint, matchSegment);
                } else {
                    mainPoint.checkAndSwap2(matchPoint, matchEndPoint, matchBasePoint, matchSegment);
                }
                if (matchPoint.numMatches == 0) {
                    matchPoint.setMatch1(mainPoint, matchBasePoint, matchEndPoint, matchSegment);
                } else {
                    matchPoint.checkAndSwap2(mainPoint, matchBasePoint, matchEndPoint, matchSegment);
                }
                mainPoint.numMatches++;
                matchPoint.numMatches++;
                if (!runList.contains(mainPoint)) {
                    if (endpointReached) {
                        runList.add(0, mainPoint);
                    } else {
                        runList.add(mainPoint);
                    }
                }
                if (endpointReached) {
                    runList.add(0, matchPoint);
                } else {
                    runList.add(matchPoint);
                }
                boolean mainIsFull = false;
                if (mainPoint.numMatches == 2) {
                    unvisited.remove(mainPoint);
                    visited.add(mainPoint);
                    mainIsFull = true;
                }
                if (matchPoint.numMatches == 2) {
                    unvisited.remove(matchPoint);
                    visited.add(matchPoint);
                    if (mainIsFull) {
                        VirtualPoint first = runList.get(0);
                        VirtualPoint last = runList.get(runList.size() - 1);

                        if (runList.contains(first.match1) && runList.contains(first.match2)
                                && runList.contains(last.match1) && runList.contains(last.match2)) {
                            if (runList.get(0).equals(runList.get(runList.size() - 1))) {
                                runList.remove(runList.size() - 1);
                            }
                            Knot k = new Knot(runList, this);
                            knots.add(k);
                            runList = new ArrayList<>();
                            if (toVisit.size() == 0) {
                                return knots;
                            }
                            mainPoint = toVisit.get(0);
                            endpointReached = false;
                            continue;
                        }

                    }
                }

                mainPoint = matchPoint;
            } else {
                if (endpointReached) {
                    if (runList.size() == 2 && toVisit.size() == 0 && runList.get(0).isKnot && runList.get(1).isKnot) {

                        Knot k = new Knot(runList, this);
                        knots.add(k);
                        return knots;
                    }
                    endPoint2 = mainPoint;

                    boolean knotFlag = false;
                    runList = RunListUtils.flattenRunPoints(runList, false);
                    RunListUtils.fixRunList(runList, runList.size() - 1);
                    for (VirtualPoint vp : runList) {
                        vp.topGroup = vp;
                        vp.group = vp;
                        for (VirtualPoint kp : vp.knotPointsFlattened) {
                            kp.topGroup = vp;
                        }
                    }
                    if (runList.size() > 2) {

                        for (int i = 0; i < runList.size() && runList.size() > 1; i++) {

                            VirtualPoint vp = runList.get(i);
                            Segment s1 = vp.getFirstUnmatched(runList);
                            VirtualPoint other = s1.getOtherKnot(vp).topGroup;
                            Segment s2 = other.getFirstUnmatched(runList);
                            if (vp.match1.isKnot && vp.shouldJoinKnot((Knot) vp.match1)) {

                                knotFlag = true;
                                makeHalfKnot(runList, vp, vp.match1);
                                i = -1;

                            } else if (vp.isKnot && vp.match1.shouldKnotConsume((Knot) vp)) {

                                knotFlag = true;
                                makeHalfKnot(runList, vp.match1, vp);
                                i = -1;

                            } else if (vp.isKnot && vp.match2 != null && vp.match2.shouldKnotConsume((Knot) vp)) {

                                knotFlag = true;
                                makeHalfKnot(runList, vp.match2, vp);
                                i = -1;

                            } else if (s1.equals(s2) && runList.contains(other)) {
                                if (other.match1.isKnot && other.shouldJoinKnot((Knot) other.match1)) {
                                    knotFlag = true;
                                    makeHalfKnot(runList, other, other.match1);
                                    i = -1;
                                } else if (vp.match1.isKnot && vp.shouldJoinKnot((Knot) vp.match1)) {

                                    knotFlag = true;
                                    makeHalfKnot(runList, vp, vp.match1);
                                    i = -1;
                                } else {
                                    knotFlag = true;
                                    makeHalfKnot(runList, vp, other);
                                    i = -1;
                                }
                            } else if (vp.isKnot && runList.contains(other)
                                    && other.shouldKnotConsumeExclude((Knot) vp, runList)) {
                                knotFlag = true;
                                makeHalfKnot(runList, vp, other);
                                i = -1;
                            } else if (vp.isKnot && !runList.contains(other)) {
                                // TODO: Need to figure out what to do here, if the other's next best point is
                                // also in the runlist, form a knot!
                            }
                        }

                    }
                    if (true) {
                        double minDistLeft = Double.MAX_VALUE;
                        VirtualPoint leftEndMinPoint = null;
                        VirtualPoint leftEnd = runList.get(0);
                        boolean changed = false;
                        if (!leftEnd.isKnot) {
                            for (VirtualPoint runPoint : runList) {
                                if (!leftEnd.hasMatch(runPoint) && leftEnd.id != runPoint.id) {
                                    Segment s = runPoint.getClosestSegment(leftEnd, null);
                                    if (s.distance < minDistLeft) {
                                        leftEndMinPoint = runPoint;
                                        minDistLeft = s.distance;
                                        if (!runPoint.isKnot) {
                                            changed = true;
                                        }
                                    }
                                }
                            }
                        }
                        if (changed && leftEnd.shouldJoinEndsExclude(leftEndMinPoint, runList)) {
                            knotFlag = true;
                            makeHalfKnot(runList, leftEnd, leftEndMinPoint);
                        }
                        VirtualPoint rightEnd = runList.get(runList.size() - 1);
                        double minDistRight = Double.MAX_VALUE;
                        changed = false;
                        VirtualPoint rightEndMinPoint = null;
                        if (!rightEnd.isKnot) {
                            for (VirtualPoint runPoint : runList) {
                                if (!rightEnd.hasMatch(runPoint)
                                        && rightEnd.id != runPoint.id) {
                                    Segment s = runPoint.getClosestSegment(rightEnd, null);
                                    if (s.distance < minDistRight) {
                                        rightEndMinPoint = runPoint;
                                        minDistRight = s.distance;
                                        if (!runPoint.isKnot) {
                                            changed = true;
                                        }
                                    }
                                }
                            }
                        }
                        if (changed && rightEnd.shouldJoinEndsExclude(rightEndMinPoint, runList)) {
                            knotFlag = true;
                            makeHalfKnot(runList, rightEnd, rightEndMinPoint);
                        }
                    }
                    if (knotFlag) {
                        for (VirtualPoint vp : runList) {
                            vp.reset();
                            knots.add(vp);
                        }

                        runList = new ArrayList<>();
                        if (toVisit.size() == 0) {
                            return knots;
                        }
                        mainPoint = toVisit.get(0);
                        endpointReached = false;
                        halfKnotCount++;

                        continue;
                    }

                    visited.add(endPoint1);
                    unvisited.remove(endPoint1);

                    visited.add(endPoint2);
                    unvisited.remove(endPoint2);

                    Run k = new Run(runList, this);
                    knots.add(k);
                    runList = new ArrayList<>();
                    if (toVisit.size() == 0) {
                        return knots;
                    }
                    mainPoint = toVisit.get(0);
                    endpointReached = false;
                } else {

                    endPoint1 = mainPoint;
                    endpointReached = true;
                    if (runList.size() == 0) {
                        knots.add(mainPoint);
                        runList = new ArrayList<>();
                        if (toVisit.size() == 0) {
                            return knots;
                        }
                        mainPoint = toVisit.get(0);
                        endpointReached = false;
                        continue;
                    }
                    mainPoint = runList.get(0);
                }
            }
        }
        return knots;
    }

    public void makeHalfKnot(ArrayList<VirtualPoint> runList, VirtualPoint vp, VirtualPoint other) {
        int vpIdx = runList.indexOf(vp);
        int otherIdx = runList.indexOf(other);
        if (vpIdx > otherIdx) {
            VirtualPoint temp = other;
            other = vp;
            vp = temp;
            int tempi = otherIdx;
            otherIdx = vpIdx;
            vpIdx = tempi;
        }
        ArrayList<VirtualPoint> subList = new ArrayList<VirtualPoint>(runList.subList(vpIdx, otherIdx + 1));
        if (vpIdx == 0 && otherIdx == runList.size() - 1) {
            subList = runList;
        }
        VirtualPoint tempMatch;
        Point tempME;
        Point tempBP;
        Segment tempS;

        if (subList.contains(vp.match1)) {
            tempMatch = vp.match2;
            tempME = vp.match2endpoint;
            tempBP = vp.basePoint2;
            tempS = vp.s2;
            vp.setMatch2(null, null, null, null);
        } else {
            tempMatch = vp.match1;
            tempME = vp.match1endpoint;
            tempBP = vp.basePoint1;
            tempS = vp.s1;
            for (VirtualPoint vp1 : runList) {
                buff.add(vp1.fullString());
            }
            vp.swap();
            vp.setMatch2(null, null, null, null);
        }
        VirtualPoint temp2Match;
        Point temp2ME;
        Point temp2BP;
        Segment temp2S;
        if (subList.contains(other.match1)) {
            temp2Match = other.match2;
            temp2ME = other.match2endpoint;
            temp2BP = other.basePoint2;
            temp2S = other.s2;
            other.setMatch2(null, null, null, null);
        } else {
            temp2Match = other.match1;
            temp2ME = other.match1endpoint;
            temp2BP = other.basePoint1;
            temp2S = other.s1;
            buff.add(other);
            buff.add(subList);
            for (VirtualPoint vp1 : runList) {
                buff.add(vp1.fullString());
            }
            other.swap();
            other.setMatch2(null, null, null, null);
        }
        Knot k = new Knot(subList, this);
        k.setMatch1(tempMatch, tempME, tempBP, tempS);
        if (tempMatch != null) {
            buff.add(tempMatch.fullString());
            if (tempMatch.match1endpoint.equals(tempBP)) {
                tempMatch.match1 = k;
            } else {
                tempMatch.match2 = k;
            }
        }

        k.setMatch2(temp2Match, temp2ME, temp2BP, temp2S);
        if (temp2Match != null) {
            if (temp2Match.match1endpoint.equals(temp2BP)) {
                temp2Match.match1 = k;
            } else {
                temp2Match.match2 = k;
            }
        }
        runList.removeAll(subList);
        runList.add(vpIdx, k);
    }

    int halfKnotCount = 0;
    int sameKnotPointCount = 0;

    public void initPoints(DistanceMatrix distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
        int numPoints = distanceMatrix.size();
        for (int i = 0; i < numPoints; i++) {
            PointND pnd = distanceMatrix.getPoints().get(i);
            Point p = new Point(pnd, this);
            pointMap.put(pnd.getID(), p);
        }
        for (int i = 0; i < numPoints; i++) {
            Point p1 = (Point) pointMap.get(i);
            for (int j = 0; j < numPoints; j++) {
                if (i != j) {
                    Point p2 = (Point) pointMap.get(j);
                    Segment s = new Segment(p1, p2, distanceMatrix.getDistance(p1.p, p2.p));
                    p1.sortedSegments.add(s);
                    p1.segmentLookup.put(s.id, s);
                }
            }
            p1.sortedSegments.sort(null);
        }
    }

    @SuppressWarnings("unused")
    public Shell tspSolve(Shell A, DistanceMatrix distanceMatrix) throws SegmentBalanceException, BalancerException {

        Shell result = new Shell();
        visited = new ArrayList<VirtualPoint>();
        pointMap = new HashMap<>();
        unvisited = new ArrayList<VirtualPoint>();
        initPoints(distanceMatrix);
        int idx = 0;
        while (unvisited.size() > 1) {
            ArrayList<VirtualPoint> knots = createKnots();
            buff.add("\n================= - Layer: " + idx + " - =================");
            unvisited = knots;
            buff.add("unvisited:" + unvisited);
            buff.add("visited:" + visited);
            buff.add("================= - Layer: " + idx + " - =================\n");
            if (idx == 30) {
                System.out.println("Recursion Limit REACHED");
                float zero = 1 / 0;
            }
            idx++;
        }
        buff.add("\n================= - WARNING - =================");
        buff.add("警告:ゴーディアスノットを切断します");
        buff.add("システムロックが解除されました");
        buff.add("ナイフが噛み合った");
        buff.add("カット開始");
        buff.add("================= - WARNING - =================\n");
        int knotsCleared = 0;
        if (unvisited.size() == 1) {
            VirtualPoint gp1 = unvisited.get(0);
            if (gp1.isKnot) {
                result = cutKnot((Knot) gp1);
            }
        }
        return result;
    }

    public ArrayList<VirtualPoint> slowSolve(Shell A, DistanceMatrix distanceMatrix, int layers) {
        this.distanceMatrix = distanceMatrix;
        visited = new ArrayList<VirtualPoint>();
        pointMap = new HashMap<>();
        unvisited = new ArrayList<VirtualPoint>();
        int numPoints = distanceMatrix.size();
        for (int i = 0; i < numPoints; i++) {
            Point p = new Point(distanceMatrix.getPoints().get(i), this);
            pointMap.put(i, p);
        }
        for (int i = 0; i < numPoints; i++) {
            Point p1 = (Point) pointMap.get(i);
            for (int j = 0; j < numPoints; j++) {
                if (i != j) {
                    Point p2 = (Point) pointMap.get(j);
                    Segment s = new Segment(p1, p2, distanceMatrix.getDistance(p1.p, p2.p));
                    p1.sortedSegments.add(s);
                    p1.segmentLookup.put(s.id, s);
                }
            }
            p1.sortedSegments.sort(null);
        }
        int idx = 0;
        while (unvisited.size() > 1 && idx != layers) {
            ArrayList<VirtualPoint> knots = createKnots();
            buff.add("\n================= - Layer: " + idx + " - =================");
            unvisited = knots;
            buff.add("unvisited:" + unvisited);
            buff.add("visited:" + visited);
            buff.add("================= - Layer: " + idx + " - =================\n");
            idx++;
        }
        return unvisited;
    }

    public Shell cutKnot(Knot mainKnot) throws SegmentBalanceException, BalancerException {
        cutEngine.totalLayers = mainKnot.getHeight();
        ArrayList<VirtualPoint> knotList = cutEngine.cutKnot(mainKnot.knotPoints, 1);
        Knot knot = new Knot(knotList, this);
        if (!cutEngine.flatKnots.containsKey(knot.id)) {
            this.updateSmallestKnot(knot);
            this.updateSmallestCommonKnot(knot);
            cutEngine.setFlatKnot(0, knot, mainKnot);
        }
        Shell result = new Shell();
        for (VirtualPoint p : knotList) {
            result.add(((Point) p).p);
        }
        return result;
    }

    public Integer[][] smallestCommonKnotLookup;
    public Integer[][] largestUncommonKnotLookup;
    public Integer[] smallestKnotLookup;

    public void updateSmallestKnot(Knot knotNew) {

        if (smallestKnotLookup == null) {

            smallestKnotLookup = new Integer[distanceMatrix.size()];
            Arrays.fill(smallestKnotLookup, -1);
        }

        for (VirtualPoint vp : knotNew.knotPointsFlattened) {
            int low = vp.id;
            if (smallestKnotLookup[low] == -1) {
                smallestKnotLookup[low] = knotNew.id;
            }
        }
    }

    public void updateSmallestCommonKnot(Knot knotNew) {

        if (smallestCommonKnotLookup == null) {

            smallestCommonKnotLookup = new Integer[distanceMatrix.size()][distanceMatrix.size()];
            for (int i = 0; i < smallestCommonKnotLookup.length; i++) {
                Arrays.fill(smallestCommonKnotLookup[i], -1);
            }
        }

        for (VirtualPoint vp : knotNew.knotPointsFlattened) {
            int low = vp.id;
            for (VirtualPoint vp2 : knotNew.knotPointsFlattened) {
                int high = vp2.id;
                if (smallestCommonKnotLookup[high][low] != -1) {
                    continue;
                }
                smallestCommonKnotLookup[high][low] = knotNew.id;
                smallestCommonKnotLookup[low][high] = knotNew.id;
            }
        }
    }

    public Shell solveBetweenEndpoints(PointND first, PointND last, Shell A, DistanceMatrix d)
            throws SegmentBalanceException, BalancerException {
        PointSet ps = new PointSet();

        assert (!first.equals(last));

        ps.add(first);
        if (!first.equals(last)) {
            ps.add(last);
        }
        ps.addAll(A);
        DistanceMatrix d1 = new DistanceMatrix(ps, d);
        PointND dummy = d1.addDummyNode(-1, first, last);
        ps.add(dummy);
        Shell answer = new Shell();
        answer.add(first);
        answer.addAll(A.copyShallow());
        answer.add(last);
        answer.add(dummy);
        Shell result = tspSolve(answer, d1);

        assert (d1.getZero() != 0);
        assert (d1.getMaxDist() / 2 <= d1.getZero()) : "Zero: " + d1.getZero() + " MaxDist: " + d1.getMaxDist();

        ps.remove(dummy);
        result = result.removeRotate(ps);
        if (!result.get(0).equals(first)) {
            result = result.reverse();
        }

        return result;

    }

    /**
     * Initializes a new shell with no parent or child; a blank slate
     * 
     * @param points
     */

    public Shell(PointND... points) {
        for (int i = 0; i < points.length; i++) {
            this.add(points[i]);
        }
    }

    /**
     * Get the length of the shell
     * 
     * @return the length of the path between all points in the shell
     */
    public double getLength() {
        if (this.size() == 0) {
            return 0;
        }
        PointND first = null, last = null;
        double length = 0.0;
        for (PointND p : this) {
            if (first == null) {
                last = p;
                first = p;
            } else {
                length += last.distance(p);
                last = p;
            }
        }
        length += last.distance(first);
        return length;

    }

    public double getLengthEndpoints() {
        PointND first = null, last = null;
        double length = 0.0;
        for (PointND p : this) {
            if (first == null) {
                last = p;
                first = p;
            } else {
                length += last.distance(p);
                last = p;
            }
        }
        return length;

    }

    /**
     * Gets the distance from a point to its neighboring points in the shell
     * 
     * @param p
     * @param maxDist
     * @return the sum of the distance from p to the prev point in the shell and the
     *         distance from p to the next point in the shell
     */
    public double distanceToNeighbors(PointND p, DistanceMatrix d) {
        PointND prevP = prevPoint(p), nextP = nextPoint(p);

        return d.getDistance(p, prevP) + d.getDistance(p, nextP);

    }

    /**
     * Gets the distance from the point previous to p and the point after p in the
     * shell
     * 
     * @param p
     * @param maxDist
     * @return the sum of the distance from the prev point in the shell to the next
     *         point in the shell
     */
    public double distanceBetweenNeighbors(PointND p, DistanceMatrix d) {
        PointND prevP = prevPoint(p), nextP = nextPoint(p);

        return d.getDistance(nextP, prevP);

    }

    /**
     * Finds the previous point in the shell
     * 
     * @param p reference point
     * @return the point that comes before p in the shell
     */
    public PointND prevPoint(PointND p) {
        int i = this.indexOf(p), before = 0;
        if (i == 0) {
            before = this.size() - 1;
        } else {
            before = i - 1;
        }
        return this.get(before);
    }

    /**
     * Finds the next point in the shell
     * 
     * @param p reference point
     * @return the point that comes after p in the shell
     */
    public PointND nextPoint(PointND p) {
        int i = this.indexOf(p), after = 0;
        if (i == this.size() - 1) {
            after = 0;
        } else {
            after = i + 1;
        }
        return this.get(after);
    }

    public static Shell replaceByID(Shell A, PointSet ps) {
        Shell result = new Shell();
        for (PointND p : A) {
            result.add(ps.getByID(p.getID()));
        }
        return result;
    }

    public int getIndexByID(int idTarget) throws IdDoesNotExistException {
        int idx = 0;
        for (PointND p : this) {
            if (p.getID() == idTarget) {
                return idx;
            }
            idx++;
        }
        throw new IdDoesNotExistException(idTarget);
    }

    public PointND removeByID(int idTarget) throws IdDoesNotExistException {
        int idx = getIndexByID(idTarget);
        return this.remove(idx);
    }

    public Shell removeRotate(PointSet ps) {

        Shell before = new Shell(), after = new Shell();

        boolean isBeforePoint = true;
        for (PointND p : this) {
            if (!ps.contains(p)) {
                isBeforePoint = false;
            } else {
                if (isBeforePoint) {
                    before.add(p);
                } else {
                    after.add(p);
                }
            }
        }
        after.addAll(before);

        assert (after.size() == this.size() - 1);

        return after;
    }

    public void rotateTo(PointND p1, PointND p2) {
        Shell before = new Shell(), after = new Shell();

        boolean isBeforePoint = true;
        for (PointND p : this) {
            if ((p.equals(p1) && this.nextPoint(p).equals(p2)) || (p.equals(p2) && this.nextPoint(p).equals(p1))) {
                isBeforePoint = false;
                before.add(p);
            } else {
                if (isBeforePoint) {
                    before.add(p);
                } else {
                    after.add(p);
                }
            }
        }
        this.removeAll(before);
        this.addAll(before);
    }

    /**
     * Shallow copies a shell so that it does not point to any childern
     * 
     * @return a copy of the current shell with no references to its children
     */
    public Shell copyShallow() {
        Shell copy = new Shell();

        for (PointND q : this) {
            copy.add(q);
        }
        return copy;
    }

    /**
     * Turns a shell into a PointSet object
     * 
     * @return all of the points in the Shell and its children
     */
    public PointSet toPointSet() {
        PointSet ps = new PointSet();
        Shell currShell = this;
        while (currShell != null) {
            for (PointND p : currShell) {
                ps.add(p);

            }
            currShell = currShell.child;
        }
        return ps;

    }

    /**
     * Determines equality of shells based on if they represent the same tsp path
     * 
     * @param o shell to compare to
     * @return true if the shells are equal and false if they are not
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Shell) {
            Shell other = (Shell) o;
            if (other.size() != this.size()) {
                return false;
            }
            PointND otherFirst = other.getFirst();
            int startIndex = -1;
            for (PointND p : this) {
                if (p.equals(otherFirst)) {
                    startIndex = this.indexOf(p);
                    break;
                }
            }
            if (startIndex == -1) {
                return false;
            }
            for (int i = 0; i < other.size(); i++) {
                if (!other.get(i).equals(this.get(startIndex))) {
                    return false;
                }
                startIndex = (startIndex + 1) % other.size();
            }
            return true;
        }
        return false;

    }

    public Shell reverse() {
        Shell result = new Shell();
        for (PointND p : this) {
            result.addFirst(p);
        }
        return result;
    }

    @Override
    public String toString() {
        String str = "Shell[";
        for (int i = 0; i < this.size(); i++) {
            PointND p = this.get(i);
            if (p.getID() != -1) {
                str += p.getID();
            } else {
                str += p.toString();
            }
            if (i < this.size() - 1) {
                str += ", ";
            }
        }

        return str + "]";
    }

    public static String compareTo(Shell A, Shell B) {
        String str = "Shell A[";
        for (int i = 0; i < A.size() - 1; i++) {
            str += (i) + ", ";
        }
        str += A.size() - 1 + "]";

        str += "\nShell B[";
        for (int i = 0; i < B.size() - 1; i++) {
            str += (A.indexOf(B.get(i))) + ", ";
        }
        str += (A.indexOf(B.get(B.size() - 1))) + "]";

        return str;

    }

    @Override
    public boolean add(PointND e) {
        super.add(e);
        return true;

    }

    @Override
    public boolean addAll(Collection<? extends PointND> c) {
        super.addAll(c);
        return true;
    }

    public boolean addAllFirst(Collection<? extends PointND> c) {
        Object[] points = c.toArray();
        for (int i = points.length - 1; i >= 0; i--) {
            this.addFirst((PointND) points[i]);
        }
        return true;
    }

    public void addAfter(PointND contained, PointND insert) {
        super.add(this.indexOf(contained) + 1, insert);
    }

    public void addOutside(PointND contained, PointND insert) {
        assert (this.getLast().equals(contained) || this.getFirst().equals(contained))
                : insert.getID() + " " + contained.getID() + " " + this.toString();
        super.add(this.indexOf(contained) + 1, insert);
        if (this.getLast().equals(contained)) {
            this.rotateTo(this.getFirst(), insert);
        } else {
            this.rotateTo(this.getLast(), insert);
        }
    }

    public void addAllAtSegment(PointND contained, PointND connector, Shell other) {
        if (this.getLast().equals(contained)) {
            if (other.getLast().equals(connector)) {
                Shell reverse = other.reverse();
                this.addAll(reverse);
            } else {
                this.addAll(other);
            }
        } else {
            if (other.getLast().equals(connector)) {
                this.addAllFirst(other);
            } else {
                Shell reverse = other.reverse();
                this.addAllFirst(reverse);
            }
        }
    }

    public PointND getOppositeOutside(PointND endpoint) {
        assert (this.getLast().equals(endpoint) || this.getFirst().equals(endpoint)) : endpoint.getID();
        if (this.getLast().equals(endpoint)) {
            return this.getFirst();
        } else {
            return this.getLast();
        }
    }

    public boolean isEndpoint(PointND p) {
        return p.equals(this.getLast()) || p.equals(this.getFirst());
    }

    public boolean containsID(int id) {
        for (PointND pointND : this) {
            if (pointND.getID() == id) {
                return true;
            }
        }
        return false;
    }

    public boolean containsRange(Range r) {
        boolean hasStart = false;
        boolean hasEnd = false;
        for (PointND pointND : this) {
            if (pointND.getID() == r.endIdx) {
                hasEnd = true;
            }

            if (pointND.getID() == r.startIdx) {
                hasStart = true;
            }
        }
        return hasStart && hasEnd;
    }

    public PointND getNext(int i) {
        if (i + 1 >= this.size()) {
            return this.get(0);
        }
        return this.get(i + 1);
    }

    public PointND getPrev(int i) {
        if (i - 1 < 0) {
            return this.get(this.size() - 1);
        }
        return this.get(i - 1);
    }

    public void moveAfter(Range idTarget, int idDest) throws IdDoesNotExistException {
        if (!containsRange(idTarget)) {
            throw new IdDoesNotExistException(idTarget);
        }
        if (!containsID(idDest)) {
            throw new IdDoesNotExistException(idDest);
        }
        ArrayList<PointND> p = this.removeAllInRange(idTarget);
        int idxDest = this.getIndexByID(idDest);
        if (idTarget.reversed) {
            Collections.reverse(p);
            this.addAll(idxDest + 1, p);
        } else {
            this.addAll(idxDest + 1, p);
        }
    }

    public void moveBefore(Range idTarget, int idDest) throws IdDoesNotExistException {
        if (!containsRange(idTarget)) {
            throw new IdDoesNotExistException(idTarget);
        }
        if (!containsID(idDest)) {
            throw new IdDoesNotExistException(idDest);
        }
        ArrayList<PointND> p = this.removeAllInRange(idTarget);
        int idxDest = this.getIndexByID(idDest);
        this.addAll(idxDest, p);
    }

    public void moveBetween(Range idTarget, int idDest1, int idDest2)
            throws IdDoesNotExistException, IdsNotConcurrentException {
        if (!containsRange(idTarget)) {
            throw new IdDoesNotExistException(idTarget);
        }
        if (!containsID(idDest1)) {
            throw new IdDoesNotExistException(idDest1);
        }
        if (!containsID(idDest2)) {
            throw new IdDoesNotExistException(idDest2);
        }
        int idxDest1 = this.getIndexByID(idDest1);
        int idxDest2 = this.getIndexByID(idDest2);
        if (idxDest1 + 1 != idxDest2 && idxDest2 + 1 != idxDest1
                && !((idxDest1 == 0 && idxDest2 == this.size()) || (idxDest2 == 0 && idxDest1 == this.size()))) {
            throw new IdsNotConcurrentException(idxDest1, idxDest2);
        }
        ArrayList<PointND> p = this.removeAllInRange(idTarget);
        idxDest1 = this.getIndexByID(idDest1);
        idxDest2 = this.getIndexByID(idDest2);
        if (idxDest1 == 0 && idxDest2 == this.size()) {
            this.addAll(idxDest1, p);
        } else if ((idxDest2 == 0 && idxDest1 == this.size())) {
            this.addAll(idxDest2, p);
        } else if (idxDest1 > idxDest2) {
            this.addAll(idxDest2, p);
        } else {
            this.addAll(idxDest1, p);
        }
    }

    public boolean hasPoint(int id) {
        for (PointND p : this) {
            if (p.getID() == id) {
                return true;
            }
        }
        return false;
    }

    public void addAllInRange(Range r, Shell orgShell) {
        for (PointND p : orgShell) {
            if (r.hasPoint(p)) {
                this.add(p);
            }
        }
    }

    public ArrayList<PointND> getAllInRange(Range r) {
        ArrayList<PointND> points = new ArrayList<>();
        for (PointND p : this) {
            if (r.hasPoint(p)) {
                points.add(p);
            }
        }
        return points;
    }

    public ArrayList<PointND> removeAllInRange(Range r) {
        ArrayList<PointND> points = new ArrayList<>();
        for (PointND p : this) {
            if (r.hasPoint(p)) {
                points.add(p);
            }
        }
        this.removeAll(points);
        return points;
    }

    public Pair<PointND, Pair<PointND, PointND>> isLocalMinima() {
        for (int i = 0; i < this.size(); i++) {
            PointND curr = this.get(i);
            PointND next = this.getNext(i);
            PointND prev = this.getPrev(i);
            double delta = next.distance(prev) - next.distance(curr) - prev.distance(curr);
            for (int j = 0; j < this.size(); j++) {
                int nextJ = j + 1 >= this.size() ? 0 : j + 1;
                if (i != j && i != nextJ) {
                    PointND currD = this.get(j);
                    PointND nextD = this.get(nextJ);
                    double delta2 = delta - currD.distance(nextD) + currD.distance(curr) + nextD.distance(curr);
                    if (delta2 < 0 && delta2 < -0.0000001) {
                        return new Pair<PointND, Pair<PointND, PointND>>(curr,
                                new Pair<PointND, PointND>(currD, nextD));
                    }
                }
            }
        }
        return null;
    }

}
