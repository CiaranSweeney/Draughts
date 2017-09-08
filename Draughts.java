import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.transform.Translate;
import javafx.stage.Modality;
import javafx.stage.Stage;
//IO imports
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class Draughts extends Application {
	//Layout Elements
	AnchorPane ap_main;
	VBox vb_labels, vb_current_draw;
	HBox hb_menu, hb_p1_labels, hb_p2_labels, hb_current_player, hb_data_panel;
	Button btn_newGame, btn_loadGame, btn_saveGame, btn_quitGame, btn_declareDraw;
	Label p1_name, p1_score, p1_remainingPieces, p2_name, p2_score, p2_remainingPieces, player_turn, messages;
	TextArea txt_p1, txt_p2;
	Rectangle player_swatch;
	
	int cell_width, cell_height;
	DraughtControl dboard;
	
	public void init(){
		//initialise all stage elements
		ap_main = new AnchorPane();
		hb_menu = new HBox();
		hb_data_panel = new HBox();
		vb_labels = new VBox();
		hb_current_player = new HBox();
		vb_current_draw = new VBox();
		hb_p1_labels = new HBox();
		hb_p2_labels = new HBox();
		dboard = new DraughtControl();
		
		//Menu Buttons
		btn_newGame = new Button("New Game");
		btn_loadGame = new Button("Load Game");
		btn_saveGame = new Button("Save Game");
		btn_quitGame = new Button("Save and Quit");
		btn_declareDraw = new Button("Declare Draw");
		
		//implement stage hierarchy
		p1_name = new Label("Player 1 : ");
		p2_name = new Label("Player 2 : ");
		p1_score = new Label(" Score: 0 ");
		p2_score = new Label(" Score: 0 ");
		p1_remainingPieces = new Label(" Pieces: 12 ");
		p2_remainingPieces = new Label(" Pieces: 12 ");
		//Current player name and colour swatch
		player_turn = new Label(" Player "+dboard.current_player().get()+"'s turn");
		player_swatch = new Rectangle(12,12);
		player_swatch.setFill(Color.RED);
		//To display notifications
		messages = new Label("");
		messages.setMinWidth(140);
		messages.setMaxWidth(140);
		
		//Arrange elements on stage
		//Add elements to their panes
		hb_menu.getChildren().addAll(btn_newGame,btn_loadGame,btn_saveGame,btn_quitGame);
		hb_p1_labels.getChildren().addAll(p1_name, p1_score, p1_remainingPieces, messages, player_swatch, player_turn);
		hb_p2_labels.getChildren().addAll(p2_name, p2_score, p2_remainingPieces, btn_declareDraw);
		hb_current_player.getChildren().addAll(player_swatch,player_turn);
		vb_labels.getChildren().addAll(hb_p1_labels, hb_p2_labels);
		vb_current_draw.getChildren().addAll(hb_current_player,btn_declareDraw);
		hb_data_panel.getChildren().addAll(vb_labels, vb_current_draw);
		ap_main.getChildren().addAll(hb_menu,dboard,hb_data_panel);
		
		vb_current_draw.setAlignment(Pos.TOP_RIGHT);
		//Anchor all elements in place on the main AnchorPane (easier to automatically resize GridPane)
		AnchorPane.setTopAnchor(dboard, 30.0);
		AnchorPane.setRightAnchor(dboard, 0.0);
		AnchorPane.setLeftAnchor(dboard, 0.0);
		AnchorPane.setBottomAnchor(dboard, 55.0);
		AnchorPane.setBottomAnchor(hb_data_panel, 0.0);
		
		//Listeners for menu buttons
		btn_newGame.setOnAction(new EventHandler<ActionEvent>(){
					public void handle(ActionEvent e){
						//Reset label values and game board
						p1_remainingPieces.setText(" Pieces: 12 ");
						p2_remainingPieces.setText(" Pieces: 12 ");
						p1_score.setText(" Score: 0 ");
						p2_score.setText(" Score: 0 ");
						dboard.newGame();
					}
				});
		btn_saveGame.setOnAction(new EventHandler<ActionEvent>(){
					public void handle(ActionEvent e){
						dboard.saveGame();
					}
				});
		btn_loadGame.setOnAction(new EventHandler<ActionEvent>(){
					public void handle(ActionEvent e){
						dboard.loadGame();
					}
				});
		btn_quitGame.setOnAction(new EventHandler<ActionEvent>(){
					public void handle(ActionEvent e){
						dboard.saveGame();
						Platform.exit();
					}
				});
		btn_declareDraw.setOnAction(new EventHandler<ActionEvent>(){
					public void handle(ActionEvent e){
						drawGame(); //Calls local function to create popup window
					}
				});
		
		//Listeners for labels
		//These listeners are bound to IntegerProperties of variables, meaning they are called whenever that property is altered
		//(Using its set() method)
		dboard.current_player().addListener(new ChangeListener<Number>(){ //Change current player number and colour swatch
			@Override public void changed(ObservableValue<? extends Number> o,Number oldVal, Number newVal){
	             //System.out.println("Player has changed!");
	             player_turn.setText(" Player "+Integer.toString(dboard.current_player().get())+"'s turn");
	     		 player_swatch.setFill((dboard.current_player().get()==1) ? Color.RED : Color.BLACK);
	        }
		});
		dboard.player1_score().addListener(new ChangeListener<Number>(){ //Update player 1 score and remaining pieces count
			@Override public void changed(ObservableValue<? extends Number> o,Number oldVal, Number newVal){
	             p1_score.setText(" Score: "+dboard.player1_score().get()+" ");
	             p1_remainingPieces.setText(" Pieces: "+(12 - dboard.player2_score().get()));
	             p2_remainingPieces.setText(" Pieces: "+(12 - dboard.player1_score().get()));
			}
		});
		dboard.player2_score().addListener(new ChangeListener<Number>(){ //Update player 2 score and remaining pieces count
			@Override public void changed(ObservableValue<? extends Number> o,Number oldVal, Number newVal){
	             p2_score.setText(" Score: "+dboard.player2_score().get()+" ");
	             p1_remainingPieces.setText(" Pieces: "+(12 - dboard.player2_score().get())+" ");
	             p2_remainingPieces.setText(" Pieces: "+(12 - dboard.player1_score().get())+" ");
			}
		});
		
		dboard.messages().addListener(new ChangeListener<Number>(){ //Update when messages change and displays new message to players
			@Override public void changed(ObservableValue<? extends Number> o,Number oldVal, Number newVal){
				/*
			     Message Log: 10 messages	: 20 errors
			     ----------------------------------------
			   	 1 Save Game (11 success	: 21 failure)
			   	 2 Load Game (12 success	: 22 failure)
			   	 
			   	 31 Player 1 wins
			   	 32 Player 2 wins
			   */
				switch(dboard.messages().get()){
					case 11:
						messages.setText("   * Game Saved *");
						messages.setTextFill(Color.BLUE);
						break;
					case 12:
						messages.setText("   * Game Loaded *");
						messages.setTextFill(Color.BLUE);
						break;
					case 21:
						messages.setText(" * Save Game Failed *");
						messages.setTextFill(Color.RED);
						break;
					case 22:
						messages.setText(" * Load Game Failed *");
						messages.setTextFill(Color.RED);
						break;
					case 31:
						messages.setText("   * Player 1 Wins *");
						messages.setTextFill(Color.BLUE);
						player_swatch.setVisible(false);
						player_turn.setVisible(false);
						break;
					case 32:
						messages.setText("   * Player 2 Wins *");
						messages.setTextFill(Color.BLUE);
						player_swatch.setVisible(false);
						player_turn.setVisible(false);
						break;
					default:
						messages.setText("");
						messages.setTextFill(Color.RED);
						player_swatch.setVisible(true);
						player_turn.setVisible(true);
						break;
				}
			}
		});
	}
	
	public void drawGame(){ //Creates popup stage asking if both players agree to draw
		Stage drawGameStage = new Stage();
		drawGameStage.initModality(Modality.WINDOW_MODAL);
		drawGameStage.setTitle("Declare Draw");
		Label lbl_drawGame = new Label("Do both players agree to declare a draw?\n\n");
		Button btn_yes = new Button("Yes"), btn_no = new Button("No");
		HBox hb_buttons = new HBox(5,btn_yes,btn_no);
		hb_buttons.setAlignment(Pos.CENTER);
		VBox drawPopup = new VBox(lbl_drawGame, hb_buttons);
		drawPopup.setAlignment(Pos.CENTER);
		btn_yes.setMinWidth(40);
		btn_no.setMinWidth(40);
		btn_yes.setPadding(new Insets(5));
		btn_no.setPadding(new Insets(5));
		drawGameStage.setScene(new Scene(drawPopup,300,70));
		
		//Handle yes and no buttons
		//No closes stage and resumes game
		btn_no.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent ae){
				drawGameStage.close();
			}
		});
		//Yes exits the application
		btn_yes.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent ae){
				drawGameStage.close();
				Platform.exit();
			}
		});
		
		drawGameStage.setAlwaysOnTop(true);
		drawGameStage.showAndWait();
	}
	
	public void start(Stage primaryStage){
		primaryStage.setTitle("Draughts");
		primaryStage.setScene(new Scene(ap_main,400,480));
		//Set minimum stage dimensions (Prevent stage from obscuring the top menu bar)
		primaryStage.setMinWidth(btn_newGame.getWidth()+btn_loadGame.getWidth()+btn_saveGame.getWidth()+btn_quitGame.getWidth()+16);
		primaryStage.setMinHeight(300);
		primaryStage.show();
	}
	public void stop(){}
	public static void main(String[] args){
		Application.launch(args);
	}
}


