package pathFinding;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pathFinding.AppColors;

import static java.awt.Color.blue;
import static pathFinding.AppColors.green;
import static pathFinding.AppColors.red;


public class Node {

    private final Double xKoord;
    private final Double yKoord;
    private Node northNode = null;
    private Node soutNode = null;
    private Node eastNode = null;
    private Node westNode = null;
    private Node NorthWestNode = null;
    private Node NorthEastNode = null;
    private Node SouthEastNode = null;
    private Node SouthWestNode = null;
    private boolean visited = false;
    private final JLabel label;
    private List<Node> neighborNodes = new ArrayList<>();

    public void setNorthWestNode(Node northWestNode) {
        NorthWestNode = northWestNode;
        neighborNodes.add(northWestNode);
    }

    public void setNorthEastNode(Node northEastNode) {
        NorthEastNode = northEastNode;
        neighborNodes.add(northEastNode);
    }

    public void setSouthEastNode(Node southEastNode) {
        SouthEastNode = southEastNode;
        neighborNodes.add(southEastNode);
    }

    public Node getNorthWestNode() {
        return NorthWestNode;
    }

    public Node getNorthEastNode() {
        return NorthEastNode;
    }

    public Node getSouthEastNode() {
        return SouthEastNode;
    }

    public Double getxKoord() {
        return xKoord;
    }

    public Double getyKoord() {
        return yKoord;
    }

    public Node(JLabel label, double xKoord, double yKoord) {
        this.label = label;
        this.xKoord = xKoord;
        this.yKoord = yKoord;
    }

    public void setNeighborNodes(List<Node> neighborNodes) {
        this.neighborNodes = neighborNodes;
    }

    public List<Node> getNeighborNodes() {
        return neighborNodes;
    }

    public Color getColor() {
        return label.getBackground();
    }

    public void setNorthNode(Node northNode) {
        this.northNode = northNode;
        neighborNodes.add(northNode);
    }

    public void setSoutNode(Node soutNode) {
        this.soutNode = soutNode;
        neighborNodes.add(soutNode);
    }

    public void setEastNode(Node eastNode) {
        this.eastNode = eastNode;
        neighborNodes.add(eastNode);
    }

    public void setWestNode(Node westNode) {
        this.westNode = westNode;
        neighborNodes.add(westNode);
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
        if (!Objects.equals(getColor(), red.getColorObject())) {
            label.setBackground(AppColors.lightGray2.getColorObject());
        }
    }

    public Node getNorthNode() {
        return northNode;
    }

    public Node getSoutNode() {
        return soutNode;
    }

    public Node getEastNode() {
        return eastNode;
    }

    public Node getWestNode() {
        return westNode;
    }

    public boolean isVisited() {
        return visited;
    }

    public JLabel getLabel() {
        return label;
    }

    public Node getSouthWestNode() {
        return SouthWestNode;
    }

    public void setSouthWestNode(Node southWestNode) {
        SouthWestNode = southWestNode;
        neighborNodes.add(southWestNode);
    }
}
