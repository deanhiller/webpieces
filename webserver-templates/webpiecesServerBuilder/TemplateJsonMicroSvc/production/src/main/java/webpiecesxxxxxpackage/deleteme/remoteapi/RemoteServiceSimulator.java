package webpiecesxxxxxpackage.deleteme.remoteapi;

import org.webpieces.util.futures.XFuture;

import java.util.ArrayList;
import java.util.List;

public class RemoteServiceSimulator implements RemoteApi {
    @Override
    public XFuture<FetchValueResponse> fetchValue(FetchValueRequest request) {
        FetchValueResponse resp = new FetchValueResponse();
        resp.setNum(99);

        TempAnswer answer1 = new TempAnswer();
        answer1.setNumber(901);
        TempAnswer answer2 = new TempAnswer();
        answer2.setNumber(902);

        List<TempAnswer> list = new ArrayList<>();
        list.add(answer1);
        list.add(answer2);
        resp.setAnswers(list);

        List<MyEnum> enumList = new ArrayList<>();
        enumList.add(MyEnum.ENUMCASE);
        enumList.add(MyEnum.DEAN);
        resp.setEnumList(enumList);
        return XFuture.completedFuture(resp);
    }
}
