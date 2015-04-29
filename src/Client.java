import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import lenz.htw.kimpl.Move;
import lenz.htw.kimpl.net.NetworkClient;


public class Client {

	public static void main(String[] args) {
		String serverIP = args[0];
		try {
			NetworkClient networkClient = new NetworkClient(serverIP, "meinTeamName0", ImageIO.read(new File("logo/hellokitty256.png")));
			
			int playerNumber = networkClient.getMyPlayerNumber();
			networkClient.getTimeLimitInSeconds();
			int i = 0;
			while(true){
				Move move = networkClient.receiveMove();
				
				if(move == null){
					// ich bin dran
					// zug berechnen
					// Zeit beachten
					//new Timer().
					
					Move myNextMove = null;
					if(playerNumber == 0)
						myNextMove = new Move(4, i, 4, i+1);
					else if(playerNumber == 1)
						myNextMove = new Move(7-i, 4, 7-i-1, 4);
					else if(playerNumber == 2)
						myNextMove = new Move(4, 7-i, 4, 7-i-1);
					else if(playerNumber == 3)
						myNextMove = new Move(i, 4, i+1, 4);
					
					networkClient.sendMove(myNextMove);
					++i;
				}else{
					// jemand anders is dran
				}
			}
		} catch (IOException e){
			throw new RuntimeException("", e);
			
		}
	}

}
