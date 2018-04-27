


import java.io.IOException;
import MessageHandling.*;
import java.util.*;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ChokeUnchoke extends NormalMessage{
	
	public ChokeUnchoke () {
		
	}
	
	public ChokeUnchoke (int messageLength, int type) {
		super(messageLength, type);
	}
	
	public void SendChokeMsg (OutputStream os) throws IOException {
		
		synchronized (os) {
			ObjectOutputStream oos = new ObjectOutputStream(os);  			  
			oos.writeObject(this);
		} 		
	}
	
	public void SendUnchokeMsg (OutputStream os) throws IOException {
		 
		synchronized (os) {
			ObjectOutputStream oos = new ObjectOutputStream(os);  			  
			oos.writeObject(this);	
		}
		
	}
	
	public boolean ReceiveChokeUnchokeMsg (InputStream is) {
		
		// TODO
		return false;
		
	}

	public synchronized HashSet<Integer> SelectPreferredNeighbors (Set<Integer> ListOfInterestedPeers, int nPrefNeighbors) {
	
		HashSet<Integer> preferredNeighborsSet = new HashSet<Integer>();
		int totalSize = ListOfInterestedPeers.size();
		Random r = new Random();

		for (int i = 0; i < nPrefNeighbors; i++) {
			
			List<Integer> interestedPeersList = new ArrayList<Integer>(ListOfInterestedPeers);
			System.out.println("Totsize" + totalSize);
			int peerid;
			if(totalSize>1){
			peerid = interestedPeersList.get(r.nextInt(totalSize-1));
			preferredNeighborsSet.add(peerid);
			}
			if(totalSize==1)
				preferredNeighborsSet.add(interestedPeersList.get(i));
		}
		
		return preferredNeighborsSet;
	}
	
	public synchronized int SelectOptNeighbors(Set<Integer> ListOfInterestedPeers, Set<Integer> PreferredNeighbors) {
		
		int i=0;
		int j=0;
		int optPeerId = 0;
		
		ArrayList<Integer> ipL = new ArrayList<Integer>(ListOfInterestedPeers);
		ArrayList<Integer> pnL = new ArrayList<Integer>(PreferredNeighbors);
		ArrayList<Integer> listUtil = new ArrayList<Integer>();
		
		Random r = new Random();
		
		for (i = 0; i < ipL.size(); i++) {
			for (j = 0; j < pnL.size(); j++) {
				if ((ipL.get(i) != pnL.get(j)) && j == (pnL.size() - 1) ){
					listUtil.add(ipL.get(i));
				}
			}
		}
		if(listUtil.size()>1)
		optPeerId = ipL.get(r.nextInt(listUtil.size()));
		if(listUtil.size()==1)
			optPeerId = ipL.get(0);
				
		return optPeerId;	
	}
	
	public synchronized ArrayList<Integer> prepareChokeList (Set<Integer> PreferredNeighbors, Set<Integer> listofInterestedPeers) {
		
		ArrayList<Integer> ipL = new ArrayList<Integer>(listofInterestedPeers);
		ArrayList<Integer> pnL = new ArrayList<Integer>(PreferredNeighbors);
		ArrayList<Integer> listUtil = new ArrayList<Integer>();
		int i=0, j=0;
		
		for (i = 0; i < ipL.size(); i++) {
			for (j = 0; j < pnL.size(); j++) {
				if ( (ipL.get(i) != pnL.get(j)) && !(listUtil.contains(ipL.get(i)))) {
					listUtil.add(ipL.get(i));
				}
			}
		}
		
		return listUtil;		
	}
}
