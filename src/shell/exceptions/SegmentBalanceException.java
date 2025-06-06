package shell.exceptions;

import shell.cuts.CutInfo;
import shell.cuts.CutMatchList;
import shell.knot.Knot;
import shell.knot.Segment;
import shell.knot.VirtualPoint;
import shell.shell.Shell;

public class SegmentBalanceException extends Exception {
    public CutMatchList cutMatchList;
    public Knot topKnot;
    public Segment cut1;
    public Segment ex1;
    public Segment cut2;
    public Segment ex2;
    public String cutName;
    public Shell shell;
    public CutInfo c;

    public SegmentBalanceException(CutInfo c) {
        topKnot = c.superKnot;
        this.cut1 = c.lowerCutSegment;
        this.ex1 = c.lowerMatchSegment;
        this.cut2 = c.upperCutSegment;
        this.ex2 = c.upperMatchSegment;
        this.c = c;
        this.shell = c.shell;
    }

    public SegmentBalanceException(Shell shell, CutMatchList internalCut, CutInfo c) {
        cutMatchList = internalCut;
        topKnot = c.superKnot;
        this.cut1 = c.lowerCutSegment;
        this.ex1 = c.lowerMatchSegment;
        this.cut2 = c.upperCutSegment;
        this.ex2 = c.upperMatchSegment;
        this.shell = shell;
        this.c = c;
    }

    public SegmentBalanceException(SegmentBalanceException sbe) {
        cutMatchList = sbe.cutMatchList;
        topKnot = sbe.topKnot;
        cut1 = sbe.cut1;
        ex1 = sbe.ex1;
        cut2 = sbe.cut2;
        ex2 = sbe.ex2;
        this.c = sbe.c;
        this.shell = sbe.shell;
        cutName = sbe.cutName;
    }

    public SegmentBalanceException() {
    }

    @Override
    public String toString() {
        VirtualPoint kp1 = c.lowerKnotPoint;
        VirtualPoint kp2 = c.upperKnotPoint;
        cutName = shell.knotName + "_cut" + kp1 + "-" + cut1.getOther(kp1) + "and" + kp2
                + "-" + cut2.getOther(kp2) + "\n" + cutMatchList;
        if (c != null) {
            return "SegmentBalanceException: " + "cutID: " + c.cutID + " " + topKnot + " cut1: " + cut1 + " ex1: " + ex1
                    + " cut2: " + cut2 + " ex2: " + ex2 + " cutName: " + cutName + "\n\n" + this.getStackTrace()[0];
        } else {
            return "SegmentBalanceException: " + this.getStackTrace()[0];
        }
    }

    public void generateUnitTestFromCut() {
        // TODO: WRITE THIS
    }

}
