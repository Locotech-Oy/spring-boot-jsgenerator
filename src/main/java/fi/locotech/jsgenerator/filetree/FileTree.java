package fi.locotech.jsgenerator.filetree;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class FileTree {

  private Node root;


  public FileTree(String root) {
    this.root = new Node(null, root);
  }

  public void setRoot(Node root) {
    this.root = root;
  }

  public Node getRoot() {
    return root;
  }

  public boolean addPackage(Class clazz) {
    String pack = clazz.getPackage().getName();
    pack = pack.substring(this.root.getName().length());
    String[] splits = pack.split("[.]");
    int step = 0;
    Node currentNode = this.root;
    while (step < splits.length) {
      String navigate = splits[step];
      Node child = currentNode.getChild(navigate);
      if (child == null) {
        // add this as child
        child = new Node(currentNode, navigate);
        currentNode.addChild(child);
      }
      currentNode = child;
      step++;
    }
    // we are at the outer most part where we ever desire to be
    Node node = new Node(currentNode, clazz.getSimpleName(), true);
    currentNode.addChild(node);
    return true;
  }

  public boolean addStraightPackage(String kage) {
    String pack = kage;
    pack = pack.substring(this.root.getName().length());
    String[] splits = pack.split("[.]");
    int step = 0;
    Node currentNode = this.root;
    while (step < splits.length - 1) {
      String navigate = splits[step];
      Node child = currentNode.getChild(navigate);
      if (child == null) {
        // add this as child
        child = new Node(currentNode, navigate);
        currentNode.addChild(child);
      }
      currentNode = child;
      step++;
    }
    // we are at the outer most part where we ever desire to be
    Node node = new Node(currentNode, splits[splits.length - 1], true);
    currentNode.addChild(node);
    return true;
  }


  /**
   * Creates a import for the targetClazz to currentClazz.
   *
   * @param currentClazz the class that needs the import.
   * @param targetClazz the class that needs to be imported.
   * @return an import string
   */
  public String createImport(String currentClazz, String targetClazz) {
    // first we locate the currentClazz, where ever it is from the root
    Node clazzNode = findInChildren(root, currentClazz, null);

    Node targetNode = findInChildren(root, targetClazz, null);
    // so we do have the both nodes, now we need to

    if(targetNode == null)
      return null;

    if (clazzNode.getParent().getChild(targetClazz) != null) {
      // so we are in the same directory
      return "./" + targetClazz;
    }
    else if (findInChildren(clazzNode.getParent(), targetClazz, null) != null) {
      // we are on the same level or below, so this is straight
      // and we cannot be in the same level as the level is has been dealt with
      List<Node> path = buildDownwardsPath(clazzNode.getParent(), targetNode);
      String start = "./";
      for (Node n : path) {
        start += n.getName() + "/";
      }
      return start + targetClazz;
    }
    else {
      // we are fucked. we need to traverse upward, level by level and then go to target node
      Node root = clazzNode.getParent();
      String start = "./";
      Node nearestRoot = findInChildren(root, targetClazz, null);
      while(nearestRoot == null) {
        nearestRoot = findInChildren(root, targetClazz, null);
        if(nearestRoot == this.root) {

        }
        else if (nearestRoot != null && nearestRoot.isFile() && nearestRoot.getName().equals(targetClazz)) {
          // we have a lift off.
          break;
        }

        root = root.getParent();
        start += "../";
      }

      List<Node> path = buildDownwardsPath(root, targetNode);
      for (Node n : path) {
        start += n.getName() + "/";
      }
      return start + targetClazz;

    }
  }

  /**
   * Builds downward path to the end node from the start node.
   *
   * @param start node where the path should start.
   * @param end node where the path ends.
   * @return the path as linked list of nodes
   */
  private List<Node> buildDownwardsPath(Node start, Node end) {
    List<Node> path = new LinkedList<>();
    findInChildren(start, end.getName(), path);
    Collections.reverse(path);
    if(path.size() > 0)
      path = path.subList(1, path.size());
    return path;
  }

  /**
   * Gets a write path for the target node.
   *
   * @param target target node short name
   * @return path where the node should be written
   */
  public String getPathToWriteFile(String target) {
    List<Node> pathToTarget = buildDownwardsPath(root, findInChildren(root, target, null));
    String path = "/";
    for (Node node : pathToTarget) {
      if(!node.isFile())
        path += node.getName() + "/";
    }
    return path;
  }

  /**
   * Recursive mixed mode search.
   *
   * The search happens so that when we enter node, we go depth first so long that we find other node that can be
   * traversed to go deeper or we find the node we were looking for.
   *
   * Simply ordering the child nodes so that the primitive nodes would be first in the list would make this
   * effectively breadth first search. Otherwise ordering the traversable nodes to the top of list would make this
   * depth first search.
   *
   * But it has no impact on the performance as entering this is kind of rare and is not done during the actual
   * serving but rather before-hand.
   *
   * @param root node where we start the search.
   * @param name name of the node we are trying to find
   * @param path path to construct to the node from the root
   * @return returns the found node so that this can be used to create the whole import.
   */
  private Node findInChildren(Node root, String name, List<Node> path) {
    for (Node n : root.getChildren()) {
      if (n.isFile()) {
        if (n.getName().equals(name)) {
          // we found it
          if(path != null)
            path.add(root);
          return n;
        }
      }
    }

    for (Node n : root.getChildren()) {
      if (!n.isFile()) {
        Node found = findInChildren(n, name, path);
        if (found != null) {
          if(path != null)
            path.add(root);
          return found;
        }

      }
    }
    // this is no good, it should be there
    return null;
  }




}
