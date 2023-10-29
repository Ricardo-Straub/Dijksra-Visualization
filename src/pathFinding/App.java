package pathFinding;

import java.awt.*;
import java.awt.event.*;
import java.io.ObjectStreamException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javax.swing.*;
import javax.swing.plaf.DimensionUIResource;

import static pathFinding.AppColors.*;

public class App implements KeyListener, MouseListener{

    private JFrame frame;
    private final int NAV_HEIGHT = 75;
    private final int FRAME_WIDTH = 600;
    private final int FRAME_HEIGHT = 675;
    private final int SQUARE_SIZE = 15;
    private final int GRID_WIDTH = FRAME_WIDTH / SQUARE_SIZE;
    private final int GRID_HEIGHT = ((FRAME_HEIGHT - NAV_HEIGHT) / SQUARE_SIZE);
    private final int NUMBER_OF_SQUARES = GRID_HEIGHT * GRID_WIDTH;
    private boolean shiftPressed = false;
    private static final Logger log = Logger.getLogger(App.class.getName());
    private boolean startPressed = false;
    private boolean endPressed = false;
    private Map<Node, Node> predecessor = new HashMap<>();
    private Map<Node, Double[]> minDistance = new HashMap<>();
    private List<Node> list = new ArrayList<>(); //--!--comparator für minDistanz--!--
    private List<List<Node>> matrix = new ArrayList<>();
    private Node startNode = null;
    private Node endNode = null;
    private final int EDGE_WEIGHT = 1; // for every edge the same
    private boolean dijkstraBoolean = false;
    private boolean endNodeSet = false;
    private boolean startNodeSet = false;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    App window = new App();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public App() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        IntStream.range(0, GRID_HEIGHT)
                .forEach(x -> matrix.add(new ArrayList<>()));
        frame = new JFrame();
        frame.setBounds(100, 100, FRAME_WIDTH, FRAME_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(0, 0));

        JPanel navBar = new JPanel(new GridBagLayout());
        navBar.setBackground(blue.getColorObject());
        navBar.setPreferredSize(new DimensionUIResource(0, NAV_HEIGHT));
        frame.getContentPane().add(navBar, BorderLayout.NORTH);

