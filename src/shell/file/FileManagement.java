package shell.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import shell.DistanceMatrix;
import shell.PointND;
import shell.PointSet;
import shell.exceptions.FileParseException;
import shell.shell.Shell;

import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

public class FileManagement {

    public static final String solutionsFolder = "./src/test/solutions/";

    public static File getTestFile(String fileName) {
        String[] parts = fileName.split("_");
        return new File(solutionsFolder + parts[0] + "/" + fileName + ".ix");
    }

    /**
     * Imports the point set and optimal tsp path from a file
     * 
     * @param f
     * @return the optimal PointSetPath
     */
    public static PointSetPath importFromFile(File f) {

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            ArrayList<PointND> lines = new ArrayList<PointND>();
            String line = br.readLine();
            PointSet ps = new PointSet();
            Path2D path = new GeneralPath(GeneralPath.WIND_NON_ZERO);
            Shell tsp = new Shell();
            ArrayList<Manifold> manifolds = new ArrayList<>();
            Manifold m = null;
            boolean flag = true, first = true;
            int index = 0;
            DistanceMatrix d = null;
            HashMap<Integer, PointND> lookUp = new HashMap<>();
            ArrayList<Integer> answerOrder = new ArrayList<>();
            int lineNumber = 1;
            while (line != null) {
                if (flag == true) {
                    String[] cords = line.split(" ");
                    Point2D pt2d = null;
                    if (cords[0].equals("CIRCLE")) {
                        System.out.println("CIRCLE FOUND!");
                        double xCenter = java.lang.Double.parseDouble(cords[1]);
                        double yCenter = java.lang.Double.parseDouble(cords[2]);
                        double radius = java.lang.Double.parseDouble(cords[3]);
                        int numPoints = java.lang.Integer.parseInt(cords[4]);
                        double radians = 2 * Math.PI / ((double) numPoints);
                        for (int i = 0; i < numPoints; i++) {
                            double xCoord = radius * Math.cos(i * radians) + xCenter;
                            double yCoord = radius * Math.sin(i * radians) + yCenter;
                            PointND pt = new PointND.Double(index, xCoord, yCoord);
                            pt2d = pt.toPoint2D();
                            lookUp.put(index, pt);
                            lines.add(pt);
                            ps.add(pt);
                            tsp.add(pt);

                            if (first) {
                                path.moveTo(pt2d.getX(), pt2d.getY());
                                first = false;
                            } else {
                                path.lineTo(pt2d.getX(), pt2d.getY());
                            }
                            index++;
                        }
                    } else if (cords[0].equals("LINE")) {
                        System.out.println("LINE FOUND!");
                        double xStart = java.lang.Double.parseDouble(cords[1]);
                        double yStart = java.lang.Double.parseDouble(cords[2]);
                        double xEnd = java.lang.Double.parseDouble(cords[3]);
                        double yEnd = java.lang.Double.parseDouble(cords[4]);
                        int numPoints = java.lang.Integer.parseInt(cords[5]);
                        double slopeX = (xEnd - xStart) / ((double) numPoints - 1);
                        double slopeY = (yEnd - yStart) / ((double) numPoints - 1);
                        for (int i = 0; i < numPoints; i++) {
                            double xCoord = (slopeX * i) + xStart;
                            double yCoord = (slopeY * i) + yStart;
                            PointND pt = new PointND.Double(index, xCoord, yCoord);
                            pt2d = pt.toPoint2D();
                            lookUp.put(index, pt);
                            lines.add(pt);
                            ps.add(pt);
                            tsp.add(pt);

                            if (first) {
                                path.moveTo(pt2d.getX(), pt2d.getY());
                                first = false;
                            } else {
                                path.lineTo(pt2d.getX(), pt2d.getY());
                            }
                            index++;
                        }
                    } else if (cords[0].equals("ARC")) {
                        System.out.println("CIRCLE FOUND!");
                        double xCenter = java.lang.Double.parseDouble(cords[1]);
                        double yCenter = java.lang.Double.parseDouble(cords[2]);
                        double radius = java.lang.Double.parseDouble(cords[3]);
                        int numPoints = java.lang.Integer.parseInt(cords[4]);
                        double startAngle = java.lang.Double.parseDouble(cords[5]) * (Math.PI / 180);
                        double endAngle = java.lang.Double.parseDouble(cords[6]) * (Math.PI / 180);
                        double radians = Math.abs(endAngle - startAngle) / ((double) numPoints);
                        for (int i = 0; i < numPoints; i++) {
                            double xCoord = radius * Math.cos(i * radians + startAngle) + xCenter;
                            double yCoord = radius * Math.sin(i * radians + startAngle) + yCenter;
                            PointND pt = new PointND.Double(index, xCoord, yCoord);
                            pt2d = pt.toPoint2D();
                            lookUp.put(index, pt);
                            lines.add(pt);
                            ps.add(pt);
                            tsp.add(pt);

                            if (first) {
                                path.moveTo(pt2d.getX(), pt2d.getY());
                                first = false;
                            } else {
                                path.lineTo(pt2d.getX(), pt2d.getY());
                            }
                            index++;
                        }
                    } else if (cords[0].equals("WH")) {
                        System.out.println("WORMHOLEFOUND!");
                        if (d == null) {
                            d = new DistanceMatrix(ps);
                        }
                        int firstPointId = java.lang.Integer.parseInt(cords[1]);
                        int secondPointId = java.lang.Integer.parseInt(cords[2]);
                        PointND wormHole = d.addDummyNode(index, lookUp.get(firstPointId),
                                lookUp.get(secondPointId));
                        int insertIdx = firstPointId;
                        if (firstPointId > secondPointId) {
                            insertIdx = secondPointId;
                        }
                        pt2d = wormHole.toPoint2D();
                        lines.add(insertIdx + 1, wormHole);
                        ps.add(insertIdx + 1, wormHole);
                        tsp.add(insertIdx + 1, wormHole);
                        lookUp.put(wormHole.getID(), wormHole);

                        if (first) {
                            path.moveTo(pt2d.getX(), pt2d.getY());
                            first = false;
                        } else {
                            path.lineTo(pt2d.getX(), pt2d.getY());
                        }

                        index++;
                    } else if (cords[0].equals("MANIFOLD")) {
                        System.out.println("MANIFOLD FOUND!");
                        m = new Manifold(java.lang.Integer.parseInt(cords[1]), java.lang.Integer.parseInt(cords[2]),
                                java.lang.Integer.parseInt(cords[3]), java.lang.Integer.parseInt(cords[4]),
                                cords[5].equals("C"));
                        try {
                            m.parse(cords);
                        } catch (FileParseException fpe) {
                            throw new FileParseException(f.toPath(), f.getName(), lineNumber);
                        }
                        manifolds.add(m);

                    } else if (cords[0].equals("ANS")) {
                        for (int i = 1; i < cords.length; i++) {
                            answerOrder.add(java.lang.Integer.parseInt(cords[i]));
                        }
                    } else if (cords[0].equals("LOAD")) {
                        File loadFile = getTestFile(cords[1]);
                        PointSetPath retTup = importFromFile(loadFile);
                        manifolds.addAll(retTup.manifolds);
                        for (PointND pt : retTup.ps) {
                            pt2d = pt.toPoint2D();
                            lookUp.put(index, pt);
                            lines.add(pt);
                            ps.add(pt);
                            tsp.add(pt);

                            if (first) {
                                path.moveTo(pt2d.getX(), pt2d.getY());
                                first = false;
                            } else {
                                path.lineTo(pt2d.getX(), pt2d.getY());
                            }

                            index++;
                        }
                        if (retTup.d != null) {
                            d = new DistanceMatrix(ps);
                        }
                    } else {
                        PointND pt = new PointND.Double(index, java.lang.Double.parseDouble(cords[1]),
                                java.lang.Double.parseDouble(cords[2]));
                        pt2d = pt.toPoint2D();
                        lookUp.put(index, pt);
                        lines.add(pt);
                        ps.add(pt);
                        tsp.add(pt);

                        if (first) {
                            path.moveTo(pt2d.getX(), pt2d.getY());
                            first = false;
                        } else {
                            path.lineTo(pt2d.getX(), pt2d.getY());
                        }

                        index++;
                    }

                }
                if (line.contains("NODE_COORD_SECTION")) {
                    flag = true;
                }
                line = br.readLine();
                lineNumber++;

            }
            br.close();
            if (answerOrder.size() > 0) {
                Shell newAns = new Shell();
                int insertLoc = 0;
                for (Integer i : answerOrder) {
                    PointND vp = lookUp.get(i);
                    newAns.add(insertLoc, vp);
                    insertLoc++;
                }
                tsp = newAns;
            }

            return new PointSetPath(ps, path, tsp, d, manifolds);
        } catch (NumberFormatException | IOException | FileParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static void copyFileContents(File src, File dest) {

        List<String> lines = new ArrayList<String>();
        String line = null;
        try {
            FileReader fr = new FileReader(src);
            BufferedReader br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                lines.add(line + "\n");
            }
            fr.close();
            br.close();

            FileWriter fw = new FileWriter(dest);
            BufferedWriter out = new BufferedWriter(fw);
            for (String s : lines)
                out.write(s);
            out.flush();
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void appendAns(File f, Shell ans) {

        List<String> lines = new ArrayList<String>();
        String line = null;
        try {
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            boolean foundAns = false;
            String ansLine = "ANS ";
            for (int i = 0; i < ans.size(); i++) {
                ansLine += ans.get(i).getID() + " ";
            }
            while ((line = br.readLine()) != null) {
                if (line.contains("ANS ")) {
                    line = ansLine;
                    foundAns = true;
                }
                lines.add(line + "\n");
            }
            if (!foundAns) {
                lines.add(ansLine + "\n");
            }
            fr.close();
            br.close();

            FileWriter fw = new FileWriter(f);
            BufferedWriter out = new BufferedWriter(fw);
            for (String s : lines)
                out.write(s);
            out.flush();
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void appendLine(File f, String appLine) {

        try (FileWriter fw = new FileWriter(f, true)) {
            fw.write(appLine + "\n");
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void writeLines(File f, ArrayList<String> lines) {

        try (FileWriter fw = new FileWriter(f, false)) {
            for (String s : lines) {
                fw.write(s + "\n");
            }
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void appendCutAns(File f, ArrayList<Manifold> manifold) {
        List<String> lines = new ArrayList<String>();
        String line = null;
        try {
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            int i = 0;
            while ((line = br.readLine()) != null) {
                if (line.contains("MANIFOLD ")) {
                    Manifold m = manifold.get(i);
                    if (m.shorterPathFound) {
                        System.out.println(line);
                        line = m.toFileString();
                    }
                    i++;
                }
                lines.add(line + "\n");
            }
            fr.close();
            br.close();

            FileWriter fw = new FileWriter(f);
            BufferedWriter out = new BufferedWriter(fw);
            for (String s : lines)
                out.write(s);
            out.flush();
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
