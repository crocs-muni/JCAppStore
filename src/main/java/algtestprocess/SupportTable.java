/*  
    Copyright (c) 2008-2016 Petr Svenda <petr@svenda.com>

     LICENSE TERMS

     The free distribution and use of this software in both source and binary
     form is allowed (with or without changes) provided that:

       1. distributions of this source code include the above copyright
          notice, this list of conditions and the following disclaimer;

       2. distributions in binary form include the above copyright
          notice, this list of conditions and the following disclaimer
          in the documentation and/or other associated materials;

       3. the copyright holder's name is not used to endorse products
          built using this software without specific written permission.

     ALTERNATIVELY, provided that this notice is retained in full, this product
     may be distributed under the terms of the GNU General Public License (GPL),
     in which case the provisions of the GPL apply INSTEAD OF those given above.

     DISCLAIMER

     This software is provided 'as is' with no explicit or implied warranties
     in respect of its properties, including, but not limited to, correctness
     and/or fitness for purpose.

    Please, report any bugs to author <petr@svenda.com>
*/
package algtestprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import static java.lang.System.out;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.*;


/**
 * Generates pefrormance similarity table, contains tooltips with differences in algorithm support.
 *
 * @author rk
 */
public class SupportTable {

    // if one card results are generated
    public static final String[] JAVA_CARD_VERSION = {"2.1.2", "2.2.1", "2.2.2"};
    public static int jcv = -1;

    // if multiple card results are generated
    private static List<String> java_card_version_array = new ArrayList<String>();
    private static String appletVersion = "";
    private static String packageAIDTestPath = "";

    private static final int JC_SUPPORT_OFFSET = 25;
    private static final int AT_APPLET_OFFSET = 23;
    private static final int PACKAGE_AID_PATH_OFFSET = 17;

    public static final String CHECKSUM_STR[] = {"javacard.security.Checksum", "ALG_ISO3309_CRC16#2.2.1", "ALG_ISO3309_CRC32#2.2.1"};

    public static final String JCSYSTEM_STR[] = {"javacard.framework.JCSystem", "JCSystem.getVersion()[Major.Minor]#&le;2.1",
            "JCSystem.isObjectDeletionSupported#2.2.0", "JCSystem.MEMORY_TYPE_PERSISTENT#2.2.1", "JCSystem.MEMORY_TYPE_TRANSIENT_RESET#2.2.1",
            "JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT#2.2.1", "JCSystem.getMaxCommitCapacity()#2.1",
            "APDU.getInBlockSize()#2.1", "APDU.getOutBlockSize()#2.1", "APDU.getProtocol()#2.1", "APDU.getNAD()#2.1"};

    public static final String BASIC_INFO[] = {"Basic info", "JavaCard support version"};

    public static final String RAWRSA_1024_STR[] = {"Variable RSA 1024 - support for variable public exponent. If supported, user-defined fast modular exponentiation can be executed on the smart card via cryptographic coprocessor. This is very specific feature and you will probably not need it",
            "Allocate RSA 1024 objects", "Set random modulus", "Set random public exponent", "Initialize cipher with public key with random exponent", "Use random public exponent"};

    public static final String PACKAGE_AID_STR[] = {"<a name=\"package_support\"></a>Package AID support test - a direct testing of supported packages from the standard JavaCard API including version. Not all constants from supported package are necessarily supported.",
            "000107A0000000620001#2.1", // java.lang
            "000107A0000000620002#2.2.0",  // java.io
            "000107A0000000620003#2.2.0", // java.rmi
            // javacard.framework
            "000107A0000000620101#2.1", "010107A0000000620101#2.2.0", "020107A0000000620101#2.2.1",
            "030107A0000000620101#2.2.2", "040107A0000000620101#3.0.1", "050107A0000000620101#3.0.4",
            "060107A0000000620101#3.0.5",
            // javacard.framework.service
            "000108A000000062010101#2.2.0",
            // javacard.security
            "000107A0000000620102#2.1", "010107A0000000620102#2.1.1", "020107A0000000620102#2.2.1",
            "030107A0000000620102#2.2.2", "040107A0000000620102#3.0.1", "050107A0000000620102#3.0.4",
            "060107A0000000620102#3.0.5",
            // javacardx.crypto
            "000107A0000000620201#2.1", "010107A0000000620201#2.1.1", "020107A0000000620201#2.2.1",
            "030107A0000000620201#2.2.2", "040107A0000000620201#3.0.1", "050107A0000000620201#3.0.4",
            "060107A0000000620201#3.0.5",
            // javacardx.biometry (starting directly from version 1.2 - previous versions all from 2.2.2)
            "000107A0000000620202#2.2.2", "010107A0000000620202#2.2.2", "020107A0000000620202#2.2.2",
            "030107A0000000620202#3.0.5",
            "000107A0000000620203#2.2.2",  // javacardx.external
            "000107A0000000620204#3.0.5",  // javacardx.biometry1toN
            "000107A0000000620205#3.0.5",  // javacardx.security
            // javacardx.framework.util
            "000108A000000062020801#2.2.2", "010108A000000062020801#3.0.5",
            "000109A00000006202080101#2.2.2",  // javacardx.framework.util.intx
            "000108A000000062020802#2.2.2",  // javacardx.framework.math
            "000108A000000062020803#2.2.2",  // javacardx.framework.tlv
            "000108A000000062020804#3.0.4",  // javacardx.framework.string
            "000107A0000000620209#2.2.2",  // javacardx.apdu
            "000108A000000062020901#3.0.5",  // javacardx.apdu.util
            // org.globalplatform
            "000106A00000015100#GP2.1.1", "010106A00000015100#GP2.2", "020106A00000015100#GP2.2",
            "030106A00000015100#GP2.2", "040106A00000015100#GP2.2", "050106A00000015100#GP2.2.1",
            "060106A00000015100#GP2.2.1",
            // org.globalplatform.contactless
            "000106A00000015102#GP 2.2.1", "010106A00000015102#GP 2.2.1", "020106A00000015102#GP 2.2.1",
            "030106A00000015102#GP 2.3", "040106A00000015102#GP 2.3", "050106A00000015102#GP 2.3",
            "060106A00000015102#GP 2.3",
            // org.globalplatform.securechannel
            "000106A00000015103#GP 2.2.1", "010106A00000015103#GP 2.2.1", "020106A00000015103#GP 2.2.1",
            "030106A00000015103#GP 2.3", "040106A00000015103#GP 2.3",
            // org.globalplatform.securechannel.provider
            "000106A00000015104#GP 2.2.1", "010106A00000015104#GP 2.2.1", "020106A00000015104#GP 2.2.1",
            // org.globalplatform.privacy
            "000106A00000015105#GP 2.2.1", "010106A00000015105#GP 2.2.1", "020106A00000015105#GP 2.2.1",
            // org.globalplatform.filesystem
            "000106A00000015106#GP 2.2.1", "010106A00000015106#GP 2.2.1", "020106A00000015106#GP 2.2.1",
            // visa.openplatform
            "000107A0000000030000#OP 2.0"

    };

    public static final String EXTENDEDAPDU_STR[] = {"javacardx.apdu.ExtendedLength", "Extended APDU#2.2.2"};

