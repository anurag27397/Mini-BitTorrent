

import java.io.IOException;
import MessageHandling.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

public class clientConnect implements Runnable{
	

	public int peerID;
	public int clientPeerID;
	public int portNumber;
	public Socket clientSocket;	
	public BitFields serverPeerBitFieldMsg;
	public peerProcess pObj;
	public Boolean finished = false;
	public NormalMessage nm = new NormalMessage();	
	public OutputStream out;
	public InputStream in;
	public String fileName;
	public long fileSize;
	public int pieceSize;
	public int pieceCount;
	public BitFields bitFields;
    
    public clientConnect (Socket socket, int peerId, int clientPeerId, peerProcess pp) throws IOException {
		this.peerID = peerId; 
		this.clientPeerID = clientPeerId; 
		this.bitFields = pp.bitFields;		
		this.pObj = pp;
		this.clientSocket = socket;
		
		this.out = socket.getOutputStream();
		this.in = socket.getInputStream();
		this.fileName = pp.fileNameToBeShared;
		this.fileSize = pp.fileSize;
		this.pieceSize = pp.pieceSize;
		
		double pCount = (double)(this.fileSize)/(this.pieceSize);
		double checkPieceCount = Math.floor(pCount);
		if (pCount > checkPieceCount)
			this.pieceCount = (int)pCount + 1;
		else
			this.pieceCount = (int)pCount;
	}
	
	
	public void run() {
		
		try {
			this.finished = false;
			
			
			// Send handshake message			
			HandShake handShakeMsg = new HandShake("P2PFILESHARINGPROJ0000000000", peerID, clientPeerID);
			handShakeMsg.sendHandShake (this.out);			
						
			if ((this.clientPeerID = handShakeMsg.receiveHandShake(this.in))!= -1)
			{
				System.out.println("Handshake successful ");
				
				if (!peerProcess.clientMap.contains(this.clientPeerID)) {
					System.out.println("Server side: Mapping client " + clientPeerID);
					peerProcess.clientMap.put(this.clientPeerID, this);
				}
				
			}
			else 
			{
				System.out.println("Handshake failure");
			}
			
			
			// Send bitfield message.
			
			bitFields.SendBitFieldMsg(this.out);	
			
			// Get bitfield message from server
			BitFields receiveMsg = new BitFields();
			BitFields returnMsg;
			
			returnMsg = receiveMsg.ReceiveBitFieldMsg(this.in); 
			
			this.serverPeerBitFieldMsg = returnMsg;
			
					
			HashSet<Integer> res = bitFields.AnalyzeReceivedBitFieldMsg(returnMsg);
			
			if (res.size() != 0) {
				//send interested msg.
				InterestedMessage nIMsg = new InterestedMessage(0,2, clientPeerID);
				nIMsg.SendInterestedMsg(this.out);
		
			}
			else {
				
				//send not interested msg.
				NotInterestedMessage nIMsg = new NotInterestedMessage(0,3, clientPeerID, false);
				nIMsg.SendNotInterestedMsg(this.out);
				
			}
		
			//this.initialStage = true;
			
			MessageHandler messageHandler = new MessageHandler(peerID);
			Object readObj = null;
			HashSet<Integer> localReceivedByteIndex = new HashSet<Integer>();
			
			while (true) {
				System.out.println("Client: Waiting for messages");
				readObj = messageHandler.listenForMessages(this.in, this);
				System.out.println("Message Received");
				int msgType = this.nm.MessageType;

				messageHandler.HandleMessages(msgType, readObj, this, localReceivedByteIndex);
				readObj = null;
				
				if (this.finished == true) {
					NotInterestedMessage ntIm = new NotInterestedMessage(0, 3, clientPeerID, true);
					ntIm.SendNotInterestedMsg(this.out);
					break;
				}
			}
			
			System.out.println("Client: Closing socket");
			clientSocket.close();
			
			return;
		}
		catch (IOException ex) {
			System.out.println("Error: IOException: "+ex);
		}
	}
}
