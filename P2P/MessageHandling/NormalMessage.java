package MessageHandling;

import java.io.Serializable;

public class NormalMessage implements Serializable {

	public int MessageLength;
	public int MessageType;
	
	public NormalMessage () {
		
	}
	
	public NormalMessage (int MsgLen,int MsgType) {
		this.MessageLength = MsgLen;
		this.MessageType = MsgType;
	}	
}
