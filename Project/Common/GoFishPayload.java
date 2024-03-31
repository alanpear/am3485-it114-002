package Project.Common;

import Project.Client.Hand;

public class GoFishPayload extends Payload {
    private String action;
    private Hand cardList;
    private String pointList;


    public GoFishPayload(String a, Hand c){
        action = a;
        cardList = c;
    }
    
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    

    @Override
    public String toString() {
        return String.format("Action[%s], CardList[%s], Points[%s]", 
                getAction(), getCardList());
    }
}
