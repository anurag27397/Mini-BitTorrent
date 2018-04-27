

public class PeerInfo {
	public int peerId;
	public String peerAddress;
	public int peerPort;
	public boolean hasFile;
	
	public PeerInfo(int peerId, String peerAddress, int peerPort, boolean hasFile) {
		this.peerId = peerId;
		this.peerAddress = peerAddress;
		this.peerPort = peerPort;
		this.hasFile = hasFile;
	}
}
