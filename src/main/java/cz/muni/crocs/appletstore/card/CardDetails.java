package cz.muni.crocs.appletstore.card;

import pro.javacard.gp.GPData;
import pro.javacard.gp.GPDataException;

import javax.smartcardio.ATR;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class CardDetails {

    private ATR atr                   = null;
    //CPLC: Card Production Life Cycle Data
    private GPData.CPLC cplc          = null;
    //IIN (issuer identification number)
    private byte[] iin                = null;
    //CIN (card image number) in the card manager.
    private byte[] cin                = null;
    //other card data
    private byte[] cardData           = null;
    private byte[] cardCapabilities   = null;
    private byte[] keyInfo            = null;

    //getters
    public ATR getAtr() {
        return atr;
    }
    public GPData.CPLC getCplc() {
        return cplc;
    }
    public byte[] getIin() {
        return iin;
    }
    public byte[] getCin() {
        return cin;
    }
    public byte[] getCardData() {
        return cardData;
    }
    public byte[] getCardCapabilities() { return cardCapabilities; }
    public byte[] getKeyInfo() {
        return keyInfo;
    }

    public void setAtr(ATR atr) {
        this.atr = atr;
    }
    public void setCplc(byte[] cplc) {
        if (cplc == null) {
            this.cplc = null;
            return;
        }
        try {
            this.cplc = GPData.CPLC.fromBytes(cplc);
        } catch (GPDataException e) {
            e.printStackTrace();
            this.cplc = null;
        }
    }
    public void setIin(byte[] iin) {
        this.iin = iin;
    }
    public void setCin(byte[] cin) {
        this.cin = cin;
    }
    public void setCardData(byte[] cardData) {
        this.cardData = cardData;
    }
    public void setCardCapabilities(byte[] cardCapabilities) {
        this.cardCapabilities = cardCapabilities;
    }
    public void setKeyInfo(byte[] keyInfo) {
        this.keyInfo = keyInfo;
    }

    /**
     * Convert array to single unit
     *
     * @param data to convert
     * @return byte string in hex, bytes separated by space
     */
    public static String byteArrayToHexSpaces(byte[] data) {
        StringBuilder builder = new StringBuilder();
        for (byte b : data) {
            builder.append(String.format("%02X", b)).append(" ");
        }
        return builder.substring(0, builder.length() - 1);
    }

    /**
     * Computes the card ID
     *
     * @param details details to get data for id
     * @return card id
     */
    public static String getId(CardDetails details) {
        return "ATR=" + byteArrayToHexSpaces(details.getAtr().getBytes()) + ", ICSN=" +
                ((details.getCplc() == null) ?
                        "null" : byteArrayToHexSpaces(details.getCplc().get(GPData.CPLC.Field.ICSerialNumber)));
    }
}
