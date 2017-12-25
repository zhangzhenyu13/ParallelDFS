import net.jcip.annotations.Immutable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Immutable
final class PuzzleNode {
    final Position pos;
    final Move move;
    final PuzzleNode prev;

    PuzzleNode(Position pos, Move move, PuzzleNode prev) {
        this.pos = pos;
        this.move = move;
        this.prev = prev;
    }
    List<Move> asMoveList() {
        List<Move> solution = new LinkedList<>();
        for (PuzzleNode n = this; n.move != null; n = n.prev)
            solution.add(0, n.move);
        return solution;
    }
    @Override
    public String toString(){
        String s=pos.toString();

        return s;
    }
    @Override
    public boolean equals(Object obj){
        boolean tag=false;
        PuzzleNode node=(PuzzleNode)obj;
        if (node.pos.equals(pos)){
            tag= true;
        }

        //System.out.println(node+" : "+this+", test="+tag);
        return tag;
    }
}

@Immutable
class MyPuzzle implements Puzzle{
    Position initPosition,goalPosition;
    String[][] myMap=null;
    public final Move m_up=new Move(0,-1);
    public final Move m_down=new Move(0,1);
    public final Move m_left=new Move(-1,0);
    public final Move m_right=new Move(1,0);


    public MyPuzzle(String mapfile){

        try {
            BufferedReader reader=new BufferedReader(new FileReader(mapfile));
            Integer height=0,width=0;
            height=Integer.parseInt(reader.readLine());
            for(Integer i=0;i<height;i++){
                String s=reader.readLine();
                String []ss=s.split("");
                if(this.myMap==null) {
                    width=ss.length;
                    this.myMap = new String[height][width];
                }
                for(Integer j=0;j<width;j++){
                    if(ss[j].equals("P")) {
                        this.initPosition = new Position(j,i);
                        System.out.println("define found P "+initPosition);
                    }
                    if(ss[j].equals("G")) {
                        this.goalPosition = new Position(j,i);
                        System.out.println("define found G "+goalPosition);
                    }
                    this.myMap[i][j]=ss[j];
                }
            }
            System.out.println("map size:w,h="+myMap[0].length+","+myMap.length);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("MyMap");
        for(Integer i=0;i<this.myMap.length;i++){
            for(Integer j=0;j<this.myMap[0].length;j++)
                System.out.print(this.myMap[i][j]);
            System.out.println();
        }
    }
    @Override
    public Position initialPosition() {
        return initPosition;
    }

    @Override
    public boolean isGoal(Position position) {
        boolean isgoal= false;
        isgoal=position.equals(goalPosition);
        //System.out.println("test node as G? "+position+"-->"+isgoal);
        return isgoal;
    }

    @Override
    public Set<Move> legalMoves(Position position) {
        Set<Move> mv=new HashSet<>();
        Integer x,y;
        x=position.x+m_up.stepX;
        y=position.y+m_up.stepY;
        if(y>0&&myMap[y][x].equals("%")==false)
            mv.add(new Move(m_up));

        x=position.x+m_left.stepX;
        y=position.y+m_left.stepY;
        if(x>0&&myMap[y][x].equals("%")==false)
            mv.add(new Move(m_left));

        x=position.x+m_down.stepX;
        y=position.y+m_down.stepY;
        if(y<myMap.length&&myMap[y][x].equals("%")==false)
            mv.add(new Move(m_down));

        x=position.x+m_right.stepX;
        y=position.y+m_right.stepY;
        if(x<myMap[0].length&&myMap[y][x].equals("%")==false)
            mv.add(new Move(m_right));

        //System.out.println(mv.size()+" moves from node "+position);
        return mv;
    }

    @Override
    public Position move(Position position, Move move) {
        return new Position(position.x+move.stepX,position.y+move.stepY);
    }
}

public class MultiThreadPuzzleSolver {
    private final Puzzle puzzle;
    Vector<PuzzleNode> open=new Vector<>();
    Vector<PuzzleNode> close=new Vector<>();
    Boolean foundGoal=Boolean.FALSE;

    final Integer MaxThread=10;
    private Vector<subSolver> solvers=new Vector<>();

    public MultiThreadPuzzleSolver(Puzzle puzzle){
        this.puzzle=puzzle;
    }

    class subSolver extends Thread {
        PuzzleNode GoalNode = null;
        PuzzleNode node = null;

        public subSolver(PuzzleNode node) {
            this.node = node;
            open.add(node);

        }

        @Override
        public void run() {

            while (open.isEmpty() == false) {
                synchronized (foundGoal){
                    if(foundGoal)
                        break;
                }
                node = open.get(0);
                open.remove(0);
                System.out.println("search from node " + node);
                if (puzzle.isGoal(node.pos)) {
                    synchronized (foundGoal) {
                        foundGoal = Boolean.TRUE;
                        this.GoalNode = node;
                    }
                    System.out.println("found G "+node+" in thread"+this.getId());

                    break;
                }
                if (close.contains(node) && open.contains(node))
                    continue;

                Set<Move> nextMv = puzzle.legalMoves(node.pos);

                for (Move mv : nextMv) {
                    Position pos = puzzle.move(node.pos, mv);
                    //System.out.println("test add node "+pos);
                    PuzzleNode puzzleNode = new PuzzleNode(pos, mv, node);
                    if (close.contains(puzzleNode) || open.contains(puzzleNode))
                        continue;
                    System.out.println("add node" + puzzleNode);
                    open.add(0, puzzleNode);

                }
                close.add(node);
                //System.out.println("open size="+open.size()+",close size="+close.size());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public List<Move> solve() {
        Position pos = puzzle.initialPosition();
        return search(new PuzzleNode(pos, null, null));
    }

    private List<Move> search(PuzzleNode node) {

        open.add(node);
        while (open.isEmpty() == false) {
            synchronized (foundGoal){
                if(foundGoal){
                    for(subSolver solver : solvers){
                        if(solver.GoalNode!=null){
                            return solver.GoalNode.asMoveList();
                        }
                    }
                    System.out.println("Error-->FoundGoal");
                }
            }
            node = open.get(0);
            open.remove(0);
            System.out.println("search from node " + node);
            if (puzzle.isGoal(node.pos)) {
                synchronized (foundGoal) {
                    foundGoal = Boolean.TRUE;
                }
                System.out.println("found G "+node+" in main thread");
                return node.asMoveList();
            }
            if (close.contains(node) && open.contains(node))
                continue;

            Set<Move> nextMv = puzzle.legalMoves(node.pos);

            for (Move mv : nextMv) {
                Position pos = puzzle.move(node.pos, mv);
                //System.out.println("test add node "+pos);
                PuzzleNode puzzleNode = new PuzzleNode(pos, mv, node);
                if (close.contains(puzzleNode) || open.contains(puzzleNode))
                    continue;
                System.out.println("add node" + puzzleNode);
                if(solvers.size()>MaxThread){
                    open.add(puzzleNode);
                    continue;
                }
                subSolver solver=new subSolver(puzzleNode);
                solver.start();
                solvers.addElement(solver);

            }
            close.add(node);
            //System.out.println("open size="+open.size()+",close size="+close.size());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        return null;
    }
    public static void main(String[] args){
        MyPuzzle puzzle=new MyPuzzle("../data/MyMap.lay");
        MultiThreadPuzzleSolver solver=new MultiThreadPuzzleSolver(puzzle);
        List<Move> mvs=solver.solve();
        if(mvs==null)
            System.out.println("not found from from "+puzzle.initialPosition());
        else {
            for(Move mv:mvs)
                System.out.println("mv "+mv);
        }
    }


}



