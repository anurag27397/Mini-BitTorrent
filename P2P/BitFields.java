
import java.io.*;
import MessageHandling.*;
import java.util.*;
import java.net.*;

public class BitFields extends NormalMessage implements Serializable{

	
	public boolean []bitFieldArr;
	
	public BitFields () {
		super();
	}
	
	public BitFields(int msgLen, int msgType) {
		super(msgLen, msgType);
	}
	
	public void intializedBitFieldMsg (int myPeerId, peerProcess pp, HashMap<Integer, Boolean> hasFileMap, String fileName, int fileSize, int pieceSize) throws IOException{
		FileHandler fileHdlr = new FileHandler();
		
		double pieceCount = (double)fileSize/pieceSize;
		double checkPieceCount = Math.floor(pieceCount);
		int pc;
		if (pieceCount > checkPieceCount)
			pc = (int)pieceCount + 1;
		else
			pc = (int)pieceCount;
		this.bitFieldArr = new boolean[pc];
		
		boolean hasFile = hasFileMap.get(myPeerId);
		if (hasFile) {
			Arrays.fill(this.bitFieldArr, true);
			fileHdlr.SplitFile(fileName, fileSize, pieceSize, myPeerId);

		}
		else {
			Arrays.fill(this.bitFieldArr, false);
			
			if (pp.reqByteIndexSet.size() == 0) {
				for (int i = 0; i < pieceCount; i++) {
					pp.reqByteIndexSet.add(i);
				}
			}
		}
	}
	
	public synchronized void SendBitFieldMsg (OutputStream os) throws IOException {
		ObjectOutputStream oos;
		
		if(this.bitFieldArr !=null){
			synchronized (os) {
				oos = new ObjectOutputStream(os);  			  
				oos.writeObject(this);	
			}			
		}
	}
	
	public BitFields ReceiveBitFieldMsg (InputStream is ) throws IOException {
		
		
		try {
			
			ObjectInputStream ois = new ObjectInputStream(is);
			Object obj = ois.readObject();
			
			if (obj instanceof HaveMessage) {
				return null;
			}
			else {
			
				BitFields RespMsg = (BitFields)obj;  
				System.out.println("Received Bitfield message type:"+RespMsg.MessageType);
				if (RespMsg.bitFieldArr != null) {
					System.out.println("BitField message received");
					return RespMsg;
				}
				else {
					System.out.println("BitField message not received");
					return null;
				}
			}
		} 
		catch (ClassNotFoundException ex) {
			System.out.println(ex);
			return null;
		}
		//finally {
			//is.close();
			//ois.close();
		//}
	}
	
	public synchronized HashSet<Integer> AnalyzeReceivedBitFieldMsg (BitFields receivedBit) {
		
		boolean[] message1 = this.bitFieldArr;
		boolean[] message2 = receivedBit.bitFieldArr;		
		
		HashSet<Integer> indexList = new HashSet<Integer>();
		
		System.out.println("sizes = " + message1.length + " " + message2.length);
		for (int i = 0; i < message2.length; i++) {
			System.out.print(message2[i] + "      " + message1[i]);
		}
		
		System.out.println();
		
		for (int i=0; i < message1.length; i++) {
			if (message1[i] != message2[i] && message1[i] == false) {
				indexList.add(i);
			}
		}
		
		return indexList;
	}
	
	public synchronized void UpdateBitFieldMsg (int index) {
		
		this.bitFieldArr[index] = true;
		
	}
	
	public synchronized boolean contains (int index) {
		
		if (this.bitFieldArr[index] == true) {
			return false;
		}
		else {
			return true;
		}
	}
}
