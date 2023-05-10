package webpiecesxxxxxpackage.deleteme.remoteapi;

import java.util.ArrayList;
import java.util.List;

public class FetchValueResponse {
    private int num;
    private List<TempAnswer> answers = new ArrayList();
    private List<MyEnum> enumList = new ArrayList<>();

    public FetchValueResponse() {}
    public FetchValueResponse(int num) {
        this.num = num;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public List<TempAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<TempAnswer> answers) {
        this.answers = answers;
    }

    public List<MyEnum> getEnumList() {
        return enumList;
    }

    public void setEnumList(List<MyEnum> enumList) {
        this.enumList = enumList;
    }
}
