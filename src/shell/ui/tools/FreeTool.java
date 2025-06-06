package shell.ui.tools;

import java.util.ArrayList;

import shell.Toggle;
import shell.cameras.Camera2D;
import shell.knot.Knot;
import shell.knot.Point;
import shell.knot.Run;
import shell.knot.VirtualPoint;
import shell.point.PointND;
import shell.render.color.Color;
import shell.render.text.HyperString;
import shell.ui.Drawing;
import shell.ui.main.Main;

public class FreeTool extends Tool {

    @Override
    public void draw(Camera2D camera, float minLineThickness) {
        if (displayKP != null) {
            Drawing.drawCircle(displayKP, Color.LIGHT_GRAY, camera, minLineThickness);
        }
    }

    @Override
    public void cycleLeft() {
        ArrayList<Knot> knotsDisplayed = Main.knotsDisplayed;
        for (Knot k : knotsDisplayed) {
            if (k.contains(displayKP)) {
                selectedKP = k.getNextClockWise(displayKP);
                displayKP = selectedKP;
                return;
            }
        }
    };

    @Override
    public void cycleRight() {
        ArrayList<Knot> knotsDisplayed = Main.knotsDisplayed;
        for (Knot k : knotsDisplayed) {
            if (k.contains(displayKP)) {
                selectedKP = k.getNextCounterClockWise(displayKP);
                displayKP = selectedKP;
                return;
            }
        }
    };

    @Override
    public void confirm() {
    }

    @Override
    public HyperString buildInfoText() {
        HyperString h = new HyperString();
        HyperString pointInfo = new HyperString();
        PointND coordPoint = null;
        h.addWord("Point: ");
        if (displayKP == null) {
            h.addWord("None");
        } else {
            coordPoint = ((Point) displayKP).p;
            final PointND coordPointF = ((Point) displayKP).p;
            pointInfo.addWord(coordPointF.toString());
            h.addTooltip(displayKP.id + "", Color.BLUE_WHITE, pointInfo, () -> Main.camera.centerOnPoint(coordPointF));
        }
        h.newLine();
        h.addWord("Position:");
        if (displayKP == null) {
            h.addWord(Main.grid.toCoordString());
        } else {
            h.addWord(coordPoint.toCoordString());
        }

        h.newLine();
        h.addWord("Neighbors:");
        if (displayKP == null) {
            h.addWord("None");
        } else {
            h.addWord(displayKP.match1.id + "");
            h.addWord(displayKP.match2.id + "");
        }

        h.newLine();
        h.addWord("Closest Points:");
        if (displayKP == null) {
            h.addWord("None");
        } else {
            h.addWord(displayKP.sortedSegments.get(0).getOther(displayKP).id + "");
            h.addWord(displayKP.sortedSegments.get(1).getOther(displayKP).id + "");
        }

        h.newLine();
        h.addWord("MinKnot: ");
        Knot containingKnot = null;
        for (Knot k : Main.knotsDisplayed) {
            if (k.contains(displayKP)) {
                containingKnot = k;
            }
        }
        if (containingKnot == null) {
            h.addWord("None");
        } else {
            Color c = Main.stickyColor;
            if (canUseToggle(Toggle.DrawKnotGradient)) {
                c = Main.getKnotGradientColor(displayKP);
            } else if (canUseToggle(Toggle.DrawMetroDiagram)) {
                c = Main.getMetroColor(displayKP, containingKnot);
            }
            String pointStr = "" + displayKP.id + " ";
            final Knot reeK = containingKnot;
            final PointND coordPointF = ((Point) displayKP).p;
            HyperString minKnotInfo = new HyperString();
            if (containingKnot.s1 != null && containingKnot.s2 != null) {
                minKnotInfo.addHyperString(containingKnot.s1.toHyperString(c, false));
                minKnotInfo.addDistance(containingKnot.s1.distance, c);
                minKnotInfo.newLine();
                minKnotInfo.addHyperString(containingKnot.s2.toHyperString(c, false));
                minKnotInfo.addDistance(containingKnot.s2.distance, c);
            }
            minKnotInfo.addLine("FlatID: " + containingKnot.id, c);
            minKnotInfo.addLine("OrgID: " + Main.flattenEngine.flatKnotToKnot.get(containingKnot.id), c);
            h.addTooltip(containingKnot.beforeString(displayKP.id), c, minKnotInfo, () -> Main.camera.zoomToKnot(reeK));
            h.addTooltip(pointStr, Color.BLUE_WHITE, pointInfo, () -> Main.camera.centerOnPoint(coordPointF));
            h.addTooltip(containingKnot.afterString(displayKP.id), c, minKnotInfo, () -> Main.camera.zoomToKnot(reeK));

        }
        h.newLine();
        if (Main.result.size() > 0) {
            h.addWord("TopKnot:");
            for (VirtualPoint topStruct : Main.result) {
                if (topStruct.isKnot) {
                    h.newLine();
                    h.newLine();
                    h.addHyperString(((Knot) topStruct).toHyperString());
                }
                if (topStruct.isRun) {
                    h.newLine();
                    h.newLine();
                    h.addHyperString(((Run) topStruct).toHyperString());
                }
            }
        }
        h.wrap = true;
        return h;
    }

    @Override
    public Knot selectedKnot() {
        Knot containingKnot = null;
        for (Knot k : Main.knotsDisplayed) {
            if (k.contains(displayKP)) {
                containingKnot = k;
            }
        }
        return containingKnot;
    }

    @Override
    public Type toolType() {
        return Type.Free;
    }

    @Override
    public String displayName() {
        return "Free";
    }

    @Override
    public String fullName() {
        return "free";
    }

    @Override
    public String shortName() {
        return "fr";
    }

    @Override
    public String desc() {
        return "The default tool. Gives the most information about knot structure and connections";
    }
}
