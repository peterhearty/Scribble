import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import drawItem.FloatAndDeltasTest;
import drawItem.FreehandTest;
import drawItem.HandleTest;
import drawItem.LineTest;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        FreehandTest.class,
        FloatAndDeltasTest.class,
        HandleTest.class,
        LineTest.class
})

public class Tester {


}
