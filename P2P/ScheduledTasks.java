


import java.util.*;

public class ScheduledTasks {
	
	Timer timer1;
	Timer timer2;
	
	peerProcess pp;
	int unchokingInterval;
	int optInterval;
	
    public ScheduledTasks (peerProcess pp, int uInterval, int oInterval) {
       
    	this.pp = pp;
    	this.unchokingInterval = uInterval;
    	this.optInterval = oInterval;
    	
        timer1 = new Timer();
        timer1.scheduleAtFixedRate(new SelectPreferredNeighbors(this.pp), 0, unchokingInterval*1000);
        
        timer2 = new Timer();
        timer2.scheduleAtFixedRate(new SelectOptNeighbors(this.pp), 0, optInterval*1000);
    }

    class SelectPreferredNeighbors extends TimerTask {
        
    	peerProcess pp;
    	public SelectPreferredNeighbors (peerProcess pp) {
    		this.pp = pp;
    	}    	
    	
        public void run() {
            
        	try {
    			 
    			FileHandler f = new FileHandler();
    			int prefN=0;
    			System.out.println("Selecting neighbors");

    			if (this.pp.hasFileMap.get(this.pp.ServerPeerID)) {
    				
    				System.out.println("Peer has entire file. Start timer.");
    				//Don't send a unchoke message if the interested list is empty. Keep waiting.

    					if (this.pp.listofInterestedPeersSet.isEmpty()) {
    						System.out.println("Return timer");
    						return;
    					}

    				
    				ChokeUnchoke c = new ChokeUnchoke(0, 1);
    				


    				prefN = this.pp.prefferedNeighbors;
    				
    				
    				//Interested peers are less than the preferred neighbors	
    				System.out.println("Synchronising list of interested peers.");
    				synchronized (this.pp.listofInterestedPeersSet) {
    					if (this.pp.listofInterestedPeersSet.size() < prefN) {
    						prefN = this.pp.listofInterestedPeersSet.size();
        				}	
					}
    				
    				
    				System.out.println("Preferred neighbors updated");
    				
    				// select preferred neighbors.
    				
    				ArrayList<Integer> preferredList;
    				
    				synchronized (this.pp.preferredNeighborsSet) {
    					this.pp.preferredNeighborsSet = c.SelectPreferredNeighbors(this.pp.listofInterestedPeersSet, prefN);
    					if (this.pp.preferredNeighborsSet.isEmpty()) {
    						System.out.println("No preferred neighbors to choose ");
    					}
    					preferredList = new ArrayList<Integer>(this.pp.preferredNeighborsSet);
					}
    				
    				
    				int []peerIdArr = new int[this.pp.preferredNeighborsSet.size()];
    				
    				ChokeUnchoke cu = new ChokeUnchoke(0, 1);
    				clientConnect clientConnect ;//= new clientConnect();
    				for (int i=0; i < preferredList.size(); i++) {
    					peerIdArr[i] = preferredList.get(i);
    					System.out.println("peer ids"+peerIdArr[i]);
    					synchronized (peerProcess.clientMap) {
							
						}
						this.pp.log.changePrefNeighborLog(peerIdArr, preferredList.size());
    					clientConnect = peerProcess.clientMap.get(peerIdArr[i]);
    					 if (clientConnect != null)
    					 {
    						 cu.SendUnchokeMsg(clientConnect.out);
    						 System.out.println("Unchoking peer " + peerIdArr[i]);
    						 
    					 } else {
    						 System.out.println("No client to connect");
    					 }
    				}
    			
    				

    			}
    			
    			// Choose the neighbors who provide the server with the highest download rate
    			else {
    				System.out.println("Choosing from multiple neighbors.");
    				//Don't send a unchoke message if the interested list is empty
    				
    				synchronized (this.pp.listofInterestedPeersSet) {
    					while (this.pp.listofInterestedPeersSet.isEmpty());
					}
    				
    				boolean isEmpty = false;
    				
    				synchronized (this.pp.preferredNeighborsSet) {
    					isEmpty = this.pp.preferredNeighborsSet.isEmpty();
					}
    				
    				
    				if (!isEmpty) {
    				
    					synchronized (this.pp.preferredNeighborsSet) {
    						this.pp.preferredNeighborsSet.clear();
						}
    									
    					ChokeUnchoke cu = new ChokeUnchoke(0, 1);
    					

    					prefN = this.pp.prefferedNeighbors;
	    				
	    				synchronized (this.pp.listofInterestedPeersSet) {
	    					//Interested peers are less than the preferred neighbors. Initially.					
		    				if (this.pp.listofInterestedPeersSet.size() < prefN) {
		    					prefN = this.pp.listofInterestedPeersSet.size();
		    				}	
						}
	    				
	    				
	    				System.out.println("Preferred neighbors updated.");
	    				
	    				ArrayList<Integer> preferredList;
	    				//select preferred neighbors.
	    				synchronized (this.pp.preferredNeighborsSet) {
	    					this.pp.preferredNeighborsSet = cu.SelectPreferredNeighbors(this.pp.listofInterestedPeersSet, prefN);	
	    					preferredList = new ArrayList<Integer>(this.pp.preferredNeighborsSet);
						}
	    				
	    				int []peer_IDs = new int[this.pp.preferredNeighborsSet.size()];
	    				
	    				
	    				ChokeUnchoke cm = new ChokeUnchoke(0, 1);
						
	    				synchronized (this.pp.clientMap) {
	    					for (int i=0; i < preferredList.size(); i++) {
	    						peer_IDs[i] = preferredList.get(i);
	    						System.out.println("Transfer file to peer " + peer_IDs[i]);
	    	    				
		    					clientConnect ec = this.pp.clientMap.get(peer_IDs[i]);
		    					cm.SendUnchokeMsg(ec.out);
		    					System.out.println("Unchoking peer " + peer_IDs[i]);
		    				}
		    				this.pp.log.changePrefNeighborLog(peer_IDs, preferredList.size());	
						}
	    				
	    				ChokeUnchoke cm1 = new ChokeUnchoke(0, 0);
	    				ArrayList<Integer> chokeList = new ArrayList<Integer>();
	    				
	    				synchronized (this.pp.preferredNeighborsSet) {
	    					synchronized (this.pp.listofInterestedPeersSet) {
	
	    						clientConnect ec1;// = new clientConnect();
	    						
	    						chokeList = cu.prepareChokeList(this.pp.preferredNeighborsSet, this.pp.listofInterestedPeersSet);
	    	    				for (int i=0; i < chokeList.size(); i++) {
	    	    					if (chokeList.get(i) != this.pp.optPeerID) {
	    	    						 synchronized (this.pp.clientMap) {
	    	    							 ec1 = this.pp.clientMap.get(chokeList.get(i));
										}
	    	    						
	    	    						if (ec1 != null) {
	    	    							cm1.SendChokeMsg(ec1.out);
	    	    						}
	    	    					}
	    	    				}
	    					}
	    				}
	    				

    				}	
    			}
    		}
        	catch(Exception ex) {
    			ex.printStackTrace();
    		}
        	System.out.println("Exit.");
        }    
    }
    
