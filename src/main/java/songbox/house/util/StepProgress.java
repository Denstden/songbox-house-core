package songbox.house.util;

import java.util.concurrent.atomic.AtomicReference;

//TODO think about better name
public class StepProgress {

    private final AtomicReference<Float> currentProgress = new AtomicReference<>((float) 0);
    private final ProgressListener progressListener;
    private final float progressStep;

    public StepProgress(ProgressListener progressListener, float progressStep) {
        this.progressListener = progressListener;
        this.progressStep = progressStep;
    }

    public Float step() {
        if (progressListener != null) {
            Float progress = currentProgress.updateAndGet(v -> v + progressStep);
            progressListener.onProgressChanged(progress);
            return progress;
        } else {
            return null;
        }
    }
}
