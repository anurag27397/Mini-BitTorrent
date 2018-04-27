

import java.io.IOException;
import MessageHandling.*;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;


class serverConnect implements Runnable, Serializable {

	int PeerID;
	peerProcess pObj;

	
	public serverConnect (int peer_id, peerProcess pp) {
		this.PeerID = peer_id;
		this.pObj = pp;
	}
	
	public void run () {
			
			//Server simply listen to clients.
			boolean active = true;
		
			try {
			
			
				while (active) {
					System.out.println("Server Socket opened: " + pObj.serverSock);
					System.out.println("Address: Local host: " + InetAddress.getLocalHost().getHostName());
					Socket sock = this.pObj.serverSock.accept();
					clientConnect cc = new clientConnect(sock, this.PeerID, this.PeerID, this.pObj);
					Thread listenerThread = new Thread(cc);
					
					listenerThread.start();
					pObj.startedThreadList.add(listenerThread);
				}
			
			}
			catch (SocketException ex) {
				ex.printStackTrace();
				active = false;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
	}


}