    public static final String SIGNATURE_STR[] = {"javacard.crypto.Signature",
            "ALG_DES_MAC4_NOPAD#&le;2.1", "ALG_DES_MAC8_NOPAD#&le;2.1",
            "ALG_DES_MAC4_ISO9797_M1#&le;2.1", "ALG_DES_MAC8_ISO9797_M1#&le;2.1", "ALG_DES_MAC4_ISO9797_M2#&le;2.1", "ALG_DES_MAC8_ISO9797_M2#&le;2.1",
            "ALG_DES_MAC4_PKCS5#&le;2.1", "ALG_DES_MAC8_PKCS5#&le;2.1", "ALG_RSA_SHA_ISO9796#&le;2.1", "ALG_RSA_SHA_PKCS1#&le;2.1", "ALG_RSA_MD5_PKCS1#&le;2.1",
            "ALG_RSA_RIPEMD160_ISO9796#&le;2.1", "ALG_RSA_RIPEMD160_PKCS1#&le;2.1", "ALG_DSA_SHA#&le;2.1", "ALG_RSA_SHA_RFC2409#&le;2.1",
            "ALG_RSA_MD5_RFC2409#&le;2.1", "ALG_ECDSA_SHA#2.2.0", "ALG_AES_MAC_128_NOPAD#2.2.0", "ALG_DES_MAC4_ISO9797_1_M2_ALG3#2.2.0",
            "ALG_DES_MAC8_ISO9797_1_M2_ALG3#2.2.0", "ALG_RSA_SHA_PKCS1_PSS#2.2.0", "ALG_RSA_MD5_PKCS1_PSS#2.2.0", "ALG_RSA_RIPEMD160_PKCS1_PSS#2.2.0",
            // 2.2.2
            "ALG_HMAC_SHA1#2.2.2", "ALG_HMAC_SHA_256#2.2.2", "ALG_HMAC_SHA_384#2.2.2", "ALG_HMAC_SHA_512#2.2.2", "ALG_HMAC_MD5#2.2.2", "ALG_HMAC_RIPEMD160#2.2.2",
            "ALG_RSA_SHA_ISO9796_MR#2.2.2", "ALG_RSA_RIPEMD160_ISO9796_MR#2.2.2", "ALG_SEED_MAC_NOPAD#2.2.2",
            //3.0.1
            "ALG_ECDSA_SHA_256#3.0.1", "ALG_ECDSA_SHA_384#3.0.1", "ALG_AES_MAC_192_NOPAD#3.0.1", "ALG_AES_MAC_256_NOPAD#3.0.1", "ALG_ECDSA_SHA_224#3.0.1", "ALG_ECDSA_SHA_512#3.0.1",
            "ALG_RSA_SHA_224_PKCS1#3.0.1", "ALG_RSA_SHA_256_PKCS1#3.0.1", "ALG_RSA_SHA_384_PKCS1#3.0.1", "ALG_RSA_SHA_512_PKCS1#3.0.1",
            "ALG_RSA_SHA_224_PKCS1_PSS#3.0.1", "ALG_RSA_SHA_256_PKCS1_PSS#3.0.1", "ALG_RSA_SHA_384_PKCS1_PSS#3.0.1", "ALG_RSA_SHA_512_PKCS1_PSS#3.0.1",
            //3.0.4
            "ALG_DES_MAC4_ISO9797_1_M1_ALG3#3.0.4", "ALG_DES_MAC8_ISO9797_1_M1_ALG3#3.0.4",
            //3.0.5
            "ALG_AES_CMAC_128#3.0.5"
    };

    public static final String CIPHER_STR[] = {"javacardx.crypto.Cipher",
            "ALG_DES_CBC_NOPAD#&le;2.1", "ALG_DES_CBC_ISO9797_M1#&le;2.1", "ALG_DES_CBC_ISO9797_M2#&le;2.1", "ALG_DES_CBC_PKCS5#&le;2.1",
            "ALG_DES_ECB_NOPAD#&le;2.1", "ALG_DES_ECB_ISO9797_M1#&le;2.1", "ALG_DES_ECB_ISO9797_M2#&le;2.1", "ALG_DES_ECB_PKCS5#&le;2.1",
            "ALG_RSA_ISO14888#&le;2.1", "ALG_RSA_PKCS1#&le;2.1", "ALG_RSA_ISO9796#&le;2.1",
            //2.1.1
            "ALG_RSA_NOPAD#2.1.1",
            //2.2.0
            "ALG_AES_BLOCK_128_CBC_NOPAD#2.2.0", "ALG_AES_BLOCK_128_ECB_NOPAD#2.2.0", "ALG_RSA_PKCS1_OAEP#2.2.0",
            //2.2.2
            "ALG_KOREAN_SEED_ECB_NOPAD#2.2.2", "ALG_KOREAN_SEED_CBC_NOPAD#2.2.2",
            //3.0.1
            "ALG_AES_BLOCK_192_CBC_NOPAD#3.0.1", "ALG_AES_BLOCK_192_ECB_NOPAD#3.0.1", "ALG_AES_BLOCK_256_CBC_NOPAD#3.0.1", "ALG_AES_BLOCK_256_ECB_NOPAD#3.0.1",
            "ALG_AES_CBC_ISO9797_M1#3.0.1", "ALG_AES_CBC_ISO9797_M2#3.0.1", "ALG_AES_CBC_PKCS5#3.0.1", "ALG_AES_ECB_ISO9797_M1#3.0.1", "ALG_AES_ECB_ISO9797_M2#3.0.1", "ALG_AES_ECB_PKCS5#3.0.1"
    };

    public static final String KEYAGREEMENT_STR[] = {"javacard.security.KeyAgreement",
            //2.2.1
            "ALG_EC_SVDP_DH/ALG_EC_SVDP_DH_KDF#2.2.1", "ALG_EC_SVDP_DHC/ALG_EC_SVDP_DHC_KDF#2.2.1",
            //3.0.1
            //was incorrectly like this: "ALG_EC_SVDP_DH_KDF#3.0.1", "ALG_EC_SVDP_DH_PLAIN#3.0.1", "ALG_EC_SVDP_DHC_KDF#3.0.1", "ALG_EC_SVDP_DHC_PLAIN#3.0.1",
            "ALG_EC_SVDP_DH_PLAIN#3.0.1", "ALG_EC_SVDP_DHC_PLAIN#3.0.1",
            //3.0.5
            "ALG_EC_PACE_GM#3.0.5", "ALG_EC_SVDP_DH_PLAIN_XY#3.0.5", "ALG_DH_PLAIN#3.0.5"
    };

    public static final String BIOBUILDER_STR[] = {"javacardx.biometry.BioBuilder",
            //2.2.2
            "FACIAL_FEATURE#2.2.2", "VOICE_PRINT#2.2.2", "FINGERPRINT#2.2.2", "IRIS_SCAN#2.2.2", "RETINA_SCAN#2.2.2", "HAND_GEOMETRY#2.2.2",
            "SIGNATURE#2.2.2", "KEYSTROKES#2.2.2", "LIP_MOVEMENT#2.2.2", "THERMAL_FACE#2.2.2", "THERMAL_HAND#2.2.2", "GAIT_STYLE#2.2.2",
            "BODY_ODOR#2.2.2", "DNA_SCAN#2.2.2", "EAR_GEOMETRY#2.2.2", "FINGER_GEOMETRY#2.2.2", "PALM_GEOMETRY#2.2.2", "VEIN_PATTERN#2.2.2"
            // ommit as password has constant 31 which is not continuious with previous ones "PASSWORD#2.2.2"
    };

    public static final String AEADCIPHER_STR[] = {"javacardx.crypto.AEADCipher",
            //3.0.5
            "CIPHER_AES_CCM#3.0.5", "CIPHER_AES_GCM#3.0.5", "ALG_AES_CCM#3.0.5", "ALG_AES_GCM#3.0.5"
    };

