package gframe;

import java.util.*;
import graph.*;

public class WorldGraph extends Graph{

  public WorldGraph(){
  	super();	
  }
  
  public WorldGraph(int nodes){
  	super(nodes);	
  }
    
  public void markVisible(Integer nodeid, boolean vis){
    markVisible(nodeid.intValue(), vis);
  }

  public boolean isVisible(Integer nodeid){
  	return isVisible(nodeid.intValue());
  }
  
  public long distance(Integer node1id, Integer node2id){
  	return distance( node1id.intValue(), node2id.intValue() );
  }
  
  public Integer getVisibleRandomNodeId(){
	List visnodes = getVisibleNodes();	  	    
	int r = (int)( Math.random() * visnodes.size() );	
	return new Integer( ((Node)visnodes.get(r)).getId() );		
  }              
    
  public List getPathOverAll(Integer startnodeid, Integer destnodeid){
  	return getPath(startnodeid.intValue(), destnodeid.intValue());
  }
  
  public List getPathOverVisibles(Integer startnodeid, Integer destnodeid){
	if (!isVisible(destnodeid.intValue())){
	  return new LinkedList();	// return empty path
	}
	
	markVisible( startnodeid.intValue(), true );
	
  	Graph vgraph = getVisibleSubgraph();			
	
	int[] g_vg_map = new int[ countNodes() ];
	int[] vg_g_map = new int[ vgraph.countNodes() ];
		
	int vnodes = 0;	
	for(Iterator it=getNodes().iterator(); it.hasNext(); ){		
	  Node n = (Node) it.next();
	  if (n.isVisible()) {
	  	vnodes++;
	    g_vg_map[n.getId()-1] = vnodes;
		vg_g_map[vnodes-1]    = n.getId();
	  }
	}
		
	int newstartid = g_vg_map[startnodeid.intValue()-1];
	int newdestid  = g_vg_map[destnodeid.intValue() -1];
	
	List path = new LinkedList();
	Iterator it = vgraph.getPath(newstartid,newdestid).iterator();
	while(it.hasNext()){
	  Integer id = (Integer) it.next();
	  path.add( new Integer(vg_g_map[id.intValue()-1]) );
	}	
	return path;  
  }      
  
}