package webpiecesxxxxxpackage.json;

public class PostTestResponse {

    private String id;
    private int number;
    private String something;

    public PostTestResponse() {}

    public PostTestResponse(String id, int number, String something) {
        this.id = id;
        this.number = number;
        this.something = something;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getSomething() {
        return something;
    }

    public void setSomething(String something) {
        this.something = something;
    }
}
