

import java.io.*;
import Logger.LogUtil;
import java.net.*;

public class HandShake implements Serializable {
	
	String Header;
	int peerId;
	int peerId2;
	
	public HandShake (String header, int peerID, int peerID2) {
		Header = header;
		peerId = peerID;
		peerId2 = peerID2;
	}	
	
	public void sendHandShake (OutputStream out) throws IOException {
		
		ObjectOutputStream oos = new ObjectOutputStream(out);  			  
		oos.writeObject(this);
		System.out.println("Initiating handshake with peer" + this.peerId2);
		LogUtil log = new LogUtil(this.peerId);
		log.tcpSendLog(this.peerId2);
	}
	
	public int receiveHandShake (InputStream in) throws IOException {
		try {
			LogUtil log = new LogUtil(this.peerId);
			
			ObjectInputStream ois = new ObjectInputStream(in);  
			HandShake RespMsg = (HandShake)ois.readObject();  
			if (RespMsg != null) {			
				log.tcpRecvLog(RespMsg.peerId);
				return RespMsg.peerId;
				
			}
			else {
				return -1;
			}
			
		} 
		catch (ClassNotFoundException ex) {
			System.out.println(ex);
		}
		finally {

		}
		return -1;
	}
}