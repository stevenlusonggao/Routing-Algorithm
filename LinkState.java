import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class LinkState {
	public static void main(String[] args) throws IOException{
		int[][] graph;  
		ArrayList<Integer> gateways = new ArrayList<Integer>();
		int vertices;
		String input;
		BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter the number of routers: ");
		try {
			input = buffer.readLine();
			vertices = Integer.parseInt(input);
			graph = new int[vertices][vertices];
			for (int i = 0; i < vertices; i++) {
				System.out.println("Enter the path weights for router " + (i + 1) +": ");
				input = buffer.readLine();
				int counter = 0;
				for (String s: input.split(" ")) {
					graph[i][counter] = Integer.parseInt(s);
					counter++;
				}
			}
			System.out.println("Enter gateway router numbers: ");
			input = buffer.readLine();
			for (String s: input.split(" ")) 
				gateways.add(Integer.parseInt(s));
			
			for (int i = 0; i < graph.length; i++) { //calculate minimum paths and print forwarding tables
				if (gateways.contains(i + 1) == false) {
					ArrayList<vertex> inputGraph = new ArrayList<vertex>();
					for (int j = 0; j < graph.length; j++) { //create the graph representation that will be used in the routing algo
						vertex vertex = new vertex();
						vertex.index = j + 1;
						for(int n = 0; n < graph[j].length; n++) {
							if (graph[j][n] != -1) {
								edge edge = new edge(j, n, graph[j][n]);
								vertex.edges.add(edge);
								if (i == j && n == i) {
									vertex.path_weight = graph[j][n];
								}
							} 
						}
						inputGraph.add(vertex);
					}
					//System.out.println(graph[0].length + " " + inputGraph.size());
					int[][] result = dijkstra(i, inputGraph); //execute routing algo
					//print results into table
					System.out.println("Forwarding Table for " + (i + 1));
					System.out.println("To   Cost   Next Hop");
					for(int j = 0; j < result.length; j++) { 
						if (gateways.contains(j + 1)) {
							System.out.println((j + 1) + "    " + result[j][0] + "      " + result[j][1] );
						}
					}
				}
			}
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	static int[][] dijkstra(int source, ArrayList<vertex> graph) {
		int[][] result = new int[graph.size()][2]; //result will return an array consisting of an array of size 2 that stores: [path weight, next hop] of each vertex for the source 
		int next_up_counter = 0;
		
		//first configure source node
		//graph.get(source).path_weight = 0;
		graph.get(source).next_hop = source + 1;
		graph.get(source).visited = true;
		graph.get(source).next_up = false;
		boolean unreachable_source = true;
		//System.out.println("processing source " + source + ", ");
		for (int i = 0; i < graph.get(source).edges.size(); i++) { //set the path weights and next hop for the neighbours of the source node
			graph.get(graph.get(source).edges.get(i).to).path_weight = graph.get(source).edges.get(i).weight;
			graph.get(graph.get(source).edges.get(i).to).next_hop = graph.get(graph.get(source).edges.get(i).to).index;
			graph.get(graph.get(source).edges.get(i).to).source_adjacent = true;
			if (graph.get(graph.get(source).edges.get(i).to).next_up == false && graph.get(source).edges.get(i).to != source) {
				graph.get(graph.get(source).edges.get(i).to).next_up = true;
				//System.out.println("source:" + graph.get(source).edges.get(i).to + "added");
				next_up_counter++;
			}
			unreachable_source = false;
		}
		
		//start working through the rest of the graph
		int visited_counter = 1;
		boolean done = false;
		if (unreachable_source == true)
			done = true;
		while (done == false) {
			for (int i = 0; i < graph.size(); i++) {
				if (graph.get(i).visited != true && graph.get(i).next_up == true) {
					ArrayList<edge> currentEdges = graph.get(i).edges;
					for (int j = 0; j < currentEdges.size(); j++) {
						if (graph.get(currentEdges.get(j).to).path_weight == -1) {
							graph.get(currentEdges.get(j).to).path_weight = graph.get(i).path_weight + currentEdges.get(j).weight;
							graph.get(currentEdges.get(j).to).next_hop = graph.get(i).next_hop;
							if (graph.get(currentEdges.get(j).to).next_up == false) {
								graph.get(currentEdges.get(j).to).next_up = true;
								//System.out.println("other added");
								next_up_counter++;
							}
						}
						else if ((graph.get(i).path_weight + currentEdges.get(j).weight) < graph.get(currentEdges.get(j).to).path_weight) {
							graph.get(currentEdges.get(j).to).path_weight = graph.get(i).path_weight + currentEdges.get(j).weight;
							graph.get(currentEdges.get(j).to).next_hop = graph.get(i).next_hop;
							if (graph.get(currentEdges.get(j).to).next_up == false) {
								graph.get(currentEdges.get(j).to).next_up = true;
								//System.out.println("other added");
								next_up_counter++;
							}
						}
					}
					graph.get(i).visited = true;
					graph.get(i).next_up = false;
					//System.out.println("index" + i + "deleted");
					next_up_counter--;
					visited_counter++;
				}
			}
			//System.out.println(next_up_counter);
			if (visited_counter >= graph.size() || next_up_counter == 0) 
				done = true;
		}
		//construct the result array
		for (int i = 0; i < graph.size(); i++) {
			result[i][0] = graph.get(i).path_weight;
			result[i][1] = graph.get(i).next_hop;
		}
		//System.out.println(Arrays.deepToString(result).replace("], ", "]\n"));
		return result;
	}
}

class vertex {
	ArrayList<edge> edges = new ArrayList<edge>();
	boolean visited = false;
	int path_weight = -1;    //from source
	int next_hop = -1;
	int index = 0;
	boolean next_up = false;
	boolean source_adjacent = false;
}

class edge {
	int from;
	int to;
	int weight;
	edge(int from, int to, int weight) {
		this.from = from;
		this.to = to;
		this.weight = weight;
	}
}