package byow.Core;

import edu.princeton.cs.algs4.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DijkstraUndirMaskedSP {
    /** Edge weighted undirected graph. */
    private TileGraph g;
    /** Distances to vertices in graph. */
    private double[] distTo;
    /** Keep track of edges connecting vertices on the shortest path computed */
    private Edge[] edgeTo;
    /** Priority queue to keep track of distances between source vertex and other vertices. */
    private IndexMinPQ<Double> pq;

    public DijkstraUndirMaskedSP(TileGraph g, int s,
                                 ArrayList<HashSet<Integer>> inaccessibleAreas,
                                 HashSet<Integer> existingPaths) {
        for (Edge e : g.edges()) {
            if (e.weight() < 0)
                throw new IllegalArgumentException("edge " + e + " has negative weight");
        }
        this.g = g;
        this.distTo = new double[g.V()];
        this.edgeTo = new Edge[g.V()];
        validateVertex(s);

        for (int v = 0; v < g.V(); v++)
            distTo[v] = Double.POSITIVE_INFINITY;
        distTo[s] = 0.0;

        Set<Integer> inaccessibleVertices = new HashSet<>();
        for (HashSet<Integer> area : inaccessibleAreas) {
            inaccessibleVertices.addAll(area);
        }
        inaccessibleVertices.addAll(existingPaths);

        // relax vertices in order of distance from s
        pq = new IndexMinPQ<>(g.V());
        pq.insert(s, distTo[s]);
        while (!pq.isEmpty()) {
            int v = pq.delMin();
            for (Edge e : g.adj(v))
                relax(e, v, inaccessibleVertices);
        }

        // check optimality conditions
        assert check(g, s);
    }

    /** For a edge e that connects vertex v, updates the shortest distance from source vertex to
     * the other vertex of e unless the other vertex is in the given exception set or the
     * existing distance is equal or shorter than the new distance. */
    private void relax(Edge e, int v, Set<Integer> exception) {
        int w = e.other(v);
        Set<Integer> wPeriphery = TileGraph.getVPeriphery(g, v);
        if (!g.isVertexOnGraphBoundary(v) && Collections.disjoint(wPeriphery, exception)
                && distTo[w] > distTo[v] + e.weight()) {
            distTo[w] = distTo[v] + e.weight();
            edgeTo[w] = e;
            if (pq.contains(w)) {
                pq.decreaseKey(w, distTo[w]);
            }
            else {
                pq.insert(w, distTo[w]);
            }
        }
    }

    /** Return the distance from source vertex to given vertex v */
    public double distTo(int v) {
        validateVertex(v);
        return distTo[v];
    }

    /** Check if there is a path between source vertex and given vertex v. */
    public boolean hasPathTo(int v) {
        validateVertex(v);
        return distTo[v] < Double.POSITIVE_INFINITY;
    }

    /** Return the path from source vertex to the given vertex in a sequence of integer,
     * including both source and target vertices. */
    public Iterable<Integer> pathTo(int v) {
        validateVertex(v);
        Stack<Integer> vPath = new Stack<>();
        if (!hasPathTo(v)) {
            return vPath;
        }
        int x = v;
        for (Edge e = edgeTo[v]; e != null; e = edgeTo[x]) {
            vPath.push(x);
            x = e.other(x);
        }
        vPath.push(x);
        return vPath;
    }

    /** Check integrity of the graph. */
    private boolean check(EdgeWeightedGraph G, int s) {
        // check that edge weights are non-negative
        for (Edge e : G.edges()) {
            if (e.weight() < 0) {
                System.err.println("negative edge weight detected");
                return false;
            }
        }

        // check that distTo[v] and edgeTo[v] are consistent
        if (distTo[s] != 0.0 || edgeTo[s] != null) {
            System.err.println("distTo[s] and edgeTo[s] inconsistent");
            return false;
        }
        for (int v = 0; v < G.V(); v++) {
            if (v == s) continue;
            if (edgeTo[v] == null && distTo[v] != Double.POSITIVE_INFINITY) {
                System.err.println("distTo[] and edgeTo[] inconsistent");
                return false;
            }
        }

        // check that all edges e = v-w satisfy distTo[w] <= distTo[v] + e.weight()
        for (int v = 0; v < G.V(); v++) {
            for (Edge e : G.adj(v)) {
                int w = e.other(v);
                if (distTo[v] + e.weight() < distTo[w]) {
                    System.err.println("edge " + e + " not relaxed");
                    return false;
                }
            }
        }

        // check that all edges e = v-w on SPT satisfy distTo[w] == distTo[v] + e.weight()
        for (int w = 0; w < G.V(); w++) {
            if (edgeTo[w] == null) continue;
            Edge e = edgeTo[w];
            if (w != e.either() && w != e.other(e.either())) return false;
            int v = e.other(w);
            if (distTo[v] + e.weight() != distTo[w]) {
                System.err.println("edge " + e + " on shortest path not tight");
                return false;
            }
        }
        return true;
    }

    private void validateVertex(int v) {
        int V = distTo.length;
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V-1));
    }
}
