package shell.ui;

import static java.awt.Font.PLAIN;
import static java.awt.Font.SANS_SERIF;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.util.Pair;
import org.joml.Random;
import org.joml.Vector2f;

import shell.DistanceMatrix;
import shell.PointSet;
import shell.Toggle;
import shell.cameras.Camera;
import shell.cameras.Camera2D;
import shell.cuts.CutInfo;
import shell.cuts.CutMatch;
import shell.cuts.CutMatchList;
import shell.cuts.route.Route;
import shell.exceptions.SegmentBalanceException;
import shell.knot.Knot;
import shell.knot.Point;
import shell.knot.Segment;
import shell.knot.VirtualPoint;
import shell.point.PointND;
import shell.render.color.Color;
import shell.render.color.ColorFixedLerp;
import shell.render.color.ColorRGB;
import shell.render.sdf.SDFCircle;
import shell.render.sdf.SDFLine;
import shell.render.text.Font;
import shell.render.text.HyperString;
import shell.shell.Shell;
import shell.ui.main.Main;

public class Drawing {

    public static final float MIN_THICKNESS_START = 2;
    public static final float FONT_HEIGHT_PIXELS_START = 30;
    public static final float CIRCLE_RADIUS_START = 7.5f;
    public static float MIN_THICKNESS = 2;
    public static float FONT_HEIGHT_PIXELS = 30;
    public static float FONT_HEIGHT_LABELS_PIXELS = 30;
    public static float CIRCLE_RADIUS = 7.5f;
    public static SDFLine sdfLine = new SDFLine();
    public static SDFCircle circle = new SDFCircle();
    public static Font font = new Font(new java.awt.Font(SANS_SERIF, PLAIN, 32), true);

    public static void initDrawingSizes(Shell shell, Camera2D camera, DistanceMatrix d) {

        double smallestLength = d.getSmallestSegmentLength();
        double smallestLengthScreenSpace = camera.pointSpaceLengthToScreenSpace(smallestLength);
        if (MIN_THICKNESS_START > smallestLengthScreenSpace) {
            MIN_THICKNESS = (float) (smallestLengthScreenSpace);
            FONT_HEIGHT_PIXELS = FONT_HEIGHT_PIXELS_START;
            FONT_HEIGHT_LABELS_PIXELS = (FONT_HEIGHT_PIXELS_START / MIN_THICKNESS_START) * MIN_THICKNESS;
            CIRCLE_RADIUS = (CIRCLE_RADIUS_START / MIN_THICKNESS_START) * MIN_THICKNESS;
        } else {
            MIN_THICKNESS = MIN_THICKNESS_START;
            FONT_HEIGHT_PIXELS = FONT_HEIGHT_PIXELS_START;
            FONT_HEIGHT_LABELS_PIXELS = FONT_HEIGHT_PIXELS_START;
            CIRCLE_RADIUS = CIRCLE_RADIUS_START;
        }
        if (MIN_THICKNESS == 0.0) {
            System.out.println(1 / 0);
        }
    }

    public static void drawCutMatch(SegmentBalanceException sbe, float lineThickness,
            PointSet ps, Camera2D camera) {
        drawCutMatch(sbe.cutMatchList, sbe.cut1, sbe.cut2, sbe.ex1, sbe.ex2, sbe.topKnot,
                lineThickness, ps, camera);
    }

