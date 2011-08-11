/*
 * PSXperia Converter Tool - Logging
 * Copyright (C) 2011 Yifan Lu (http://yifan.lu/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.yifanlu.PSXperiaTool;

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

    public int getSteps(){
        return mSteps;
    }
    
    public void jump(int steps){
        this.mSteps = steps;
        if(mCallback != null){
            mCallback.stepsTook(mSteps, mTotalSteps);
        }
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
