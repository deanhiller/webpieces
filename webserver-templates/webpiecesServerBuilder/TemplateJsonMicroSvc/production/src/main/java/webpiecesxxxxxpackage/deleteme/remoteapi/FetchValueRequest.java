package webpiecesxxxxxpackage.deleteme.remoteapi;

import java.util.ArrayList;
import java.util.List;

public class FetchValueRequest {
    private String name;
    private int number;

    private MyThing thing;

    private List<MyEnum> testEnumList = new ArrayList<>();

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

    public MyThing getThing() {
        return thing;
    }

    public void setThing(MyThing thing) {
        this.thing = thing;
    }

    public List<MyEnum> getTestEnumList() {
        return testEnumList;
    }

    public void setTestEnumList(List<MyEnum> testEnumList) {
        this.testEnumList = testEnumList;
    }
}
