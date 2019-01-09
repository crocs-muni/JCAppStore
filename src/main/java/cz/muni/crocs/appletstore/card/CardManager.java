package cz.muni.crocs.appletstore.card;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.iface.CardCommand;

import java.io.File;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class CardManager {

    private Terminals terminals = new Terminals("");

    public Terminals getTerminals() {
        return terminals;
    }

    public void chooseCard() {
        //todo enable card chooser
    }

    public void install(String filePath) throws CardCommandExecutionException {
        install(new File(filePath));
    }

    public void install(File file) throws CardCommandExecutionException {
        if (!file.exists()) {
            throw new CardCommandExecutionException(
                    Config.translation.get(150) + file.getAbsolutePath() + Config.translation.get(151));
        }
        CardCommand command = new Install();
    }

    public void uninstall(File file) {

    }

    public void uninstall(String AID) {

    }
}
