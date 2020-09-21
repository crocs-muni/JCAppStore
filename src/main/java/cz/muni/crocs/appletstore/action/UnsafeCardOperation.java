package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.card.CardNotAuthenticatedException;
import cz.muni.crocs.appletstore.card.LocalizedCardException;
import cz.muni.crocs.appletstore.card.UnknownKeyException;

public interface UnsafeCardOperation {
    void fire() throws LocalizedCardException, UnknownKeyException, CardNotAuthenticatedException;
}