    public static void drawCutMatch(CutMatchList cml,
            Segment cut1, Segment cut2, Segment ex1, Segment ex2, Knot topKnot, float lineThickness,
            PointSet ps, Camera2D camera) {

        float[] firstCoords = new float[2];
        float[] lastCoords = new float[2];
        float[] midCoords = new float[2];

        Point2D first = ((Point) cut1.first).p.toPoint2D();
        Point2D last = ((Point) cut1.last).p.toPoint2D();

        firstCoords[0] = camera.pointTransformX((float) first.getX());
        firstCoords[1] = camera.pointTransformY((float) first.getY());

        lastCoords[0] = camera.pointTransformX((float) last.getX());
        lastCoords[1] = camera.pointTransformY((float) last.getY());
        midCoords[0] = (firstCoords[0] + lastCoords[0]) / 2.0f;
        midCoords[1] = (firstCoords[1] + lastCoords[1]) / 2.0f;
        font.drawTextCentered("X", midCoords[0], midCoords[1], FONT_HEIGHT_PIXELS, Color.RED, camera);
        // Draw x 2
        first = ((Point) cut2.first).p.toPoint2D();
        last = ((Point) cut2.last).p.toPoint2D();

        firstCoords[0] = camera.pointTransformX((float) first.getX());
        firstCoords[1] = camera.pointTransformY((float) first.getY());

        lastCoords[0] = camera.pointTransformX((float) last.getX());
        lastCoords[1] = camera.pointTransformY((float) last.getY());
        midCoords[0] = (firstCoords[0] + lastCoords[0]) / 2.0f;
        midCoords[1] = (firstCoords[1] + lastCoords[1]) / 2.0f;

        font.drawTextCentered("X", midCoords[0], midCoords[1], FONT_HEIGHT_PIXELS, Color.ORANGE, camera);
        // Draw external segment 1

        Point2D knotPoint1 = ((Point) ex1.getKnotPoint(topKnot.knotPointsFlattened)).p.toPoint2D();

        firstCoords[0] = camera.pointTransformX(knotPoint1.getX());
        firstCoords[1] = camera.pointTransformY(knotPoint1.getY());

        circle.draw(new Vector2f(firstCoords[0], firstCoords[1]), CIRCLE_RADIUS * camera.ScaleFactor, Color.GREEN,
                camera);

        sdfLine.setStroke(lineThickness, false);
        drawSegment(ex1, Color.GREEN, camera);

        // Draw external segment 2

        Point2D knotPoint2 = ((Point) ex2.getKnotPoint(topKnot.knotPointsFlattened)).p.toPoint2D();

        firstCoords[0] = camera.pointTransformX(knotPoint2.getX());
        firstCoords[1] = camera.pointTransformY(knotPoint2.getY());

        circle.draw(new Vector2f(firstCoords[0], firstCoords[1]), CIRCLE_RADIUS * camera.ScaleFactor, Color.GREEN,
                camera);
        drawSegment(ex2, Color.GREEN, camera);

        // Draw Cuts and Matches
        if (cml != null) {
            for (CutMatch cutMatch : cml.cutMatches) {

                if (cutMatch.c != null) {
                    for (Segment s : cutMatch.matchSegments) {
                        if (s.id == cutMatch.c.lowerMatchSegment.id || s.id == cutMatch.c.upperMatchSegment.id) {
                            drawSegment(s, Color.GREEN, camera);
                        } else {
                            drawSegment(s, Color.CYAN, camera);
                        }
                    }

                    // Draw Cuts
                    sdfLine.setStroke(2 * lineThickness, false);
                    for (Segment s : cutMatch.cutSegments) {
                        if (s.id == cutMatch.c.lowerCutSegment.id || s.id == cutMatch.c.upperCutSegment.id) {
                            drawSegment(s, Color.MAGENTA, camera);
                        } else {
                            drawSegment(s, Color.ORANGE, camera);
                        }
                    }
                }
                // Draw SubKnot
                Shell result = new Shell();
                for (VirtualPoint p : cutMatch.knot.knotPoints) {
                    result.add(((Point) p).p);
                }

            }
        }

    }

    public static void drawCutMatch(CutMatchList cml, float lineThickness,
            PointSet ps, Camera2D camera) {
        CutInfo c = cml.getCutMatch().c;
        drawCutMatch(cml, c.lowerCutSegment, c.upperCutSegment, c.lowerMatchSegment, c.upperMatchSegment, c.knot,
                lineThickness,
                ps, camera);

    }