        JPanel parentPanel = new JPanel();
        parentPanel.setBackground(lightGray.getColorObject());
        parentPanel.setOpaque(true);
        frame.getContentPane().add(parentPanel, BorderLayout.CENTER);
        parentPanel.setLayout(new GridLayout(GRID_WIDTH, GRID_HEIGHT, 1, 1));
        for (int i = 0; i < NUMBER_OF_SQUARES; i++) {
            JLabel label = new JLabel();
            label.addMouseListener(this);
            parentPanel.add(label);
            label.setBackground(white.getColorObject());
            label.setOpaque(true);
            Node node = new Node(label, (double) matrix.get(i / GRID_WIDTH).size(), (double) i / GRID_WIDTH);
            matrix.get(i / GRID_WIDTH).add(node);
        }
        frame.addKeyListener(this);
        JButton runButton = new JButton("run");
        runButton.setEnabled(false);
        runButton.setFocusable(false);
        runButton.addActionListener((ActionEvent e) -> {
            initializeNodes();
            new Thread(() -> {
                try {
                    algorithm();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        });
        JButton resetButton = new JButton("reset");
        resetButton.setFocusable(false);
        resetButton.addActionListener((ActionEvent e) -> reset());
        ButtonGroup group = new ButtonGroup();
        JRadioButton dijkstraRadioButton = new JRadioButton("Dijkstra");
        JRadioButton aStarRadioButton = new JRadioButton("A*");
        JButton retryButton = new JButton("Retry");
        retryButton.setFocusable(false);

        retryButton.addActionListener((ActionEvent e) -> retry());
        dijkstraRadioButton.addActionListener((ActionEvent e) -> {
            System.out.println("dijkstra");
            runButton.setEnabled(true);
            dijkstraBoolean = true;
        });
        aStarRadioButton.addActionListener((ActionEvent e) -> {
            System.out.println("A*");
            runButton.setEnabled(true);
            dijkstraBoolean = false;
        });
        JButton createMaze = new JButton("create maze");
        createMaze.addActionListener((ActionEvent e) -> makeMaze());
        createMaze.setFocusable(false);
        navBar.add(createMaze, new GridBagConstraints());
        dijkstraRadioButton.setFocusable(false);
        aStarRadioButton.setFocusable(false);
        group.add(dijkstraRadioButton);
        group.add(aStarRadioButton);
        navBar.add(retryButton, new GridBagConstraints());
        navBar.add(resetButton, new GridBagConstraints());
        navBar.add(runButton, new GridBagConstraints());
        navBar.add(dijkstraRadioButton);
        navBar.add(aStarRadioButton);
    }

    private void initializeNodes() {
        matrix.stream()
                .flatMap(Collection::stream)
                .filter(node -> Objects.equals(node.getColor(), red.getColorObject()))
                .forEach(node -> {
                    endNode = node;
                    minDistance.put(endNode, new Double[]{Double.POSITIVE_INFINITY, 0.0,
                            Double.POSITIVE_INFINITY});
                });
        /*for (int i = 0; i < GRID_HEIGHT; i++) {
            for (int k = 0; k < GRID_WIDTH; k++) {
                if (Objects.equals(matrix.get(i).get(k).getLabel().getBackground(), red.getColorObject())) {
                    endNode = new Object[] {k, i, matrix.get(i).get(k)};
                    minDistance.put((Node) endNode[2], new Double[]{Double.POSITIVE_INFINITY, 0.0,
                            Double.POSITIVE_INFINITY});
                }
            }
        }*/
        for (int i = 0; i < GRID_HEIGHT; i++) {
            for (int k = 0; k < GRID_WIDTH; k++) {
                Node node = matrix.get(i).get(k);
                if (Objects.equals(node.getColor(), darkGray.getColorObject()))  continue;
                //North Node
                if (i - 1 >= 0 && !Objects.equals(matrix.get(i - 1).get(k).getColor(), darkGray.getColorObject())) {
                    node.setNorthNode(matrix.get(i - 1).get(k));
                }
                //west Node
                if (k - 1 >= 0 && !Objects.equals(matrix.get(i).get(k - 1).getColor(), darkGray.getColorObject())) {
                    node.setWestNode(matrix.get(i).get(k - 1));
                }
                //East Node
                if (k + 1 < GRID_WIDTH && !Objects.equals(matrix.get(i).get(k + 1).getColor(), darkGray.getColorObject())) {
                    node.setEastNode(matrix.get(i).get(k + 1));
                }
                //Sout Node
                if (i + 1 < GRID_HEIGHT && !Objects.equals(matrix.get(i + 1).get(k).getColor(), darkGray.getColorObject())) {
                    node.setSoutNode(matrix.get(i + 1).get(k));
                }
                //North-West Node
                if (k - 1 >= 0 && i - 1 >= 0 && !Objects.equals(matrix.get(i - 1).get(k - 1).getColor(), darkGray.getColorObject())) {
                    node.setNorthWestNode(matrix.get(i - 1).get(k - 1));
                }
                //North-Eeast Node
                if (k + 1 <  GRID_WIDTH && i - 1 >= 0 && !Objects.equals(matrix.get(i - 1).get(k + 1).getColor(), darkGray.getColorObject())) {
                    node.setNorthEastNode(matrix.get(i - 1).get(k + 1));
                }
                //Sout-West Node
                if (k - 1 >= 0 && i + 1 < GRID_WIDTH && !Objects.equals(matrix.get(i + 1).get(k - 1).getColor(), darkGray.getColorObject())) {
                    node.setSouthWestNode(matrix.get(i + 1).get(k - 1));
                }
                //Sout-East Node
                if (k + 1 < GRID_WIDTH && i + 1 < GRID_WIDTH && !Objects.equals(matrix.get(i + 1).get(k + 1).getColor(), darkGray.getColorObject())) {
                    node.setSouthEastNode(matrix.get(i + 1).get(k + 1));
                }
                if (Objects.equals(node.getColor(), green.getColorObject())) {
                    startNode = node;
                    minDistance.put(node, new Double[] {0.0, 0.0, 0.0});
                }
                else if(!Objects.equals(matrix.get(i).get(k).getLabel().getBackground(), red.getColorObject())) {
                    Double distanceToEndNode = Math.sqrt(Math.abs( endNode.getxKoord() - k) +
                            Math.abs(endNode.getyKoord() - i)) * 3;
                    minDistance.put(node, new Double[]{Double.POSITIVE_INFINITY, distanceToEndNode,
                            Double.POSITIVE_INFINITY});
                }
                list.add(node);
            }
        }
    }

    private void retry() {
        matrix.stream()
                .flatMap(Collection::stream)
                .forEach(x -> {
                    if (Objects.equals(x.getLabel().getBackground(), red.getColorObject())) {
                        x.setVisited(false);
                    }
                    else if (x.isVisited()) {
                        x.setVisited(false);
                        x.getLabel().setBackground(white.getColorObject());
                    }
                    x.setSoutNode(null);
                    x.setEastNode(null);
                    x.setNorthNode(null);
                    x.setNorthNode(null);
                    x.setNorthEastNode(null);
                    x.setNorthWestNode(null);
                    x.setSouthEastNode(null);
                    x.setSouthWestNode(null);
                    x.setNeighborNodes(new ArrayList<>());
                    SwingUtilities.invokeLater(() -> frame.repaint());
                });
        list.clear();
        minDistance.clear();
        predecessor.clear();
    }

    public void reset() {
        matrix.stream()
                .flatMap(Collection::stream)
                .forEach(x -> {
                    x.setVisited(false);
                    x.getLabel().setBackground(white.getColorObject());
                    x.setSoutNode(null);
                    x.setEastNode(null);
                    x.setNorthNode(null);
                    x.setNorthNode(null);
                    x.setNorthEastNode(null);
                    x.setNorthWestNode(null);
                    x.setSouthEastNode(null);
                    x.setSouthWestNode(null);
                    x.setNeighborNodes(new ArrayList<>());
                    SwingUtilities.invokeLater(() -> frame.repaint());
                });
        endNode = null;
        startNode = null;
        list.clear();
        minDistance.clear();
        predecessor.clear();
        startNodeSet = false;
        endNodeSet = false;
    }

    private void makeMaze() {
        matrix.stream()
                .flatMap(Collection::stream)
                .forEach(x -> {
                    if (Math.random() < 0.5) {
                        x.getLabel().setBackground(darkGray.getColorObject());
                    }
                });
        try {
            TimeUnit.MILLISECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> frame.repaint());
    }

    public void dijkstraSort(List<Node> list) {
        list.sort((a,b) -> (int) (minDistance.get(a)[0] - minDistance.get(b)[0]));
    }

    public void aStarSort(List<Node> list) {
        list.sort(Comparator.comparing(a -> minDistance.get(a)[2]));
    }

    public void algorithm() throws InterruptedException {
        Node node = startNode;//start Node
        predecessor.put(node, null);
        list.remove(node);
        while (!endNode.isVisited()) {
            for (int i = 0; i < node.getNeighborNodes().size(); i++) {
                Node neighborNode = node.getNeighborNodes().get(i);
                if (minDistance.get(neighborNode)[0] > minDistance.get(node)[0] + EDGE_WEIGHT) {
                    Double weight = minDistance.get(node)[0] + EDGE_WEIGHT;
                    Double distanceToEndNode = minDistance.get(neighborNode)[1];
                    minDistance.put(neighborNode, new Double[] {weight,
                            distanceToEndNode, distanceToEndNode + weight});
                    predecessor.put(neighborNode, node);
                }
            }
            if (dijkstraBoolean) {
                dijkstraSort(list);
            } else {
                aStarSort(list);
            }
            node = list.get(0);
            node.setVisited(true);
            list.remove(0);
            SwingUtilities.invokeLater(() -> frame.repaint());
            TimeUnit.MILLISECONDS.sleep(5);
        }
        while (predecessor.get(node) != null) {
            SwingUtilities.invokeLater(()->frame.repaint());
            TimeUnit.MILLISECONDS.sleep(10);
            if (!Objects.equals(node.getLabel().getBackground(), red.getColorObject())) {
                node.getLabel().setBackground(yellow.getColorObject());
            }
            node = predecessor.get(node);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Object labelObject = e.getSource();
        if (startPressed && labelObject instanceof JLabel label) {
            label.setBackground(green.getColorObject());
            startNodeSet = true;
        }
        if (endPressed && labelObject instanceof JLabel label && !endNodeSet) {
            label.setBackground(red.getColorObject());
            endNodeSet = true;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {
        Object labelObject = e.getSource();
        JLabel label = labelObject instanceof JLabel ? (JLabel) labelObject : null;
        if (label ==  null || !shiftPressed) { return; }
        if (Objects.equals(label.getBackground(), green.getColorObject())) {
            startNodeSet = false;
        }
        else if (Objects.equals(label.getBackground(), red.getColorObject())) {
            endNodeSet = false;
        }
        label.setBackground(darkGray.getColorObject());

    }

    @Override
    public void mouseExited(MouseEvent e) {
        Object labelObject = e.getSource();
        JLabel label = labelObject instanceof JLabel ? (JLabel) labelObject : null;
        if (label ==  null || !shiftPressed) { return; }
        if (Objects.equals(label.getBackground(), green.getColorObject())) {
            startNodeSet = false;
        }
        else if (Objects.equals(label.getBackground(), red.getColorObject())) {
            endNodeSet = false;
        }
        label.setBackground(darkGray.getColorObject());
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if ( code == 16) { //shift key
            shiftPressed = true;
        }
        else if (code == 83) { //"s" key
            startPressed = true;
        }
        else if (code == 69) {//"e" key
            endPressed = true;
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == 16) {
            shiftPressed = false;
        }
        else if (code == 83) {
            startPressed = false;
        }
        else if (code == 69) {
            endPressed = false;
        }

    }



}
