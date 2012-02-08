/**
 * 
 */
package org.idch.util;

/**
 * @author Neal_2
 */
public class StopWatch {
    String name = "Timer";
    
    long startTime = 0;
    long restartTime = 0;
    
    long accumulatedTime = 0;
    long elapsedTime = 0;
    
    long increment = 0;
    long reportIncrement = 100;
    boolean resetOnAutoReport = true;
    
    public StopWatch() {
        
    }
    
    public StopWatch(String name) {
        this.name = name;
    }
    
    public StopWatch(String name, long inc) {
        this.name = name;
        this.reportIncrement = inc;
    }
    
    public void start() {
        restartTime = System.currentTimeMillis();
        
        if (startTime == 0) {
            elapsedTime = 0;
            startTime = restartTime;
        }
    }
    
    public void pause() {
        long current = System.currentTimeMillis();
        if (restartTime == 0) 
            return; // not running
        
        accumulatedTime += current - restartTime;
        restartTime = 0;
        
        if ((reportIncrement != 0) && (++increment % reportIncrement == 0)) {
            System.out.println(report());
            
            if (resetOnAutoReport) {
                stop();
            }
        }
    }
    
    public void stop() {
        pause();
        elapsedTime = getElapsedTime();
        
        startTime = 0;
        accumulatedTime = 0;
        
    }
    
    public long getElapsedTime() {
        long current = System.currentTimeMillis();
        if (elapsedTime != 0)
            return elapsedTime;
        
        return current - startTime;
    }
    
    public String report() {
        long elapsed = getElapsedTime();
        float timePerIteration = (float)accumulatedTime / reportIncrement;
        float totalPerIteration = ((float)(elapsed - accumulatedTime) / reportIncrement);
        
        StringBuilder sb = new StringBuilder();
        sb.append("Timer Report (").append(name).append("):  \n")
          .append("  Elapsed Time (").append(increment).append("):       ").append(elapsed).append("\n")
          .append("  Accumulated Time (").append(increment).append("):   ").append(timePerIteration).append("\n")
          .append("  Gap Time (").append(increment).append("):           ").append(totalPerIteration).append("\n");
        
        return sb.toString();
    }
    
    public void endTimer() {
        if (++increment % reportIncrement == 0) {
//System.out.println("Structure Handler (" + structure.getName() + "): \n" +
           
        }
    }
}
