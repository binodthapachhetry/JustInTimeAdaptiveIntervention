package edu.neu.android.wocketslib.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.graphics.PointF;
import android.location.Location;

public class DBSCANAlgorithm {
	
	// singleton
	private static DBSCANAlgorithm sAlgorithm;
	
	public static DBSCANAlgorithm getInstance() {
		if (sAlgorithm == null) {
			sAlgorithm = new DBSCANAlgorithm();
		}				
		
		return sAlgorithm;
	}
	
	private List<HashSet<PointF>> mClusters;
	private HashMap<PointF, Boolean> mVisited;
	private PointF[] mDataset;
	private HashMap<PointF, Float[]> mDistMatrix;
	
	/**
	 * 	 
		DBSCAN(D, eps, MinPts)
		   C = 0
		   for each unvisited point P in dataset D
		      mark P as visited
		      NeighborPts = regionQuery(P, eps)
		      if sizeof(NeighborPts) < MinPts
		         mark P as NOISE
		      else
		         C = next cluster
		         expandCluster(P, NeighborPts, C, eps, MinPts)
	*/
	public List<HashSet<PointF>> doCluster(PointF[] dataset, float eps, int minPts) {
		mClusters = new ArrayList<HashSet<PointF>>();
		mDataset = dataset;
		mVisited = new HashMap<PointF, Boolean>();
		
		// Get the distance matrix
		mDistMatrix = getDistanceMatrix();
		
		// Initially all points are not visited
		for (int i = 0; i < dataset.length; ++i) {
			mVisited.put(dataset[i], false);
		}
		
		for (int i = 0; i < dataset.length; ++i) {
			PointF current = dataset[i];
			if (mVisited.get(current)) {
				continue;
			}
			// Mark the point as visited
			mVisited.put(current, true);
			
			// Find all the neighbor points
			HashSet<PointF> neighbors = getNeighborPoints(current, eps);
			
			if (neighbors.size() < minPts) {
				; // Mark this point as Noise
			} else {
				HashSet<PointF> cluster = new HashSet<PointF>();
				expandCluster(current, neighbors, cluster, eps, minPts);
				mClusters.add(cluster);
			}
		}
		
		return mClusters;		
	}
	
	/**
	 * expandCluster(P, NeighborPts, C, eps, MinPts)
		   add P to cluster C
		   for each point P' in NeighborPts 
		      if P' is not visited
		         mark P' as visited
		         NeighborPts' = regionQuery(P', eps)
		         if sizeof(NeighborPts') >= MinPts
		            NeighborPts = NeighborPts joined with NeighborPts'
		      if P' is not yet member of any cluster
		         add P' to cluster C	 
	 */
	private void expandCluster(PointF current, HashSet<PointF> neighbors, HashSet<PointF> cluster,
			float eps, int minPts) {		
		cluster.add(current);
				
		HashSet<PointF> neighborsNew;
		do {
			neighborsNew = new HashSet<PointF>();
			for (PointF point : neighbors) {
				if (!mVisited.get(point)) {
					mVisited.put(point, true);
					HashSet<PointF> temp = getNeighborPoints(point, eps);
					if (temp.size() >= minPts) {
						neighborsNew.addAll(temp);
					}					
				}
				if (!cluster.contains(point) && !isInClusters(point)) {
					cluster.add(point);
				}				
			}
			neighbors.addAll(neighborsNew);
		} while (neighborsNew.size() > 0);
	}
	
	private boolean isInClusters(PointF point) {
		for (HashSet<PointF> cluster : mClusters) {
			if (cluster.contains(point)) {
				return true;
			}
		}
		
		return false;
	}
	
	private HashMap<PointF, Float[]> getDistanceMatrix() {
		int n = mDataset.length;
		HashMap<PointF, Float[]> matrix = new HashMap<PointF, Float[]>();
		
		float[] results = new float[3];
		for (int i = 0; i < n; ++i) {
			PointF from = mDataset[i];
			Float[] row = new Float[n];
			for (int j = 0; j < n; ++j) {
				PointF to = mDataset[j];
				Location.distanceBetween(from.x, from.y, to.x, to.y, results);
				row[j] = results[0];
			}
			matrix.put(from, row);
		}
		
		return matrix;
	}
	
	private HashSet<PointF> getNeighborPoints(PointF p, float eps) {
		HashSet<PointF> neighborPts = new HashSet<PointF>();
		Float[] dist = mDistMatrix.get(p);
		assert(dist.length == mDataset.length);
		
		for (int i = 0; i < dist.length; ++i) {
			if (dist[i] <= eps) {
				neighborPts.add(mDataset[i]);
			}
		}
		
		return neighborPts;
	}
}
