package Project.Common;

import Project.Client.ClientPlayer;
import Project.Client.Hand;

public class GoFishPayload extends Payload {
    
    private Hand cardList;
    private String pointList;
    private String target; 
    private String requestedCard;
    private ClientPlayer match;

    public GoFishPayload(Hand c, String t){
        cardList = c;
        target = t;
    }
    
    public GoFishPayload(String t, String c, ClientPlayer p){
        target = t;
        requestedCard = c;
        match = p;
    }

    public GoFishPayload(){
    }

    public String getRequestedCard() {
        return requestedCard;
    }

    public void setRequestedCard(String requestedCard) {
        this.requestedCard = requestedCard;
    }

    public String getPointList() {
        return pointList;
    }

    public void setPointList(String pointList) {
        this.pointList = pointList;
    }

    public Hand getCardList() {
        return cardList;
    }

    public void setCardList(Hand cardList) {
        this.cardList = cardList;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(", Card List[%s], Target[%s], Points[%s]", 
                 getCardList(), getTarget(), getPointList());
    }
}
