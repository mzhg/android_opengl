package jet.learning.opengl.common;

/**
 * Created by mazhen'gui on 2017/3/21.
 */

public class MathUtil {

    private MathUtil(){}

    public static int randomInt(){
        return (int)(Integer.MAX_VALUE * Math.random());
    }

    public static float random(float low, float high){
        return (float) (low + Math.random() * (high - low));
    }
}
