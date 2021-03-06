package game2015;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

	public static final int size = 20;
	public static final int scene_height = size * 20 + 100;
	public static final int scene_width = size * 20 + 200;

	public static Image image_floor;
	public static Image image_wall;
	public static Image hero_right, hero_left, hero_up, hero_down;

	public static Player me;
	public static List<Player> players = new ArrayList<Player>();

	private Label[][] fields;
	private TextArea scoreList;

	private String[] board = { // 20x20
	"wwwwwwwwwwwwwwwwwwww", "w        ww        w", "w w  w  www w  w  ww",
			"w w  w   ww w  w  ww", "w  w               w",
			"w w w w w w w  w  ww", "w w     www w  w  ww",
			"w w     w w w  w  ww", "w   w w  w  w  w   w",
			"w     w  w  w  w   w", "w ww ww        w  ww",
			"w  w w    w    w  ww", "w        ww w  w  ww",
			"w         w w  w  ww", "w        w     w  ww",
			"w  w              ww", "w  w www  w w  ww ww",
			"w w      ww w     ww", "w   w   ww  w      w",
			"wwwwwwwwwwwwwwwwwwww" };

	// -------------------------------------------
	// | Maze: (0,0) | Score: (1,0) |
	// |-----------------------------------------|
	// | boardGrid (0,1) | scorelist |
	// | | (1,1) |
	// -------------------------------------------

	private String[] ips = { "10.10.133.180" };

	private ServerSocket srvSock;

	public void initConnect() throws IOException {

		new Thread(new Runnable() {
			@Override
			public void run() {
				for (String player : Main.this.ips) {
					try (Socket sock = new Socket(player, 55551);
							OutputStream os = sock.getOutputStream();
							PrintWriter writer = new PrintWriter(os);) {
						writer.println("NAME " + me.getName() + " "
								+ me.getXpos() + " " + me.getYpos());
					} catch (IOException e) {
					}
				}

			}
		}).start();
	}

	public void initListen() throws IOException {
		ServerSocket srvSock = new ServerSocket(55551);
		setSS(srvSock);

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try (Socket sock = srvSock.accept();
							BufferedReader reader = new BufferedReader(
									new InputStreamReader(sock.getInputStream()));) {
						System.out.println(reader.readLine());
					} catch (IOException e) {
					}
				}
			}
		}).start();
	}

	public void setSS(ServerSocket ss) {
		this.srvSock = ss;
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			GridPane grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(0, 10, 0, 10));

			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					if (event.equals(WindowEvent.WINDOW_CLOSE_REQUEST)) {
						Platform.exit();
						System.exit(0);
					}
				}
			});

			Text mazeLabel = new Text("Maze:");
			mazeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

			Text scoreLabel = new Text("Score:");
			scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

			this.scoreList = new TextArea();

			GridPane boardGrid = new GridPane();

			image_wall = new Image(getClass().getResourceAsStream(
					"Image/wall4.png"), size, size, false, false);
			image_floor = new Image(getClass().getResourceAsStream(
					"Image/floor1.png"), size, size, false, false);

			hero_right = new Image(getClass().getResourceAsStream(
					"Image/heroRight.png"), size, size, false, false);
			hero_left = new Image(getClass().getResourceAsStream(
					"Image/heroLeft.png"), size, size, false, false);
			hero_up = new Image(getClass().getResourceAsStream(
					"Image/heroUp.png"), size, size, false, false);
			hero_down = new Image(getClass().getResourceAsStream(
					"Image/heroDown.png"), size, size, false, false);

			this.fields = new Label[20][20];
			for (int j = 0; j < 20; j++) {
				for (int i = 0; i < 20; i++) {
					switch (this.board[j].charAt(i)) {
					case 'w':
						this.fields[i][j] = new Label("", new ImageView(
								image_wall));
						break;
					case ' ':
						this.fields[i][j] = new Label("", new ImageView(
								image_floor));
						break;
					default:
						throw new Exception("Illegal field value: "
								+ this.board[j].charAt(i));
					}
					boardGrid.add(this.fields[i][j], i, j);
				}
			}
			this.scoreList.setEditable(false);

			grid.add(mazeLabel, 0, 0);
			grid.add(scoreLabel, 1, 0);
			grid.add(boardGrid, 0, 1);
			grid.add(this.scoreList, 1, 1);

			Scene scene = new Scene(grid, scene_width, scene_height);
			primaryStage.setScene(scene);
			primaryStage.show();

			scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
				switch (event.getCode()) {
				case UP:
					playerMoved(0, -1, "up");
					break;
				case DOWN:
					playerMoved(0, +1, "down");
					break;
				case LEFT:
					playerMoved(-1, 0, "left");
					break;
				case RIGHT:
					playerMoved(+1, 0, "right");
					break;
				case L:
					synchronized (this) {
						try {
							initListen();
							Thread.sleep(10000);
							initConnect();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					break;
				default:
					break;
				}
			});

			// Setting up standard players

			me = new Player("PsykoHenrik", 13, 27, "up");
			players.add(me);
			this.fields[9][4].setGraphic(new ImageView(hero_up));

			this.scoreList.setText(getScoreList());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void playerMoved(int delta_x, int delta_y, String direction) {
		me.direction = direction;
		int x = me.getXpos(), y = me.getYpos();

		if (this.board[y + delta_y].charAt(x + delta_x) == 'w') {
			me.addPoints(-1);
		} else {
			Player p = getPlayerAt(x + delta_x, y + delta_y);
			if (p != null) {
				me.addPoints(10);
				p.addPoints(-10);
			} else {
				me.addPoints(1);

				this.fields[x][y].setGraphic(new ImageView(image_floor));
				x += delta_x;
				y += delta_y;

				if (direction.equals("right")) {
					this.fields[x][y].setGraphic(new ImageView(hero_right));
				}
				;
				if (direction.equals("left")) {
					this.fields[x][y].setGraphic(new ImageView(hero_left));
				}
				;
				if (direction.equals("up")) {
					this.fields[x][y].setGraphic(new ImageView(hero_up));
				}
				;
				if (direction.equals("down")) {
					this.fields[x][y].setGraphic(new ImageView(hero_down));
				}
				;

				me.setXpos(x);
				me.setYpos(y);
			}
		}
		this.scoreList.setText(getScoreList());
	}

	public String getScoreList() {
		StringBuffer b = new StringBuffer(100);
		for (Player p : players) {
			b.append(p + "\r\n");
		}
		return b.toString();
	}

	public Player getPlayerAt(int x, int y) {
		for (Player p : players) {
			if (p.getXpos() == x && p.getYpos() == y) {
				return p;
			}
		}
		return null;
	}

	public static void main(String[] args) {
		Application.launch(args);
	}
}
