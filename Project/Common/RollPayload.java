package Project.Common;

public class RollPayload extends Payload {
    private int numDice;
    private int numSides;
    private int results;
    
    //am3485 4/15/24
    private String rollPrompt;
    public String getRollPrompt() {
        return rollPrompt;
    }

    public void setRollPrompt(String rollPrompt) {
        this.rollPrompt = rollPrompt;
    }

    //am3485 4/1/2024
    public RollPayload() {
        setPayloadType(PayloadType.ROLL);
    }

    public int getNumDice() {
        return numDice;
    }

    public void setNumDice(int numDice) {
        this.numDice = numDice;
    }

    public int getNumSides() {
        return numSides;
    }

    public void setNumSides(int numSides) {
        this.numSides = numSides;
    }

    public int getResults() {
        return results;
    }

    public void setResults(int results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return String.format("Type[%s], Result[%s], ClientId[%s] ", getPayloadType().toString(),
                getResults(), getClientId());
    }

    //am3485 4/15/2024
    public String lolString() {
        return String.format("Type[%s], Roll Prompt[%s], Result[%s] ", getPayloadType().toString(),
                getRollPrompt(), getResults());
    }
}