class DraughtControl extends Control {
	public DraughtControl(){
		game_board = new Board();
		this.getChildren().add(game_board);
		setSkin(new DraughtControlSkin(this));
	}
	@Override
	public void resize(double width, double height){
		super.resize(width,height);
		game_board.resize(width,height);
	}
	// Methods to be used by UI Buttons
	public void newGame(){
		game_board.resetGame();
	}
	public void saveGame(){
		game_board.saveGame();
	}
	public void loadGame(){
		game_board.loadGame();
	}
	//Methods to allow UI Labels to access game stats
	public SimpleIntegerProperty current_player(){
		return game_board.current_player();
	}
	public SimpleIntegerProperty player1_score(){
		return game_board.player1_score();
	}
	public SimpleIntegerProperty player2_score(){
		return game_board.player2_score();
	}
	public SimpleIntegerProperty messages(){
		return game_board.messages();
	}
	Board game_board;
}

class DraughtControlSkin extends SkinBase<DraughtControl> {
	// default constructor for the class
   public DraughtControlSkin(DraughtControl dc) {
      super(dc);
   }
}


class Board extends Pane {
	public Board(){
		//Initialise variables
		gp_board = new GridPane();
		grid = new Rectangle[8][8];
		render = new DraughtPiece[8][8];
		selected = new boolean[8][8];
	    surrounding=new int[3][3];
	    another_jump=false;
	    messages = new SimpleIntegerProperty(0); //Clear message label
        player1_score = new SimpleIntegerProperty(0);
        player2_score = new SimpleIntegerProperty(0);
        current_player = new SimpleIntegerProperty(1);
        
		//Checkerboard pattern
		for(int i=0;i<8;i++){
			for(int n=0;n<8;n++){
				grid[i][n] = new Rectangle(cell_width,cell_height);
				if((i+n) % 2 != 0)
					grid[i][n].setFill(Color.WHITE);
				else if((i+n) % 2 == 0)
					grid[i][n].setFill(Color.GREEN);
				gp_board.add(grid[i][n], i, n);
				
				//Set all pieces to deselected
				selected[i][n]=false;
			}
		}
		this.getChildren().add(gp_board);
		
		//When the board is clicked, check if a piece can be selected or moved
		setOnMouseClicked(
	            new EventHandler<MouseEvent>() {
	               public void handle(MouseEvent me) {
	            	   selectOrPlacePiece(me.getX(), me.getY());
	               }
	            });
		
		initialiseRender();
		resetGame();
	}

