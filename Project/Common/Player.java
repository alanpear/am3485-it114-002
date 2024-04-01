package Project.Common;

 import Project.Client.Hand;
/**
 * For chatroom projects, you can call this "User"
 */
public class Player {
    private boolean isReady;
    
    //am3485
    private Hand hand;

    public Player(){
        hand = new Hand();
    }
    //edit

    public boolean isReady() {
        return isReady;
    }
    
    public Hand getHand(){
        return this.hand;
    }
    //edit

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    private boolean takenTurn;

    public boolean didTakeTurn() {
        return takenTurn;
    }

    public void setTakenTurn(boolean takenTurn) {
        this.takenTurn = takenTurn;
    }

    private boolean isMyTurn;

    public boolean isMyTurn() {
        return isMyTurn;
    }

    public void setMyTurn(boolean isMyTurn) {
        this.isMyTurn = isMyTurn;
    }

}