    public static void drawRouteComparison(Route r1, Color r1MatchColor, Color r1CutColor, Route r2, Color r2MatchColor,
            Color r2CutColor,
            float lineThickness, PointSet ps, Camera2D camera) {

        // Draw Matches for route 1 and 2

        sdfLine.setStroke(lineThickness, false);
        for (Segment s : r1.matches) {
            if (r2.matches.contains(s)) {
                drawSegment(s, Color.CYAN, camera);
            } else {
                drawSegment(s, r1MatchColor, camera);
            }
        }
        for (Segment s : r2.matches) {
            if (!r1.matches.contains(s)) {
                drawSegment(s, r2MatchColor, camera);
            }
        }

        // Draw Cuts for route 1 and 2
        sdfLine.setStroke(2 * lineThickness, false);
        for (Segment s : r1.cuts) {
            if (r2.cuts.contains(s)) {
                drawSegment(s, Color.ORANGE, camera);
            } else {
                drawSegment(s, r1CutColor, camera);
            }
        }

        for (Segment s : r2.cuts) {
            if (!r1.cuts.contains(s)) {
                drawSegment(s, r2CutColor, camera);
            }
        }
    }

    public static void drawManifoldCut(VirtualPoint hoverKP, VirtualPoint hoverCP, Camera2D camera,
            float lineThickness) {
        Drawing.drawManifoldCut(hoverKP, hoverCP, Color.GREEN, Color.ORANGE, camera, lineThickness);
    }

    public static void drawManifoldCut(VirtualPoint hoverKP, VirtualPoint hoverCP, Color circleColor, Color xColor,
            Camera2D camera, float lineThickness) {

        float[] kpCoords = new float[2];
        float[] cpCoords = new float[2];
        float[] midCoords = new float[2];

        Point2D kp = ((Point) hoverKP).p.toPoint2D();
        Point2D cp = ((Point) hoverCP).p.toPoint2D();

        kpCoords[0] = camera.pointTransformX(kp.getX());
        kpCoords[1] = camera.pointTransformY(kp.getY());

        cpCoords[0] = camera.pointTransformX(cp.getX());
        cpCoords[1] = camera.pointTransformY(cp.getY());
        midCoords[0] = (kpCoords[0] + cpCoords[0]) / 2.0f;
        midCoords[1] = (kpCoords[1] + cpCoords[1]) / 2.0f;
        font.drawTextCentered("X", midCoords[0], midCoords[1], FONT_HEIGHT_PIXELS, xColor, camera);
        circle.draw(new Vector2f(kpCoords[0], kpCoords[1]), CIRCLE_RADIUS * camera.ScaleFactor, circleColor, camera);
    }

    public static void drawSegment(Segment segment, Color c, float thickness, Camera2D camera) {
        sdfLine.setStroke(thickness, false);
        drawSegment(segment, c, camera);
    }

    public static void drawScaledSegment(Segment segment, Color c, float thickness, Camera2D camera) {
        sdfLine.setStroke(thickness * camera.ScaleFactor, false);
        drawSegment(segment, c, camera);
    }

    public static void drawScaledSegment(Vector2f a, Vector2f b, Color c, float thickness, Camera2D camera) {
        sdfLine.setStroke(thickness * camera.ScaleFactor, false);
        sdfLine.dashed = false;
        sdfLine.draw(a, b, c, camera);
    }

    private static void drawSegment(Segment ex1, Color c, Camera2D camera) {
        Point2D first;
        Point2D last;
        if (ex1.first.isKnot) {
            first = ((Point) ((Knot) ex1.first).knotPoints.get(0)).p.toPoint2D();
        } else {
            first = ((Point) ex1.first).p.toPoint2D();
        }
        if (ex1.last.isKnot) {
            last = ((Point) ((Knot) ex1.last).knotPoints.get(0)).p.toPoint2D();
        } else {
            last = ((Point) ex1.last).p.toPoint2D();
        }
        Vector2f firstVec = new Vector2f(camera.pointTransformX(first.getX()), camera.pointTransformY(first.getY()));
        Vector2f lastVec = new Vector2f(camera.pointTransformX(last.getX()), camera.pointTransformY(last.getY()));

        sdfLine.dashed = false;
        sdfLine.draw(firstVec, lastVec, c, camera);
    }

