import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Scanner;

public class Solution {

    private static Vertex root = null;

    /**
     * An Ordered ArrayList. Guarantees to keep items in order so we can use
     * Arrays.binarySearch (O(logn)) for instance.
     * 
     * @author Fabio Fonseca
     *
     * @param <T>
     *            T is a generic type of the list element.
     */
    public static class OrderedArrayList<T> {
        private ArrayList<T> list;

        public OrderedArrayList() {
            list = new ArrayList<T>();
        }

        public void addAt(int index, T element) {
            list.add(index, element);
        }

        public int search(T element) {
            return Arrays.binarySearch(list.toArray(), element);
        }

        public T at(int index) {
            return list.get(index);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (T i : list) {
                sb.append(i.toString()).append(", ");
            }
            return sb.substring(0, sb.length() - 2).toString();
        }

        public int size() {
            return list.size();
        }

        public void add(T element) {
            int index = Arrays.binarySearch(list.toArray(), element);
            if (index < 0) {
                index = (index + 1) * (-1);
                list.add(index, element);
            }

        }
    }

    public static class Edge implements Comparable<Edge> {
        Vertex node1;
        Vertex node2;
        int cost;

        public Edge(Vertex n1, Vertex n2, int c) {
            node1 = n1;
            node2 = n2;
            cost = c;
        }

        Vertex n1() {
            return node1;
        }

        Vertex n2() {
            return node2;
        }

        int cost() {
            return cost;
        }

        public String toString() {
            return new StringBuilder().append("[").append(node1.node).append(" --(").append(cost)
                    .append(")-- ").append(node2.node).append("]").toString();
        }

        @Override
        public int compareTo(Edge that) {
            if (cost > that.cost) {
                return 1;
            }
            if (cost < that.cost) {
                return -1;
            }
            return 0;
        }

        public boolean equals(Object that) {
            if (that == null) {
                return false;
            }
            if (this == that) {
                return true;
            }
            if (this.getClass() != that.getClass()) {
                return false;
            }
            Edge eThat = (Edge) that;
            if (this.node1 == eThat.node1 && this.node2 == eThat.node2 && this.cost == eThat.cost) {
                return true;
            }
            return false;
        }
    }

    public static class Vertex implements Iterator<Edge>, Comparable<Vertex> {
        private int node;
        private ArrayList<Edge> edges;

        public Vertex(int n) {
            node = n;
            edges = new ArrayList<Edge>();
        }

        public void addEdge(Edge e) {
            edges.add(e);
        }

        public int node() {
            return node;
        }

        public void addEdge(Vertex toVertex, int cost) {
            // search if node already exists and get its reference
            // create a new edge and add it into both nodes' lists
            Edge edge = new Edge(this, toVertex, cost);
            edges.add(edge);
            toVertex.addEdge(edge);
        }

        @Override
        public boolean hasNext() {
            return edges.iterator().hasNext();
        }

        @Override
        public Edge next() {
            return edges.iterator().next();
        }

        public Vertex findVertex(Vertex prior, int n) {
            // perform a recursive search to find Vertex n (DSF)
            if (node == n) {
                return this;
            }
            for (Edge e : edges) {
                Vertex v = (e.node1.node == node ? e.node2 : e.node1);
                if (v.node == n) {
                    return v;
                }
                if (v == prior) {
                    continue;
                }
                Vertex found = v.findVertex(this, n);
                if (found != null) {
                    return found;
                }
            }
            return null;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder().append(Integer.toString(node)).append("\n");
            for (Edge e : edges) {
                sb.append(e.toString()).append("\n");
            }
            return sb.toString();
        }

        @Override
        public int compareTo(Vertex that) {
            // to be used in the sorting phase
            if (node > that.node) {
                return 1;
            }
            if (node < that.node) {
                return -1;
            }
            return 0;
        }
    }

    public static void main(String[] args) {

        // check command line arguments
        if (args.length < 1) {
            System.out.println("Need one parameter (for instance edges.txt)");
            return;
        }

        // open file
        File file = new File(args[0]);

        Scanner in;
        try {
            in = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        // read its content
        int n = in.nextInt(); // number of nodes
        int v = in.nextInt(); // number of edges

        for (int i = 0; i < v; i++) {
            int node1 = in.nextInt();
            int node2 = in.nextInt();
            int cost = in.nextInt();

            Vertex node1V;
            Vertex node2V;
            // if root == null the first vertex has to be inserted into it
            // else first try to find if the vertex exists, otherwise create a
            // new one
            if (root == null) {
                // first vertex in the graph
                root = new Vertex(node1);
                node1V = root;
            } else {
                // find node 1
                // O(n)
                node1V = root.findVertex(root, node1);
            }
            // if node1 == null then there is something wrong...

            // now find node2
            node2V = root.findVertex(root, node2);

            if (node2V == null) {
                // new Vertex
                node2V = new Vertex(node2);
            }

            node1V.addEdge(node2V, cost);
        }

        in.close();

        // finished creating the graph

        // Prim's MST algorithm
        // keep track of the spanned vertexes
        // use some data structure that allows for quick finding... ordered
        // queue or something like that
        // I'll have to create mine, since ArrayList or similar does not
        // guarantee ordering of elements
        OrderedArrayList<Integer> spannedVertex = new OrderedArrayList<Integer>();

        // keep track of the less costly edge
        // insert all edges related to the spanned vertex into a PQ (Heap)
        // and poll from it the less costly
        PriorityQueue<Edge> edgesPQ = new PriorityQueue<Edge>();

        // start with the root Vertex
        spannedVertex.addAt(0, root.node);
        edgesPQ.addAll(root.edges);

        int accumulatedCost = 0;
        // go through that edge and span that vertex
        // get the new vertex of that edge and insert into the above mentioned
        // data structure
        // insert all the edges that would link to unvisited vertexes into the
        // PQ
        // accumulate cost of all edges visited
        // keep going until all vertexes are spanned
        while (spannedVertex.size() < n) {
            Edge e = edgesPQ.poll(); // got the cheapest edge
            // what vertex visit next?
            Vertex vertex = (spannedVertex.search(e.node1.node) < 0 ? e.node1 : e.node2);
            // verify if the vertex was not visited already
            if (spannedVertex.search(vertex.node) > 0) {
                // vertex already visited
                continue;
            }
            spannedVertex.add(vertex.node);
            accumulatedCost += e.cost; // accumulate cost of spanned vertexes
            // through their cheapest edges
            for (Edge edge : vertex.edges) {
                if (!edge.equals(e)) {
                    edgesPQ.add(edge);
                }
            }
        }
        System.out.println("Accumulated cost: ".concat(Integer.toString(accumulatedCost)));
    }

}
