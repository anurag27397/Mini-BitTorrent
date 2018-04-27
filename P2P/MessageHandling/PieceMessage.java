package MessageHandling;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
public class PieceMessage extends NormalMessage {

	public int msgByteIndex;
	public ArrayList<Integer> Filepiece;
	public int peerId;
	
	public PieceMessage () {
		
	}
	
	public PieceMessage (int msgLen, int msgType, int msgByteIndex, ArrayList<Integer> filePiece, int clientPeerID) {
		super (msgLen, msgType);
		this.msgByteIndex = msgByteIndex;
		this.Filepiece = filePiece;
		this.peerId  = clientPeerID;
	}
	
	public void SendPieceMsg (OutputStream os) throws IOException {
		
		System.out.println("In send piece ");
		ObjectOutputStream oos;
		synchronized (os) {
			oos = new ObjectOutputStream(os);  			  
			oos.writeObject(this);
		}
		System.out.println("exiting send piece ");
		
	}
	
	public int ReceivePieceMsg (InputStream is) throws IOException {
		
		try {
			ObjectInputStream ois = new ObjectInputStream(is);  
			PieceMessage pm = (PieceMessage)ois.readObject(); 
			
			if (pm != null) {
				return pm.msgByteIndex;
			}
			else {
				return -1;
			}
		} 
		catch (ClassNotFoundException ex) {
			System.out.println(ex);
			return -1;
		}
		finally {
			//is.close();
			//ois.close();
		}
	}
}