	@Override
	public void resize(double width, double height){
		super.resize(width, height);
		cell_width = width/8;
		cell_height= height/8;
		
		for(int i=0;i<8;i++){
			for(int n=0;n<8;n++){
				grid[i][n].setWidth(cell_width);
				grid[i][n].setHeight(cell_height);
			}
		}
		pieceResizeRelocate();
	}
	 public void resetGame() {
         //first set all renders to 0
	      resetRenders();
         //set all moves to false
         resetMove();
	      //set starting pieces in starting positions
	      for (int j=5; j<render.length; j++){
	         for(int i=0; i<render.length;i++){
	            if((i+j)%2!=0){
	               render[i][j].setPiece(1,false);
	            }
	         }
	      }
	      for (int j=0; j<3; j++){
	         for(int i=0; i<render.length;i++){
	            if((i+j)%2!=0){
	               render[i][j].setPiece(2,false);
	            }
	         }
	      }

	      //set up starting values
	      current_player.set(1);
	      opposing=2;
	      
	      player1_score.set(0);
	      player2_score.set(0);
	      //Reset warning label
	      messages.set(0);
	   }
      
      private void initialiseRender() {
	      //give all the values in array 0 
	      for(int j=0; j<render.length;j++){
	         for(int i=0; i<render.length;i++){
	            render[j][i]=new DraughtPiece(0);
	            //Set translate of background rectangles to same as pieces (prevents overlap when resizing)
	            grid[j][i].getTransforms().add(render[j][i].t());
	            getChildren().addAll(grid[j][i],render[j][i]);
	         }
	      }      
	   }
      //set all pieces to 0 and set king as false
	   private void resetRenders() {
	      for(int j=0; j<render.length; j++){
	         for(int i=0; i<render.length;i++){
	            render[j][i].setPiece(0,false);
	         }
	      }
	   }
	   private void pieceResizeRelocate(){
	      for(int j=0; j<render.length; j++){
	         for(int i=0; i<render.length;i++){
	            render[j][i].resize(cell_width,cell_height);
	            render[j][i].relocate(j*cell_width,i*cell_height);
	         }
	      }
	   }
	   
