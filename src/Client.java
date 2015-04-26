import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import lenz.htw.kimpl.Move;
import lenz.htw.kimpl.net.NetworkClient;


public class Client {

	public static void main(String[] args) {
		String serverIP = args[0];
		try {
			NetworkClient networkClient = new NetworkClient(serverIP, "meinTeamName4", ImageIO.read(new File("logo/hellokitty256.png")));
			
			networkClient.getMyPlayerNumber();
			networkClient.getTimeLimitInSeconds();
			
			while(true){
				Move move = networkClient.receiveMove();
				if(move == null){
					// ich bin dran
					// zug berechnen
					// Zeit beachten
					//new Timer().
					Move myNextMove = new Move(4, 0, 4, 1);
					networkClient.sendMove(myNextMove);
					
				}else{
					// jemand anders is dran
				}
			}
		} catch (IOException e){
			throw new RuntimeException("", e);
			
		}
	}

}
