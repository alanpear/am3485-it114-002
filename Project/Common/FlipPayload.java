package Project.Common;

public class FlipPayload extends Payload {
    private String result;
    
    public FlipPayload(){
        setPayloadType(PayloadType.FLIP);
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return String.format("Type[%s], Result[%s], ClientId[%s], ", getPayloadType().toString(),
                getResult(), getClientId());
    }
}
//am3485 4/1/2024