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
public abstract class CardAbstractRoutine<TRet, TArg> extends CardAbstractActionBase<TRet, TArg> {

    private final int timeUnit;
    private final TimeUnit unitMeaning;
    private ScheduledFuture<?> scheduledFuture;

    protected CardAbstractRoutine(OnEventCallBack<TRet, TArg> call, int timeUnit, TimeUnit unitMeaning) {
        super(call);
        this.timeUnit = timeUnit;
        this.unitMeaning = unitMeaning;
    }

    @Override
    protected void execute(CardExecutable<TArg> toExecute, String loggerMessage, String title) {
        Runnable job = job(toExecute, loggerMessage, title);
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(job, timeUnit, timeUnit, unitMeaning);
    }

    @Override
    protected void execute(CardExecutable<TArg> toExecute, String loggerMessage, String title,
                           int timeOut, TimeUnit unitsMeaning) {
        Runnable job = job(toExecute, loggerMessage, title);
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(/*2*/1);
        //note: calling scheduleAtFixedRate( () -> job(args..) ) would cause repeated CONSTRUCTING of the job, not calling it
        scheduledFuture = executor.scheduleAtFixedRate(job, timeUnit, timeUnit, unitMeaning);

//        executor.schedule(() -> {
//            scheduledFuture.cancel(true);
//        }, timeOut, unitsMeaning);
    }

    protected void breakExecution() {
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) scheduledFuture.cancel(true);
    }
}
