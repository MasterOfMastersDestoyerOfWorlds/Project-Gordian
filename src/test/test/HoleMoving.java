package test;

import shell.BalanceMap;
import shell.BalancerException;
import shell.CutInfo;
import shell.CutMatchList;
import shell.DistanceMatrix;
import shell.FileManagement;
import shell.InternalPathEngine.Route;
import shell.InternalPathEngine.RouteInfo;
import shell.InternalPathEngine.RouteType;
import shell.Knot;
import shell.Point;
import shell.PointND;
import shell.PointSet;
import shell.PointSetPath;
import shell.Segment;
import shell.SegmentBalanceException;
import shell.Shell;
import shell.VirtualPoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * Tests to verify that our tsp solver works as expected
 */
public class HoleMoving {

	/**
	 * Tests that our solver solves the djibouti problem set correctly
	 */

	@Test
	public void test_lines_kp1_10_kp2_9_layer_1() {
		testMethod("lines", "lines_kp1_10_kp2_9_layer_1", 1, 10, 0, 9, 19, true);
	}

	@Test
	public void test_lines_kp1_10_kp2_9_layer_2() {
		testMethod("lines", "lines_kp1_10_kp2_9_layer_2", 2, 10, 0, 9, 19, true);
	}


	public void testMethod(String fileName, String stateFile, int layer, int kp1, int cp1, int kp2, int cp2,
			boolean knotPointsConnected) {
		PointSetPath retTup = FileManagement.importFromFile(new File("./src/test/solutions/" + fileName));
		Shell answer = new Shell();
		int n = retTup.ps.size();

		Shell AB = new Shell();
		for (int i = 0; i < n && i < retTup.tsp.size(); i++) {
			answer.add(retTup.tsp.get(i));
			AB.add(retTup.tsp.get(i));
		}

		DistanceMatrix d = retTup.d;
		if (retTup.d == null) {
			d = new DistanceMatrix(retTup.ps);
		}
		PointND wormHole = d.addDummyNode(d.size(), retTup.ps.getByID(kp1), retTup.ps.getByID(kp2));
		AB.initPoints(d);
		ArrayList<VirtualPoint> kPoints = new ArrayList<>();
		VirtualPoint wh = AB.pointMap.get(wormHole.getID());
		for (PointND p : retTup.tsp) {
			kPoints.add(AB.pointMap.get(p.getID()));
		}
		Knot k = new Knot(kPoints, AB, true);
		VirtualPoint knotPoint1 = AB.pointMap.get(kp1);
		VirtualPoint cutPoint1 = AB.pointMap.get(cp1);
		VirtualPoint knotPoint2 = AB.pointMap.get(kp2);
		VirtualPoint cutPoint2 = AB.pointMap.get(cp2);
		Segment cutSegment1 = k.getSegment(knotPoint1, cutPoint1);
		Segment cutSegment2 = k.getSegment(knotPoint2, cutPoint2);

		VirtualPoint external1 = wh;
		VirtualPoint external2 = wh;

		HashMap<Integer, RouteInfo> routeMap = AB.cutEngine.internalPathEngine.ixdar(
				knotPoint1, cutPoint1, external1,
				knotPoint2, cutPoint2, external2, k, knotPointsConnected, cutSegment1, cutSegment2, layer);

		try {

			BufferedReader br = new BufferedReader(new FileReader(new File("./src/test/routeMap/" + stateFile)));
			ArrayList<PointND> lines = new ArrayList<PointND>();
			String line = br.readLine();
			while (line != null) {
				String[] cords = line.split(" ");
				int id = java.lang.Integer.parseInt(cords[0]);
				RouteInfo r = routeMap.get(id);
				checkRoute(r, cords, 1, "prevC", id, AB);
				checkRoute(r, cords, 4, "prevDC", id, AB);
				checkRoute(r, cords, 7, "nextC", id, AB);
				checkRoute(r, cords, 10, "nextDC", id, AB);

				line = br.readLine();

			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();

			boolean flag = false;
			assert (flag) : "" + e.toString();
		} catch (IOException e) {
			e.printStackTrace();
			boolean flag = false;
			assert (flag) : "" + e.toString();
		}

		System.out.println("reee");
	}

	public void checkRoute(RouteInfo r, String[] cords, int offset, String routeName, int id, Shell AB) {

		String routeType = cords[offset];
		assert (routeType.equals(routeName)) : "Malformed State Test: " + routeName + " expected";
		VirtualPoint ancestor = null;
		if (!cords[offset + 1].equals("NULL")) {
			int ancestorId = java.lang.Integer.parseInt(cords[offset + 1]);
			ancestor = AB.pointMap.get(ancestorId);
		}
		double delta = Double.MAX_VALUE;
		if (!cords[offset + 2].equals("INF")) {
			delta = java.lang.Double.parseDouble(cords[offset + 2]);
		}
		Route route = r.getRoute(RouteType.valueOf(routeName));
		if (ancestor == null) {
			assert (route.ancestor == null)
					: "Point Id: " + id + " " + routeName + " ancestor: " + route.ancestor + " expected: " + null;
		} else {
			if (route.ancestor == null) {
				boolean flag = false;
				assert (flag) :  "Point Id: " + id + " " + routeName + " ancestor: " + route.ancestor + " expected: " + ancestor;
			}
			assert (route.ancestor.equals(ancestor));
		}
		assert (Math.abs(route.delta - delta) < 0.1) : "Point Id: " + id + " prevC delta: " +
				route.delta + " expected: " + delta;
	}
}