package Project.Common;

import Project.Client.Hand;

public class GoFishPayload extends Payload {
    private String action;
    private Hand cardList;
    private String pointList;
    private String target; 

    public GoFishPayload(String a, Hand c, String t){
        action = a;
        cardList = c;
        target = t;
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

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return String.format("Action[%s], Card List[%s], Target[%s], Points[%s]", 
                getAction(), getCardList(), getTarget(), getPointList());
    }
}