	   private void selectOrPlacePiece(final double x, final double y){
		   int cx = (int)(x/cell_width);
		   int cy = (int)(y/cell_height);
		   
		   //if cell contains own piece, select that piece
		   if(getPiece(cx,cy) == current_player.get() && another_jump==false)
			   selectPiece(cx,cy);
		   //else if cell is empty, determine if a move can be made
		   else if(getPiece(cx,cy) == 0)
			   placePiece(cx,cy);
		   //Note: ignore a click on opponent's piece
	   }
	   
	   public void selectPiece(final int x, final int y){
         //reset all the old positions where you can move to
         resetMove();
         //determines what is surrounding the selected piece
         whatIsSurrounding(x,y);
         selected_x=x;
         selected_y=y;
		   for(int i=0; i<selected.length; i++)
			   for(int n=0; n<selected.length; n++){
				   selected[i][n]=false;
				   render[i][n].opacityProperty().set(1);
			   }
		   selected[x][y] = true;
		   
		   double opacity = 1;
		   for(int i=0;i<selected.length; i++)
			   for(int n=0; n<selected.length; n++)
				   opacity = (selected[i][n]==true ? 0.5 : opacity);
		   render[x][y].opacityProperty().set(opacity);
	   }
	   public boolean pieceSelected(){
		   for(int i=0; i<selected.length; i++)
			   for(int n=0; n<selected.length; n++)
				   if(selected[i][n])
					   return true;
		   return false;
	   }
	   public void placePiece(final int x, final int y) {
         //if another jump is true then a another jump will be performed if not go through the rest of the method
         if(another_jump==true){
            performJumpAgain(x,y);
            return;
         }
         //single move is used to make sure not to take a single move we there is no need for it
         boolean single_move=true;
         //checks if there is a jump
         checkJump();
		 //if square not empty, or is green square, return
		 if (getPiece(x,y) != 0 || (x+y)%2 == 0 || canMove(x,y)==false)
			 return;
		 //if no piece selected, return
		 if(!pieceSelected())
			 return;
         //if there is a jump then perform jump
         if(checkJump()==true){
            single_move=false;
            performJump(x,y);
          }
		  //set the piece in its new position
          render[x][y].setPiece(current_player.get(),render[selected_x][selected_y].isKing());
          //Remove selected piece from board before replacing it on new square
		  for(int i=0; i<selected.length; i++){
			   for(int n=0; n<selected.length; n++){
				   if(selected[i][n]==true)
					   render[i][n].setPiece(0,false);
				   	selected[i][n]=false;
					  	render[i][n].setOpacity(1.0);
			   }
		  }
          //checks to see if the piece in the new position turns into king
          checkKing(x,y);
          //reset all the old positions where you can move to
          resetMove();
          //if another jump is available then set the another_jump to true
          if(checkJumpAgain(x,y)==true&&single_move==false){
            selected[x][y]=true;
            //highlights the piece that can jump again
			   render[x][y].setOpacity(0.5);
            another_jump=true;
            selected_x=x;
            selected_y=y;
            return;
          }
          //checks if the game is over
          determineEndGame();
          //changes the players turn
		  swapPlayers();
	   }
	   

