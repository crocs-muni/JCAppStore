package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.iface.OnEventCallBack;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Abstract card action wrapper providing failure management
 * TODO consider remowing TRet parameter both from wapper and OnEventCallBack iface
 *
 * @author Jiří Horák
 * @version 1.0
 */
public abstract class CardAbstractAction<TRet, TArg> extends CardAbstractActionBase<TRet, TArg> {

    protected CardAbstractAction(OnEventCallBack<TRet, TArg> call) {
        super(call);
    }

    @Override
    protected void execute(CardExecutable<TArg> toExecute, String loggerMessage, String title) {
        new Thread(job(toExecute, loggerMessage, title)).start();
    }

    @Override
    protected void execute(CardExecutable<TArg> toExecute, String loggerMessage, String title,
                           int timeOut, TimeUnit unitsMeaning) {
        Runnable job = job(toExecute, loggerMessage, title);
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

        //note: calling scheduleAtFixedRate( () -> job(args..) ) would cause repeated CONSTRUCTING of the job, not calling it
        final ScheduledFuture<?> scheduledFuture = executor.schedule(job, 0, TimeUnit.SECONDS);

        executor.schedule(() -> {
            scheduledFuture.cancel(true);
        }, timeOut, unitsMeaning);
    }
}
