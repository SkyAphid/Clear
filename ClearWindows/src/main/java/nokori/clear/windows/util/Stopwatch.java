package nokori.clear.windows.util;

import java.util.concurrent.TimeUnit;

/**
 * This is a basic utility class that can be used to measure time. This can be useful for setting up delays and transitions.
 *
 * @author Brayden
 */
public class Stopwatch {
    private boolean started = false;
    private long startTimeInNanoseconds, endTimeInNanoseconds;

    public void timeInMilliseconds(long endTimeDistanceInMilliseconds) {
        timeInNanoseconds(TimeUnit.MILLISECONDS.toNanos(endTimeDistanceInMilliseconds));
    }

    /**
     * Sets a starting time for the stopwatch, enabling the use of all of the other utilities in this class.
     *
     * @param endTimeDistance - sets an end time time stamp relative to the start time
     */
    public void timeInNanoseconds(long endTimeDistanceInNanoseconds) {
        started = true;
        startTimeInNanoseconds = System.nanoTime();
        endTimeInNanoseconds = startTimeInNanoseconds + endTimeDistanceInNanoseconds;
    }

    public boolean hasTimePassedInMillis(long timeInMillis) {
        long currentTime = System.nanoTime();
        return (started && TimeUnit.NANOSECONDS.toMillis(currentTime - startTimeInNanoseconds) > timeInMillis);
    }

    public boolean hasTimePassedInNanoseconds(long timeInNanoseconds) {
        long currentTime = System.nanoTime();
        return (started && (currentTime - startTimeInNanoseconds) > timeInNanoseconds);
    }

    public long getStartTimeInMilliseconds() {
        return TimeUnit.NANOSECONDS.toMillis(startTimeInNanoseconds);
    }

    public long getStartTimeInNanoseconds() {
        return startTimeInNanoseconds;
    }

    public long getEndTimeInMilliseconds() {
        return TimeUnit.NANOSECONDS.toMillis(endTimeInNanoseconds);
    }

    public long getEndTimeInNanoseconds() {
        return endTimeInNanoseconds;
    }

    public boolean isCurrentTimePassedEndTime() {
        return (System.nanoTime() > getEndTimeInNanoseconds());
    }

    /**
     * @param endTimeInMilliseconds - the set end time in milliseconds
     * @return a value between 0 and 1 indicating the time passed between this Stopwatch's start time and the given end time.
     */
    public float getNormalizedDistanceBetweenTimeInMilliseconds(long endTimeInMilliseconds) {
        return getNormalizedDistanceBetweenTimeInNanoseconds(TimeUnit.MILLISECONDS.toNanos(endTimeInMilliseconds));
    }

    /**
     * @param endTimeInNanoseconds - the set end time in nanoseconds
     * @return a value between 0 and 1 indicating the time passed between this Stopwatch's start time and the given end time.
     */
    public float getNormalizedDistanceBetweenTimeInNanoseconds(long endTimeInNanoseconds) {
        if (!started) {
            return 0f;
        }

        long currentTime = System.nanoTime();

        double maxDistance = endTimeInNanoseconds - startTimeInNanoseconds;
        double distance = Math.max(endTimeInNanoseconds - currentTime, 0);

        float progress = 1f - (float) (distance / maxDistance);

        return progress;
    }

    /**
     * @return a value between 0 and 1 indicating the time passed between this Stopwatch's start time and end time.
     */
    public float getNormalizedDistanceBetweenTime() {
        return getNormalizedDistanceBetweenTimeInNanoseconds(endTimeInNanoseconds);
    }
}
