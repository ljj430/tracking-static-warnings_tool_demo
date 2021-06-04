package edu.concordia.tracking;

import org.jgrapht.Graph;
import org.jgrapht.alg.matching.KuhnMunkresMinimalWeightBipartitePerfectMatching;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;

import java.util.HashSet;

public class HungarianMatrixConstructor extends DefaultWeightedEdge{
    //1initial
    public static Graph<BugInstanceCommit, DefaultWeightedEdge> createGraph() {
        Graph<BugInstanceCommit, DefaultWeightedEdge> graph = new WeightedMultigraph<>(DefaultWeightedEdge.class);
        return graph;
    }
    //2add edge or weight
    public static Graph<BugInstanceCommit, DefaultWeightedEdge> addWeightedEdge(Graph<BugInstanceCommit, DefaultWeightedEdge> graph, BugInstanceCommit pa, BugInstanceCommit ch){
        if (graph.containsEdge(pa,ch)){
            graph.setEdgeWeight(pa,ch,graph.getEdgeWeight(graph.getEdge(pa,ch)) + 1);
        }
        else{
            if(!graph.containsVertex(pa)){
                graph.addVertex(pa);
            }
            if(!graph.containsVertex(ch)){
                graph.addVertex(ch);
            }
            if(!graph.containsEdge(pa,ch)){
                graph.addEdge(pa,ch);
                graph.setEdgeWeight(pa,ch,1);
            }

        }
        return graph;
    }
    //3calculateMatching
    public static HashSet<DefaultWeightedEdge> getHungarianMatchedEdge(Graph<BugInstanceCommit, DefaultWeightedEdge> graph, HashSet<BugInstanceCommit> partition1, HashSet<BugInstanceCommit> partition2){
        MaximumWeightBipartiteMatching mwbm = new MaximumWeightBipartiteMatching(graph,partition1,partition2);
        return (HashSet<DefaultWeightedEdge>) mwbm.getMatching().getEdges();
    }


    public static void main (String[] args){
        Graph<String, DefaultWeightedEdge> graph = new WeightedMultigraph<>(DefaultWeightedEdge.class);
        HashSet<String> partition1 = new HashSet<>();
        HashSet<String> partition2 = new HashSet<>();



        String v1 = "v1";
        String v2 = "v2";
        String v3 = "v3";
        String s1 = "s1";
        String s2 = "s2";
        String s3 = "s3";

        partition1.add(v1);
        partition1.add(v2);
        partition1.add(v3);
        partition2.add(s1);
        partition2.add(s2);
        partition2.add(s3);

        // add the vertices
        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addVertex(v3);
        graph.addVertex(s1);
        graph.addVertex(s2);
        graph.addVertex(s3);

        // add edges to create a circuit
        graph.addEdge(v1, s1);
        graph.addEdge(v1, s2);
        graph.addEdge(v2, s2);
        graph.addEdge(v2, s3);
        graph.addEdge(v3, s3);
        graph.setEdgeWeight(v1,s1,1);
        graph.setEdgeWeight(v1,s2,1);
        graph.setEdgeWeight(v2,s2,1);
        graph.setEdgeWeight(v2,s3,1);
        graph.setEdgeWeight(v3,s3,1);
        MaximumWeightBipartiteMatching km = new MaximumWeightBipartiteMatching(graph,partition1,partition2);
        System.out.println(km.getMatching().getEdges());
        HashSet<DefaultWeightedEdge> matching = (HashSet<DefaultWeightedEdge>) km.getMatching().getEdges();
        for (DefaultWeightedEdge edge : matching) {
            System.out.println(graph.getEdgeSource(edge));
            System.out.println(graph.getEdgeTarget(edge));
        }
    }



//    private static Graph<String, DefaultWeightedEdge> createGraph()
//    {
//        Graph<String, DefaultWeightedEdge> graph = new WeightedMultigraph<>(DefaultWeightedEdge.class);
//        return graph;
//    }

}