	   public void saveGame(){
		   //Write render[][] array and current_player, as well as scores and pieces remaining to a text file
		   try {
			   saveFile = new FileWriter("draughts_save_game.txt"); //Create/overwrite file in default location
			   BufferedWriter output;
			   output = new BufferedWriter(saveFile);
			   //Write to file as xml
			   output.write("<?xml version='1.0'?>");
			   //Write render[][] data
			   for(int i=0; i<render.length; i++){
				   for(int n=0; n<render.length; n++){
					   output.write("<coordinate>"
					   					+ "<x>"+i+"</x>"
					   					+ "<y>"+n+"</y>"
					   					+ "<player>"+getPiece(i,n)+"</player>"
					   					+ "<king>"+(render[i][n].isKing() ? 1 : 0)+"</king>"
					   				+ "</coordinate>");
				   }
			   }
			   //Write current player and player scores
			   output.write("<stats>"
						   		+ "<current_player>"+current_player.get()+"</current_player>"
						   		+ "<player1_score>"+player1_score.get()+"</player1_score>"
						   		+ "<player2_score>"+player2_score.get()+"</player2_score>"
						   		+ "<player1_pieces>"+player1_pieces+"</player1_pieces>"
						   		+ "<player2_pieces>"+player2_pieces+"</player2_pieces>"
						   	+ "</stats>");
			   output.flush();
			   System.out.println("Game Saved Successfully");
			   messages.set(11); //Display save success message in UI label
			   output.close();
		   } catch (IOException e) {
			   System.out.println("Save Game Failed");
			   messages.set(21); //Display save failure message in UI label
			   e.printStackTrace();
		   }
	   }
	  
	   public void loadGame(){
		   	try {
		   		String xml = readSaveFile(); //Returns contents of saveFile as a String
		   		int x,y,p;
		   		boolean k;
		   		
		   		//Find xml tags as substrings of xml String and extract relevant data as ints
		   		for(int i=0; i<xml.length()-44;i++){
		   			if(xml.toLowerCase().substring(i,i+3).equals("<x>")){
		   				x=(int)(Character.getNumericValue(xml.charAt(i+3)));
		   				y=(int)(Character.getNumericValue(xml.charAt(i+11)));
		   				p=(int)(Character.getNumericValue(xml.charAt(i+24)));
		   				k=(boolean) ((Character.getNumericValue(xml.charAt(i+40))==1)? true : false);
		   				//Apply extracted data to render[][] array
		   				render[x][y].setPiece(p, k);
		   				render[x][y].setKing(k);
		   			}
		   		}
		   		//Get stats from end of xml String and set game variables
   				current_player.set((int)Character.getNumericValue(xml.charAt(xml.indexOf("<current_player>")+16)));
   				opposing = (current_player.get()==1)? 2 : 1;
   				player1_score.set((int)Character.getNumericValue(xml.charAt(xml.indexOf("<player1_score>")+15)));
   				player2_score.set((int)Character.getNumericValue(xml.charAt(xml.indexOf("<player2_score>")+15)));
   				player1_pieces = (int)Character.getNumericValue(xml.charAt(xml.indexOf("<player1_pieces>")+16));
   				player1_pieces = (int)Character.getNumericValue(xml.charAt(xml.indexOf("<player1_pieces>")+16));
   				System.out.println("Game Loaded Successfully");
 			    messages.set(12); //Display load success message in UI label
			} catch (Exception e) {
				System.out.println("Load Failed");
				messages.set(22); //Display load success message in UI label
			}
	   }
	   
	   
	   public String readSaveFile() throws IOException { //Returns default save file contents as String
	        StringBuffer stringBuffer = new StringBuffer();
	        File loadFile = new File("draughts_save_game.txt"); //Create loadFile that matches save file default location
	        
	        try{
				BufferedReader reader = Files.newBufferedReader(loadFile.toPath()); //Load file path into buffer
		        
		        String line = null;
		        while ((line = reader.readLine()) != null) {
		            stringBuffer.append(line); //Add each line in the reader buffer to a stringBuffer
		        }
	
		        reader.close();
	        }
	        catch(Exception e){
	        	System.out.println("Error! No Save Game Found");
	        }
	        //Return contents of stringBuffer
	        return stringBuffer.toString();
	    }
	   //private method for getting a piece on the board. this will return the board value unless we access an index that doesn't exist.
	   //(Easier method of error-checking than using try-catch statement all the time
	   private int getPiece(final int x, final int y) {
	      try {
	         return render[x][y].getPiece();
	      } 
	      catch (Exception e) {
	         return -1;
	      }
	   }
	   
	   // private method for swapping the players
	   private void swapPlayers() {
	      int temp = current_player.get();
	      current_player.set(opposing);
	      opposing = temp;
	   }
	   
	   //Return integer properties in order to update stats labels in init() of main application
	   public SimpleIntegerProperty current_player(){return current_player;}
	   public SimpleIntegerProperty player1_score(){return player1_score;}
	   public SimpleIntegerProperty player2_score(){return player2_score;}
	   public SimpleIntegerProperty messages(){return messages;}
	   
