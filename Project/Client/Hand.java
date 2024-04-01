package Project.Client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Hand {
    private List<String> hand;

    public Hand(){
        this.hand = new ArrayList();
    }

    public Hand(ArrayList<String> S){
        this.hand = S;
    }

    public List<String> getHand(){
        return hand;
    }

    public void setHand(String cardOne, String cardTwo, String cardThree, String cardFour, String cardFive){
        hand.addAll(Arrays.asList(cardOne, cardTwo, cardThree, cardFour, cardFive));
    }

    public void addToHand(String card){
        hand.add(card);
    }

    public void addToHand(List<String> cards){
        hand.addAll(cards);
    }

    public List<String> removeCards(String requestedCard){
        List<String> removedCards = new ArrayList<String>();
        
        for(String card: hand){
         if (card.startsWith(requestedCard)) {
                        // If present, remove the card from the hand and store it in a variable
                        removedCards.add(hand.remove(hand.indexOf(requestedCard)));
                        System.out.println("Removed card: " + card);
                    }
        
        }
        return removedCards;
    }

}
