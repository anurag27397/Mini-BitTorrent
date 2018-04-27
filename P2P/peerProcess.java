
import java.io.*;
import Logger.*;
import java.net.*;
import java.util.*;
public class peerProcess extends Thread implements Serializable {

	public ServerSocket serverSock;
	public int ServerPeerID;
	public int iPeerID;
	public int optPeerID;
	public BitFields bitFields;	
	public static int prefferedNeighbors;
	public static int unchokingInterval;
	public static int optimistacallyChosenInterval;
	public static String fileNameToBeShared;
	public static int fileSize;
	public static int pieceSize;
	public static Hashtable<Integer, clientConnect> clientMap = new Hashtable<Integer, clientConnect>();
	public HashSet<Integer> interestedPeersHashSet = new HashSet<Integer>();
	public  Set<Integer> listofInterestedPeersSet = Collections.synchronizedSet(interestedPeersHashSet);
	public HashSet<Integer> preferredNeighborsSet = new HashSet<Integer>();
	public HashSet<Integer> reqByteIndexSet = new HashSet<Integer>();	
	public ArrayList<Integer> haveList = new ArrayList<Integer>();
	public LogUtil log;
	ArrayList<Thread> startedThreadList= new ArrayList<Thread>();	
	public static Vector<PeerInfo> peerInfoV;
	public static HashMap<Integer, Boolean> hasFileMap;
	public HashSet<Integer> receivedByteIndexSet = new HashSet<Integer>();
	
	    
	public peerProcess (int peerID) throws UnknownHostException, IOException {
		this.ServerPeerID = peerID;
		this.iPeerID = peerID;
		
		// Read common config file and obtain corresponding parameters
		readCommonConfigFile();
				
				
		// Read peer info confid file and obtain corresponding parameters
		readPeerInfoConfigFile(peerID);
		
		connectionSetup();
	}
	