      //determines what is surrounding the selected piece and checks where the piece can move
      private void whatIsSurrounding(final int x, final int y){
         for(int j=0; j<surrounding.length; j++){
            for(int i=0; i<surrounding.length; i++){
                surrounding[j][i]=-1;
            }
         }
         //if its a king the piece a can move in any direction   
         if(render[x][y].isKing()==true){
            for(int j=0; j<3; j++){
               for(int i=0; i<3; i++){
                  surrounding[j][i]=getPiece(x-(j-1),y-(i-1));
                  if(surrounding[j][i]==0){
                     render[x-(j-1)][y-(i-1)].setMove(true);
                  }
               }
            }
         }
         //if its not king and its a black piece, the piece can move in the down direction
         else if(render[x][y].getPiece()==2){
            for(int j=0; j<3; j++){
               surrounding[j][0]=getPiece(x-(j-1),y-(0-1));
               if(surrounding[j][0]==0){
                  render[x-(j-1)][y-(0-1)].setMove(true);
               }
            }
         }
         //if its not king and its a red piece, the piece can move in the up direction
         else if(render[x][y].getPiece()==1){
             for(int j=0; j<3; j++){
               surrounding[j][2]=getPiece(x-(j-1),y-(2-1));
	           if(surrounding[j][2]==0){
	              render[x-(j-1)][y-(2-1)].setMove(true);
	           }
            }
         }
      }
      //resets all the possible moves to false
      private void resetMove(){
         for(int j=0; j<render.length; j++){
            for(int i=0; i<render.length; i++){
               render[j][i].setMove(false);
            }
         }
      }
      //this method is use to stop errors with canMove in the piece class
      private boolean canMove(final int x, final int y){
         try{
            return render[x][y].canMove();
         }
         catch(Exception e){
            return false;
         }
      }
      //this method looks through each of the current player pieces and checks if any of them can make a jump
      private boolean checkJump(){
         boolean find_jump=false;
         for(int j=0; j<render.length; j++){
            for(int i=0; i<render.length; i++){
               //checks if the current piece is the current player's piece then checks if the piece that is going be jumped is an opponent piece.
               //then it checks if the position where it will jump to will be empty. it also checks what direction this piece can move in
               if(getPiece(j,i)==current_player.get()&&getPiece(j+1,i+1)==opposing&&getPiece(j+2,i+2)==0&&render[j][i].getBack()==true){
                  //if a jump has not been found and you entered the first if statement, set find jump to true and reset all moves
                  if(find_jump==false){
                     find_jump=true;
                     resetMove();
                  }
                  //you can move to this position
                  render[j+2][i+2].setMove(true);
                }
                if(getPiece(j,i)==current_player.get()&&getPiece(j-1,i+1)==opposing&&getPiece(j-2,i+2)==0&&render[j][i].getBack()==true){
                  //if a jump has not been found and you entered the first if statement, set find jump to true and reset all moves
                  if(find_jump==false){
                     find_jump=true;
                     resetMove();
                  }
                  //you can move to this position
                  render[j-2][i+2].setMove(true);
                }
                if(getPiece(j,i)==current_player.get()&&getPiece(j+1,i-1)==opposing&&getPiece(j+2,i-2)==0&&render[j][i].getForward()==true){
                  //if a jump has not been found and you entered the first if statement, set find jump to true and reset all moves
                  if(find_jump==false){
                     find_jump=true;
                     resetMove();
                  }
                  //you can move to this position
                  render[j+2][i-2].setMove(true);
                }
                if(getPiece(j,i)==current_player.get()&&getPiece(j-1,i-1)==opposing&&getPiece(j-2,i-2)==0&&render[j][i].getForward()==true){
                	//if a jump has not been found and you entered the first if statement, set find jump to true and reset all moves
	                if(find_jump==false){
	                   find_jump=true;
	                   resetMove();
	                }
	                //you can move to this position
	                render[j-2][i-2].setMove(true);
                }
            }
         }
         //return true if you found a jump else return false
         return find_jump;
      }
      //this method is used for the jump and gets rid of the piece that was jumped
      private void performJump(final int x,final int y){
         if(x-2==selected_x&&y-2==selected_y){
            render[x-1][y-1].setPiece(0,false);
         }
         else if(x+2==selected_x&&y-2==selected_y){
            render[x+1][y-1].setPiece(0,false);
         }
         else if(x-2==selected_x&&y+2==selected_y){
            render[x-1][y+1].setPiece(0,false);
         }
         else if(x+2==selected_x&&y+2==selected_y){
            render[x+1][y+1].setPiece(0,false);
         }
         //this updates the scores
         if(current_player.get()==1) player1_score.set(player1_score.get()+1);
         else if(current_player.get()==2) player2_score.set(player2_score.get()+1);
      }
      //this method checks if you can jump again
      private boolean checkJumpAgain(final int x,final int y){
         boolean find_jump=false;
         //
         if(getPiece(x,y)==current_player.get()&&getPiece(x+1,y+1)==opposing&&getPiece(x+2,y+2)==0&&render[x][y].getBack()==true){
            //if a jump has not been found and you entered the first if statement, set find jump to true and reset all moves
             if(find_jump==false){
               find_jump=true;
               resetMove();
             }
             //you can move to this position
             render[x+2][y+2].setMove(true);
         }
         if(getPiece(x,y)==current_player.get()&&getPiece(x-1,y+1)==opposing&&getPiece(x-2,y+2)==0&&render[x][y].getBack()==true){
            //if a jump has not been found and you entered the first if statement, set find jump to true and reset all moves
            if(find_jump==false){
               find_jump=true;
               resetMove();
            }
            //you can move to this position
            render[x-2][y+2].setMove(true);
         }
         if(getPiece(x,y)==current_player.get()&&getPiece(x+1,y-1)==opposing&&getPiece(x+2,y-2)==0&&render[x][y].getForward()==true){
            //if a jump has not been found and you entered the first if statement, set find jump to true and reset all moves
            if(find_jump==false){
               find_jump=true;
               resetMove();
            }
            //you can move to this position
            render[x+2][y-2].setMove(true);
         }
         if(getPiece(x,y)==current_player.get()&&getPiece(x-1,y-1)==opposing&&getPiece(x-2,y-2)==0&&render[x][y].getForward()==true){
            //if a jump has not been found and you entered the first if statement, set find jump to true and reset all moves
            if(find_jump==false){
               find_jump=true;
               resetMove();
            }
            //you can move to this position
            render[x-2][y-2].setMove(true);
         }
         return find_jump;
      }
      //this method is used for the second and many more jumps
      public void performJumpAgain(final int x,final int y){
         //if square not empty, or is green square, return
         if (getPiece(x,y) != 0 || (x+y)%2 == 0 ||canMove(x,y)==false)
			      return;
         //if no piece selected, return      
         if(!pieceSelected())
			      return;
         //Perform jump
         performJump(x,y);
         //set the piece in its new position
         render[x][y].setPiece(current_player.get(),render[selected_x][selected_y].isKing());
         //Remove selected piece from board before replacing it on new square
         for(int i=0; i<selected.length; i++)
			   for(int n=0; n<selected.length; n++){
				   if(selected[i][n]==true)
					   render[i][n].setPiece(0,false);
				      selected[i][n]=false;
					   render[i][n].setOpacity(1.0);
            }
         //checks to see if the piece in the new position turns into king  
         checkKing(x,y);
         //set another jump to false so you don't keep going into this method if there is not another jump available
         another_jump=false;
         //reset all the old positions where you can move to
         resetMove();
         //if another jump is available then set the another_jump to true
         if(checkJumpAgain(x,y)==true){
            another_jump=true;
            selected[x][y]=true;
            //highlights the piece that can jump again
            render[x][y].setOpacity(0.5);
            selected_x=x;
            selected_y=y;
            return;
         }
         //checks if the game is over
         determineEndGame();
         //changes the players turn
		   swapPlayers();
      }
     //this check if the piece will turn into a king by checking the piece position
     private void checkKing(final int x,final int y){
         if(getPiece(x,y)==1&&y==0){
            render[x][y].setKing(true);
         }
         else if(getPiece(x,y)==2&&y==7){
            render[x][y].setKing(true);
         }
      }
      //this checks if the game has ended
      private void determineEndGame(){
         boolean red=false;
         boolean black=false;
         //looks for pieces of player 1 and player 2.
         for(int j=0; j<render.length; j++){
            for(int i=0; i<render.length; i++){
               if(getPiece(i,j)==1){
                  red=true;
               }
               if(getPiece(i,j)==2){
                  black=true;
               }
            }
         }
         //if player 1 or player 2 pieces are not found then end the game and declare a winner
         if(red==false||black==false){
            System.out.println("Game is over");
            if(red==true){
               System.out.print("Player 1 is the winner");
               messages.set(31); //Display winner in UI label
            }
            else{
               System.out.print("player 2 is the winner");
               messages.set(32);  //Display winner in UI label
            }
            //Platform.exit();
         }
         
      }
      