    public static void drawDashedSegment(Segment ex1, Color c, Camera2D camera) {
        Point2D first;
        Point2D last;
        if (ex1.first.isKnot) {
            first = ((Point) ((Knot) ex1.first).knotPoints.get(0)).p.toPoint2D();
        } else {
            first = ((Point) ex1.first).p.toPoint2D();
        }
        if (ex1.last.isKnot) {
            last = ((Point) ((Knot) ex1.last).knotPoints.get(0)).p.toPoint2D();
        } else {
            last = ((Point) ex1.last).p.toPoint2D();
        }

        Vector2f firstVec = new Vector2f(camera.pointTransformX(first.getX()), camera.pointTransformY(first.getY()));
        Vector2f lastVec = new Vector2f(camera.pointTransformX(last.getX()), camera.pointTransformY(last.getY()));
        sdfLine.setStroke(Drawing.MIN_THICKNESS * camera.ScaleFactor, true, 20 * camera.ScaleFactor, 1f, true,
                false);
        sdfLine.draw(firstVec, lastVec, c, camera);
    }

    public static void drawGradientSegment(Segment s, Color color1, Color color2, Camera2D camera) {
        Point2D first;
        Point2D last;
        if (s.first.isKnot) {
            first = ((Point) ((Knot) s.first).knotPoints.get(0)).p.toPoint2D();
        } else {
            first = ((Point) s.first).p.toPoint2D();
        }
        if (s.last.isKnot) {
            last = ((Point) ((Knot) s.last).knotPoints.get(0)).p.toPoint2D();
        } else {
            last = ((Point) s.last).p.toPoint2D();
        }
        float[] firstCoords = new float[2];
        float[] lastCoords = new float[2];
        firstCoords[0] = camera.pointTransformX(first.getX());
        firstCoords[1] = camera.pointTransformY(first.getY());

        lastCoords[0] = camera.pointTransformX(last.getX());
        lastCoords[1] = camera.pointTransformY(last.getY());
        sdfLine.draw(new Vector2f(firstCoords), new Vector2f(lastCoords), color1, color2, camera);
    }

    /**
     * Draws gradient segment from the first in the segment to the last in the
     * segment with length being a value from zero to one indicating the distance
     * along that segment to draw. Calling this method with a length of 1 draws the
     * segment normally.
     * 
     * @param s
     * @param color1
     * @param color2
     * @param length
     * @param camera
     */
    public static void drawGradientSegmentPartial(Segment s, Color color1, Color color2, float length,
            Camera2D camera) {
        Point2D first;
        Point2D last;
        if (s.first.isKnot) {
            first = ((Point) ((Knot) s.first).knotPoints.get(0)).p.toPoint2D();
        } else {
            first = ((Point) s.first).p.toPoint2D();
        }
        if (s.last.isKnot) {
            last = ((Point) ((Knot) s.last).knotPoints.get(0)).p.toPoint2D();
        } else {
            last = ((Point) s.last).p.toPoint2D();
        }

        Vector2f firstCoords = new Vector2f(camera.pointTransformX(first.getX()), camera.pointTransformY(first.getY()));
        Vector2f lastCoords = new Vector2f(camera.pointTransformX(last.getX()), camera.pointTransformY(last.getY()));

        Vector2f newLast = new Vector2f(lastCoords).sub(firstCoords).mul(length).add(firstCoords);

        sdfLine.draw(firstCoords, newLast, color1, new ColorFixedLerp(color1, color2, length), camera);
    }

