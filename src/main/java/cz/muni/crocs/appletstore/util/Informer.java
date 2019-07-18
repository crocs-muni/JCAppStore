package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.iface.CallBack;
import cz.muni.crocs.appletstore.ui.Warning;

public interface Informer {

    void showInfo(String info);

    void showWarning(String msg, Warning.Importance status, Warning.CallBackIcon icon, CallBack callable);

    void showWarningToClose(String langKey, Warning.Importance status);

    void closeWarning();
}
