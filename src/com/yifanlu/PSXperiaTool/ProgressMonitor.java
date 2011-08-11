package com.yifanlu.PSXperiaTool;

/**
 * Created by IntelliJ IDEA.
 * User: yifanlu
 * Date: 8/10/11
 * Time: 4:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProgressMonitor {
    public interface ProgressCallback {
        public void nextStep(String message);
        public void stepsTook(int steps, int total);
    }
    private ProgressCallback mCallback;
    private int mTotalSteps = 0;
    private int mSteps = 0;

    public void setCallback(ProgressCallback callback){
        mCallback = callback;
    }

    public void setTotalSteps(int steps){
        this.mTotalSteps = steps;
    }

    public void nextStep(String message){
        Logger.info(message);
        mSteps++;
        if(mCallback != null){
            mCallback.nextStep(message);
            mCallback.stepsTook(mSteps, mTotalSteps);
        }
    }
}
