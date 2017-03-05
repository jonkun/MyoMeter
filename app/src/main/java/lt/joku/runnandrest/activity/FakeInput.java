package lt.joku.runnandrest.activity;

import java.util.ArrayList;
import java.util.List;

public class FakeInput {

    public static float[] input = {
            1.5f, 2.65f, 2.7f, 2.8f, 3.0f, 3.2f, 3.5f, 4.6f, 5.3f, 6.2f, 7.8f, 8.6f, 9.8f, 10.0f
    };

    public static List<Float> getAsList() {
        List<Float> floatList = new ArrayList<>();
        for (float v: input) {
//            floatList.add(Float.valueOf(v));
            floatList.add(0f);
        }
        return floatList;
    }

}
