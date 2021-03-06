import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Minesweeper extends JPanel implements ActionListener {

    private  int gridSize = 15;
    private final int TILE_SIZE = 45;
    private final int GRID_BASE = 2;
    private int BORDER = 2;

    Point mouseLoc;
    boolean isLeftMouse;
    
    private static String[] difficulty = {
            "Easy (6x6 Grid, 6 Mines)",
            "Medium (9x9 Grid, 20 Mines)",
            "Nightmare (15x15 Grid, 90 mines)"
    };
    private static String selectedDifficulty = "Easy";

    private String[] openFields;
    private String[] closedFields;

    private int mineAmount;
    private int[] mineDetectors;

    private int countMarked = 0;
    
    private static JFrame chooserWindow;
    private static JFrame frame;


    public boolean isGameOver=false;

    public Minesweeper() {
        addMouseListener(new NewMouseAdapter(this));
        addMouseMotionListener(new NewMouseMotionAdapter(this));

    }


    public void actionPerformed(ActionEvent click) {
        if (click.getActionCommand().equals("Random")) {
            JOptionPane.showMessageDialog(this,JOptionPane.YES_NO_OPTION);

        }
    }

    public Dimension getPreferredSize() {
        int d = TILE_SIZE * gridSize + 2 * GRID_BASE;
        return new Dimension(d, d);
    }

    public Point mouseToGridCoords(double xMouse, double yMouse) {
        if (xMouse < GRID_BASE || gridSize * TILE_SIZE + GRID_BASE - BORDER < xMouse ||
                yMouse < GRID_BASE || gridSize * TILE_SIZE + GRID_BASE - BORDER < yMouse) {
            throw new RuntimeException("Ignore mouse outside grid: ");
        }


        int gridX = (int) ((xMouse - GRID_BASE) / TILE_SIZE);
        int gridY = (int) ((yMouse - GRID_BASE) / TILE_SIZE);

        return new Point(gridX, gridY);
    }

    private int gridCoordsToArrayIndex(int column, int row) {
        return column + gridSize * row;
    }

    void onClick(boolean isLeftMouse, int xMouse, int yMouse) {
        Point gridCoords = mouseToGridCoords(xMouse, yMouse);
        int index = gridCoordsToArrayIndex(gridCoords.x, gridCoords.y);

        if(selectedDifficulty.equals("Easy")) {
            mineAmount = 6;
        }
        if(selectedDifficulty.equals("Medium")){
            mineAmount = 20;
        }
        if(selectedDifficulty.equals("Hard")) {
            mineAmount = 90;
        }

        if (isLeftMouse) {
            if (closedFields[index].equals("*")) {
                gameOver();
            } else if (closedFields[index].equals("")) {
                if (mineDetectors[index] == 0) {
                    openNoMineFields(index);
                } else {
                    closedFields[index] = "open";
                    openFields[index] = mineDetectors[index]+"";
                }
            }
        }
        else {
            if (!closedFields[index].equals("open") && openFields[index].equals("")) {
                openFields[index] = "X";
                if (openFields[index].equals("X") && closedFields[index].equals("*")) {
                    countMarked++;
                }
            }
            else if (!closedFields[index].equals("open") && openFields[index].equals("X")) {
                openFields[index] = "?";
                if (openFields[index].equals("?") && closedFields[index].equals("*")) {
                    countMarked--;
                }
            }
            else if (!closedFields[index].equals("open") && openFields[index].equals("?")) {
                openFields[index] = "";
            }
            if(countMarked == mineAmount && !isGameOver){
                winner();
            }
        }
        repaint();
    }

    private void newGame(boolean restart) {
        if(selectedDifficulty.equals("Easy")){
            gridSize = 6;
        }
        if(selectedDifficulty.equals("Medium")){
            gridSize = 9;
        }
        if(selectedDifficulty.equals("Hard")){
            gridSize = 15;
        }

        countMarked = 0;
        if (!restart) frame = new JFrame("Minesweeper 1.0");
        openFields = new String[gridSize * gridSize];
        closedFields = new String[gridSize * gridSize];
        Arrays.fill(closedFields, "");
        Arrays.fill(openFields, "");
        closedFields=placeRandomMines();
        mineDetectors=new int[gridSize * gridSize];
        mineDetectors = findAdjacent(closedFields);
        isGameOver=false;
        mouseLoc=null;
        repaint();
    }

    private void gameOver() {
        ImageIcon icon = new ImageIcon(getClass().getResource("/gameover.png"));

        isGameOver=true;
        JOptionPane yesNo = new JOptionPane();

        for (int i=0; i<closedFields.length; i++) {
            if (closedFields[i].equals("*")) {
                openFields[i]="*";
            }
        }
        repaint();
        if (yesNo.showConfirmDialog(null, "Game Over\nWanna play again?", "Game Over",0,
                JOptionPane.YES_NO_OPTION, icon) == JOptionPane.YES_OPTION) {
            newGame(true);
        } else {
            chooserWindow.setVisible(true);
            frame.dispose();
            isGameOver = false;
        }
    }

    private void winner() {
        ImageIcon icon = new ImageIcon(getClass().getResource("/youwon.gif"));

        JOptionPane youWon = new JOptionPane();

        if (youWon.showConfirmDialog(null, "Congratulations, You Won!\nWanna play again?",
                "You Won", 0, JOptionPane.YES_NO_OPTION, icon) == JOptionPane.YES_OPTION) {
            newGame(true);
        }
        else{
            chooserWindow.setVisible(true);
            frame.dispose();
            isGameOver = false;
        }

    }

    private Point arrayIndex2GridCoords(int index) {
        return new Point(index % gridSize, index / gridSize);
    }

    private int[] findAdjacent(String[] field) {
        ArrayList<Integer> indexes = new ArrayList(); // Mayinlari closedfields tan tespit eder listeye ekler
        for (int s = 0; s < field.length; s++) {
            if (field[s].equals("*")) {
                indexes.add(s);
            }
        }
        int[] adjacents = new int[gridSize * gridSize];
        for (int index : indexes) {
            for (int j = -1; j < 2; j++) {
                int range = gridSize * j;
                for (int i = -1; i < 2; i++) {
                    try {
                        if (!closedFields[index + range + i].equals("*")
                                && ((int) (index / gridSize)) * gridSize + gridSize > (index + i)
                                && ((int) (index / gridSize)) * gridSize <= (index + i)) {
                            adjacents[index + range + i] += 1;
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
        return adjacents;

    }

    private String[] placeRandomMines() {
        Random rand = new Random();
        String[] newField = new String[gridSize * gridSize];
        Arrays.fill(newField, "");

        mineAmount = 6;

        if(selectedDifficulty.equals("Medium")){
            mineAmount = 20;
        }
        if(selectedDifficulty.equals("Hard")){
            mineAmount = 90;
        }

        for (int i = 0; i < gridSize * gridSize; i++) {
            if(newField[i].equals("")){
                newField[i] = (rand.nextInt(8) < 1 ? "*" : "");
                if(newField[i].equals("*")){
                    mineAmount--;
                }
            }
            if(mineAmount != 0 && i == (gridSize * gridSize) - 1 ){
                i = 0;
            }
            if(mineAmount == 0){
                break;
            }
        }
        return newField;
    }

    private void openNoMineFields(int index) {
        if (index >= gridSize * gridSize) {
            return;
        }
        for (int j = -1; j < 2; j++) {
            int range = gridSize * j;
            for (int i = -1; i < 2; i++) {
                try {
                    if (!closedFields[index + range + i].equals("*")
                            && !closedFields[index + range + i].equals("open")
                            && (arrayIndex2GridCoords(index).y + 1) * gridSize > index + i
                            && (arrayIndex2GridCoords(index).y) * gridSize <= index + i) {
                        if (mineDetectors[index + range + i] == 0) {
                            closedFields[index + range + i] = "open";
                            openFields[index + range + i] = " ";
                            openNoMineFields(index + range + i);
                        } else {
                            closedFields[index + range + i] = "open";
                            openFields[index + range + i] = mineDetectors[index + range + i] + "";
                        }

                    }
                } catch (Exception e) {
                }
            }

        }
    }


    @Override
    public void paint(Graphics g) {
        for (int row = 0; row < gridSize; ++row) {
            for (int column = 0; column < gridSize; ++column) {
                drawTile(g, row, column);
            }
        }

    }

    private void drawTile(Graphics g, int yTile, int xTile) {
        int RECT_SIZE = TILE_SIZE - BORDER;

        int topLeftCornerX = xTile * TILE_SIZE + (GRID_BASE + BORDER);
        int topLeftCornerY = yTile * TILE_SIZE + (GRID_BASE + BORDER);


        if (closedFields[gridCoordsToArrayIndex(xTile, yTile)].equals("open"))  {
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(topLeftCornerX, topLeftCornerY, RECT_SIZE, RECT_SIZE);
        } else {
            if (isGameOver) {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(topLeftCornerX, topLeftCornerY, RECT_SIZE, RECT_SIZE);
            } else {
                g.setColor(Color.GRAY);
                g.fillRect(topLeftCornerX, topLeftCornerY, RECT_SIZE, RECT_SIZE);
            }
        }
        if (mouseLoc != null && !isGameOver) {
            if (mouseLoc.equals(new Point(xTile, yTile))) {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(topLeftCornerX, topLeftCornerY, RECT_SIZE, RECT_SIZE);
            }
        }
        g.setFont(new Font("Sans", Font.BOLD, 20));
        g.setColor(Color.BLUE);
        g.drawString(openFields[gridCoordsToArrayIndex(xTile, yTile)], topLeftCornerX + TILE_SIZE / 2 - 6, topLeftCornerY + TILE_SIZE / 2 + 5);
//        g.drawString(mineDetectors[gridCoordsToArrayIndex(xTile, yTile)] + "", topLeftCornerX + TILE_SIZE / 2, topLeftCornerY + TILE_SIZE / 2);

    }

    public static void main(String[] args) {
        ImageIcon icon = new ImageIcon(Minesweeper.class.getResource("/MS.png"));
        ImageIcon explanationIcon = new ImageIcon(Minesweeper.class.getResource("/title.jpg"));

        JComboBox difficultyChooser = new JComboBox(difficulty);
        difficultyChooser.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        if(e.getStateChange()==ItemEvent.SELECTED){
                            if(difficultyChooser.getSelectedIndex() == 0){
                                Minesweeper.selectedDifficulty = "Easy";
                            }
                            else if(difficultyChooser.getSelectedIndex() == 1){
                                Minesweeper.selectedDifficulty = "Medium";
                            }
                            else{
                                Minesweeper.selectedDifficulty = "Hard";
                            }
                        }
                    }
                });

        JButton ok = new JButton("Start");
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Minesweeper game = new Minesweeper();

                game.newGame(false);


                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setIconImage((icon.getImage()));

                frame.add("Center", game);

                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                chooserWindow.setVisible(false);

            }
        });


        JLabel explanation = new JLabel();
        explanation.setIcon(explanationIcon);
        explanation.setPreferredSize(new Dimension(300,120));

        chooserWindow = new JFrame("Minesweeper 1.0");
        chooserWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        chooserWindow.setIconImage((icon.getImage()));

        chooserWindow.add("North", explanation);
        chooserWindow.add("Center", difficultyChooser);
        chooserWindow.add("East", ok);

        chooserWindow.pack();
        chooserWindow.setSize(315,200);
        chooserWindow.setLocationRelativeTo(null);
        chooserWindow.setVisible(true);

    }

}

class NewMouseAdapter extends MouseAdapter {
    Minesweeper game;

    NewMouseAdapter(Minesweeper p) {
        game = p;
    }

    public void mouseClicked(MouseEvent e) {
        try {
            if (e.getButton() == 1) {
                game.isLeftMouse = true;
            } else {
                game.isLeftMouse = false;
            }
            game.onClick(game.isLeftMouse, e.getX(), e.getY());
        } catch (RuntimeException exc) {
            if (!exc.getMessage().startsWith("Ignor")) {
                throw exc;
            }
        }
    }
}

class NewMouseMotionAdapter extends MouseMotionAdapter {
    Minesweeper game;

    NewMouseMotionAdapter(Minesweeper g) {
        game = g;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        try {
                game.mouseLoc = game.mouseToGridCoords(e.getX(), e.getY());
                game.repaint();
        } catch (Exception a) {
//            System.out.println(a);
        }
        super.mouseMoved(e);
    }
}