	  private DraughtPiece render[][]; //tracks location and type of pieces on board
	  private int opposing;
	  private SimpleIntegerProperty current_player;
	  private SimpleIntegerProperty player1_score, player2_score; //Opponent pieces captured
	  private int player1_pieces, player2_pieces; //Pieces remaining
      private SimpleIntegerProperty messages; //For displaying messages to the player (save game, winner, etc.)
      
      private int selected_x;        //saves the position of x in the selected piece              
      private int selected_y;       //saves the position of y in the selected piece 
      private int surrounding[][];  //Array of pieces directly surrounding a selected piece
	   
	   private boolean selected[][]; //tracks currently selected piece
      boolean another_jump;
	   
	   private GridPane gp_board; //Game board
	   private Rectangle[][] grid; //Contains all rectangles that make up the game board's checkerboard pattern
	   private double cell_width, cell_height;
	   
	   FileWriter saveFile; //Contains location of save file after Save Game button clicked
}


class DraughtPiece extends Group {
	// default constructor for the class
   public DraughtPiece(int player) {
      // create a new translate object and take a copy of the player
      this.player=player;
      piece=new Ellipse();
      t=new Translate();
      piece.getTransforms().add(t);
      forward=false;
      back=false;
      king=false;
      //Label to denote if piece is a king
      k = new Label("K");
      k.getTransforms().add(t);
      
      getChildren().addAll(piece,k);
   }
	
