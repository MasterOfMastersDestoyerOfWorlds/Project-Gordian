package shell.ui.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.util.Pair;

import shell.Main;
import shell.ToggleType;
import shell.cameras.Camera2D;
import shell.file.Manifold;
import shell.knot.Knot;
import shell.knot.Segment;
import shell.knot.VirtualPoint;
import shell.ui.Drawing;

public class NegativeCutMatchViewTool extends Tool {

    public Manifold manifold;
    public HashMap<Long, ArrayList<Segment>> negativeSegmentMap;
    public int layerCalculated;
    HashMap<Long, Integer> colorLookup;
    public static ArrayList<Color> colors;

    public NegativeCutMatchViewTool() {
        disallowedToggles = new ToggleType[] { ToggleType.DrawCutMatch, ToggleType.DrawKnotGradient,
                ToggleType.DrawMetroDiagram, ToggleType.DrawDisplayedKnots };
        colors = new ArrayList<>();
        colors.add(Color.BLUE);
        colors.add(Color.RED);
    }

    @Override
    public void reset() {
        hover = null;
        hoverCP = null;
        hoverKP = null;
        manifold = null;
        negativeSegmentMap = null;

    }

    @Override
    public void draw(Graphics2D g2, Camera2D camera, int minLineThickness) {
        if (layerCalculated != Main.metroDrawLayer) {
            initSegmentMap();
            return;
        }
        if (hover != null) {
            long matchId = Segment.idTransformOrdered(hoverKP.id, hoverCP.id);
            ArrayList<Segment> matchSegments = negativeSegmentMap.get(matchId);
            long cutId = Segment.idTransform(hoverKP.id, hoverCP.id);
            Segment cutSeg = hoverKP.segmentLookup.get(cutId);
            if (matchSegments != null) {
                for (Segment s : matchSegments) {
                    Drawing.drawSingleCutMatch(Main.main, g2, s, cutSeg, Drawing.MIN_THICKNESS * 2, Main.retTup.ps,
                            camera);
                }
                Drawing.drawCircle(g2, hoverKP, Color.green, camera, minLineThickness);
            }
        }
        for (Knot k : Main.knotsDisplayed) {
            Drawing.drawGradientPath(g2, k, lookupPairs(k), colorLookup, colors,
                    camera,
                    Drawing.MIN_THICKNESS);
        }
    }

    public static ArrayList<Pair<Long, Long>> lookupPairs(Knot k) {

        ArrayList<Pair<Long, Long>> idTransform = new ArrayList<>();
        for (int i = 0; i < k.manifoldSegments.size(); i++) {
            Segment s = k.manifoldSegments.get(i);
            long matchId = Segment.idTransformOrdered(s.first.id, s.last.id);
            long matchId2 = Segment.idTransformOrdered(s.last.id, s.first.id);
            idTransform.add(new Pair<Long, Long>(matchId, matchId2));
        }
        return idTransform;
    }

    @Override
    public void click(Segment s, VirtualPoint kp, VirtualPoint cp) {

    }

    @Override
    public String displayName() {
        return "Negative Cut Match View";
    }

    public void initSegmentMap() {
        layerCalculated = Main.metroDrawLayer;
        ArrayList<Knot> knotsDisplayed = Main.knotsDisplayed;
        negativeSegmentMap = new HashMap<>();
        colorLookup = new HashMap<>();
        for (Knot k : knotsDisplayed) {
            for (Segment s : k.manifoldSegments) {
                long idFirst = Segment.idTransformOrdered(s.first.id, s.last.id);
                long idLast = Segment.idTransformOrdered(s.last.id, s.first.id);
                ArrayList<Segment> firstNegativeSegments = new ArrayList<>();
                ArrayList<Segment> lastNegativeSegments = new ArrayList<>();
                for (VirtualPoint vp : k.knotPointsFlattened) {
                    if (!s.contains(vp)) {
                        Segment firstSegment = vp.getSegment(s.last);
                        Segment lastSegment = vp.getSegment(s.first);
                        if (!k.hasSegment(firstSegment) && firstSegment.distance - s.distance < 0) {
                            firstNegativeSegments.add(firstSegment);
                        }
                        if (!k.hasSegment(lastSegment) && lastSegment.distance - s.distance < 0) {
                            lastNegativeSegments.add(lastSegment);
                        }
                    }
                }
                negativeSegmentMap.put(idFirst, firstNegativeSegments);
                negativeSegmentMap.put(idLast, lastNegativeSegments);
            }
            for (Segment s : k.manifoldSegments) {
                long matchId = Segment.idTransformOrdered(s.first.id, s.last.id);
                if (negativeSegmentMap.get(matchId).size() > 0) {
                    colorLookup.put(matchId, 1);
                } else {
                    colorLookup.put(matchId, 0);
                }

                long matchId2 = Segment.idTransformOrdered(s.last.id, s.first.id);
                if (negativeSegmentMap.get(matchId2).size() > 0) {
                    colorLookup.put(matchId2, 1);
                } else {
                    colorLookup.put(matchId2, 0);
                }
            }

        }
        Main.main.repaint();
    }
}
