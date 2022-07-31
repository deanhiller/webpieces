package webpiecesxxxxxpackage.deleteme.remoteapi;

public class FetchValueRequest {
    private String name;
    private int number;

    public FetchValueRequest() {}
    public FetchValueRequest(String name, int number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
