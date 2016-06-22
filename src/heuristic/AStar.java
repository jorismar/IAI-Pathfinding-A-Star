package heuristic;

import grafo.Node;
import java.util.LinkedList;

public class AStar {
    private Node origin;
    private Node destiny;
    private LinkedList<AStarNode> open;
    private LinkedList<AStarNode> close;

    public AStar() {
        this.origin = null;
        this.destiny = null;
        this.open = new LinkedList<>();
        this.close =  new LinkedList<>();
    }

    public Node getOrigin() {
        return origin;
    }

    public void setOrigin(Node origin) {
        this.origin = origin;
    }

    public Node getDestiny() {
        return destiny;
    }

    public void setDestiny(Node destiny) {
        this.destiny = destiny;
    }
    
    public LinkedList<Node> findRoute(Node orig, Node dest) {
        if(orig == null || dest == null)
            return null;
        
        this.origin = orig;
        this.destiny = dest;
        
        this.open.clear();
        this.close.clear();
           
        this.open.add(new AStarNode(orig, 0, 0, orig.getDistanceTo(this.destiny)));

        return this.recursiveFind();
    }

    private LinkedList<Node> recursiveFind() {
        double bestf = -1;
        int i = 0, candidate = 0;
        
        for(AStarNode nd : this.open) {
//            if(this.close.isEmpty() || (!this.close.isEmpty() && nd.getNode().getNeighbors().contains(this.close.getLast().getNode()))) {
                if(bestf < 0) {
                    bestf = nd.getF();
                    candidate = i;
                } else if(nd.getF() < bestf) {
                    bestf = nd.getF() < bestf ? nd.getF() : bestf;
                    candidate = i;
                }
            //}
    
            i++;
        }
        
        this.close.add(this.open.remove(candidate));
        System.err.println("Added=[" + this.close.getLast().getNode().getCoordinate()[0] + "," + this.close.getLast().getNode().getCoordinate()[1] + "]");

        for(int j = 0; j < this.close.getLast().getNode().getNeighbors().size(); j++) {
            Node node = this.close.getLast().getNode().getNeighbors().get(j);
            boolean has = false;

            for(AStarNode nd : this.close)
                if(nd.getNode() == node) {
                    has = true;
                    break;
                }
            
            if(has)
                continue;
            
            // Verificar se este nó já esta na lista aberta
            for (AStarNode nd : this.open) {
                // Se este nó estiver na lista, verificar se o caminho atual ainda é melhor, caso não seja
                // remova todos os filhos do seu pai na lista fechada e adicione-o como novo filho
                if (nd.getNode() == node) {
                    if (this.close.getLast().getCoast() + node.getCostTo(this.close.getLast().getNode()) < nd.getCoast()) {
                        nd.setFatherID(this.close.size() - 1); // Define o novo pai desse no
                        nd.setCoast(nd.getNode().getCostTo(this.close.getLast().getNode())); // Custo do no para o novo pai
                    }
                    // Se estiver na lista, termine a busca
                    has = true;
                    break;
                }
            }
            
            // Se não estava na lista, adicione-o
            if(!has)
                this.open.add(new AStarNode(node, this.close.size() - 1, node.getCostTo(this.close.getLast().getNode()) + this.close.getLast().getCoast(), node.getDistanceTo(this.destiny)));
        }
        
        // Se o último adicionado é o destino, retorne
        if(this.destiny == this.close.getLast().getNode()) {
            LinkedList<Node> path = new LinkedList<>();

            this.close.stream().forEach(
                (nd) -> {
                    path.add(nd.getNode());
                }
            );
            
            return path;
        }

        // Se a lista aberta esta vazia, o caminho não existe
        if(this.open.isEmpty())
            return null;
        
        return this.recursiveFind();
    }
}
