
import java.net.*;
import MessageHandling.*;
import Logger.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.io.*;


public class MessageHandler implements Serializable {

	public int localPeerId;
	public final int CHOKE = 0;
	public final int UNCHOKE = 1;
	public final int INTERESTED = 2;
	public final int NOTINTERESTED = 3;
	public final int HAVE = 4;
	public final int BITFIELD = 5;
	public final int REQUEST = 6;
	public final int PIECE = 7;
	
	public MessageHandler(int peerId)
	{
		this.localPeerId = peerId;
	}
	
	public Object listenForMessages (InputStream is, clientConnect clientConnection) throws IOException {
		
		try {
			ObjectInputStream ois = new ObjectInputStream(is);
			Object obj = ois.readObject();
			clientConnection.nm = (NormalMessage)obj;
			return obj;
		}
		catch (ClassNotFoundException ex) {
			System.out.println(ex);
		}
		
		return null;
		
	}
	
	public void HandleMessages (int messageType, Object obj, clientConnect clientConnection, HashSet<Integer> localReceivedByteIndex) throws IOException {
		
		System.out.println("Message handler type: "+messageType);
		
		if (messageType == UNCHOKE) {

			
			RequestMessage reqMsg = new RequestMessage();
			int pieceIndex=0;
			synchronized (clientConnection.pObj.reqByteIndexSet) {
				
				pieceIndex = reqMsg.getPieceIndex(clientConnection.pObj.reqByteIndexSet);
			}
			System.out.println("Requesting for peice index " + pieceIndex);
			RequestMessage reqMsg1 = new RequestMessage(4, REQUEST, pieceIndex);
			reqMsg1.SendRequestMsg(clientConnection.out);
			
		} else if (messageType == CHOKE) {		
		
			// TODO
		} else if (messageType == INTERESTED) {		

			clientConnection.pObj.log.receivedInterestedLog(clientConnection.clientPeerID);
			
			synchronized (clientConnection.pObj.listofInterestedPeersSet) {
				System.out.println("Add peer to interested peer list: " + clientConnection.clientPeerID);
				clientConnection.pObj.listofInterestedPeersSet.add(clientConnection.clientPeerID);
			}					
		} else if (messageType == NOTINTERESTED) {
					
			
			NotInterestedMessage niMessage = (NotInterestedMessage)obj;
			clientConnection.clientPeerID = niMessage.clientPeerID;

			clientConnection.pObj.log.receivedNotInterestedLog(clientConnection.clientPeerID);
			
			if (niMessage.finished == true && clientConnection.pObj.listofInterestedPeersSet.contains(clientConnection.clientPeerID)) {
				clientConnection.pObj.listofInterestedPeersSet.remove(clientConnection.clientPeerID);
			}
			
		} else if (messageType == HAVE) {
	
				HaveMessage msg = new HaveMessage();
				//int byteIndex = rxHvMsg.ReceiveHaveMsg(clientConnection.in);
				msg = (HaveMessage)obj;
				int byteIndex = msg.msgByteIndex;
				if (byteIndex != -1) {
					//System.out.println("Received a have message from:"+clientConnection.peerID);
					clientConnection.pObj.log.receivedHaveLog(msg.peerId, byteIndex);
					//System.out.println("and byteIndex:"+byteIndex);
					clientConnection.serverPeerBitFieldMsg.UpdateBitFieldMsg(byteIndex);
					if (clientConnection.bitFields.bitFieldArr[byteIndex] == false) {
						InterestedMessage im = new InterestedMessage(0,INTERESTED, clientConnection.clientPeerID);
						im.SendInterestedMsg(clientConnection.out);
					}
					else {
						NotInterestedMessage ntm = new NotInterestedMessage(0, NOTINTERESTED, clientConnection.clientPeerID, false);
						ntm.SendNotInterestedMsg(clientConnection.out);
					}					
				}
				else {
					System.out.println("Error in receiving have msg");
				}
			
		} else if (messageType == BITFIELD) {
		
			// TODO - Check
		} else if (messageType == REQUEST) {
			
			//Get request and send piece
			RequestMessage reqMsg2 = (RequestMessage)obj;
			int pieceIndex1 = reqMsg2.msgByteIndex;
			
			System.out.println("Piece request received for:"+pieceIndex1);
			
			if (pieceIndex1 != -1) {
				//Send piece message.
				FileHandler f = new FileHandler();
				ArrayList<Integer> filePiece = f.readPiece(pieceIndex1, clientConnection.peerID, clientConnection.fileName);
				
				boolean check = false;
				
				synchronized (clientConnection.pObj.preferredNeighborsSet) {
					// Check TODO	
				}
				check = true;
				if ( check == true || (clientConnection.pObj.optPeerID == clientConnection.clientPeerID)) {
														
					
					PieceMessage pMessage = new PieceMessage(4, PIECE, pieceIndex1, filePiece, localPeerId);
					pMessage.SendPieceMsg(clientConnection.out);
					System.out.println("Sent piece index:"+pieceIndex1);
					System.out.println("End of piece msg transfer");
				}
				else {
					// Check ToDO
				}					
			}
			
		} else if (messageType == PIECE) {
			
			PieceMessage pMessage = (PieceMessage)obj;
			FileHandler f = new FileHandler();
			f.writePiece(pMessage.Filepiece, pMessage.msgByteIndex, clientConnection.clientPeerID, clientConnection.fileName);
			
			System.out.println("Received piece index:"+pMessage.msgByteIndex);
			clientConnection.pObj.receivedByteIndexSet.add(pMessage.msgByteIndex);
			
			synchronized (clientConnection.pObj.bitFields) {
				if (!clientConnection.pObj.bitFields.contains(pMessage.msgByteIndex)) {
					clientConnection.pObj.bitFields.UpdateBitFieldMsg(pMessage.msgByteIndex);
				}
			}
			
			synchronized (clientConnection.pObj.reqByteIndexSet) {
				clientConnection.pObj.reqByteIndexSet.remove(pMessage.msgByteIndex);	
			}
			
			synchronized (clientConnection.pObj.haveList) {
				clientConnection.pObj.haveList.add(pMessage.msgByteIndex);	
			}
			
			int pieceIndex2=-1;
			synchronized (clientConnection.pObj.reqByteIndexSet) {
				synchronized (clientConnection.pObj.receivedByteIndexSet) {
					if (!clientConnection.pObj.reqByteIndexSet.isEmpty()) {
						
						RequestMessage reqMsg_2 = new RequestMessage();
						synchronized (clientConnection.pObj.reqByteIndexSet) {
							pieceIndex2 = reqMsg_2.getPieceIndex(clientConnection.pObj.reqByteIndexSet);
						}
						
						
						System.out.println("Sending request for: "+pieceIndex2);
						clientConnection.pObj.log.downloadedPieceLog(pMessage.peerId, pieceIndex2, clientConnection.pObj.receivedByteIndexSet.size());
						RequestMessage reqMsg_3 = new RequestMessage(4, REQUEST, pieceIndex2);
						reqMsg_3.SendRequestMsg(clientConnection.out);
						
						System.out.println("Sent request for piece: "+pieceIndex2);
						synchronized (clientConnection.pObj.clientMap) {
							
							for (Integer key: clientConnection.pObj.clientMap.keySet()) {
								System.out.println("Sending have message to:"+key); 
								clientConnect tempClientConnection = clientConnection.pObj.clientMap.get(key);
								HaveMessage hm = new HaveMessage(4, HAVE, pMessage.msgByteIndex, localPeerId);
								hm.SendHaveMsg(tempClientConnection.out);
							}
						}
				
					}
					else {
						//Terminate once the client has received all the pieces.
						// CHECK - TODO
						clientConnection.pObj.log.completedDownloadLog();
						FileHandler f1 = new FileHandler();

						f1.JoinFile(clientConnection.fileName, clientConnection.fileSize, clientConnection.pieceSize, clientConnection.pieceCount, clientConnection.clientPeerID);
						clientConnection.pObj.listofInterestedPeersSet.remove(clientConnection.clientPeerID);

						
						clientConnection.finished = true;
						
					}
				}
				
			}
		}
		else			
		{
			System.exit(0);
		}
		
	}
}
