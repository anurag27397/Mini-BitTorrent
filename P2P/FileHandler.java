

import java.io.*;
import java.util.ArrayList;

public class FileHandler {

	public String inputFileName;
	public long fileSize;
	public int pieceSize;
	public int pieceCount;
	
			
	public void SplitFile (String FileName, long FileSize, int PieceSize, int PeerID) throws IOException {
		
		FileName = System.getProperty("user.dir")+"/peer_"+PeerID+"/"+FileName;
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(FileName));
		int bytesRead=0;
		int bytesReadCount=0;
		int count=0;
		
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(FileName+"."+count));
		
		while ((bytesRead = bis.read()) != -1) {
			
			if (bytesReadCount < PieceSize) {
				bos.write(bytesRead);
				bytesReadCount++;
			}
			else {
				count++;
				bos.close();
				bos = new BufferedOutputStream(new FileOutputStream(FileName+"."+count));
				bos.write(bytesRead);
				bytesReadCount=1;				
			}
		}
		
		bos.close();
		bis.close();
	}
	
	public void JoinFile (String FileName, long FileSize, int PieceSize, int pieceCount, int PeerID) throws IOException {
		
		int bytesRead = 0;
		BufferedInputStream bis = null;
		
		FileName = System.getProperty("user.dir")+"/peer_"+PeerID+"/"+FileName;
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(FileName+".new"));
		
		for (int i=0; i<pieceCount; i++) {
			bis = new BufferedInputStream(new FileInputStream(FileName+"."+i));
			while ((bytesRead = bis.read()) != -1) {
				bos.write(bytesRead);
			}
			bis.close();
		}
		bos.close();
	}
	
	public void WriteToFile (String FileName, String data) throws IOException{

		File file = new File(FileName);
		
		// check if required to create a new
		if (!file.exists()) {
			file.createNewFile();
		}
		
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(data);
		bw.close();
	}
	
	public ArrayList<Integer> readPiece (int pieceIndex, int PeerID, String fileName) throws IOException {
		
		int bytesRead=0;
		String FileName = System.getProperty("user.dir")+"/peer_"+PeerID+"/"+fileName;
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(FileName+"."+pieceIndex));
		ArrayList<Integer> fileRead = new ArrayList<Integer> ();
		
		
		try {
		
		while ((bytesRead = bis.read()) != -1) {
			fileRead.add(bytesRead);
		}
			
		bis.close();
		}
		catch (EOFException ex) {
			//Do nothing
		}
		
		return fileRead;
		
	}
	
	public void writePiece (ArrayList<Integer> FilePiece, int pieceIndex, int PeerID, String fileName) throws IOException {
		
		String FileName = System.getProperty("user.dir")+"/peer_"+PeerID+"/"+fileName;
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(FileName+"."+pieceIndex));
		for (int i = 0; i < FilePiece.size(); i++) {
			bos.write(FilePiece.get(i));		
		}
		bos.close();
	}
}
