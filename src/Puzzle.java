import net.jcip.annotations.Immutable;

import java.util.Set;

public interface Puzzle{
    Position initialPosition();
    boolean isGoal(Position position);
    Set<Move> legalMoves(Position position);
    Position move(Position position, Move move);
}

@Immutable
class Position{
    public final Integer x,y;
    public Position(Integer x, Integer y){
        this.x=x;
        this.y=y;
    }
    @Override
    public String toString(){
        return "("+x+","+y+")";
    }
    @Override
    public boolean equals(Object obj){
        Position pos=(Position)obj;
        return pos.x==this.x&&pos.y==this.y;
    }
}

@Immutable
class Move{
    public final Integer stepX,stepY;
    public Move(Integer dx, Integer dy){
        this.stepX=dx;
        this.stepY=dy;
    }
    public Move(Move m1){
        stepX=m1.stepX;
        stepY=m1.stepY;
    }
    @Override
    public String toString(){
        String direction="up";
        if(stepY<0)
            direction="up";
        if(stepY>0)
            direction="down";
        if(stepX<0)
            direction="left";
        if(stepX>0)
            direction="right";
        return direction;
    }
}