	// Method for reading common config file
		public static void readCommonConfigFile() 
		{
			try 
			{
				// Read from Common.cfg file
				String configFileName = System.getProperty("user.dir")+"/Common.cfg";
				BufferedReader bufReader = new BufferedReader(new FileReader(configFileName));
	            String lineInput = bufReader.readLine();
	            
	            
         

         
	            while (lineInput != null)
	            {
	            	String[] token = lineInput.split(" ");
	            	// Get the tokens by splitting the string for whitespace
	            	// Format of
	            	/*
	            	    NumberOfPreferredNeighbors 2
						UnchokingInterval 5
						OptimisticUnchokingInterval 15
						FileName TheFile.dat
						FileSize 10000232
						PieceSize 32768
	            	 */
	            	if (token[0].equals("NumberOfPreferredNeighbors"))
	            		prefferedNeighbors =  Integer.parseInt(token[1]);
	            	else if (token[0].equals("UnchokingInterval"))
	            		unchokingInterval = Integer.parseInt(token[1]);
	            	else if (token[0].equals("OptimisticUnchokingInterval"))
	            		optimistacallyChosenInterval = Integer.parseInt(token[1]);
	            	else if (token[0].equals("FileName"))
	            		fileNameToBeShared = token[1];
	            	else if (token[0].equals("FileSize"))
	            		fileSize = Integer.parseInt(token[1]);
	            	else if (token[0].equals("PieceSize"))
	            		pieceSize = Integer.parseInt(token[1]);
	            	
	            	lineInput = bufReader.readLine();
	            }
	            bufReader.close();
	            
	            
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		
		// Method for reading PeerInfo.cfg file
		public static void readPeerInfoConfigFile(int curpId) 
		{
			//System.out.println("Called peerinfo read function 1");
			peerInfoV = new Vector<PeerInfo>(); 
			hasFileMap = new HashMap<Integer, Boolean>();
			// Format
			// [peer ID] [host name] [listening port] [has file or not]
			try
			{
				String peerInfoFileName = System.getProperty("user.dir")+"/PeerInfo.cfg";
				BufferedReader bufReader = new BufferedReader(new FileReader(peerInfoFileName));
				String lineInput = bufReader.readLine();
				int peerId;
				String hostName;
				int listeningPort;
				boolean hasFile;
				
				String[] token;
				//System.out.println("Called peerinfo read function 2");
				while (lineInput != null)
				{
					//System.out.println("Called peerinfo read function 3");
					token = lineInput.split(" ");
					peerId = Integer.parseInt(token[0]);
					hostName = token[1];
					listeningPort = Integer.parseInt(token[2]);
					if(Integer.parseInt(token[3]) == 1) {
						hasFile = true;
						hasFileMap.put(peerId, true);
					}
					else {
						hasFile = false;
						hasFileMap.put(peerId, false);
					}
					peerInfoV.addElement(new PeerInfo(peerId, hostName, listeningPort, hasFile));
					System.out.println("Added peerinfo for  "+peerId);
					
					
					lineInput = bufReader.readLine();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.exit(-1);
			}
		}
	
	public static void main (String []args) throws IOException {
		
		int myPeerId = Integer.parseInt(args[0]);
		
		peerProcess pTemp = new peerProcess(myPeerId);
		
		try {
		    Thread.currentThread().sleep (1000*10);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		
		
		
		//FileHandler f = new FileHandler();
		//int []time = f.GetIntervalTimes();
		
		
		//Start the scheduled tasks.
		ScheduledTasks s = new ScheduledTasks(pTemp, unchokingInterval, optimistacallyChosenInterval);
		
		for (int i = 0; i < pTemp.startedThreadList.size(); i++) {
		    try {
		    	pTemp.startedThreadList.get(i).join();
		    } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
		
		System.out.println("Exiting");
		
		while (args != null);
		
		//serverSock.close();
	}
	
	void connectionSetup () throws UnknownHostException, IOException {
		startedThreadList = new ArrayList<Thread>();
		
		
	
		PeerInfo peerInfo;
		
		this.log = new LogUtil(this.iPeerID);
		
		if (peerInfoV != null)
		{
			for (int i=0; i< peerInfoV.size(); i++) 
			{
				// Get peer info mentioned in peer config file
				peerInfo = peerInfoV.elementAt(i);			
				
				if (peerInfo.peerId == this.iPeerID) {
					
					// Creating a server socket for the host in which this peer id is running
					this.serverSock = new ServerSocket(peerInfo.peerPort, 0, InetAddress.getLocalHost());
									
				    Thread ServerThread = new Thread(new serverConnect(this.iPeerID, this));
				    ServerThread.start();				
				}
				else 
				{
					Socket clientSock = null;
					
					try {
						if (clientMap.containsKey(peerInfo.peerId)) {
							return;
						}
						BitFields bF = new BitFields(4, 5);
						bF.intializedBitFieldMsg(this.iPeerID, this, hasFileMap, fileNameToBeShared, fileSize, pieceSize);
						this.bitFields = bF;
											
						System.out.println("Connect to " + peerInfo.peerId + " from " + peerInfo.peerAddress + " : " + peerInfo.peerPort + " , " + iPeerID);
						clientSock = new Socket(peerInfo.peerAddress, peerInfo.peerPort);
						clientConnect ec = new clientConnect(clientSock, this.iPeerID, peerInfo.peerId, this);
						Thread ClientThreads = new Thread(ec);
						
						ClientThreads.start();
						clientMap.put(peerInfo.peerId, ec);
						System.out.println("Put in map " + peerInfo.peerId);
						startedThreadList.add(ClientThreads);					
					}
					catch (IOException e) {
						//e.printStackTrace();
						System.out.println("connection failed: " + iPeerID);
					}
				
				}
			}
		}
		else
		{
			System.out.println("Error: Empty PeerInfo vector");
		}
		
		
	}
}
