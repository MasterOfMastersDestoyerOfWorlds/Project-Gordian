package shell;

import java.util.ArrayList;

public class MultipleCyclesFoundException extends SegmentBalanceException {
    ArrayList<Segment> matchSegments;
    ArrayList<Segment> cutSegments;

    public MultipleCyclesFoundException(SegmentBalanceException sbe) {
        super(sbe);
    }

    public MultipleCyclesFoundException(Shell shell, CutMatchList internalCuts12, ArrayList<Segment> matchSegments,
            ArrayList<Segment> cutSegments, CutInfo c) {
        super(shell, internalCuts12, c);
        this.matchSegments = matchSegments;
        this.cutSegments = cutSegments;
    }

    @Override
    public String toString() {
        return "MultipleCyclesFoundException: \n matchSegments: " + matchSegments + "\n cutSegments: " + cutSegments
                + "\n" + "cutID: " + c.cutID + " " + topKnot + " cut1: " + cut1 + " ex1: " + ex1 + " cut2: " + cut2
                + " ex2: " + ex2 + " cutName: " + cutName;
    }

}