    class SelectOptNeighbors extends TimerTask {
        
    	peerProcess pp;
		
    	public SelectOptNeighbors (peerProcess pp) {
    		this.pp = pp;
    	}    	
    	
        public void run(){
            
        	try {

        		System.out.println("Wait for list of interested peers");
        		//Don't send a choke message if the interested list is empty
        		synchronized (this.pp.listofInterestedPeersSet) {
        			if (this.pp.listofInterestedPeersSet.isEmpty()){
        				return;
        			}
        		}
        		
    			//If the peer has the entire file, it has to randomly select K preferred neighbors 
				ChokeUnchoke cu = new ChokeUnchoke(0, 1);
				int optPeerID=0;
				
				//select opt neighbors.
				synchronized (this.pp.listofInterestedPeersSet) {
					synchronized (this.pp.preferredNeighborsSet) {
						optPeerID = cu.SelectOptNeighbors(this.pp.listofInterestedPeersSet, this.pp.preferredNeighborsSet);
					}
				}
				
    			
				System.out.println("Selected opt unchoked neighbor "+optPeerID);
				this.pp.optPeerID = optPeerID;
			
				this.pp.log.changeOptimUnchokedNeighborLog(optPeerID);
				clientConnect cc = this.pp.clientMap.get(optPeerID);
				
				if (cc != null && optPeerID != 0) {
				cu.SendUnchokeMsg(cc.out);
				}
				
    		}
        	
    		catch(Exception ex) {
    			System.out.println(ex);
    		}
        }

    }
}
