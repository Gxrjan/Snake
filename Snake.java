import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


class Snake extends JFrame {
    Snake snake;

    /**
     * This a separate thread specifically for the actual gameplay.
     */
    static class GameThread extends Thread {
        public void run() {
            try {
                new Snake();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Class that represents a record by some player
     */
    static class Record {
        String name;
        int points;

        public Record(String name, int points) {
            this.name = name;
            this.points = points;
        }
    }


    /**
     * This frame is responsible for the main menu
     */
    static class MENU extends JFrame {
        JPanel menu_panel;
        JPanel score;

        public MENU() throws IOException {
            menu_panel = new JPanel(new GridBagLayout());
            update_scores();

            GridBagConstraints gbc = new GridBagConstraints();

            JPanel stub = new JPanel();
            JPanel stub1 = new JPanel();
            JButton play = new JButton("PLAY");
            play.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                    GameThread t = new GameThread();
                    t.start();
                    setVisible(true);
                }
            });
            JButton scores = new JButton("HIGH SCORES");
            scores.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    show_scores();
                }
            });
            JButton exit = new JButton("EXIT");
            exit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
            gbc.fill = gbc.HORIZONTAL;
            gbc.insets = new Insets(10,10,10,10);

            gbc.ipady = 10;
            gbc.gridx = 1;
            gbc.gridy = 1;
            menu_panel.add(play, gbc);
            gbc.gridx = 1;
            gbc.gridy = 3;
            menu_panel.add(scores, gbc);
            gbc.gridx = 1;
            gbc.gridy = 5;
            menu_panel.add(exit, gbc);

            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation(dim.width/4-this.getSize().width/2, dim.height/4-this.getSize().height/2);

            add(menu_panel);
            setVisible(true);
            setResizable(false);
            setSize(500, 500);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
        }


        /**
         * Changes from 'main' menu to 'high scores' menu
         */
        public void show_scores() {
            update_scores();
            remove(menu_panel);
            add(score);
            invalidate();
            validate();
            repaint();
        }

        /**
         * Updates scores panel
         * <p>
         * If there are no records yet, then this method will add "No scores yet" to a scores panel.
         * Otherwise it will load records from the file "Records.txt", sort it, and display 10 highest scores
         * </p>
         */
        public void update_scores() {
            ArrayList<Record> records = new ArrayList<>();
            score = new JPanel();
            score.setLayout(new BoxLayout(score, BoxLayout.Y_AXIS));
            score.add(Box.createRigidArea(new Dimension(100, 50)));
            try (BufferedReader input = new BufferedReader(new FileReader("Records.txt", StandardCharsets.UTF_8))) {
                String line;
                while((line=input.readLine()) != null) {
                    String[] pair = line.split("-");
                    records.add(new Record(pair[0], Integer.parseInt(pair[1])));
                }
                Collections.sort(records, new Comparator<Record>() {
                    @Override
                    public int compare(Record o1, Record o2) {
                        if (o1.points < o2.points) {
                            return 1;
                        } else if (o1.points > o2.points) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                });
                int place = 1;
                for (Record r : records) {
                    if (place>10) {
                        break;
                    }
                    JPanel recordPanel = new JPanel();
                    recordPanel.setLayout(new BoxLayout(recordPanel, BoxLayout.X_AXIS));
                    JLabel nameLabel = new JLabel(r.name);
                    //JLabel separator = new JLabel("-----");
                    JLabel pointLabel = new JLabel(""+r.points);
                    recordPanel.add(Box.createRigidArea(new Dimension(150,10)));
                    recordPanel.add(nameLabel);
                    recordPanel.add(Box.createHorizontalGlue());
                    //recordPanel.add(separator);
                    recordPanel.add(Box.createHorizontalGlue());
                    recordPanel.add(pointLabel);
                    recordPanel.add(Box.createRigidArea(new Dimension(150,10)));
                    recordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    /*JLabel sc = new JLabel(r.name + "------" + r.points);
                    sc.setAlignmentX(Component.CENTER_ALIGNMENT);
                    score.add(sc);*/
                    score.add(recordPanel);
                    score.add(Box.createRigidArea(new Dimension(100,10)));
                    place++;
                }
            } catch (Exception e) {
                JLabel sc = new JLabel("NO SCORES YET");
                sc.setAlignmentX(Component.CENTER_ALIGNMENT);
                score.add(sc);
            }
            score.add(Box.createVerticalGlue());
            JButton back_button = new JButton("BACK");

            back_button.setAlignmentX(Component.CENTER_ALIGNMENT);
            back_button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    remove(score);
                    add(menu_panel);
                    invalidate();
                    validate();
                    repaint();
                }
            });
            score.add(back_button);
            score.add(Box.createRigidArea(new Dimension(100, 50)));
        }
    }


    /**
     * Class that is needed to make the snake change direction
     */
    class DirectionChange {
        Vector direction;
        int index;

        public DirectionChange(Vector direction) {
            this.direction = direction;
            this.index = 0;
        }
    }

    /**
     * Class that represents a direction
     */
    class Vector {
        int x,y;

        public Vector(int x, int y) {
            this.x = x;
            this.y = y;
        }

    }

    /**
     * Class that represents a position on the board
     */
    class Position {
        public int row,col;

        public Position(int row, int col) {
            this.row = row;
            this.col = col;
        }

    }


    /**
     * Class that represents one cell of the snake
     * Snake is just a Cell array
     */
    class Cell {
        int row,col;
        Vector direction = new Vector(1,0);

        public Cell(int row, int col, Vector dir) {
            this.row = row;
            this.col = col;
            this.direction = dir;
        }
    }


    /**
     * Class representing apples that snake eats
     */
    class Apples {
        ArrayList<Position> apples = new ArrayList<>();

        /**
         * Adds an apple to the board
         * @param apple
         */
        public void add_apple(Position apple) {
            apples.add(apple);
        }


        /**
         * Verifies that there is an apple at a certain position
         * @param row
         * @param col
         * @return true if position is occupied by an apple
         */
        public boolean is_apple(int row, int col) {
            for (Position apple : apples) {
                if (apple.row == row && apple.col == col) {
                    return true;
                }
            }
            return false;
        }


        /**
         * Removes an apple from a certain position
         * @param row
         * @param col
         */
        public void remove_apple(int row, int col) {
            ArrayList<Position> listToRemove = new ArrayList<>();
            for (Position apple : apples) {
                if (apple.row == row && apple.col == col) {
                    listToRemove.add(apple);
                }
            }
            apples.removeAll(listToRemove);
        }

        /**
         * Gives a random coordinates for a new apple
         * <p>
         * The while loop in this method will output position until it makes sure that
         * this position is not occupied by some cell of the snake
         * </p>
         * @param p
         * @return a Position for a new apple, that is not occupied
         */
        public Position get_random_apple_coords(Player p) {
            int row = 0;
            int col = 0;
            Random r = new Random();
            while (true) {
                boolean will_do = true;
                row = r.nextInt(15);
                col = r.nextInt(15);
                for (Cell cell : p.snake) {
                    if (cell.row == row && cell.col == col) {
                        will_do = false;
                    }
                }
                if (will_do){
                    break;
                }
            }
            return new Position(row, col);
        }
    }


    /**
     * Class that represents a player
     */
    public class Player {
        ArrayList<DirectionChange> changes = new ArrayList<>();
        ArrayList<Cell> snake = new ArrayList<>();


        public Player() {
            this.snake.add(new Cell(0,1, new Vector(1,0)));
            this.snake.add(new Cell(0,0, new Vector(1,0)));
        }


        /**
         * Returns true if snake collided with itself
         * @return true if snake collides with itself
         */
        public boolean collision_detected() {
            for (Cell cell : snake) {
                for (Cell other_cell : snake) {
                    if(cell == other_cell) {
                        continue;
                    }
                    if (cell.row == other_cell.row && cell.col == other_cell.col) {
                        return true;
                    }
                }
            }
            return false;
        }


        /**
         * Returns true if the position is occupied by the snake
         * @param row
         * @param col
         * @return true if position is occupied by some cell of the snake
         */
        public boolean belongs_to_snake(int row, int col) {
            for (Cell cell : snake) {
                if ((cell.row == row) && (cell.col == col)) {
                    return true;
                }
            }
            return false;
        }


        /**
         * Moves snake one cell forward
         */
        public void walk() {
            for (Cell cell : snake) {
                cell.col = (cell.col + cell.direction.x) % 15;
                if (cell.col == -1) {
                    cell.col = 14;
                }
                cell.row = (cell.row + cell.direction.y) % 15;
                if (cell.row == -1) {
                    cell.row = 14;
                }
            }
        }


        /**
         * Moves snake one cell backwards
         */
        public void walk_back() {
            for (Cell cell : snake) {
                cell.col = (cell.col - cell.direction.x) % 15;
                if (cell.col == 15) {
                    cell.col = 0;
                }
                cell.row = (cell.row - cell.direction.y) % 15;
                if (cell.row == 15) {
                    cell.row = 0;
                }
            }
        }


        /**
         * Adds a direction change to a snake after player presses 'W', 'A', 'S' or 'D'
         * @param direction
         */
        public void add_direction_change(Vector direction) {
            this.changes.add(new DirectionChange(direction));
        }


        /**
         * Applies one round of direction changes
         */
        public void apply_changes() {
            ArrayList<DirectionChange> listToRemove = new ArrayList<>();
            for (DirectionChange change : changes) {
                if (change.index > snake.size()-1) {
                    listToRemove.add(change);
                } else {
                    snake.get(change.index).direction = change.direction;
                    change.index++;
                }
            }
            changes.removeAll(listToRemove);
        }


        /**
         * Appends a line with a record to the Records.txt
         * @param name
         * @param score
         * @throws IOException
         */
        public void record(String name, int score) throws IOException {
            File recordFile = new File("Records.txt");
            if (!recordFile.exists()) {
                System.out.println("HELLO");
                recordFile.createNewFile();
            }
            FileWriter fileWriter = new FileWriter("Records.txt", true);
            PrintWriter writer = new PrintWriter(fileWriter);
            String s = name+"-"+score+"\n";
            writer.append(s);
            writer.flush();
        }

    }

    final Vector UP = new Vector(0,-1);
    final Vector DOWN = new Vector(0,1);
    final Vector RIGHT = new Vector(0,-1);
    final Vector LEFT = new Vector(0,-1);
    JPanel main_panel;
    JPanel menu_panel;
    Player player = new Player();
    Apples apples = new Apples();
    int length = 2;
    int score = 0;


    public Snake() throws InterruptedException, IOException {
        Launch();
    }


    /**
     * Launches the actual gameplay.
     * @throws InterruptedException
     * @throws IOException
     */
    public void Launch() throws InterruptedException, IOException {
        apples.apples.add(new Position(5,5));
        main_panel = new JPanel(new GridLayout(15,15));
        main_panel.getInputMap().put(KeyStroke.getKeyStroke("S"), "pressed S");
        main_panel.getActionMap().put("pressed S", move_down);
        main_panel.getInputMap().put(KeyStroke.getKeyStroke("W"), "pressed W");
        main_panel.getActionMap().put("pressed W", move_up);
        main_panel.getInputMap().put(KeyStroke.getKeyStroke("D"), "pressed D");
        main_panel.getActionMap().put("pressed D", move_right);
        main_panel.getInputMap().put(KeyStroke.getKeyStroke("A"), "pressed A");
        main_panel.getActionMap().put("pressed A", move_left);
        refresh_main_panel();

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width/4-this.getSize().width/2, dim.height/4-this.getSize().height/2);
        getContentPane().add(main_panel);
        setResizable(false);
        setSize(500,500);

        setVisible(true);

        Random r = new Random();
        while (true) {
            //System.out.println("HERE");
            Thread.sleep(300-(score*10));
            player.apply_changes();
            if (r.nextInt(10) == 1) {
                Position apple_position = apples.get_random_apple_coords(player);
                apples.add_apple(apple_position);
            }
            player.walk();
            refresh_main_panel();
            if (player.collision_detected()) {
                setTitle("YOU LOST. YOUR SCORE: " + score);
                String name = JOptionPane.showInputDialog(this, "YOU LOST. YOUR SCORE: "+score+"\nENTER YOUR NAME", null);
                player.record(name, score);
                //System.out.println("Length is: " + length);
                /*for (Cell cell : player.snake) {
                    System.out.println(cell.row + "|" + cell.col);
                }*/
                break;
            }
        }
    }


    /**
     * This action fires when player hits "S", and it adds a direction change to the snake
     */
    Action move_down = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            if (player.snake.get(0).direction.y != -1) {
                player.add_direction_change(new Vector(0, 1));
            }
        }
    };

    /**
     * This action fires when player hits "W", and it adds a direction change to the snake
     */
    Action move_up = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            if (player.snake.get(0).direction.y != 1) {
                player.add_direction_change(new Vector(0, -1));
            }
        }
    };

    /**
     * This action fires when player hits "D", and it adds a direction change to the snake
     */
    Action move_right = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            if (player.snake.get(0).direction.x != -1) {
                player.add_direction_change(new Vector(1, 0));
            }
        }
    };

    /**
     * This action fires when player hits "A", and it adds a direction change to the snake
     */
    Action move_left = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            if (player.snake.get(0).direction.x != 1) {
                player.add_direction_change(new Vector(-1, 0));
            }

        }
    };


    /**
     * Refreshes gameplay panel
     */
    public void refresh_main_panel() {
        main_panel.removeAll();
        boolean apple_eaten = false;
        int ip = 0;
        int jp = 0;
        for (int i=0;i<15;i++) {
            for (int j=0;j<15;j++) {
                if (player.belongs_to_snake(i, j) && apples.is_apple(i, j)) {
                    JLabel label = new JLabel(/*i+"|"+j*/);
                    label.setOpaque(true);
                    label.setBackground(Color.GREEN);
                    main_panel.add(label).setLocation(j, i);
                    apple_eaten = true;
                    ip = i;
                    jp = j;
                } else if (player.belongs_to_snake(i, j)) {
                    JLabel label = new JLabel(/*i+"|"+j*/);
                    label.setOpaque(true);
                    label.setBackground(Color.GREEN);
                    main_panel.add(label).setLocation(j, i);
                } else if (apples.is_apple(i,j)) {
                    JLabel label = new JLabel("Apple");
                    label.setOpaque(true);
                    label.setBackground(Color.RED);
                    main_panel.add(label).setLocation(j, i);
                } else {
                    JLabel label = new JLabel("");
                    main_panel.add(label).setLocation(j, i);
                }
            }
        }
        main_panel.updateUI();
        if (apple_eaten) {
            score++;
            length++;
            apples.remove_apple(ip, jp);
            player.snake.add(0, new Cell(ip+player.snake.get(0).direction.y, jp+player.snake.get(0).direction.x,
                    player.snake.get(0).direction));
            player.walk_back();
        }
    }

    /**
     * Main
     * @param args
     * @throws InterruptedException
     * @throws IOException
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        new MENU();
    }
}