	// overridden version of the resize method to give the piece the correct size
   @Override
   public void resize(double width, double height) {
      super.resize(width,height);
      //re-center the ellipse
      piece.setCenterX(width/2);
      piece.setCenterY(height/2);
      //update the radii
      piece.setRadiusX(width/2);
      piece.setRadiusY(height/2);
      //Update position and size of king label
      double pos = k.getFont().getSize();
      k.relocate((width-pos)/2, (height-pos)/2);
      k.setFont(new Font("Arial",(width+height)/4));
   }
	
	// overridden version of the relocate method to position the piece correctly
   @Override
   public void relocate(double x, double y) {     
        super.relocate(x,y);
        t.setX(x);
        t.setY(y);
   }
	  
	// method that will set the piece type
   public void setPiece(final int type,final boolean rank){
      player=type;
      setKing(rank);
      //piece will be red if its player 1
      if(type==1){
         piece.setFill(Color.RED);
         piece.setVisible(true);
         k.setVisible(isKing()); //Display 'K' if the piece is a king
         if(isKing()==true){
            forward=true;
            back=true;
         }
         else{
            forward=true;
            back=false;
         }
      }
      //piece will be black if its player 2
      else if(type==2){
         piece.setFill(Color.BLACK);
         piece.setVisible(true);
         k.setVisible(isKing());
         if(isKing()==true){
            forward=true;
            back=true;
         }
         else{
            back=true;
            forward=false;
         }
      }
      //there will be no piece if its none of the players
      else{
         piece.setVisible(false);
         k.setVisible(false);
         forward=false;
         back=false;
         setKing(false);
      }
       
   }
   //set if it can move
	public void setMove(boolean m){
         move=m;
      }
   //returns if it can move
   public boolean canMove(){
      return move;
   }  
   // returns the type of this piece
   public int getPiece() {
      return player;
   }
   //checks if the piece can go backwards
   public boolean getBack(){
      return back;
   }
   //checks if the piece can go forwards
   public boolean getForward(){
      return forward;
   }
   //if a piece can go forwards and backwards then it is a king
   public void setKing(boolean rank){
      king = rank;
      k.setVisible(isKing());
      if(rank){
    	  forward=true;
      	  back=true;
      }
   }
   //return to see if its a king
   public boolean isKing(){return king;}
   
   //used to apply same translate to background rectangles (prevents pieces rendering outside of cell boundaries)
   public Translate t(){return t;}
   
   // private fields
   
   private int player;		// the player that this piece belongs to
   private Ellipse piece;	// ellipse representing the player's piece
   private Translate t;	   // translation for the player piece
   private boolean move;   //used to see if a piece can move
   private boolean king;   //used to see if a piece is a king
   private boolean forward;// true if piece is moving up the board, else false
   private boolean back;   //true if piece is moving down the board, else false
   private Label k;	      //Displays 'K' if piece is king
}