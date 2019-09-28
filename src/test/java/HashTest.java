import cz.muni.crocs.appletstore.crypto.Hash;
import cz.muni.crocs.appletstore.crypto.LocalizedSignatureException;
import cz.muni.crocs.appletstore.crypto.SHA512;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HashTest {
    Hash hash;

    @Before
    public void prepare() {
        hash = new SHA512();
    }

    @Test
    public void testShortValueSetting() throws LocalizedSignatureException {
        //TODO more
        assertEquals("ABC string", hash.process("abc"), "ddaf35a193617aba" +
                "cc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a2192992a274fc1a8" +
                "36ba3c23a3feebbd454d4423643ce80e2a9ac94fa54ca49f");
    }

}
