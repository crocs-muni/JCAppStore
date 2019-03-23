package cz.muni.crocs.appletstore.iface;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public interface OnEventCallBack<Start, Fail, Finish> {

    Start onStart();

    Fail onFail();

    Finish onFinish();
}