    /**
     * String array used in KeyBuilder testing for printing alg names.
     */
    public static final String KEYBUILDER_STR[] = {
            "javacard.security.KeyBuilder",
            "TYPE_DES_TRANSIENT_RESET#&le;2.1", "TYPE_DES_TRANSIENT_DESELECT#&le;2.1", "TYPE_DES LENGTH_DES#&le;2.1", "TYPE_DES LENGTH_DES3_2KEY#&le;2.1", "TYPE_DES LENGTH_DES3_3KEY#&le;2.1",
            //2.2.0
            "TYPE_AES_TRANSIENT_RESET#2.2.0", "TYPE_AES_TRANSIENT_DESELECT#2.2.0", "TYPE_AES LENGTH_AES_128#2.2.0", "TYPE_AES LENGTH_AES_192#2.2.0", "TYPE_AES LENGTH_AES_256#2.2.0",
            "TYPE_RSA_PUBLIC LENGTH_RSA_512#&le;2.1", "TYPE_RSA_PUBLIC LENGTH_RSA_736#2.2.0", "TYPE_RSA_PUBLIC LENGTH_RSA_768#2.2.0", "TYPE_RSA_PUBLIC LENGTH_RSA_896#2.2.0",
            "TYPE_RSA_PUBLIC LENGTH_RSA_1024#&le;2.1", "TYPE_RSA_PUBLIC LENGTH_RSA_1280#2.2.0", "TYPE_RSA_PUBLIC LENGTH_RSA_1536#2.2.0", "TYPE_RSA_PUBLIC LENGTH_RSA_1984#2.2.0", "TYPE_RSA_PUBLIC LENGTH_RSA_2048#&le;2.1", "TYPE_RSA_PUBLIC LENGTH_RSA_3072#never#0", "TYPE_RSA_PUBLIC LENGTH_RSA_4096#3.0.1",
            "TYPE_RSA_PRIVATE LENGTH_RSA_512#&le;2.1", "TYPE_RSA_PRIVATE LENGTH_RSA_736#2.2.0", "TYPE_RSA_PRIVATE LENGTH_RSA_768#2.2.0", "TYPE_RSA_PRIVATE LENGTH_RSA_896#2.2.0",
            "TYPE_RSA_PRIVATE LENGTH_RSA_1024#&le;2.1", "TYPE_RSA_PRIVATE LENGTH_RSA_1280#2.2.0", "TYPE_RSA_PRIVATE LENGTH_RSA_1536#2.2.0", "TYPE_RSA_PRIVATE LENGTH_RSA_1984#2.2.0", "TYPE_RSA_PRIVATE LENGTH_RSA_2048#&le;2.1", "TYPE_RSA_PRIVATE LENGTH_RSA_3072#never#0", "TYPE_RSA_PRIVATE LENGTH_RSA_4096#3.0.1",
            "TYPE_RSA_PRIVATE_TRANSIENT_RESET#3.0.1", "TYPE_RSA_PRIVATE_TRANSIENT_DESELECT#3.0.1",
            "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_512#&le;2.1", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_736#2.2.0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_768#2.2.0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_896#2.2.0",
            "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1024#&le;2.1", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1280#2.2.0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1536#2.2.0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_1984#2.2.0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_2048#&le;2.1", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_3072#never#0", "TYPE_RSA_CRT_PRIVATE LENGTH_RSA_4096#3.0.1",
            "TYPE_RSA_CRT_PRIVATE_TRANSIENT_RESET#3.0.1", "TYPE_RSA_CRT_PRIVATE_TRANSIENT_DESELECT#3.0.1",
            "TYPE_DSA_PRIVATE LENGTH_DSA_512#&le;2.1", "TYPE_DSA_PRIVATE LENGTH_DSA_768#&le;2.1", "TYPE_DSA_PRIVATE LENGTH_DSA_1024#&le;2.1", "TYPE_DSA_PRIVATE_TRANSIENT_RESET#3.0.1", "TYPE_DSA_PRIVATE_TRANSIENT_DESELECT#3.0.1",
            "TYPE_DSA_PUBLIC LENGTH_DSA_512#&le;2.1", "TYPE_DSA_PUBLIC LENGTH_DSA_768#&le;2.1", "TYPE_DSA_PUBLIC LENGTH_DSA_1024#&le;2.1",
            "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_113#2.2.0", "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_131#2.2.0", "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_163#2.2.0", "TYPE_EC_F2M_PRIVATE LENGTH_EC_F2M_193#2.2.0", "TYPE_EC_F2M_PRIVATE_TRANSIENT_RESET#3.0.1", "TYPE_EC_F2M_PRIVATE_TRANSIENT_DESELECT#3.0.1",
            "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_112#2.2.0", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_128#2.2.0", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_160#2.2.0", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_192#2.2.0", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_224#3.0.1", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_256#3.0.1", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_320#never#0", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_384#3.0.1", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_512#never#0", "TYPE_EC_FP_PRIVATE LENGTH_EC_FP_521#3.0.4", "TYPE_EC_FP_PRIVATE_TRANSIENT_RESET#3.0.1", "TYPE_EC_FP_PRIVATE_TRANSIENT_DESELECT#3.0.1",
            "TYPE_KOREAN_SEED_TRANSIENT_RESET#2.2.2", "TYPE_KOREAN_SEED_TRANSIENT_DESELECT#2.2.2", "TYPE_KOREAN_SEED LENGTH_KOREAN_SEED_128#2.2.2",
            "TYPE_HMAC_TRANSIENT_RESET#2.2.2", "TYPE_HMAC_TRANSIENT_DESELECT#2.2.2", "TYPE_HMAC LENGTH_HMAC_SHA_1_BLOCK_64#2.2.2", "TYPE_HMAC LENGTH_HMAC_SHA_256_BLOCK_64#2.2.2", "TYPE_HMAC LENGTH_HMAC_SHA_384_BLOCK_64#2.2.2", "TYPE_HMAC LENGTH_HMAC_SHA_512_BLOCK_64#2.2.2",
    };

    public static final String KEYPAIR_RSA_STR[] = {"javacard.security.KeyPair ALG_RSA on-card generation",
            "ALG_RSA LENGTH_RSA_512#2.1.1", "ALG_RSA LENGTH_RSA_736#2.2.0", "ALG_RSA LENGTH_RSA_768#2.1.1", "ALG_RSA LENGTH_RSA_896#2.2.0",
            "ALG_RSA LENGTH_RSA_1024#2.1.1", "ALG_RSA LENGTH_RSA_1280#2.2.0", "ALG_RSA LENGTH_RSA_1536#2.2.0", "ALG_RSA LENGTH_RSA_1984#2.2.0", "ALG_RSA LENGTH_RSA_2048#2.1.1",
            "ALG_RSA LENGTH_RSA_3072#never#0", "ALG_RSA LENGTH_RSA_4096#3.0.1"
    };

    public static final String KEYPAIR_RSACRT_STR[] = {"javacard.security.KeyPair ALG_RSA_CRT on-card generation",
            "ALG_RSA_CRT LENGTH_RSA_512#2.1.1", "ALG_RSA_CRT LENGTH_RSA_736#2.2.0", "ALG_RSA_CRT LENGTH_RSA_768#2.1.1", "ALG_RSA_CRT LENGTH_RSA_896#2.2.0",
            "ALG_RSA_CRT LENGTH_RSA_1024#2.1.1", "ALG_RSA_CRT LENGTH_RSA_1280#2.2.0", "ALG_RSA_CRT LENGTH_RSA_1536#2.2.0", "ALG_RSA_CRT LENGTH_RSA_1984#2.2.0", "ALG_RSA_CRT LENGTH_RSA_2048#2.1.1",
            "ALG_RSA_CRT LENGTH_RSA_3072#never#0", "ALG_RSA_CRT LENGTH_RSA_4096#3.0.1"
    };

    public static final String KEYPAIR_DSA_STR[] = {"javacard.security.KeyPair ALG_DSA on-card generation",
            "ALG_DSA LENGTH_DSA_512#2.1.1", "ALG_DSA LENGTH_DSA_768#2.1.1", "ALG_DSA LENGTH_DSA_1024#2.1.1"
    };

    public static final String KEYPAIR_EC_F2M_STR[] = {"javacard.security.KeyPair ALG_EC_F2M on-card generation",
            "ALG_EC_F2M LENGTH_EC_F2M_113#2.2.1", "ALG_EC_F2M LENGTH_EC_F2M_131#2.2.1", "ALG_EC_F2M LENGTH_EC_F2M_163#2.2.1", "ALG_EC_F2M LENGTH_EC_F2M_193#2.2.1"
    };

    public static final String KEYPAIR_EC_FP_STR[] = {"javacard.security.KeyPair ALG_EC_FP on-card generation",
//        "ALG_EC_FP LENGTH_EC_FP_112#2.2.1", "ALG_EC_FP LENGTH_EC_FP_128#2.2.1", "ALG_EC_FP LENGTH_EC_FP_160#2.2.1", "ALG_EC_FP LENGTH_EC_FP_192#2.2.1", "ALG_EC_FP LENGTH_EC_FP_224#3.0.1", "ALG_EC_FP LENGTH_EC_FP_256#3.0.1", "ALG_EC_FP LENGTH_EC_FP_320#never#0", "ALG_EC_FP LENGTH_EC_FP_384#3.0.1", "ALG_EC_FP LENGTH_EC_FP_512#never#0", "ALG_EC_FP LENGTH_EC_FP_521#3.0.4"
            "ALG_EC_FP LENGTH_EC_FP_112#2.2.1", "ALG_EC_FP LENGTH_EC_FP_128#2.2.1", "ALG_EC_FP LENGTH_EC_FP_160#2.2.1", "ALG_EC_FP LENGTH_EC_FP_192#2.2.1", "ALG_EC_FP LENGTH_EC_FP_224#3.0.1", "ALG_EC_FP LENGTH_EC_FP_256#3.0.1", "ALG_EC_FP LENGTH_EC_FP_384#3.0.1", "ALG_EC_FP LENGTH_EC_FP_521#3.0.4"
    };

    public static final String MESSAGEDIGEST_STR[] = {"javacard.security.MessageDigest",
            "ALG_SHA#&le;2.1", "ALG_MD5#&le;2.1", "ALG_RIPEMD160#&le;2.1",
            //2.2.2
            "ALG_SHA_256#2.2.2", "ALG_SHA_384#2.2.2", "ALG_SHA_512#2.2.2",
            //3.0.1
            "ALG_SHA_224#3.0.1",
            //3.0.5
            "ALG_SHA3_224#3.0.5", "ALG_SHA3_256#3.0.5", "ALG_SHA3_384#3.0.5", "ALG_SHA3_512#3.0.5"
    };

