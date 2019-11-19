import cz.muni.crocs.appletstore.crypto.CmdTask;
import cz.muni.crocs.appletstore.crypto.LocalizedSignatureException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CmdTest {

    @Before
    public void prepare() {
    }

    @Test
    public void testShortValueSetting() throws LocalizedSignatureException {
        CmdTask task = new CmdTask().add("cd");
        System.out.println(task.processToString());

    }

}
