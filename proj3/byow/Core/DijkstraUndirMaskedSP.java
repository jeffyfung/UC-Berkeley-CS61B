package byow.Core;

import edu.princeton.cs.algs4.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DijkstraUndirMaskedSP {
    private TileGraph g;
    private double[] distTo;
    private Edge[] edgeTo;
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

    // do not relax any vertices in exception
    // TODO: run with width=1 path for testing first - restrict path from touching boundaries?
    private void relax(Edge e, int v, Set<Integer> exception) {
        int w = e.other(v);
        Set<Integer> wPeriphery = TileGraph.getVPeriphery(g, v);
        if (Collections.disjoint(wPeriphery, exception) && distTo[w] > distTo[v] + e.weight()) {
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

    public double distTo(int v) {
        validateVertex(v);
        return distTo[v];
    }

    public boolean hasPathTo(int v) {
        validateVertex(v);
        return distTo[v] < Double.POSITIVE_INFINITY;
    }

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
