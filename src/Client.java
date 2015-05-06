import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import lenz.htw.kimpl.Move;
import lenz.htw.kimpl.net.NetworkClient;


public class Client {

	static byte[][] board = new byte[8][8];
	static NetworkClient networkClient;
	static int playerNumber;
	static int timeLimit;
	
	public static void main(String[] args) {
		String serverIP = args[0];
	
		try {
			networkClient = new NetworkClient(serverIP, "meinTeamName0", ImageIO.read(new File("logo/hellokitty256.png")));
			
			while(true){
				Move move = networkClient.receiveMove();
				
				if(move == null){
					calculateMove();
				}else{
					saveMove(move);
				}
				printBoard();
			}
		} catch (IOException e){
			throw new RuntimeException("", e);
			
		}
	}
	
	public static void init(){
		playerNumber = networkClient.getMyPlayerNumber();
		timeLimit = networkClient.getTimeLimitInSeconds();
		createBoard();
		printBoard();
	}
	
	public static void createBoard(){
		for(int i = 1; i < 7; ++i){
			board[i][0] = 1;
			board[7][i] = 2;
			board[i][7] = 3;
			board[0][i] = 4;
		}
	}
	
	public static void printBoard(){
		for(int i = 7; i >= 0; i--){
			for(int j = 0; j < 8; j++){
				System.out.print(board[j][i] + " ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public static void calculateMove(){
		// ich bin dran
		// zug berechnen
		// Zeit beachten
		// new Timer().
		
		Move myNextMove = null;
		if(playerNumber == 0)
			myNextMove = new Move(4, 0, 4, 1);
		else if(playerNumber == 1)
			myNextMove = new Move(7, 4, 7-1, 4);
		else if(playerNumber == 2)
			myNextMove = new Move(4, 7, 4, 7-1);
		else if(playerNumber == 3)
			myNextMove = new Move(0, 4, 1, 4);
		
		networkClient.sendMove(myNextMove);

	}
	
	public static void saveMove(Move move){
		byte movingPlayer = board[move.fromX][move.fromY];
		board[move.toX][move.toY] = movingPlayer;				
		board[move.fromX][move.fromY] = 0;
	}
	
	public byte rate(byte[][] newBoard, byte movingPlayer){
		byte cnt = 0;
		for(byte i = 0; i < 8; ++i){
			for(byte j = 0; j < 8; ++j){
				if(newBoard[i][j] == movingPlayer)
					++cnt;
			}
		}
		return cnt;
	}

}
