package shell.file;

import java.awt.geom.Path2D;
import java.util.ArrayList;

import shell.DistanceMatrix;
import shell.PointSet;
import shell.shell.Shell;

/**
 * The optimal tsp path in a pointset ps
 */
public class PointSetPath {
	public PointSet ps;
	public Path2D path;
	public Shell tsp;
	public DistanceMatrix d;
	public ArrayList<Manifold> manifolds;


	/**
	 * Initializes the path and pointset variables
	 */
	public PointSetPath(PointSet ps, Path2D path, Shell tsp, DistanceMatrix d, ArrayList<Manifold> manifolds) {
		this.path = path;
		this.ps = ps;
		this.tsp = tsp;
		this.d = d;
		this.manifolds = manifolds;
	}

}