    /**
     * Draws the Shell and its children if drawChildren is true
     * 
     * @param frame        where to draw the shell
     * @param g2           graphics object for frame
     * @param drawChildren whether or not to draw child shells
     * @param c            the color to draw the shell (set to null to get a random
     *                     color)
     */
    public static void drawShell(Shell shell, boolean drawChildren, float lineThickness, Color c,
            PointSet ps, Camera2D camera) {
        if (c == null) {
            Random colorSeed = new Random();
            Drawing.drawPath(shell, lineThickness,
                    new ColorRGB(colorSeed.nextFloat(), colorSeed.nextFloat(), colorSeed.nextFloat()), ps,
                    true, false, false, false, camera);
        } else {
            Drawing.drawPath(shell, lineThickness, c, ps, true, false, false, false, camera);
        }
    }

    /**
     * Draws the tsp path of the pointset ps
     * 
     * @param frame
     * @param shell
     * @param color
     * @param ps
     * @param drawLines
     * @param drawCircles
     * @param drawNumbers
     */
    public static void drawPath(Shell shell, float lineThickness, Color color,
            PointSet ps,
            boolean drawLines, boolean drawCircles, boolean drawNumbers, boolean dashed, Camera2D camera) {
        float scale = camera.ScaleFactor;
        if (!Toggle.ScalePath.value) {
            scale = 3;
        }
        if (shell.size() == 0) {
            return;
        }
        if (dashed) {
            sdfLine.setStroke(lineThickness * scale, true, 60f, 1f, true, true);
        } else {
            sdfLine.setStroke(lineThickness * scale, false);
        }
        PointND last = shell.getLast();
        PointND next;
        int count = 0;
        for (PointND p : shell) {
            next = shell.getNext(count);
            float x = camera.pointTransformX(p.getScreenX());
            float y = camera.pointTransformY(p.getScreenY());
            if (drawCircles) {
                circle.draw(new Vector2f(x, y), CIRCLE_RADIUS * scale, color, camera);
            }

            if (drawNumbers) {
                float numberPixelDistance = scale * FONT_HEIGHT_LABELS_PIXELS / 4;
                Vector2f point = new Vector2f(x, y);
                Vector2f lastVector = new Vector2f(camera.pointTransformX(last.getScreenX()),
                        camera.pointTransformY(last.getScreenY())).sub(point);
                Vector2f nextVector = new Vector2f(camera.pointTransformX(next.getScreenX()),
                        camera.pointTransformY(next.getScreenY())).sub(point);
                Vector2f bisector = new Vector2f(lastVector).normalize().add(new Vector2f(nextVector).normalize())
                        .normalize().mul(numberPixelDistance);
                Vector2f textCenter = point.sub(bisector);
                HyperString number = new HyperString();
                HyperString pointInfo = new HyperString();
                pointInfo.addWord(p.toString());
                number.addTooltip(p.getID() + "", color, pointInfo, () -> {
                });
                number.debug = true;
                Drawing.font.drawHyperString(number, textCenter.x, textCenter.y,
                        scale * FONT_HEIGHT_LABELS_PIXELS, camera);
            }
            if (drawLines) {
                float lx = camera.pointTransformX(last.getScreenX());
                float ly = camera.pointTransformY(last.getScreenY());
                sdfLine.draw(new Vector2f(lx, ly), new Vector2f(x, y), color, camera);
            }
            last = p;
            count++;
        }
    }

