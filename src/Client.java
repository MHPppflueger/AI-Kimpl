import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import lenz.htw.kimpl.Move;
import lenz.htw.kimpl.net.NetworkClient;

public class Client {	

	static byte[][] board = new byte[8][8];
	static NetworkClient networkClient;
	static int playerNumber;
	static int timeLimit;
	static Vec2[] posP1 = new Vec2[6];
	static Vec2[] posP2 = new Vec2[6];
	static Vec2[] posP3 = new Vec2[6];
	static Vec2[] posP4 = new Vec2[6];
	static final byte EMPTY_FIELD = 0;
	
	public static void main(String[] args) {
		String serverIP = args[0];
	
		try {
			networkClient = new NetworkClient(serverIP, "meinTeamName0", ImageIO.read(new File("logo/hellokitty256.png")));
			int roundCnt = 0;
			init();
						
			while(true){
				Move move = networkClient.receiveMove();
				roundCnt++;
				System.out.println("Started round " + roundCnt);
				if(move == null){
					move = calculateMove();
					networkClient.sendMove(move);
					System.out.println("\n moving from " + move.fromX + "," + move.fromY + " to " + move.toX + "," + move.toY);
					//saveMove(move);
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
		playerNumber = networkClient.getMyPlayerNumber() + 1;
		System.out.println("Player" + playerNumber);
		timeLimit = networkClient.getTimeLimitInSeconds();
		createBoard();
		printBoard();
		createPlayerPositions();
	}
	
	public static void createBoard(){
		for(int i = 1; i < 7; ++i){
			board[i][0] = 1;
			board[7][i] = 2;
			board[i][7] = 3;
			board[0][i] = 4;
		}
	}
	
	public static void createPlayerPositions(){
		for(byte i = 0; i < 6; ++i){
			posP1[i] = new Vec2((byte)(1+i), (byte)0); 
			posP2[i] = new Vec2((byte)(7), (byte)(1+i));
			posP3[i] = new Vec2((byte)(1+i), (byte)(7)); 
			posP4[i] = new Vec2((byte)(0), (byte)(1+i)); 
		}
		int i = 0;
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
	
	public static Move calculateMove(){
		// ich bin dran
		// zug berechnen
		// Zeit beachten
		// new Timer().
		Vec2[][] possibleMoves = generatePossibleMoves((byte) playerNumber);
		
		
		
		Move myNextMove = new Move(possibleMoves[0][0].x, possibleMoves[0][0].y, 
				possibleMoves[0][1].x, possibleMoves[0][1].y);
		
		return myNextMove;

	}
	
	public static void saveMove(Move move){
		byte movingPlayer = board[move.fromX][move.fromY];
		byte killedPlayer = board[move.toX][move.toY];
		board[move.toX][move.toY] = movingPlayer;				
		board[move.fromX][move.fromY] = 0;
		
		
		switch (killedPlayer){
		case 1:
			for(Vec2 stone : posP1){
				if(stone.x == move.toX && stone.y == move.toY){	// this stone was killed
					stone.x = 8;
					stone.y = 8;
				}
			}
		case 2:
			for(Vec2 stone : posP2){
				if(stone.x == move.toX && stone.y == move.toY){	
					stone.x = -1;
					stone.y = -1;
				}
			}
		case 3:
			for(Vec2 stone : posP3){
				if(stone.x == move.toX && stone.y == move.toY){	
					stone.x = -1;
					stone.y = -1;
				}
			}
		case 4:
			for(Vec2 stone : posP4){
				if(stone.x == move.toX && stone.y == move.toY){	
					stone.x = 8;
					stone.y = 8;
				}
			}
	}
		switch (movingPlayer){
			case 1:
				for(Vec2 stone : posP1){
					if(stone.x == move.fromX && stone.y == move.fromY){		// this stone was moved
						stone.x = (byte) move.toX;
						stone.y = (byte) move.toY;
					}
				}
			case 2:
				for(Vec2 stone : posP2){
					if(stone.x == move.fromX && stone.y == move.fromY){
						stone.x = (byte) move.toX;
						stone.y = (byte) move.toY;
					}
				}
			case 3:
				for(Vec2 stone : posP3){
					if(stone.x == move.fromX && stone.y == move.fromY){
						stone.x = (byte) move.toX;
						stone.y = (byte) move.toY;
					}
				}
			case 4:
				for(Vec2 stone : posP4){
					if(stone.x == move.fromX && stone.y == move.fromY){
						stone.x = (byte) move.toX;
						stone.y = (byte) move.toY;
					}
				}
		}
	}
	
	public byte rate(byte[][] newBoard, byte movingPlayer){
		byte cnt = 0;
		for(byte i = 0; i < 8; ++i){
			for(byte j = 0; j < 8; ++j){
				if(newBoard[i][j] == movingPlayer)					// +1 for own stone
					++cnt;
				if(newBoard[i][j] != movingPlayer 					// -1 for enemy stone
						&& newBoard[i][j] != EMPTY_FIELD){		
					--cnt;											
				}
			}
		}
		return cnt;
	}

	public static Vec2[][] generatePossibleMoves(byte movingPlayer){
		Vec2[][] possibleMoves = new Vec2[18][2];
		byte moveCnt = 0;
		switch (movingPlayer){
			case 1:
				for(Vec2 stone : posP1){
					if(stone.y >= 7) 				// Stone is at the top end 
						continue;					// of the field and can't move
					
					// Look if we can move straight
					if(board[stone.x][stone.y + 1] == EMPTY_FIELD){
						possibleMoves[moveCnt][0] = new Vec2(stone.x, stone.y);
						possibleMoves[moveCnt][1] = new Vec2(stone.x, (byte) (stone.y + 1));
						++moveCnt;
					}
					// Look if we can hit diagonal left
					if(stone.x > 0
							&& board[stone.x - 1][stone.y + 1] != EMPTY_FIELD
							&& board[stone.x - 1][stone.y + 1] != movingPlayer){
						possibleMoves[moveCnt][0] = new Vec2(stone.x, stone.y);
						possibleMoves[moveCnt][1] = new Vec2((byte) (stone.x - 1), (byte) (stone.y + 1));
						++moveCnt;
					}
					// Look if we can hit diagonal right
					if(stone.x < 7 
							&& board[stone.x + 1][stone.y + 1] != EMPTY_FIELD
							&& board[stone.x + 1][stone.y + 1] != movingPlayer){
						possibleMoves[moveCnt][0] = new Vec2(stone.x, stone.y);
						possibleMoves[moveCnt][1] = new Vec2((byte) (stone.x + 1), (byte) (stone.y + 1));
						++moveCnt;
					}
				}
				// indicate end of array
				System.out.println("Debug: Created " + moveCnt + " moves for player " + movingPlayer);
				for(int i = 0; i < moveCnt; ++i){
					System.out.println(possibleMoves[i][0].x + "," + possibleMoves[i][0].y + " -> " + possibleMoves[i][1].x + "," + possibleMoves[i][1].y);
				}
				if(moveCnt <= possibleMoves.length)
					possibleMoves[moveCnt][0] = new Vec2((byte) -1, (byte) -1);
				return possibleMoves;
			case 2:
				for(Vec2 stone : posP2){
					if(stone.x <= 0) 				// Stone is at the left end 
						continue;					// of the field and can't move
					
					// Look if we can move straight
					if(board[stone.x - 1][stone.y] == EMPTY_FIELD){
						possibleMoves[moveCnt][0] = new Vec2(stone.x, stone.y);
						possibleMoves[moveCnt][1] = new Vec2((byte) (stone.x - 1), stone.y);
						++moveCnt;
					}
					// Look if we can hit diagonal left
					if(stone.y < 0  
							&& board[stone.x - 1][stone.y - 1] != EMPTY_FIELD
							&& board[stone.x - 1][stone.y - 1] != movingPlayer){
						possibleMoves[moveCnt][0] = new Vec2(stone.x, stone.y);
						possibleMoves[moveCnt][1] = new Vec2((byte) (stone.x - 1), (byte) (stone.y - 1));
						++moveCnt;
					}
					// Look if we can hit diagonal right
					if(stone.y < 7 
							&& board[stone.x - 1][stone.y + 1] != EMPTY_FIELD
							&& board[stone.x - 1][stone.y + 1] != movingPlayer){
						possibleMoves[moveCnt][0] = new Vec2(stone.x, stone.y);
						possibleMoves[moveCnt][1] = new Vec2((byte) (stone.x - 1), (byte) (stone.y + 1));
						++moveCnt;
					}
				}
				// indicate end of array
				System.out.println("Debug: Created " + moveCnt + " moves for player " + movingPlayer);
				for(int i = 0; i < moveCnt; ++i){
					System.out.println(possibleMoves[i][0].x + "," + possibleMoves[i][0].y + " -> " + possibleMoves[i][1].x + "," + possibleMoves[i][1].y);
				}
				if(moveCnt <= possibleMoves.length)
					possibleMoves[moveCnt][0] = new Vec2((byte) -1, (byte) -1);
				return possibleMoves;
			case 3:
				for(Vec2 stone : posP3){
					if(stone.y <= 0) 				// Stone is at the top end 
						continue;					// of the field and can't move
					
					// Look if we can move straight
					if(board[stone.x][stone.y - 1] == EMPTY_FIELD){
						possibleMoves[moveCnt][0] = new Vec2(stone.x, stone.y);
						possibleMoves[moveCnt][1] = new Vec2(stone.x, (byte) (stone.y - 1));
						++moveCnt;
					}
					// Look if we can hit diagonal left
					if(stone.x < 7
							&& board[stone.x + 1][stone.y - 1] != EMPTY_FIELD
							&& board[stone.x + 1][stone.y - 1] != movingPlayer){
						possibleMoves[moveCnt][0] = new Vec2(stone.x, stone.y);
						possibleMoves[moveCnt][1] = new Vec2((byte) (stone.x + 1), (byte) (stone.y - 1));
						++moveCnt;
					}
					// Look if we can hit diagonal right
					if(stone.x > 0 
							&& board[stone.x - 1][stone.y - 1] != EMPTY_FIELD
							&& board[stone.x - 1][stone.y - 1] != movingPlayer){
						possibleMoves[moveCnt][0] = new Vec2(stone.x, stone.y);
						possibleMoves[moveCnt][1] = new Vec2((byte) (stone.x - 1), (byte) (stone.y - 1));
						++moveCnt;
					}
				}
				// indicate end of array
				System.out.println("Debug: Created " + moveCnt + " moves for player " + movingPlayer);
				for(int i = 0; i < moveCnt; ++i){
					System.out.println(possibleMoves[i][0].x + "," + possibleMoves[i][0].y + " -> " + possibleMoves[i][1].x + "," + possibleMoves[i][1].y);
				}
				if(moveCnt <= possibleMoves.length)
					possibleMoves[moveCnt][0] = new Vec2((byte) -1, (byte) -1);
				return possibleMoves;
			case 4:
				for(Vec2 stone : posP4){
					if(stone.x >= 7) 				// Stone is at the left end 
						continue;					// of the field and can't move
					
					// Look if we can move straight
					if(board[stone.x + 1][stone.y] == EMPTY_FIELD){
						possibleMoves[moveCnt][0] = new Vec2(stone.x, stone.y);
						possibleMoves[moveCnt][1] = new Vec2((byte) (stone.x + 1), stone.y);
						++moveCnt;
					}
					// Look if we can hit diagonal left
					if(stone.y < 7  
							&& board[stone.x + 1][stone.y + 1] != EMPTY_FIELD
							&& board[stone.x + 1][stone.y + 1] != movingPlayer){
						possibleMoves[moveCnt][0] = new Vec2(stone.x, stone.y);
						possibleMoves[moveCnt][1] = new Vec2((byte) (stone.x + 1), (byte) (stone.y + 1));
						++moveCnt;
					}
					// Look if we can hit diagonal right
					if(stone.y < 0 
							&& board[stone.x + 1][stone.y - 1] != EMPTY_FIELD
							&& board[stone.x + 1][stone.y - 1] != movingPlayer){
						possibleMoves[moveCnt][0] = new Vec2(stone.x, stone.y);
						possibleMoves[moveCnt][1] = new Vec2((byte) (stone.x + 1), (byte) (stone.y - 1));
						++moveCnt;
					}
				}
				// indicate end of array
				System.out.println("Debug: Created " + moveCnt + " moves for player " + movingPlayer);
				for(int i = 0; i < moveCnt; ++i){
					System.out.println(possibleMoves[i][0].x + "," + possibleMoves[i][0].y + " -> " + possibleMoves[i][1].x + "," + possibleMoves[i][1].y);
				}
				if(moveCnt <= possibleMoves.length)
					possibleMoves[moveCnt][0] = new Vec2((byte) -1, (byte) -1);
				return possibleMoves;
			default:
				System.out.println("Debug: Falsche Spielernummer beim Generieren angegeben");
				return null;
				
		}
			
		
	}
}