    public static final String RANDOMDATA_STR[] = {"javacard.security.RandomData",
            "ALG_PSEUDO_RANDOM#&le;2.1", "ALG_SECURE_RANDOM#&le;2.1",
            //3.0.5
            "ALG_TRNG#3.0.5", "ALG_ALG_PRESEEDED_DRBG#3.0.5", "ALG_FAST#3.0.5", "ALG_KEYGENERATION#3.0.5"
    };

    public static final String[] ALL_CLASSES_STR[] = {
            BASIC_INFO, JCSYSTEM_STR, EXTENDEDAPDU_STR, CIPHER_STR, SIGNATURE_STR, MESSAGEDIGEST_STR, RANDOMDATA_STR, KEYBUILDER_STR,
            KEYPAIR_RSA_STR, KEYPAIR_RSACRT_STR, KEYPAIR_DSA_STR, KEYPAIR_EC_F2M_STR,
            KEYPAIR_EC_FP_STR, KEYAGREEMENT_STR, CHECKSUM_STR, RAWRSA_1024_STR, PACKAGE_AID_STR
    };

    public static final Map<String, String> PACKAGE_AID_NAMES_STR;
    static {
        PACKAGE_AID_NAMES_STR = new HashMap<>();
        PACKAGE_AID_NAMES_STR.put("000107A0000000620001", "java.lang v1.0");
        PACKAGE_AID_NAMES_STR.put("000107A0000000620002", "java.io v1.0");
        PACKAGE_AID_NAMES_STR.put("000107A0000000620003", "java.rmi v1.0");
        // javacard.framework
        PACKAGE_AID_NAMES_STR.put("000107A0000000620101", "javacard.framework v1.0");
        PACKAGE_AID_NAMES_STR.put("010107A0000000620101", "javacard.framework v1.1");
        PACKAGE_AID_NAMES_STR.put("020107A0000000620101", "javacard.framework v1.2");
        PACKAGE_AID_NAMES_STR.put("030107A0000000620101", "javacard.framework v1.3");
        PACKAGE_AID_NAMES_STR.put("040107A0000000620101", "javacard.framework v1.4");
        PACKAGE_AID_NAMES_STR.put("050107A0000000620101", "javacard.framework v1.5");
        PACKAGE_AID_NAMES_STR.put("060107A0000000620101", "javacard.framework v1.6");
        // javacard.framework.service
        PACKAGE_AID_NAMES_STR.put("000108A000000062010101", "javacard.framework.service v1.0");
        // javacard.security
        PACKAGE_AID_NAMES_STR.put("000107A0000000620102", "javacard.security v1.0");
        PACKAGE_AID_NAMES_STR.put("010107A0000000620102", "javacard.security v1.1");
        PACKAGE_AID_NAMES_STR.put("020107A0000000620102", "javacard.security v1.2");
        PACKAGE_AID_NAMES_STR.put("030107A0000000620102", "javacard.security v1.3");
        PACKAGE_AID_NAMES_STR.put("040107A0000000620102", "javacard.security v1.4");
        PACKAGE_AID_NAMES_STR.put("050107A0000000620102", "javacard.security v1.5");
        PACKAGE_AID_NAMES_STR.put("060107A0000000620102", "javacard.security v1.6");
        // javacardx.crypto
        PACKAGE_AID_NAMES_STR.put("000107A0000000620201", "javacardx.crypto v1.0");
        PACKAGE_AID_NAMES_STR.put("010107A0000000620201", "javacardx.crypto v1.1");
        PACKAGE_AID_NAMES_STR.put("020107A0000000620201", "javacardx.crypto v1.2");
        PACKAGE_AID_NAMES_STR.put("030107A0000000620201", "javacardx.crypto v1.3");
        PACKAGE_AID_NAMES_STR.put("040107A0000000620201", "javacardx.crypto v1.4");
        PACKAGE_AID_NAMES_STR.put("050107A0000000620201", "javacardx.crypto v1.5");
        PACKAGE_AID_NAMES_STR.put("060107A0000000620201", "javacardx.crypto v1.6");
        // javacardx.biometry (starting directly from version 1.2 - previous versions all from 2.2.2)
        PACKAGE_AID_NAMES_STR.put("000107A0000000620202", "javacardx.biometry v1.0");
        PACKAGE_AID_NAMES_STR.put("010107A0000000620202", "javacardx.biometry v1.1");
        PACKAGE_AID_NAMES_STR.put("020107A0000000620202", "javacardx.biometry v1.2");
        PACKAGE_AID_NAMES_STR.put("030107A0000000620202", "javacardx.biometry v1.3");

        PACKAGE_AID_NAMES_STR.put("000107A0000000620203", "javacardx.external v1.0");
        PACKAGE_AID_NAMES_STR.put("000107A0000000620204", "javacardx.biometry1toN v1.0");
        PACKAGE_AID_NAMES_STR.put("000107A0000000620205", "javacardx.security v1.0");
        // javacardx.framework.util
        PACKAGE_AID_NAMES_STR.put("000108A000000062020801", "javacardx.framework.util v1.0");
        PACKAGE_AID_NAMES_STR.put("010108A000000062020801", "javacardx.framework.util v1.1");
        PACKAGE_AID_NAMES_STR.put("000109A00000006202080101", "javacardx.framework.util.intx v1.0");
        PACKAGE_AID_NAMES_STR.put("000108A000000062020802", "javacardx.framework.math v1.0");
        PACKAGE_AID_NAMES_STR.put("000108A000000062020803", "javacardx.framework.tlv v1.0");
        PACKAGE_AID_NAMES_STR.put("000108A000000062020804", "javacardx.framework.string v1.0");
        PACKAGE_AID_NAMES_STR.put("000107A0000000620209", "javacardx.apdu v1.0");
        PACKAGE_AID_NAMES_STR.put("000108A000000062020901", "javacardx.apdu.util v1.0");
        // org.globalplatform
        PACKAGE_AID_NAMES_STR.put("000106A00000015100", "org.globalplatform v1.0");
        PACKAGE_AID_NAMES_STR.put("010106A00000015100", "org.globalplatform v1.1");
        PACKAGE_AID_NAMES_STR.put("020106A00000015100", "org.globalplatform v1.2");
        PACKAGE_AID_NAMES_STR.put("030106A00000015100", "org.globalplatform v1.3");
        PACKAGE_AID_NAMES_STR.put("040106A00000015100", "org.globalplatform v1.4");
        PACKAGE_AID_NAMES_STR.put("050106A00000015100", "org.globalplatform v1.5");
        PACKAGE_AID_NAMES_STR.put("060106A00000015100", "org.globalplatform v1.6");
        // org.globalplatform.contactless
        PACKAGE_AID_NAMES_STR.put("000106A00000015102", "org.globalplatform.contactless v1.0");
        PACKAGE_AID_NAMES_STR.put("010106A00000015102", "org.globalplatform.contactless v1.1");
        PACKAGE_AID_NAMES_STR.put("020106A00000015102", "org.globalplatform.contactless v1.2");
        PACKAGE_AID_NAMES_STR.put("030106A00000015102", "org.globalplatform.contactless v1.3");
        PACKAGE_AID_NAMES_STR.put("040106A00000015102", "org.globalplatform.contactless v1.4");
        // org.globalplatform.securechannel
        PACKAGE_AID_NAMES_STR.put("000106A00000015103", "org.globalplatform.securechannel v1.0");
        PACKAGE_AID_NAMES_STR.put("010106A00000015103", "org.globalplatform.securechannel v1.1");
        PACKAGE_AID_NAMES_STR.put("020106A00000015103", "org.globalplatform.securechannel v1.2");
        // org.globalplatform.securechannel.provider
        PACKAGE_AID_NAMES_STR.put("000106A00000015104", "org.globalplatform.securechannel.provider v1.0");
        PACKAGE_AID_NAMES_STR.put("010106A00000015104", "org.globalplatform.securechannel.provider v1.1");
        PACKAGE_AID_NAMES_STR.put("020106A00000015104", "org.globalplatform.securechannel.provider v1.2");
        // org.globalplatform.privacy
        PACKAGE_AID_NAMES_STR.put("000106A00000015105", "org.globalplatform.privacy v1.0");
        PACKAGE_AID_NAMES_STR.put("010106A00000015105", "org.globalplatform.privacy v1.1");
        PACKAGE_AID_NAMES_STR.put("020106A00000015105", "org.globalplatform.privacy v1.2");
        // org.globalplatform.filesystem
        PACKAGE_AID_NAMES_STR.put("000106A00000015106", "org.globalplatform.filesystem v1.0");
        PACKAGE_AID_NAMES_STR.put("010106A00000015106", "org.globalplatform.filesystem v1.1");
        PACKAGE_AID_NAMES_STR.put("020106A00000015106", "org.globalplatform.filesystem v1.2");
        // visa.openplatform
        PACKAGE_AID_NAMES_STR.put("000107A0000000030000", "visa.openplatform v1.0");
        PACKAGE_AID_NAMES_STR.put("010107A0000000030000", "visa.openplatform v1.1");

    }

