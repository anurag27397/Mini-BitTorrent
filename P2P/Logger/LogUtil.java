package Logger;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogUtil {
	Logger logger;
	int peerId;
	LogManager lm = LogManager.getLogManager();
	String DirectoryPath = System.getProperty("user.dir") + File.separator;
	FileHandler fh;
	public LogUtil(int peer_ID){
		try {
			this.peerId = peer_ID;
			// log file format - ~/project/log_peer_1001.log
			fh = new FileHandler(DirectoryPath + "log_peer_"+peer_ID+".log");
			logger = Logger.getLogger("Log for peer" + peer_ID);
		    lm.addLogger(logger);
		    logger.setLevel(Level.INFO);
		    fh.setFormatter(new SimpleFormatter());
		    logger.addHandler(fh);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	// Logging for TCP related
	
	public void tcpSendLog(int peer2)
	{
		// [Time]: Peer [peer_ID 1] makes a connection to Peer [peer_ID 2]
		logger.log(Level.INFO, "Peer " + peerId + " makes a connection to Peer "+peer2+"\n");
	}
	
	public void tcpRecvLog(int peer2)
	{
		// [Time]: Peer [peer_ID 1] is connected from Peer [peer_ID 2] 
		logger.log(Level.INFO, "Peer " + peerId + " is connected from Peer "+peer2+"\n");
	}
	
	public void changePrefNeighborLog(int []peerIDs, int numberOfNeighbors)
	{
		//[Time]: Peer [peer_ID] has the preferred neighbors [preferred neighbor ID list]
		logger.log(Level.INFO, "Peer " + peerId + " has the preferred neighbors ");
		for(int i = 0; i<numberOfNeighbors; i++){
			if(i!=0){
				logger.log(Level.INFO, ",");
			}
			logger.log(Level.INFO, peerIDs[i]+"");
		}
		logger.log(Level.INFO, "\n");
	}
	
	
	public void changeOptimUnchokedNeighborLog(int neighborPeerId)
	{
		// [Time]: Peer [peer_ID] has the optimistically unchoked neighbor [optimistically unchoked neighbor ID]
		logger.log(Level.INFO, "Peer " + peerId + " has the optimistically unchoked neighbor " + neighborPeerId + "\n");
	}
	
	public void UnchokedLog(int unchokingPeerId)
	{
		// [Time]: Peer [peer_ID 1] is unchoked by [peer_ID 2] 
		logger.log(Level.INFO, "Peer " + peerId + " is unchoked by  " + unchokingPeerId + "\n");
	}
	
	
	public void ChokedLog(int chokingPeerId)
	{
		// [Time]: Peer [peer_ID 1] is choked by [peer_ID 2]
		logger.log(Level.INFO, "Peer " + peerId + " is choked by  " + chokingPeerId + "\n");
	}
	
	
	public void receivedHaveLog(int neighborPeerId, int pieceIndex)
	{
		// [Time]: Peer [peer_ID 1] received the have message from [peer_ID 2] for the piece [piece index]
		logger.log(Level.INFO, "Peer " + peerId + " received the have message from  " + neighborPeerId + " for the piece " + pieceIndex + "\n");
	}
	
	public void receivedInterestedLog(int neighborPeerId)
	{
		// [Time]: Peer [peer_ID 1] received the interested message from [peer_ID 2]
		logger.log(Level.INFO, "Peer " + peerId + " received the interested message from  " + neighborPeerId + "\n");
	}
	
	public void receivedNotInterestedLog(int neighborPeerId)
	{
		// [Time]: Peer [peer_ID 1] received the not interested message from [peer_ID 2]
		logger.log(Level.INFO, "Peer " + peerId + " received the not interested message from  "+neighborPeerId +"\n");
	}
	
	public void downloadedPieceLog(int neighborPeerId, int pieceIndex, int numberOfPieces)
	{
		// [Time]: Peer [peer_ID 1] has downloaded the piece [piece index] from [peer_ID 2]
		logger.log(Level.INFO, "Peer " + peerId + " has downloaded the piece " + pieceIndex + " from " + neighborPeerId + ". Now the number of pieces it has is " + numberOfPieces + "\n");
	}
	public void completedDownloadLog()
	{
		// [Time]: Peer [peer_ID] has downloaded the complete file
		logger.log(Level.INFO, "Peer " + peerId + " has downloaded the complete file \n");
	}
}
