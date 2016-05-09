/**
 *
 * This is an implementation of the algorithm 2.1. given in: 
 * Kshipra Bhawalkar, Jon Kleinberg, Kevin Lewi, Tim Roughgarden, and Aneesh Sharma
 * Preventing Unraveling in Social Networks: The Anchored k-Core Problem
 * Algorithm 2.1. An efficient, exact algorithm for anchored 2-core
 * 
 * The graph is stored using Webgraph (see P. Boldi and S. Vigna. The webgraph framework I: compression techniques. WWW 04.)
 *
 * @author Babak Tootoonchi, babakt@uvic.ca, 2015
 */

package ca.uvic.css.unrevealing;

import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.NodeIterator;

import ca.uvic.css.util.Queue;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import ca.uvic.css.graph.*;
import ca.uvic.css.kcore.KCoreWG_BZ;
import ca.uvic.css.util.*;

public class Anchored {
    private static final boolean DEBUG = false;
    private static final int ANCHOR_DEGREE = 1000;
	private static Anchored uniqueInstance;
    private static SeparateChainingHash<Integer, Integer> sth;
    private static int vertexIndex; 
    private static KCoreWG_BZ kc3;
    private static int degree[];
    private static int vertices[];
    private static int degreeKCore[];

    private static char vertexTreeStatus[];
	private static int removeCoreVertices[];
	private static Queue<Integer> q_vertices = new Queue<Integer>();
	
	private Anchored() {}

	public static Anchored getInstance() {
		if (uniqueInstance == null) 
			uniqueInstance = new Anchored();
		return uniqueInstance;
	}

	private static void getKCoreWGBZInstance(String path_input) {
		if (kc3 == null) {
			try {
                kc3 = new KCoreWG_BZ(path_input, "webgraph");
			} catch(Exception e){
        		System.out.println("Cannot create an instance of KCoreWG_BZ!");
    		}
    	}
	}

