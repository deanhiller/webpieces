package webpiecesxxxxxpackage.json;

public class MethodResponse {
    private int number;
    private String id;

    public MethodResponse() {}

    public MethodResponse(int number, String id) {
        this.number = number;
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
