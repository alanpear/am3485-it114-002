package Project1.Common;

public class RollPayload extends Payload {
    private int numDice;
    private int numSides;
    private int results;

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
        return String.format("Type[%s], Result[%s], ClientId[%s], ", getPayloadType().toString(),
                getResults(), getClientId());
    }
}