    public static void findAnchors(String path_input, String path_output, int budget) throws  ClassNotFoundException, IllegalArgumentException, SecurityException, IllegalAccessException, Exception, IOException {
        long startTime = System.currentTimeMillis();
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path_output)));

        kc3 = new KCoreWG_BZ(path_input, "webgraph");
        // getKCoreWGBZInstance(path_input);
        kc3.KCoreCompute();

        degree = kc3.getDegree();
        vertices = kc3.getVertex();

        degreeKCore = new int[degree.length];
        vertexTreeStatus = new char[vertices.length];
		removeCoreVertices = new int[vertices.length];
        boolean verMarked[] = new boolean[vertices.length];
        boolean verVisited[] = new boolean[verMarked.length];
        System.arraycopy(degree, 0, degreeKCore, 0, degree.length);
		out.write("\nvertices.length : " + vertices.length);
        long endTimeb = System.currentTimeMillis();
        out.write("\nbudget " + budget);
        out.write("\nbudget time for processing " + (endTimeb - startTime) + " ms.");
        out.write("degreeKCore[vertices[0]] = "+degreeKCore[vertices[0]]+" vertices[0] = "+vertices[0]);
	while (budget > 0) {
            Arrays.fill(verMarked, true);
            Arrays.fill(verVisited, false);
            Arrays.fill(vertexTreeStatus, 'v'); 
            Arrays.fill(removeCoreVertices, 0);
            
            out.write("\ncreateKCore_BZ " + budget);
            int countRemoveCore = createKCore_BZ(2);
            long endTimec = System.currentTimeMillis();
            out.write("\ncreateKCore_BZ time for processing " + (endTimec - startTime) + " ms.");
            out.write("\ncountRemoveCore " + countRemoveCore);
            if (countRemoveCore > 0) {
                boolean isRooted = false;
                Arrays.fill(verMarked, false);

                //sth = new SeparateChainingHash<Integer, Integer>(countRemoveCore);
                sth = new SeparateChainingHash<Integer, Integer>(vertices.length);
                /*for (vertexIndex = 0; vertexIndex <= vertices.length-1; ++vertexIndex) {

                    int distance = 0;
                    if (degreeKCore[vertices[vertexIndex]] == 0) {
                        isRooted = checkRootedTree(degreeKCore, verVisited, vertices[vertexIndex], verMarked, distance);
                        if (!isRooted && !verVisited[vertices[vertexIndex]])
                            vertexTreeStatus[vertices[vertexIndex]] = 'n';
                        // if (DEBUG) System.out.println("vertex[" + vertexIndex + "]=" + vertices[vertexIndex] + " isRooted: " + isRooted + " verMarked: " + verMarked[vertices[vertexIndex]] + " verVisited: " + verVisited[vertices[vertexIndex]]);
                    }
                }*/
				
				//removeCoreVertices = new int[countRemoveCore];
				//int rvIndex = 0;
				for(int rvIndex = 0; rvIndex <= countRemoveCore; ++rvIndex) {
					//vertexIndex = q_vertices.dequeue();
					vertexIndex  = removeCoreVertices[rvIndex];
                    // if (DEBUG) System.out.println("\nrvIndex :" + rvIndex + " vertexIndex: " + vertexIndex + " degreeKCore[vertices[vertexIndex]]: " + degreeKCore[vertices[vertexIndex]]);
					int distance = 0;
                    if (degreeKCore[vertexIndex] == 0) {
                        isRooted = checkRootedTree(rvIndex, degreeKCore, verVisited, vertexIndex, verMarked, distance);
                        if (!isRooted && !verVisited[vertexIndex])
                            vertexTreeStatus[vertexIndex] = 'n';
                        // if (DEBUG) System.out.println("vertex[" + vertexIndex + "]=" + vertices[vertexIndex] + " isRooted: " + isRooted + " verMarked: " + verMarked[vertices[vertexIndex]] + " verVisited: " + verVisited[vertices[vertexIndex]]);
                    }
				}

                /*if (DEBUG) {
                    for (int i = 0; i < vertexTreeStatus.length; ++i) 
                        System.out.println("i: " + i + " vertexTreeStatus: " + vertexTreeStatus[i]);
                    for (int i = 0; i < countRemoveCore; ++i) {
                        for (Integer key : sth.st[i].keys()) {
                            System.out.println("i: " + i + ", " + key + ", " + sth.st[i].get(key) + ", " + sth.st[i].size());
                        }
                    }
                }*/ //DEBUG

                int rootedNumberVerticesSaved = 0, nonRootedNumberVerticesSaved = 0;
                Map<Integer,Integer> vertexMap = new HashMap<Integer,Integer>();
                List<Map.Entry<Integer,Integer>> anchorVertices = new ArrayList<Map.Entry<Integer,Integer>>(vertexMap.entrySet());
                //anchorVertices = findFurthestLongestVertices(countRemoveCore, vertices);
                anchorVertices = findFurthestLongestVertices(vertices.length, vertices);
                for (int l = 0; l < anchorVertices.size(); ++l) {
                    if (l < 2)
                        rootedNumberVerticesSaved += anchorVertices.get(l).getValue();
                    else
                        nonRootedNumberVerticesSaved += anchorVertices.get(l).getValue();
                    System.out.println("key: " + anchorVertices.get(l).getKey() + " value: " + anchorVertices.get(l).getValue());
                    out.write("\nkey: " + anchorVertices.get(l).getKey() + " value: " + anchorVertices.get(l).getValue());
                }

                /*if (DEBUG) {
                    Iterator<Map.Entry<Integer,Integer>> anchorVerticesIterator = anchorVertices.iterator();
                    while (anchorVerticesIterator.hasNext()) {
                        System.out.println(anchorVerticesIterator.next());
                    }
                    
                    for (int l = 0; l < anchorVertices.size(); ++l) 
                        System.out.println("key: " + anchorVertices.get(l).getKey() + " value: " + anchorVertices.get(l).getValue());
                }*/ //DEBUG
            
                if (rootedNumberVerticesSaved > nonRootedNumberVerticesSaved || budget == 1) {
                    updateVertex(anchorVertices, countRemoveCore, 'r');
                    --budget;
                } else {
                    updateVertex(anchorVertices, countRemoveCore, 'n');
                    budget -= 2;
                }

                /*if (DEBUG) {
                    System.out.println("budget: " + budget);
                    for(int i = 0; i < degree.length; ++i) {
                        System.out.println("degree[" + i + "] = " + degree[i] + " degreeKCore[" + i + "] = " + degreeKCore[i]);
                    }
                }*/ //DEBUG
            }
	    else
		budget--;
        }

        long endTime = System.currentTimeMillis();
        System.out.println ("Total time for processing " + (endTime - startTime) + " ms.");
        out.write("\nTotal time for processing " + (endTime - startTime) + " ms.");
        out.close();
        System.out.println( "finish" );
    }

    private static int createKCore_BZ(int k) throws Exception, IOException {
        /*long startTime = System.currentTimeMillis();
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("./dataset/createKCore_BZ.txt")));*/
        boolean verMarked[] = new boolean[vertices.length];
        Arrays.fill(verMarked, true);

        int countRemoveCore = 0;
        boolean isIterate = false;
        int v;
        int rvIndex = 0;
        do {
            v = 0;
            isIterate = false;
            while((degreeKCore[vertices[v]] < k && degree[v] > 0 && v < vertices.length-1)) {
                if (verMarked[v]) {
                    degreeKCore[vertices[v]] = 0;
                    // if (DEBUG) System.out.println("\nv: "+v+" vertices[v]: "+vertices[v]+" degreeKCore[vertices[v]]: "+degreeKCore[vertices[v]]);
                    removeCoreVertices[rvIndex] = vertices[v];
                    ++countRemoveCore;
                    int[] neighbors = kc3.graph.getNeighbors(vertices[v]);
                  
                    for(int i = 0; i <= neighbors.length-1; ++i) {
                        int vertex = neighbors[i];
                        if (degreeKCore[vertex] > 0)
                            degreeKCore[vertex]--; 
                        if (degreeKCore[vertex] < k && degreeKCore[vertex] > 0)
                            isIterate = true;
                    }
                    verMarked[v] = false;
                    rvIndex++;
                }
                v++;
            }
        } while(isIterate && v < vertices.length-1);

        // if (!DEBUG) {
        //     // while (!q.isEmpty()) {
        //     //     System.out.println(q.dequeue() + " ");
        //     // }
        //     // System.out.println("(" + q.size() + " left on queue)");

        //     // for(int i = 0; i < degree.length; ++i) {
        //     //     System.out.println("degree[" + i + "] = " + degree[i] + " vertices[" + i + "] = " + vertices[i] + " degreeKCore[" + i + "] = " + degreeKCore[i] + " verMarked[" + i + "] = " + verMarked[i] + "\n");
        //     // }

        //     for(int i = 0; i < degree.length; ++i)
        //         System.out.println("removeCoreVertices[" + i + "] = "  + removeCoreVertices[i]);
        // } //DEBUG
        /*long endTime = System.currentTimeMillis();
        out.write ("\nCount Remove Core " + countRemoveCore);
        out.write("\nTotal time for processing " + (endTime - startTime) + " ms.");
        out.close();*/
        return countRemoveCore;
    }

    private static boolean checkRootedTree(int rvIndex, int[] degKCore, boolean[] verVisited, int v, boolean[] verMarked, int distance) throws Exception, IOException {
        /*long startTime = System.currentTimeMillis();
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("./dataset/checkRootedTree.txt")));*/
        boolean isRooted = false;

        // if (DEBUG) System.out.println("v: " + v + " neighbors(): " + degKCore[v]);
        if (degKCore[v] == 0 && !verMarked[v]) {
            verMarked[v] = true;
            int[] neighbors = kc3.graph.getNeighbors(v);
            // if (DEBUG) System.out.println("length: " + neighbors.length);
			/*out.write("\nv: " + v + " neighbors(): " + degKCore[v] + " length: " + neighbors.length + " vertexIndex: "+vertexIndex);
			out.close();*/
            ++distance;
            for (int i = 0; i <= neighbors.length-1; ++i) {
				if (neighbors[i] > vertices.length-1) {
					//out.write("\n neighbors["+i+"]: "+neighbors[i]+" out ");
					continue;
				}
                if (!verMarked[neighbors[i]]) {
					//if (!sth.st[vertexIndex].contains(neighbors[i])) {
					if (!sth.st[rvIndex].contains(neighbors[i])) {
						//out.write("\n checkRootedTree neighbors[" + i + "]: " + neighbors[i]);
                    	//sth.st[vertexIndex].put(neighbors[i], distance);
						sth.st[rvIndex].put(neighbors[i], distance);
                   	 	vertexTreeStatus[neighbors[i]] = 'n';
	                    // if (!DEBUG) System.out.println("checkRootedTree rvIndex: "+ rvIndex +" neighbors[" + i + "]: " + neighbors[i] +" distance: " + distance);
					}
                }
                // if (DEBUG) System.out.println("vertexIndex: " + vertexIndex + " i: " + i + " neighbors(" + neighbors[i] + "): " + degKCore[neighbors[i]] + " " + verMarked[neighbors[i]]+ " v " + v + " Marked " + verMarked[v]);

                if (degKCore[neighbors[i]] == 0) {
                    // if (DEBUG) System.out.println("neighbors[i]: " + neighbors[i]);

                    isRooted = checkRootedTree(rvIndex, degKCore, verVisited, neighbors[i], verMarked, distance); 
                }
                else {
                    isRooted = true;
                    distance = 0;
                    /*boolean verVisited[] = new boolean[verMarked.length];
                    Arrays.fill(verVisited, false);*/ // TODO: maybe we can use verMarked instead of verVisited. Check it out!
                    //sth.st[vertexIndex].put(neighbors[i], 0);
					sth.st[rvIndex].put(neighbors[i], 0);
                    // if (DEBUG) System.out.println("checkRootedTree rvIndex: "+ rvIndex +" neighbors[" + i + "]: " + neighbors[i] +" distance: " + distance);
                    vertexTreeStatus[neighbors[i]] = 'r';
                    computeDistance(rvIndex, degKCore, neighbors[i], verVisited, distance);
                }
            }
        }
        //long endTime = System.currentTimeMillis();
        //out.write("\nTotal time for processing " + (endTime - startTime) + " ms.");
        //out.close();
        return isRooted;
    }

    private static void computeDistance(int rvIndex, int[] degKCore, int v, boolean[] verVisited, int distance) {
        int[] neighbors = kc3.graph.getNeighbors(v);
        // if (DEBUG) System.out.println("distance: "+ distance+ " v: "+v + " length: "+neighbors.length);

        verVisited[v] = true;
        ++distance;
        for (int i = 0; i <= neighbors.length-1; ++i) {
            // if (DEBUG) {
            //     // if (sth.st[vertexIndex].contains(neighbors[i]))
            //     if (sth.st[rvIndex].contains(neighbors[i]))
            //         System.out.println("contains "+ sth.st[rvIndex].contains(neighbors[i]) + " neighbors[i]: " + neighbors[i]);
            //     // System.out.println("vertexIndex: "+ vertexIndex + " i: " + i + " neighbors(" + neighbors[i] + "): " + degKCore[neighbors[i]] + " " + verVisited[neighbors[i]] + " v " + v + " Marked " + verVisited[v]);
            //     System.out.println("rvIndex: "+ rvIndex + " i: " + i + " neighbors(" + neighbors[i] + "): " + degKCore[neighbors[i]] + " " + verVisited[neighbors[i]] + " v " + v + " Marked " + verVisited[v]);
            // } //DEBUG

            if (degKCore[neighbors[i]] == 0 && !verVisited[neighbors[i]]) {
                // if (DEBUG) System.out.println("computeDistance neighbors[" + i + "]: " + neighbors[i] + " distance: " + distance);
                
                verVisited[neighbors[i]] = true;
                // sth.st[vertexIndex].put(neighbors[i], distance);
                sth.st[rvIndex].put(neighbors[i], distance);
                vertexTreeStatus[neighbors[i]] = 'r';
                computeDistance(rvIndex, degKCore, neighbors[i], verVisited, distance);
            }
        }
    }

    private static List<Map.Entry<Integer,Integer>> findFurthestLongestVertices(int countRemoveCore, int[] vertex) {
        char isRootedTree = 'r', isNonRootedTree = 'n';
        int furthestValue1 = 0, furthestValue2 = 0;
        int furthestKey1 = 0, furthestKey2 = 0;
        int endPointValue1 = 0, endPointValue2 = 0;
        int endpointKey1 = 0, endpointKey2 = 0;
        
        for (int i = 0; i < countRemoveCore; ++i) {
            for (Integer key : sth.st[i].keys()) {
                if ((furthestValue1 == 0 || furthestValue1 < sth.st[i].get(key)) && (sth.st[i].get(key) != 0) && (isRootedTree == vertexTreeStatus[key])) {
                    furthestValue1 = sth.st[i].get(key);
                    furthestKey1 = key;
                }

                if ((furthestValue2 == 0 || furthestValue2 < sth.st[i].get(key)) && (sth.st[i].get(key) <= furthestValue1) && (furthestKey1 != key) && (sth.st[i].get(key) != 0) && (isRootedTree == vertexTreeStatus[key])) {
                    furthestValue2 = sth.st[i].get(key);
                    furthestKey2 = key;
                }

                if ((endPointValue2 == 0 || endPointValue2 < sth.st[i].get(key)) && (sth.st[i].get(key) != 0) && (isNonRootedTree == vertexTreeStatus[key])) {
                    endPointValue2 = sth.st[i].get(key);
                    endpointKey2 = key;

                    endpointKey1 = vertex[i];
                    endPointValue1 = 0;
                }
            }
        }

        List<Map.Entry<Integer,Integer>> result = new ArrayList<Map.Entry<Integer,Integer>>();
        result.add(new AbstractMap.SimpleEntry<Integer, Integer>(furthestKey1, furthestValue1));
        result.add(new AbstractMap.SimpleEntry<Integer, Integer>(furthestKey2, furthestValue2));
        result.add(new AbstractMap.SimpleEntry<Integer, Integer>(endpointKey1, endPointValue1));
        result.add(new AbstractMap.SimpleEntry<Integer, Integer>(endpointKey2, endPointValue2));

        return result;
    }

    private static void updateVertex(List<Map.Entry<Integer,Integer>> anchorVertices, int countRemoveCore, char typeTree) {
        int index = 0;
        outerloop:
        for (; index < countRemoveCore; ++index) {
            for (Integer key : sth.st[index].keys()) {
                if (typeTree == 'r' && key == anchorVertices.get(0).getKey())
                    break outerloop;
                if (typeTree == 'n' && key == anchorVertices.get(3).getKey())
                    break outerloop;
            }
        }

        if (index < countRemoveCore) {
            for (Integer key : sth.st[index].keys())
                degreeKCore[key] += ANCHOR_DEGREE;
            if (typeTree == 'n')
                degreeKCore[anchorVertices.get(2).getKey()] += ANCHOR_DEGREE;
        }
    }
}

