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
	static Vec2[] posP1 = new Vec2[6];
	static Vec2[] posP2 = new Vec2[6];
	static Vec2[] posP3 = new Vec2[6];
	static Vec2[] posP4 = new Vec2[6];
	static Vec2[][] savedMove = new Vec2[1][2];
	static final byte EMPTY_FIELD = 0;
	static int depth = 10;
	
	public static void main(String[] args) {
		String serverIP = args[0];
	
		try {
			networkClient = new NetworkClient(serverIP, "neueKI", ImageIO.read(new File("logo/hellokitty256.png")));
			int roundCnt = 0;
			init();
			Vec2[] tempPosP1 = new Vec2[6]; 
			Vec2[] tempPosP2 = new Vec2[6]; 
			Vec2[] tempPosP3 = new Vec2[6]; 
			Vec2[] tempPosP4 = new Vec2[6];
			for(int i = 0; i < 6; i++){
				tempPosP1[i] = new Vec2((byte)0,(byte)0);
				tempPosP2[i] = new Vec2((byte)0,(byte)0);
				tempPosP3[i] = new Vec2((byte)0,(byte)0);
				tempPosP4[i] = new Vec2((byte)0,(byte)0);
			}
			
			while(true){
				Move move = networkClient.receiveMove();
				long milliStart = System.currentTimeMillis();
				
				roundCnt++;
				System.out.println("Started round " + roundCnt);
				if(move == null){
					
					byte[][] tempBoard = new byte[8][8];
					for(int i = 0; i < 8; ++i){
						for(int j = 0; j < 8; j++){
							tempBoard[i][j] = board[i][j];
						}
					}
					
					for(int i = 0; i < 6; ++i){
						tempPosP1[i].x = posP1[i].x;
						tempPosP1[i].y = posP1[i].y;
						tempPosP2[i].x = posP2[i].x;
						tempPosP2[i].y = posP2[i].y;
						tempPosP3[i].x = posP3[i].x;
						tempPosP3[i].y = posP3[i].y;
						tempPosP4[i].x = posP4[i].x;
						tempPosP4[i].y = posP4[i].y;
					}
					

					move = calculateMove();
					networkClient.sendMove(move);
					//System.out.println("\n moving from " + move.fromX + "," + move.fromY + " to " + move.toX + "," + move.toY);
					long calcTime = (System.currentTimeMillis() - milliStart);
					System.out.println("Calculation time: " + calcTime + " ms" + " Depth: " + depth);
					for(int i = 0; i < 8; ++i){
						for(int j = 0; j < 8; j++){
							board[i][j] = tempBoard[i][j];
						}
					}
			
					for(int i = 0; i < 6; ++i){
						posP1[i].x = tempPosP1[i].x;
						posP1[i].y = tempPosP1[i].y;
						posP2[i].x = tempPosP2[i].x;
						posP2[i].y = tempPosP2[i].y;
						posP3[i].x = tempPosP3[i].x;
						posP3[i].y = tempPosP3[i].y;
						posP4[i].x = tempPosP4[i].x;
						posP4[i].y = tempPosP4[i].y;
					}
					if(calcTime < 300)
						depth++;
					//saveMove(move);
				}else{
					saveMove(move);
				}
				//printBoard();
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
		savedMove[0][0] = new Vec2((byte)-1,(byte)-1);
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
		//Vec2[][] possibleMoves = generatePossibleMoves((byte) playerNumber);
		
		savedMove[0][0].x = -1;				// mache den zug ungültig
		
		int rating = miniMax((byte) playerNumber, depth, -1000, 1000);
		if(savedMove[0][0].x == -1){
			System.out.println("Debug: Es gab keine weiteren Züge mehr :(");
			return null;
		}	
		else{
			System.out.println("Debug: Zug hat ein Rating von " + rating);
			return new Move(savedMove[0][0].x, savedMove[0][0].y, 
					savedMove[0][1].x, savedMove[0][1].y);
			}
	}
	
	
	public static void saveMove(Move move){
		byte movingPlayer = board[move.fromX][move.fromY];
		byte killedPlayer = board[move.toX][move.toY];
		board[move.toX][move.toY] = movingPlayer;				
		board[move.fromX][move.fromY] = 0;
		
		//System.out.println("Saved Move " + move.fromX + "," + move.fromY + " -> " + move.toX + "," + move.toY);
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
	
	public static int rate(byte movingPlayer){
		//Anzahl der eigenen Steine +1
		//Anzahl der gegnerischen Stein -1
		//TODO: blockierte Steine - endgültig - selbst +1
		//									  - Gegner -1
		//					- vorübergehend - selbst -2
		//									- Gegner +2
		//Entfernung zum Startpunkt - selbst - je näher desto besser (am start 7 pkte, pro reihe weg - 1)
		//							- gegner - je näher desto besser (am start -7 pkt, pro reihe weg + 1)
		//Gewichtung der Kriterien
		byte ownStones = 0;
		byte ownStonesWeighting = 1;
		byte enemyStones = 0;
		byte enemyStonesWeighting = -1;
		byte blockedStonesPlayer1 = 0;
		byte blockedStonesPlayer2 = 0;
		byte blockedStonesPlayer3 = 0;
		byte blockedStonesPlayer4 = 0;
		byte tempBlockedStonesPlayer1 = 0;
		byte tempBlockedStonesPlayer2 = 0;
		byte tempBlockedStonesPlayer3 = 0;
		byte tempBlockedStonesPlayer4 = 0;
		byte ownBlockedStones = 0;
		byte enemyBlockedStones = 0;
		byte tempOwnBlockedStones = 0;
		byte tempEnemyBlockedStones = 0;
		byte ownBlockedStonesWeighting = 1;
		byte tempOwnBlockedStonesWeighting = -2;
		byte enemyBlockedStonesWeighting = -1;
		byte tempEnemyBlockedStonesWeighting = 2;
		byte distancePlayer1 = 0;
		byte distancePlayer2 = 0;
		byte distancePlayer3 = 0;
		byte distancePlayer4 = 0;
		byte distanceOwn = 0;
		byte distanceEnemy = 0;
		byte distanceOwnWeighting = 1;
		byte distanceEnemyWeighting = -1;
		
		for(byte i = 0; i < 8; ++i){
			for(byte j = 0; j < 8; ++j){
				if(board[i][j] == movingPlayer)	{				// +1 for own stone
					++ownStones;
				}
				if(board[i][j] == 1){
					if(j == 7)
						blockedStonesPlayer1++;
					else if(board[i][j+1] != EMPTY_FIELD)
						tempBlockedStonesPlayer1++;
					distancePlayer1 += (7 - j);
				}else if(board[i][j] == 2){
					if(i == 0)
						blockedStonesPlayer2++;
					else if(board[i-1][j] != EMPTY_FIELD)
						tempBlockedStonesPlayer2++;
					distancePlayer2 += i;
				}else if(board[i][j] == 3){
					if(j == 0)
						blockedStonesPlayer3++;	
					else if(board[i][j-1] != EMPTY_FIELD)
						tempBlockedStonesPlayer3++;
					distancePlayer3 += j;
				}else if(board[i][j] == 4){
					if(i == 7)
						blockedStonesPlayer4++;	
					else if(board[i+1][j] != EMPTY_FIELD)
						tempBlockedStonesPlayer4++;
					distancePlayer4 += (7 - i);
				}
				
				if(board[i][j] != movingPlayer 					// -1 for enemy stone
						&& board[i][j] != EMPTY_FIELD){		
					++enemyStones;											
				}
			}
		}
		if(movingPlayer == 1){
			ownBlockedStones = blockedStonesPlayer1;
			tempOwnBlockedStones = tempBlockedStonesPlayer1;
			enemyBlockedStones = (byte)(blockedStonesPlayer2 + blockedStonesPlayer3 + blockedStonesPlayer4);
			tempEnemyBlockedStones = (byte)(tempBlockedStonesPlayer2 + tempBlockedStonesPlayer3 + tempBlockedStonesPlayer4);
			distanceOwn = distancePlayer1;
			distanceEnemy = (byte) (distancePlayer2 + distancePlayer3 + distancePlayer4);
		}else if(movingPlayer == 2){
			ownBlockedStones = blockedStonesPlayer2;
			tempOwnBlockedStones = tempBlockedStonesPlayer2;
			enemyBlockedStones = (byte)(blockedStonesPlayer1 + blockedStonesPlayer3 + blockedStonesPlayer4);
			tempEnemyBlockedStones = (byte)(tempBlockedStonesPlayer1 + tempBlockedStonesPlayer3 + tempBlockedStonesPlayer4);
			distanceOwn = distancePlayer2;
			distanceEnemy = (byte) (distancePlayer1 + distancePlayer3 + distancePlayer4);
		}
		else if(movingPlayer == 3){
			ownBlockedStones = blockedStonesPlayer3;
			tempOwnBlockedStones = tempBlockedStonesPlayer3;
			enemyBlockedStones = (byte)(blockedStonesPlayer1 + blockedStonesPlayer2 + blockedStonesPlayer4);
			tempEnemyBlockedStones = (byte)(tempBlockedStonesPlayer1 + tempBlockedStonesPlayer2 + tempBlockedStonesPlayer4);
			distanceOwn = distancePlayer3;
			distanceEnemy = (byte) (distancePlayer1 + distancePlayer2 + distancePlayer4);
		}
		else if(movingPlayer == 4){
			ownBlockedStones = blockedStonesPlayer4;
			tempOwnBlockedStones = tempBlockedStonesPlayer4;
			enemyBlockedStones = (byte)(blockedStonesPlayer1 + blockedStonesPlayer2 + blockedStonesPlayer3);
			tempEnemyBlockedStones = (byte)(tempBlockedStonesPlayer1 + tempBlockedStonesPlayer2 + tempBlockedStonesPlayer3);
			distanceOwn = distancePlayer4;
			distanceEnemy = (byte) (distancePlayer1 + distancePlayer2 + distancePlayer3);
		}
		return ownStones * ownStonesWeighting 
				+ enemyStones * enemyStonesWeighting
				+ ownBlockedStones * ownBlockedStonesWeighting
				+ tempOwnBlockedStones * tempOwnBlockedStonesWeighting
				+ enemyBlockedStones * enemyBlockedStonesWeighting
				+ tempEnemyBlockedStones * tempEnemyBlockedStonesWeighting
				+ distanceOwn * distanceOwnWeighting
				+ distanceEnemy * distanceEnemyWeighting;
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
				/*
				System.out.println("Debug: Created " + moveCnt + " moves for player " + movingPlayer);
				for(int i = 0; i < moveCnt; ++i){
					System.out.println(possibleMoves[i][0].x + "," + possibleMoves[i][0].y + " -> " + possibleMoves[i][1].x + "," + possibleMoves[i][1].y);
				}
				*/
				if(moveCnt <= possibleMoves.length)
					possibleMoves[moveCnt][0] = new Vec2((byte) -1, (byte) -1);			// indicate end of array
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
					if(stone.y > 0  
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
				/*
				System.out.println("Debug: Created " + moveCnt + " moves for player " + movingPlayer);
				for(int i = 0; i < moveCnt; ++i){
					System.out.println(possibleMoves[i][0].x + "," + possibleMoves[i][0].y + " -> " + possibleMoves[i][1].x + "," + possibleMoves[i][1].y);
				}
				*/
				if(moveCnt <= possibleMoves.length)
					possibleMoves[moveCnt][0] = new Vec2((byte) -1, (byte) -1);			// indicate end of array
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
				/*
				System.out.println("Debug: Created " + moveCnt + " moves for player " + movingPlayer);
				for(int i = 0; i < moveCnt; ++i){
					System.out.println(possibleMoves[i][0].x + "," + possibleMoves[i][0].y + " -> " + possibleMoves[i][1].x + "," + possibleMoves[i][1].y);
				}
				*/
				if(moveCnt <= possibleMoves.length)
					possibleMoves[moveCnt][0] = new Vec2((byte) -1, (byte) -1);		// indicate end of array
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
					if(stone.y > 0 
							&& board[stone.x + 1][stone.y - 1] != EMPTY_FIELD
							&& board[stone.x + 1][stone.y - 1] != movingPlayer){
						possibleMoves[moveCnt][0] = new Vec2(stone.x, stone.y);
						possibleMoves[moveCnt][1] = new Vec2((byte) (stone.x + 1), (byte) (stone.y - 1));
						++moveCnt;
					}
				}
				// indicate end of array
				/*
				System.out.println("Debug: Created " + moveCnt + " moves for player " + movingPlayer);
				for(int i = 0; i < moveCnt; ++i){
					System.out.println(possibleMoves[i][0].x + "," + possibleMoves[i][0].y + " -> " + possibleMoves[i][1].x + "," + possibleMoves[i][1].y);
				}
				*/
				if(moveCnt <= possibleMoves.length)
					possibleMoves[moveCnt][0] = new Vec2((byte) -1, (byte) -1);		// indicate end of array
				return possibleMoves;
			default:
				System.out.println("Debug: Falsche Spielernummer beim Generieren angegeben");
				return null;
				
		}
			
		
	}

	public static int miniMax(byte movingPlayer, int depth, int alpha, int beta){
		
		if(movingPlayer >= 5)
			movingPlayer = 1;
		//System.out.println("Player: " + movingPlayer + " Depth: " + depth);
		Vec2[][] possibleMoves = generatePossibleMoves(movingPlayer);
		
		if(depth <= 0 || possibleMoves[0][0].x == -1){
			if(movingPlayer == 1)
				movingPlayer = 5;
			return rate(--movingPlayer);
		}
		int maxValue = alpha;
		int i = 0;
		while(possibleMoves[i][0].x != -1){
			byte[][] tempBoard = new byte[8][8];
			for(int k = 0; k < 8; ++k){
				for(int j = 0; j < 8; j++){
					tempBoard[k][j] = board[k][j];
				}
			}
			Vec2[] tempPosP1 = new Vec2[6]; 
			Vec2[] tempPosP2 = new Vec2[6]; 
			Vec2[] tempPosP3 = new Vec2[6]; 
			Vec2[] tempPosP4 = new Vec2[6]; 

			for(int k = 0; k < 6; ++k){
				tempPosP1[k] = new Vec2((byte)0,(byte)0);
				tempPosP2[k] = new Vec2((byte)0,(byte)0);
				tempPosP3[k] = new Vec2((byte)0,(byte)0);
				tempPosP4[k] = new Vec2((byte)0,(byte)0);
				tempPosP1[k].x = posP1[k].x;
				tempPosP1[k].y = posP1[k].y;
				tempPosP2[k].x = posP2[k].x;
				tempPosP2[k].y = posP2[k].y;
				tempPosP3[k].x = posP3[k].x;
				tempPosP3[k].y = posP3[k].y;
				tempPosP4[k].x = posP4[k].x;
				tempPosP4[k].y = posP4[k].y;
			}
			
			saveMove(new Move(possibleMoves[i][0].x, possibleMoves[i][0].y,
					possibleMoves[i][1].x, possibleMoves[i][1].y));
			//printBoard();
			int value = -1 * miniMax((byte)(movingPlayer + 1), depth - 1, -1 * beta, -1 * maxValue);
			//System.out.println("Value: " + value + " MaxValue: " + maxValue);
			
			for(int k = 0; k < 8; ++k){
				for(int j = 0; j < 8; j++){
					board[k][j] = tempBoard[k][j];
				}
			}
	
			for(int k = 0; k < 6; ++k){
				posP1[k].x = tempPosP1[k].x;
				posP1[k].y = tempPosP1[k].y;
				posP2[k].x = tempPosP2[k].x;
				posP2[k].y = tempPosP2[k].y;
				posP3[k].x = tempPosP3[k].x;
				posP3[k].y = tempPosP3[k].y;
				posP4[k].x = tempPosP4[k].x;
				posP4[k].y = tempPosP4[k].y;
			}
			
			if(value > maxValue) {
				maxValue = value;
				if(maxValue >= beta)
					break;
				if(depth == Client.depth){
					savedMove[0][0] = possibleMoves[i][0];
					savedMove[0][1] = possibleMoves[i][1];
					System.out.println("Speichere Zug " + savedMove[0][0].x + "," + savedMove[0][0].y + " -> " 
					+ savedMove[0][1].x + "," + savedMove[0][1].y);
					
				}				
			}
			++i;
		}
		return maxValue;
	}
}