    public static void drawGradientPath(Knot k,
            ArrayList<Pair<Long, Long>> lookUpPairs, HashMap<Long, Integer> colorLookup,
            ArrayList<Color> colors, Camera2D camera, float minLineThickness) {

        sdfLine.setStroke(minLineThickness * camera.ScaleFactor, false, 1f, 0f, true, false);
        for (int i = 0; i < k.manifoldSegments.size(); i++) {
            Segment s = k.manifoldSegments.get(i);
            if (lookUpPairs != null) {
                Pair<Long, Long> lookUpPair = lookUpPairs.get(i);

                if (colorLookup.containsKey(lookUpPair.getFirst())) {
                    Drawing.drawGradientSegment(s, colors.get(colorLookup.get(lookUpPair.getFirst())),
                            colors.get(colorLookup.get(lookUpPair.getSecond())),
                            camera);
                }
            } else {
                if (colorLookup.containsKey((long) s.first.id)) {
                    Drawing.drawGradientSegment(s, colors.get(colorLookup.get((long) s.first.id)),
                            colors.get(colorLookup.get((long) s.last.id)),
                            camera);
                }
            }
        }

    }

    public static void drawGradientPath(Knot k, ArrayList<Pair<Long, Long>> lookupPairs,
            HashMap<Long, Color> colorLookup, Camera2D camera, float minLineThickness) {

        sdfLine.setStroke(minLineThickness * camera.ScaleFactor, false, 1f, 0f, true, false);
        for (int i = 0; i < k.manifoldSegments.size(); i++) {
            Segment s = k.manifoldSegments.get(i);
            if (lookupPairs != null) {
                Pair<Long, Long> lookUpPair = lookupPairs.get(i);

                if (colorLookup.containsKey(lookUpPair.getFirst())) {
                    Drawing.drawGradientSegment(s, colorLookup.get(lookUpPair.getFirst()),
                            colorLookup.get(lookUpPair.getSecond()),
                            camera);
                }
            } else {
                if (colorLookup.containsKey((long) s.first.id)) {
                    Drawing.drawGradientSegment(s, colorLookup.get((long) s.first.id),
                            colorLookup.get((long) s.last.id),
                            camera);
                }
            }
        }
    }

    public static void drawSingleCutMatch(Main main, Segment matchSegment,
            Segment cutSegment, float lineThickness,
            PointSet ps, Camera2D camera) {

        sdfLine.setStroke(lineThickness * camera.ScaleFactor, false);
        Drawing.drawSegment(matchSegment, Color.CYAN, camera);

        sdfLine.setStroke(2 * lineThickness * camera.ScaleFactor, false);
        Drawing.drawSegment(cutSegment, Color.ORANGE, camera);
    }

    public static void drawCircle(VirtualPoint displayPoint, Color color, Camera2D camera,
            float lineThickness) {
        sdfLine.setStroke(lineThickness, false);
        Point p = (Point) displayPoint;
        double xCoord = camera.pointTransformX(p.p.getScreenX());
        double yCoord = camera.pointTransformY(p.p.getScreenY());
        circle.draw(new Vector2f((float) xCoord, (float) yCoord), CIRCLE_RADIUS * camera.ScaleFactor, color, camera);
    }

    public static void drawCircle(Vector2f cameraPoint, Color color, Camera2D camera,
            float lineThickness) {
        sdfLine.setStroke(lineThickness, false);
        circle.draw(new Vector2f(cameraPoint.x, cameraPoint.y), CIRCLE_RADIUS * camera.ScaleFactor, color, camera);
    }

    public static void drawCircle(Vector2f cameraPoint, Color color, Camera camera,
            float lineThickness) {
        sdfLine.setStroke(lineThickness, false);
        circle.draw(new Vector2f(cameraPoint.x, cameraPoint.y), CIRCLE_RADIUS, color, camera);
    }

    public static void drawKnot(Knot k, Color c, float lineThickness, Camera2D camera) {
        sdfLine.setStroke(lineThickness * camera.ScaleFactor, false, 1f, 0f, true, false);
        for (int i = 0; i < k.manifoldSegments.size(); i++) {
            Segment s = k.manifoldSegments.get(i);
            Drawing.drawSegment(s, c, camera);
        }
    }

    public static void setScaledStroke(Camera2D camera) {
        sdfLine.setStroke(MIN_THICKNESS * camera.ScaleFactor, false, 1f, 0f, true, false);
    }

}