    /**
     * Method takes HTML file with two smart card algorithm support results and marks differences between them.
     *
     * @param basePath Path to folder with HTML file which must be named 'AlgTest_html_table.html'.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void compareSupportedAlgs(String basePath) throws FileNotFoundException, IOException {
        /* String containing input file path. */
        String inputFileName = basePath + "AlgTest_html_table.html";
        /* String containing output file path. */
        String outputFileName = basePath + "AlgTest_html_table_comparison.html";
        /* String containing line to search for in HTML file. */
        String lineToSearch = "<tr>";
        /* String containing style information for not matching algorithms in HTML file. */
        String styleInfo = "<tr style='outline: solid'>";

        /* String array for loaded file. */
        ArrayList<String> loadedFile = new ArrayList<>();

        /* Creating object of FileReader. */
        FileReader inputFile = new FileReader(inputFileName);
        BufferedReader reader = new BufferedReader(inputFile);

        String line = null;     // buffer for input file
        /* Loading file to ArrayList object. */
        while ((line = reader.readLine()) != null) {    // read if there is another line to read
            loadedFile.add(line);
        }
        /* Searching for algs in loaded file. */
        for (int i = 0; i < loadedFile.size(); i++) {
            if (loadedFile.get(i).contains(lineToSearch)) {  // checking if line[i] is HTML row definition
                if (!loadedFile.get(i + 3).contains(">c")) {  // so the program doesn't check algorithm's class names
                    String aux = loadedFile.get(i + 3).substring(loadedFile.get(i + 3).indexOf(">") + 1);   // getting first occurence of '>' char and rest of the string behinf him
                    if (!loadedFile.get(i + 4).contains(aux)) {  // checking if next algorithm support is the same
                        loadedFile.set(i, styleInfo);           // setting new string to ArrayList (with border)
                    }
                }
            }
        }

