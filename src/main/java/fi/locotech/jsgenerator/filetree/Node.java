package fi.locotech.jsgenerator.filetree;

import java.util.LinkedList;
import java.util.List;

public class Node {
  private Node parent;
  private List<Node> children;
  private String name;
  private boolean isFile;


  public Node(Node parent, String name) {
    this(parent, name, false);
  }
  public Node(Node parent, String name, boolean isFile) {
    this.parent = parent;
    this.children = new LinkedList<>();
    this.name = name;
    this.isFile = isFile;
  }

  public boolean isFile() {
    return isFile;
  }

  public Node getParent() {
    return parent;
  }

  public void addChild(Node node) {
    this.children.add(node);
  }

  public Node getChild(String child) {
    for(Node node : this.children) {
      if(node.name.equals(child)) {
        return node;
      }
    }
    return null;
  }

  public List<Node> getChildren() {
    return children;
  }

  public String getName() {
    return name;
  }

  public boolean isRoot() {
    return parent == null;
  }
}