        FileOutputStream output = new FileOutputStream(outputFileName);
        /* Writing to output file. */
        for (int i = 0; i < loadedFile.size(); i++) {
            String aux = loadedFile.get(i);
            aux = aux + "\r\n";     // adding end of line to every line written to HTML file
            output.write(aux.getBytes());
            output.flush();
        }
        output.close();
    }


    public static void generateHTMLTable(String basePath) throws IOException {
        String filesPath = basePath + "results" + File.separator;
        File dir = new File(filesPath);
        String[] filesArray = dir.list();

        if ((filesArray != null) && (dir.isDirectory() == true)) {

            HashMap filesSupport[] = new HashMap[filesArray.length];

            for (int i = 0; i < filesArray.length; i++) {
                filesSupport[i] = new HashMap();
                parseSupportFile(filesPath + filesArray[i], filesSupport[i]);
            }

            //
            // HTML HEADER
            //
            String fileName = basePath + "AlgTest_html_table.html";
            FileOutputStream file = new FileOutputStream(fileName);
            String header = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\r\n<html>\r\n<head>"
                    + "<meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\">\r\n"
                    + "<link type=\"text/css\" href=\"style.css\" rel=\"stylesheet\">\r\n"
                    + "<script class=\"jsbin\" src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js\"></script>\r\n"
                    + "<title>JavaCard support test</title>\r\n"
                    + "<script>$(function(){ $(\"#tab td\").hover(function(){$(\"#tab col\").eq($(this).index()).css({\"border\":\" 2px solid #74828F\"});$(this).closest(\"tr\").css({\"border\":\" 2px solid #74828F\"});},function(){$(\"#tab col\").eq($(this).index()).css({\"border\":\" 0px\"}); $(this).closest(\"tr\").css({\"border\":\" 0px\"});});});</script>\r\n"
                    + "</head>\r\n"
                    + "<body style=\\\"margin-top:50px; padding:20px\\\">\\n\\n\";\r\n\r\n";

            String cardList = "<div class=\"container-fluid\">\n<h3 id=\"LIST\">Tested cards abbreviations</h3>\r\n";

            HashMap<String, Integer> authors = new HashMap<>();
            String shortNamesList[] = new String[filesArray.length];
            for (int i = 0; i < filesArray.length; i++) {
                String cardIdentification = filesArray[i];
                cardIdentification = cardIdentification.replace('_', ' ');
                cardIdentification = cardIdentification.replace(".csv", "");
                cardIdentification = cardIdentification.replace("3B", ", ATR=3B");
                cardIdentification = cardIdentification.replace("3b", ", ATR=3b");
                cardIdentification = cardIdentification.replace("ALGSUPPORT", "");

                String cardShortName = cardIdentification.substring(0, cardIdentification.indexOf("ATR"));
                // Get rid of '   , ' at the end of card name
                cardShortName = cardShortName.trim();
                if (cardShortName.charAt(cardShortName.length() - 1) == ',') {
                    cardShortName = cardShortName.substring(0, cardShortName.length() - 1);
                }
                cardShortName = cardShortName.trim();

                shortNamesList[i] = cardShortName;

                // Extract providing person name
                String PROVIDED_BY = "provided by ";
                String AND = " and ";
                int startAuthorsOffset = 0;
                if ((startAuthorsOffset = cardIdentification.indexOf(PROVIDED_BY)) > -1) {
                    startAuthorsOffset += PROVIDED_BY.length();

                    if (cardIdentification.indexOf(" and ", startAuthorsOffset) > -1) {
                        // Two authors, extract first one
                        String authorName = cardIdentification.substring(startAuthorsOffset, cardIdentification.indexOf(AND, startAuthorsOffset));
                        if (authors.containsKey(authorName)) {
                            authors.replace(authorName, authors.get(authorName) + 1);
                        } else {
                            authors.put(authorName, 1);
                        }
                        startAuthorsOffset = cardIdentification.indexOf(AND, startAuthorsOffset) + AND.length();
                    }
                    String authorName = cardIdentification.substring(startAuthorsOffset, cardIdentification.indexOf(")", startAuthorsOffset));
                    if (authors.containsKey(authorName)) {
                        authors.replace(authorName, authors.get(authorName) + 1);
                    } else {
                        authors.put(authorName, 1);
                    }
                }

                String cardRestName = cardIdentification.substring(cardIdentification.indexOf("ATR"));
                cardList += "<b>c" + i + "</b>	" + "<a href=\"https://github.com/crocs-muni/JCAlgTest/tree/master/Profiles/results/" + filesArray[i] + "\">" + cardShortName + "</a> , " + cardRestName + ",";

                String cardName = "";
                if (filesSupport[i].containsKey("Performance")) {
                    cardName = (String) filesSupport[i].get("Card name");
                    cardName = cardName.replace(" ", "");
                    cardName = cardName.replace("_", "");
                    cardList += "&nbsp;<a target=\"_blank\" href=\"run_time/" + cardName + ".html\">Performance</a>,&nbsp;";
                    cardList += "<a target=\"_blank\" href=\"scalability/" + cardName + ".html\">Graphs</a>";
                }
                cardList += "<br>\r\n";
            }
            cardList += "<br>\r\n";

            file.write(header.getBytes());
            file.write(cardList.getBytes());
            file.flush();

            String note = "Note: Some cards in the table come without full identification and ATR (\'undisclosed\') as submitters prefered not to disclose it at the momment. I'm publishing it anyway as the information that some card supporting particular algorithm exists is still interesting. Full identification might be added in future.<br><br>\r\n\r\n";
            file.write(note.getBytes());

            note = "Note: If you have card of unknown type, try to obtain ATR and take a look at smartcard list available here: <a href=\"http://smartcard-atr.appspot.com/\"> http://smartcard-atr.appspot.com/</a><br><br>\r\n\r\n";
            file.write(note.getBytes());

            // Create bat script to copy files with results to corresponding folders for the same card type
            for (int i = 0; i < shortNamesList.length; i++) {
                String justName = shortNamesList[i];
                if (justName.indexOf("ICFabDate") != -1) {
                    justName = justName.substring(0, justName.indexOf("ICFabDate"));
                    justName = justName.trim();
                }
                System.out.println("mkdir \"" + justName + "\"");
                System.out.println("copy ..\\results\\\"" + filesArray[i] + "\" \"" + justName + "\"");
            }
            System.out.println();

            // Print all providing people names found
            for (String authorName : authors.keySet()) {
                System.out.print(authorName + " (" + authors.get(authorName) + "x), ");
/*                
                if (authors.get(authorName) > 1) {
                    System.out.print(authorName + " (" + authors.get(authorName) + " cards), ");
                }
                else {
                    System.out.print(authorName + " (" + authors.get(authorName) + " card), ");
                }
*/
            }


            String explain = "<table id=\"explanation\" min-width=\"1000\" border=\"0\" cellspacing=\"2\" cellpadding=\"4\" >\r\n"
                    + "<tr>\r\n"
                    + "  <td class='dark_index' style=\"min-width:100px\">Symbol</td>\r\n"
                    + "  <td class='dark_index'>Meaning</td>\r\n"
                    + "</tr>\r\n"
                    + "<tr>\r\n"
                    + "  <td class='light_yes'>yes</td>\r\n"
                    + "  <td class='light_info_left'>This particular algorithm was tested and IS supported by given card.</td>\r\n"
                    + "</tr>\r\n"
                    + "<tr>\r\n"
                    + "  <td class='light_no'>no</td>\r\n"
                    + "  <td class='light_info_left'>This particular algorithm was tested and is NOT supported by given card.</td>\r\n"
                    + "</tr>\r\n"
                    + "<tr>\r\n"
                    + "  <td class='light_suspicious'>possibly yes</td>\r\n"
                    + "  <td class='light_info_left'>This particular algorithm was tested and is REPORTED as supported by given card. However, given algorithm was introduced in later version of JavaCard specification than version declared by the card as supported one. Mostly, algorithm is really supported. But it might be possible, that given algorithm is NOT actually supported by card as some cards may create object for requested algorithm and fail only later when object is actually used. Future version of the JCAlgTest will make more thorough tests regarding this behaviour.</td>\r\n"
                    + "</tr>\r\n"
                    + "<tr>\r\n"
                    + "  <td class='light_info'>error(ERROR_CODE)</td>\r\n"
                    + "  <td class='light_info_left'>Card returned specific error other then raising CryptoException.NO_SUCH_ALGORITHM. Most probably, algorithm is NOT supported by given card.</td>\r\n"
                    + "</tr>\r\n"
                    + "<tr>\r\n"
                    + "  <td class='light_info'>?</td>\r\n"
                    + "  <td class='light_info_left'>Card returned unspecific error. Most probably, algorithm is NOT supported by given card.</td>\r\n"
                    + "</tr>\r\n"
                    + "<tr>\r\n"
                    + "  <td class='light_maybe'>-</td>\r\n"
                    + "  <td class='light_info_left'>This particular algorithm was NOT tested. Usually, this equals to unsupported algorithm. Typical example is the addition of new constants introduced by the newer version of JavaCard standard, which are not supported by cards tested before apperance of of new version of specification. The exceptions to this rule are classes that have to be tested manually (at the moment, following information: JavaCard support version, javacardx.apdu.ExtendedLength Extended APDU) where not tested doesn't automatically means not supported. Automated upload and testing of these features will solve this in future.</td>\r\n"
                    + "</tr>\r\n"
                    + "</table>\r\n"
                    + "<br><br>\r\n";
            file.write(explain.getBytes());


            //Checkboxes to show/hide columns in table, JavaScript required 
            String checkboxes = "<h4>Click on each checkbox to show/hide corresponding column (card)</h4>\n\t<div class=\"row\" id=\"grpChkBox\">\n";
            for (int i = 0; i < filesArray.length; i++) {
                String cardIdentification = filesArray[i];
                cardIdentification = cardIdentification.replace('_', ' ');
                cardIdentification = cardIdentification.replace(".csv", "");
                cardIdentification = cardIdentification.replace("3B", ", ATR=3B");
                cardIdentification = cardIdentification.replace("3b", ", ATR=3b");
                cardIdentification = cardIdentification.replace("ALGSUPPORT", "");
                String cardShortName = cardIdentification.substring(0, cardIdentification.indexOf(",") - 1);

                if (i % (filesArray.length / 3 + 1) == 0)
                    checkboxes += "<div class=\"col-lg-4 .col-sm-4\">\n";

                checkboxes += "\t\t<p style=\"margin:0;\"><input type=\"checkbox\" name=\"" + i + "\" /> <b>c" + i + "</b> - " + cardShortName + "</p>\n";

                getShortCardName(filesArray[i]);
                if (i % (filesArray.length / 3 + 1) == filesArray.length / 3)
                    checkboxes += "\t</div>\n";
            }
            checkboxes += "\t<br>\n\t</div>\n</div>\n";
            checkboxes += "<input type=\"button\" class=\"btn btn-default\" id=\"checkAll\" onclick=\"checkAll('grpChkBox')\" value=\"Select all\">\n";
            checkboxes += "<input type=\"button\" class=\"btn btn-default\" id=\"uncheckAll\" onclick=\"uncheckAll('grpChkBox')\" value=\"Deselect all\">\n";
            checkboxes += "\n</br></br>\n\n";
            file.write(checkboxes.getBytes());


            String table = "<table id=\"tab\" width=\"600px\" border=\"0\" cellspacing=\"2\" cellpadding=\"4\">\r\n";
            // Insert helper column identification for mouseover row & column jquery highlight
            table += "<colgroup>";
            for (int i = 0; i < filesArray.length + 2; i++) {
                table += "<col />";
            } // + 2 because of column with algorithm name and introducing version
            table += "</colgroup>\r\n";

            file.write(table.getBytes());

            //
            // HTML TABLE HEAD
            //
            file.write("<thead>".getBytes());
            formatTableAlgorithm_HTML(filesArray, ALL_CLASSES_STR[0], filesSupport, file);
            file.write("</thead>".getBytes());

            //
            // HTML TABLE BODY
            //
            file.write("<tbody>".getBytes());
            for (int i = 1; i < ALL_CLASSES_STR.length; i++)
                formatTableAlgorithm_HTML(filesArray, ALL_CLASSES_STR[i], filesSupport, file);

            file.write("</tbody>".getBytes());

            //
            // FOOTER
            //
            String footer = "</table>\r\n</div>\r\n\r\n";
            footer += "<script type=\"text/javascript\" src=\"footer.js\"></script>\n";
            footer += "<a href=\"#\" class=\"back-to-top\"></a>\n";
            footer += "\r\n</body>\n</html>";
            file.write(footer.getBytes());

            file.flush();
            file.close();
        } else {
            System.out.println("directory '" + filesPath + "' is empty");
        }
    }

    static String[] parseCardName(String fileName) {
        String[] names = new String[2];

        String shortCardName = "";
        String cardName = fileName;

        cardName = cardName.replace("_ALGSUPPORT_", "");

        int atrIndex = -1;
        int index = -1;
        if ((index = cardName.indexOf("_3B ")) != -1) {
            atrIndex = index;
        }
        if ((index = cardName.indexOf("_3b ")) != -1) {
            atrIndex = index;
        }
        if ((index = cardName.indexOf("_3b_")) != -1) {
            atrIndex = index;
        }
        if ((index = cardName.indexOf("_3B_")) != -1) {
            atrIndex = index;
        }
        if (atrIndex < 0) {
            shortCardName = cardName.substring(0, atrIndex);
        }
        shortCardName = cardName.substring(0, atrIndex);

        shortCardName = shortCardName.replace('_', ' ');
        cardName = cardName.replace('_', ' ');
        if (cardName.indexOf("(provided") != -1) cardName = cardName.substring(0, cardName.indexOf("(provided"));

        names[0] = cardName;
        names[1] = shortCardName;
        return names;
    }

    static String getShortCardName(String fileName) {
        String[] names = parseCardName(fileName);
        return names[1];
    }

    static String getLongCardName(String fileName) {
        String[] names = parseCardName(fileName);
        return names[0];
    }

    static void formatTableAlgorithm_HTML(String[] filesArray, String[] classInfo, HashMap[] filesSupport, FileOutputStream file) throws IOException {
        // class (e.g., javacardx.crypto.Cipher)
        String algorithm = "<tr>\r\n" + "<td class='dark'>" + classInfo[0] + "</td>\r\n";
        algorithm += "  <td class='dark'>introduced in JC ver.</td>\r\n";
        boolean bPackageAIDSupport = false; // detect specific subsection with AID support
        if (classInfo[0].equalsIgnoreCase("Basic info")) {
            for (int i = 0; i < filesSupport.length; i++) {
                algorithm += "  <th class='dark_index " + i + "' title = '" + getLongCardName(filesArray[i]) + "'>c" + i + "</th>\r\n";
            }
        } else {
            if (classInfo[0].contains("Package AID support test")) {
                bPackageAIDSupport = true;
            }
            for (int i = 0; i < filesSupport.length; i++) {
                algorithm += "  <td class='dark_index' title = '" + getLongCardName(filesArray[i]) + "'>c" + i + "</td>\r\n";
            }
        }

        String[] jcvArray = java_card_version_array.toArray(new String[java_card_version_array.size()]);
        algorithm += "</tr>\r\n";
        // support for particular algorithm from given class
        for (int i = 0; i < classInfo.length; i++) {
            if (!classInfo[i].startsWith("@@@")) { // ignore special informative types
                String algorithmName = "";
                String fullAlgorithmName = "";
                String algorithmVersion = "";

                if (appletVersion != "") {
                    algorithmName = "AlgTest applet version";
                    algorithmVersion = appletVersion;
                    fullAlgorithmName = algorithmName;
                } else {
                    // Parse algorithm name and version of JC which introduced it
                    if (i == 0) {
                        continue;
                    }
                    algorithmName = Utils.GetAlgorithmName(classInfo[i]);
                    if (bPackageAIDSupport) {
                        fullAlgorithmName = String.format("%s (%s)", PACKAGE_AID_NAMES_STR.get(algorithmName), algorithmName);
                    } else {
                        fullAlgorithmName = algorithmName;
                    }
                    algorithmVersion = Utils.GetAlgorithmIntroductionVersion(classInfo[i]);
                    if (!Utils.ShouldBeIncludedInOutput(classInfo[i]))
                        continue; // ignore types with ignore flag set (algorith#version#include 1/0)
                }

                algorithm += "<tr>\r\n";
                // Add algorithm name
                algorithm += "  <td class='light'>" + fullAlgorithmName + "</td>\r\n";
                // Add version of JavaCard standard that introduced given algorithm
                if (algorithmVersion == appletVersion) {
                    algorithm += "  <td class='light_error'>" + "</td>\r\n";
                    appletVersion = "";
                } else {
                    algorithm += "  <td class='light_error'>" + algorithmVersion + "</td>\r\n";
                }

                // Process all files
                for (int fileIndex = 0; fileIndex < filesSupport.length; fileIndex++) {
                    algorithm += "  ";
                    HashMap fileSuppMap = filesSupport[fileIndex];
                    if (fileSuppMap.containsKey(algorithmName)) {
                        String secondToken = (String) fileSuppMap.get(algorithmName);
                        String title = "title='" + getShortCardName(filesArray[fileIndex]) + " : " + fullAlgorithmName + " : " + secondToken + "'";
                        switch (secondToken) {
                            case "no":
                                algorithm += "<td class='light_no' " + title + ">no</td>\r\n";
                                break;
                            case "yes":
                                if (java_card_version_array.size() > 0) {
                                    if (algorithmVersion.compareTo(jcvArray[fileIndex]) == 1) {
                                        // given algorithm is not present in JavaCard specification used to convert uploaded JCAlgTest applet
                                        // make warning
                                        algorithm += "<td class='light_suspicious' " + title + ">possibly yes</td>\r\n";
                                    } else {
                                        if (jcvArray[fileIndex].compareTo("not supplied") == 0) {
                                            // version of JavaCard API information was not supplied, assuming valid response
                                        }
                                        algorithm += "<td class='light_yes' " + title + ">yes</td>\r\n";
                                    }
                                } else {
                                    algorithm += "<td class='light_yes' " + title + ">yes</td>\r\n";
                                }
                                break;
                            case "error":
                                algorithm += "<td class='light_error' " + title + ">error</td>\r\n";
                                break;
                            case "maybe":
                                algorithm += "<td class='light_error' " + title + ">maybe</td>\r\n";
                                break;
                            default: {
                                algorithm += "<td class='light_info' " + title + ">" + secondToken + "</td>\r\n";
                            }
                        }
                    } else {
                        // algorithm not found in support list
                        algorithm += "<td class='light_maybe'>-</td>\r\n";
                        //algorithm += "<td >&nbsp;</td>\r\n";
                    }
                }
                algorithm += "</tr>\r\n";
            }
        }
        file.write(algorithm.getBytes());
    }

    static void parseAIDSupportFile(String filePath, HashMap suppMap) throws IOException {
        try {
            //create BufferedReader to read csv file
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String strLine;
            int tokenNumber = 0;
            boolean bSupportStartReached = false;

            //read comma separated file line by line
            while ((strLine = br.readLine()) != null) {
                if (strLine.contains("FULL PACKAGE AID;")) {
                    bSupportStartReached = true;
                    continue;
                }

                if (bSupportStartReached) { // parse all lines till the end of file
                    //break comma separated line using ";"
                    StringTokenizer st = new StringTokenizer(strLine, ";,");

                    String firstToken = "";
                    String secondToken = "";
                    while (st.hasMoreTokens()) {
                        tokenNumber++;
                        String tokenValue = st.nextToken();
                        tokenValue = tokenValue.trim();
                        if (tokenNumber == 1) {
                            firstToken = tokenValue;
                        }
                        if (tokenNumber == 2) {
                            secondToken = tokenValue;
                        }
                    }
                    if (!firstToken.isEmpty()) {
                        suppMap.put(firstToken, secondToken);
                    }

                    //reset token number
                    tokenNumber = 0;
                }
            }
        } catch (Exception e) {
            System.out.println("Exception while reading csv file: " + e);
        }
    }

    static void parseSupportFile(String filePath, HashMap suppMap) throws IOException {
        try {
            //create BufferedReader to read csv file
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String strLine;
            int lineNumber = 0;
            int tokenNumber = 0;
            boolean bJCSupportVersionPresent = false;

            //read comma separated file line by line
            while ((strLine = br.readLine()) != null) {
                // in case valid JavaCard support version is present
                if (strLine.contains("JavaCard support version")) {
                    String jcSupportVersion = (String) strLine.subSequence(JC_SUPPORT_OFFSET, strLine.length());
                    jcSupportVersion = jcSupportVersion.replace(";", "");
                    if (jcSupportVersion.length() > 0) {
                        java_card_version_array.add(jcSupportVersion);
                        bJCSupportVersionPresent = true;
                    }
                }
                if (strLine.contains("AlgTest applet version")) {
                    appletVersion = strLine.substring(AT_APPLET_OFFSET, strLine.length() - 1);
                }
                if (strLine.contains("Package_AID_test")) {
                    packageAIDTestPath = strLine.substring(PACKAGE_AID_PATH_OFFSET, strLine.length());
                    packageAIDTestPath = packageAIDTestPath.trim();

                    if (!packageAIDTestPath.isEmpty()) {
                        // Open target path and load additional info from here
                        int lastPos = filePath.lastIndexOf('\\');
                        if (lastPos == -1) {
                            lastPos = filePath.lastIndexOf('/');
                        }
                        if (lastPos != -1) {
                            String basePath = filePath.substring(0, lastPos);
                            String aidSupportFilePath = String.format("%s/../aid/%s", basePath, packageAIDTestPath);
                            parseAIDSupportFile(aidSupportFilePath, suppMap);
                        }
                    }
                }
                lineNumber++;

                //break comma separated line using ";"
                StringTokenizer st = new StringTokenizer(strLine, ";,");

                String firstToken = "";
                String secondToken = "";
                while (st.hasMoreTokens()) {
                    tokenNumber++;
                    String tokenValue = st.nextToken();
                    tokenValue = tokenValue.trim();
                    if (tokenNumber == 1) {
                        firstToken = tokenValue;
                    }
                    if (tokenNumber == 2) {
                        secondToken = tokenValue;
                    }
                }
                if (!firstToken.isEmpty()) {
                    suppMap.put(firstToken, secondToken);
                }

                //reset token number
                tokenNumber = 0;
            }

            if (!bJCSupportVersionPresent) {
                System.out.println("PROBLEM: " + filePath + " does not have 'JavaCard support version' inserted!");
                java_card_version_array.add("not supplied");
            }
        } catch (Exception e) {
            System.out.println("Exception while reading csv file: " + e);
        }
    }

    static void generateGPShellScripts() throws IOException {
        String capFileName = "AlgTest_v1.3_";
        String packageAID = "6D797061636B616731";
        String appletAID = "6D7970616330303031";

        // NXP JCOP CJ3A081
        CardProfiles.generateScript(capFileName + "jc2.2.2.cap", packageAID, appletAID, "NXP_JCOP_CJ3A081", "mode_211", "a000000003000000", "-keyind 0 -keyver 0 -mac_key 404142434445464748494a4b4c4d4e4f -enc_key 404142434445464748494a4b4c4d4e4f");
        // NXP JCOP CJ2A081
        CardProfiles.generateScript(capFileName + "jc2.2.2.cap", packageAID, appletAID, "NXP_JCOP_CJ2A081", "mode_211", "a000000003000000", "-keyind 0 -keyver 0 -mac_key 404142434445464748494a4b4c4d4e4f -enc_key 404142434445464748494a4b4c4d4e4f");
        // NXP JCOP 41 v2.2.1 72K
        CardProfiles.generateScript(capFileName + "jc2.2.1.cap", packageAID, appletAID, "NXP_JCOP_41_v221_72K", "mode_211", "a000000003000000", "-keyind 0 -keyver 0 -mac_key 404142434445464748494a4b4c4d4e4f -enc_key 404142434445464748494a4b4c4d4e4f");
        // NXP JCOP CJ3A080
        CardProfiles.generateScript(capFileName + "jc2.2.1.cap", packageAID, appletAID, "NXP_JCOP_CJ3A080", "mode_211", "a000000003000000", "-keyind 0 -keyver 0 -mac_key 404142434445464748494a4b4c4d4e4f -enc_key 404142434445464748494a4b4c4d4e4f");

        // Gemalto_TOP_IM_GXP4
        CardProfiles.generateScript(capFileName + "jc2.2.1.cap", packageAID, appletAID, "Gemalto_TOP_IM_GXP4", "mode_201\r\ngemXpressoPro", "A000000018434D00", "-keyind 0 -keyver 0 -key 47454d5850524553534f53414d504c45");
        // Gemalto_GXP_E64_PK
        CardProfiles.generateScript(capFileName + "jc2.1.2.cap", packageAID, appletAID, "Gemalto_GXP_E64_PK", "mode_201", "A000000018434D00", "-keyind 0 -keyver 0 -mac_key 404142434445464748494a4b4c4d4e4f -enc_key 404142434445464748494a4b4c4d4e4f");
        // Gemalto_GXP_R4_72K
        CardProfiles.generateScript(capFileName + "jc2.2.1.cap", packageAID, appletAID, "Gemalto_GXP_R4_72K", "mode201\r\ngemXpressoPro\n", "A000000018434D00\n", "-keyind 0 -keyver 0 -key 47454d5850524553534f53414d504c45\n");
        // Gemalto_GXP_E32_PK
        CardProfiles.generateScript(capFileName + "jc2.1.2.cap", packageAID, appletAID, "Gemalto_GXP_E32_PK", "mode_201\r\ngemXpressoPro", "A000000018434D00\n", "-keyind 0 -keyver 0 -key 47454d5850524553534f53414d504c45\n");

        // Oberthur Cosmo Dual 72K
        CardProfiles.generateScript(capFileName + "jc2.1.2.cap", packageAID, appletAID, "Oberthur_Cosmo_Dual_72K", "mode_211", "a000000003000000", "-keyind 0 -keyver 0 -mac_key 404142434445464748494a4b4c4d4e4f -enc_key 404142434445464748494a4b4c4d4e4f");
        // TODO: Oberthur Cosmo V7
        // NOTE: neither authentication, nor upload work
        //CardProfiles.generateScript(capFileName + "jc2.2.2.cap", packageAID, appletAID, "Oberthur_Cosmo_V7", "mode_211", "A0000001510000", "-keyind 0 -keyver 0 -mac_key 404142434445464748494a4b4c4d4e4f -enc_key 404142434445464748494a4b4c4d4e4f");

        // Infineon JTOP V2 16K
        CardProfiles.generateScript(capFileName + "jc2.1.2.cap", packageAID, appletAID, "Infineon_JTOP_V2_16K", "mode_201", "a000000003000000", "-keyind 0 -keyver 0 -mac_key 404142434445464748494a4b4c4d4e4f -enc_key 404142434445464748494a4b4c4d4e4f");
        // Infineon JTOP Dual Interface 80k - SLJ 52GLA080AL M8.4
        // NOTE: authentication works, but upload fails with 'install_for_load() returns 0x80206A88 (6A88: Referenced data not found.)' 
        CardProfiles.generateScript(capFileName + "jc2.2.2.cap", packageAID, appletAID, "Infineon_JTOP_Dual_Interface_80k", "mode_211", "", "-keyind 0 -keyver 0 -mac_key 404142434445464748494a4b4c4d4e4f -enc_key 404142434445464748494a4b4c4d4e4f");

        // Cyberflex Palmera V5
        CardProfiles.generateScript(capFileName + "jc2.1.2.cap", packageAID, appletAID, "Cyberflex_Palmera_V5", "mode_201", "a000000003000000", "-keyind 0 -keyver 0 -mac_key 404142434445464748494a4b4c4d4e4f -enc_key 404142434445464748494a4b4c4d4e4f");

        // Twin_GCX4_72K_PK
        CardProfiles.generateScript(capFileName + "jc2.2.1.cap", packageAID, appletAID, "Twin_GCX4_72K_PK", "mode_201\r\ngemXpressoPro", "-AID A000000018434D00", "-keyind 0 -keyver 0 -key 47454d5850524553534f53414d504c45");
    }


    private static void printMembers(Member[] mbrs, String s, String longClassName, String shortClassName) throws IllegalArgumentException, IllegalAccessException {
        int allignSpaceLength = 80;
        int methodIndex = 0;
        out.format("    // %s %s:\n", longClassName, s);
        for (Member mbr : mbrs) {
            if (mbr instanceof Field) {
                Field value = (Field) mbr;
                value.setAccessible(true);

                String result = value.toGenericString();
                result = result.replace(longClassName, shortClassName);
                result = result.replace(".", "_");
                try {
                    Object o = value.get(null);
                    out.format("    %s", result);
                    for (int i = 0; i < allignSpaceLength - result.length(); i++) out.print(" ");
                    out.format("= %d;\n", o);
                } catch (NullPointerException e) {
                    // not reasonable value
                    int a = 0;
                }
            } else if (mbr instanceof Constructor)
                out.format("  %s%n", ((Constructor) mbr).toGenericString());
            else if (mbr instanceof Method) {
                methodIndex++;
                Method value = (Method) mbr;

                String result = value.toGenericString();
                result = result.replace(longClassName, shortClassName);
                result = result.replace(".", "_");

                String msg = String.format("public static final short %s_%s", shortClassName, value.getName());
                out.format("    %s", msg);
                for (int i = 0; i < allignSpaceLength - msg.length(); i++) out.print(" ");
                out.format("= %d;\n", methodIndex);
            }
        }
        if (mbrs.length == 0)
            out.format("    //  -- No %s --%n", s);
        out.format("%n");
    }

    static void formatClass(String longClassName, String shortClassName) throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
        out.format("\n\n\n    // Class %s\n", longClassName);
        Class<?> c = Class.forName(longClassName);
        Field[] fields = c.getDeclaredFields();
        printMembers(fields, "Fields", longClassName, shortClassName);
        Method[] methods = c.getDeclaredMethods();
        printMembers(methods, "Methods", longClassName, shortClassName);
    }

    static void generateJCConstantsFile(String fileName) throws Exception {
        // NOTE: constants will be generated only for JC library version you included
        // BUGBUG: now only to stdout, store into fileName
        out.format("package AlgTest;\n\n");
        out.format("public class JCConsts { \n");

        formatClass("javacard.security.Signature", "Signature");
        formatClass("javacardx.crypto.Cipher", "Cipher");
        formatClass("javacard.security.KeyAgreement", "KeyAgreement");
        formatClass("javacard.security.KeyBuilder", "KeyBuilder");
        formatClass("javacard.security.KeyPair", "KeyPair");
        formatClass("javacard.security.MessageDigest", "MessageDigest");
        formatClass("javacard.security.RandomData", "RandomData");
        formatClass("javacard.security.Checksum", "Checksum");
        formatClass("javacardx.crypto.KeyEncryption", "KeyEncryption");
        formatClass("javacard.security.AESKey", "AESKey");
        formatClass("javacard.security.DESKey", "DESKey");
        formatClass("javacard.security.DSAKey", "DSAKey");
        formatClass("javacard.security.DSAPrivateKey", "DSAPrivateKey");
        formatClass("javacard.security.DSAPublicKey", "DSAPublicKey");
        formatClass("javacard.security.ECKey", "ECKey");
        formatClass("javacard.security.ECPrivateKey", "ECPrivateKey");
        formatClass("javacard.security.ECPublicKey", "ECPublicKey");
        formatClass("javacard.security.HMACKey", "HMACKey");
        formatClass("javacard.security.RSAPrivateCrtKey", "RSAPrivateCrtKey");
        formatClass("javacard.security.RSAPrivateKey", "RSAPrivateKey");
        formatClass("javacard.security.RSAPublicKey", "RSAPublicKey");
        formatClass("javacard.security.SignatureMessageRecovery", "SignatureMessageRecovery");

        out.format("} \n");
    }
}
